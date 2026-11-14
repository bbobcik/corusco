package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.task.GenerationCounter;
import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Default {@link TaskService}-backed data source.
 *
 * <p>This implementation is the core module's built-in bridge between a
 * synchronous {@link CoruscoDataLoader} and observable presenter state. The
 * loader runs through the supplied {@link TaskService}; terminal callbacks
 * update the current range, query, row snapshot, total count, and status on
 * the task service's callback executor. The class itself does not know whether
 * that executor is the Swing Event Dispatch Thread, a direct executor used by
 * tests, or another presenter-owned dispatcher.</p>
 *
 * <p>Each request advances a primitive refresh generation. Success, failure,
 * and cancellation callbacks compare their captured generation with the
 * source's current generation before mutating state. This suppresses stale
 * completions when a slower request finishes after a newer request. Starting a
 * new request also cancels the previous task handle when it is still running,
 * but stale-result suppression does not rely on cancellation being prompt.</p>
 *
 * <p>Refreshing keeps the previous rows visible while the new page is loading.
 * A successful page replacement swaps the immutable backing list and emits one
 * {@link ListChangeSet}. The change set contains a clear change when previous
 * rows existed and an insert change when new rows exist; it never emits one
 * event per loaded row. A failed refresh records
 * {@link CoruscoDataPhase#FAILED} and a {@link CoruscoDataError} while leaving
 * the previous row snapshot and count available to the presenter.</p>
 *
 * <p>The implementation follows Corusco's presenter-owned model convention:
 * steady-state reads are unsynchronized, listener delivery is synchronous on
 * the callback executor, and callers are responsible for choosing the right
 * owner and lifecycle. Closing the source cancels the current task, rejects
 * future requests, and publishes {@link CoruscoDataPhase#CLOSED} with
 * {@link StandardChangeOrigin#SYSTEM}.</p>
 *
 * <p>Reentrant operations and direct scalar writes wait until the current
 * transition and its notifications finish. A queued range-only request uses
 * the query current when it executes; a queued refresh likewise uses the
 * range and query current at execution. Operations queued after a close are
 * discarded when they execute. Direct scalar writes retain their nullable
 * {@link SimpleValue} semantics and do not change the accepted page identity.
 * If a loading notification or superseded-task cancellation throws before
 * submission, the source settles as FAILED with its last accepted page, then
 * propagates the exception.</p>
 *
 * @param <R> row type
 * @param <K> key type
 */
public final class DefaultCoruscoDataSource<R extends @NonNull Object, K extends @NonNull Object>
        implements CoruscoDataSource<R, K> {

    private final CoruscoRowIdentity<R, K> rowIdentity;
    private final CoruscoDataLoader<R> loader;
    private final TaskService taskService;
    private State<R> state = new State<>(CoruscoDataStatus.INITIAL, CoruscoDataRange.EMPTY,
            CoruscoDataQuery.EMPTY, CoruscoDataCount.UNKNOWN, List.of(), null);
    private final ResettableRows<R> rows = new ResettableRows<>(() -> state.rows());
    private final SimpleValue.Backing<CoruscoDataStatus> statusBacking = SimpleValue.backedBy(() -> state.status(),
            value -> state = new State<>(value, state.range(), state.query(), state.count(), state.rows(), state.accepted()), this::transition);
    private final SimpleValue.Backing<CoruscoDataRange> rangeBacking = SimpleValue.backedBy(() -> state.range(),
            value -> state = new State<>(state.status(), value, state.query(), state.count(), state.rows(), state.accepted()), this::transition);
    private final SimpleValue.Backing<CoruscoDataQuery> queryBacking = SimpleValue.backedBy(() -> state.query(),
            value -> state = new State<>(state.status(), state.range(), value, state.count(), state.rows(), state.accepted()), this::transition);
    private final SimpleValue.Backing<CoruscoDataCount> countBacking = SimpleValue.backedBy(() -> state.count(),
            value -> state = new State<>(state.status(), state.range(), state.query(), value, state.rows(), state.accepted()), this::transition);
    private final SimpleValue<CoruscoDataStatus> status = statusBacking.value();
    private final SimpleValue<CoruscoDataRange> range = rangeBacking.value();
    private final SimpleValue<CoruscoDataQuery> query = queryBacking.value();
    private final SimpleValue<CoruscoDataCount> totalCount = countBacking.value();
    private final GenerationCounter generations = new GenerationCounter();
    private final ArrayDeque<PendingTransition> pending = new ArrayDeque<>();
    private boolean transitioning;
    private @Nullable TaskHandle<?> currentTask;
    private boolean closed;

    /**
     * Creates a data source.
     *
     * <p>The data source does not take ownership of the supplied
     * {@code taskService}. Closing the data source cancels its current task but
     * does not close the service, because the service may be shared by a
     * presenter or view scope.</p>
     *
     * @param rowIdentity row identity metadata
     * @param loader synchronous page loader invoked on worker tasks
     * @param taskService task service used for loading and callback delivery
     */
    public DefaultCoruscoDataSource(
            CoruscoRowIdentity<R, K> rowIdentity,
            CoruscoDataLoader<R> loader,
            TaskService taskService
    ) {
        this.rowIdentity = Objects.requireNonNull(rowIdentity, "rowIdentity");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.taskService = Objects.requireNonNull(taskService, "taskService");
    }

    @Override
    public CoruscoRowIdentity<R, K> rowIdentity() {
        return rowIdentity;
    }

    @Override
    public ObservableReadableCollection<R> rows() {
        return rows;
    }

    @Override
    public SimpleValue<CoruscoDataStatus> status() {
        return status;
    }

    @Override
    public SimpleValue<CoruscoDataRange> range() {
        return range;
    }

    @Override
    public SimpleValue<CoruscoDataQuery> query() {
        return query;
    }

    @Override
    public SimpleValue<CoruscoDataCount> totalCount() {
        return totalCount;
    }

    /** The request whose page is displayed, or null before the first successful page. */
    @Override
    public @Nullable CoruscoDataRequest acceptedRequest() {
        return state.accepted();
    }

    @Override
    public void request(CoruscoDataRange range, ChangeOrigin origin) {
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(origin, "origin");
        ensureOpen();
        transition(() -> startRequest(range, currentQuery(), origin));
    }

    @Override
    public void request(CoruscoDataRange newRange, CoruscoDataQuery newQuery, ChangeOrigin origin) {
        Objects.requireNonNull(newRange, "range");
        Objects.requireNonNull(newQuery, "query");
        Objects.requireNonNull(origin, "origin");
        ensureOpen();
        transition(() -> startRequest(newRange, newQuery, origin));
    }

    private void startRequest(CoruscoDataRange newRange, CoruscoDataQuery newQuery, ChangeOrigin origin) {
        if (closed) return;
        GenerationCounter.Generation generation = generations.advance();
        long requestGeneration = generation.value();
        CoruscoDataRequest request = new CoruscoDataRequest(newRange, newQuery, requestGeneration);
        try {
            install(new State<>(new CoruscoDataStatus(CoruscoDataPhase.LOADING, requestGeneration, null),
                    newRange, newQuery, state.count(), state.rows(), state.accepted()), origin);
            TaskHandle<?> previousTask = currentTask;
            currentTask = null;
            if (previousTask != null && !previousTask.isDone()) previousTask.cancel();
        } catch (RuntimeException | Error observerError) {
            TaskHandle<?> previousTask = currentTask;
            currentTask = null;
            try {
                if (previousTask != null && !previousTask.isDone()) previousTask.cancel();
            } catch (RuntimeException | Error cancellationError) {
                if (cancellationError != observerError) observerError.addSuppressed(cancellationError);
            }
            try { fail(request, generation, origin, observerError); }
            catch (RuntimeException | Error failureError) {
                if (failureError != observerError) observerError.addSuppressed(failureError);
            }
            throw observerError;
        }
        try {
            currentTask = taskService.submit(cancellation -> loader.load(request, cancellation),
                    callbacks(request, generation, origin));
        } catch (RejectedExecutionException e) {
            currentTask = null;
            fail(request, generation, origin, e);
        }
    }

    @Override
    public void refresh(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        ensureOpen();
        transition(() -> startRequest(currentRange(), currentQuery(), origin));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        transition(this::closeNow);
    }

    private void closeNow() {
        if (closed) return;
        closed = true;
        GenerationCounter.Generation closedGeneration = generations.invalidate();
        try {
            TaskHandle<?> task = currentTask;
            if (task != null) task.cancel();
        } finally {
            install(new State<>(new CoruscoDataStatus(CoruscoDataPhase.CLOSED, closedGeneration.value(), null),
                    state.range(), state.query(), state.count(), state.rows(), state.accepted()), StandardChangeOrigin.SYSTEM);
        }
    }

    private TaskCallbacks<CoruscoDataPage<R>> callbacks(
            CoruscoDataRequest request,
            GenerationCounter.Generation generation,
            ChangeOrigin origin
    ) {
        return new TaskCallbacks<>() {
            @Override
            public void succeeded(CoruscoDataPage<R> page) {
                transitionTerminal(() -> {
                    if (!isCurrent(generation)) return;
                    CoruscoDataRequest accepted = new CoruscoDataRequest(page.range(), request.query(), request.refreshGeneration());
                    List<R> acceptedRows = state.rows().equals(page.rows()) ? state.rows() : List.copyOf(page.rows());
                    install(new State<>(new CoruscoDataStatus(CoruscoDataPhase.READY, request.refreshGeneration(), null),
                            page.range(), state.query(), page.totalCount(), acceptedRows, accepted), origin);
                });
            }

            @Override
            public void failed(Throwable error) {
                transitionTerminal(() -> fail(request, generation, origin, error));
            }

            @Override
            public void cancelled() {
                transitionTerminal(() -> {
                    if (!isCurrent(generation)) return;
                    install(new State<>(new CoruscoDataStatus(CoruscoDataPhase.IDLE, request.refreshGeneration(), null),
                            state.range(), state.query(), state.count(), state.rows(), state.accepted()), origin);
                });
            }
        };
    }

    private void fail(CoruscoDataRequest request, GenerationCounter.Generation generation, ChangeOrigin origin, Throwable error) {
        if (!isCurrent(generation)) return;
        install(new State<>(new CoruscoDataStatus(CoruscoDataPhase.FAILED, request.refreshGeneration(), CoruscoDataError.of(error)),
                state.range(), state.query(), state.count(), state.rows(), state.accepted()), origin);
    }

    private void transition(Runnable operation) {
        enqueue(operation, false);
    }

    private void transitionTerminal(Runnable operation) {
        enqueue(operation, true);
    }

    private void enqueue(Runnable operation, boolean terminal) {
        pending.add(new PendingTransition(operation, terminal));
        if (transitioning) return;
        transitioning = true;
        try {
            while (!pending.isEmpty()) pending.remove().operation().run();
        } catch (RuntimeException | Error e) {
            while (!pending.isEmpty()) {
                List<PendingTransition> terminalTransitions = pending.stream().filter(PendingTransition::terminal).toList();
                pending.clear();
                for (PendingTransition saved : terminalTransitions) {
                    try { saved.operation().run(); }
                    catch (RuntimeException | Error suppressed) { if (suppressed != e) e.addSuppressed(suppressed); }
                }
            }
            throw e;
        } finally {
            transitioning = false;
        }
    }

    private record PendingTransition(Runnable operation, boolean terminal) { }

    private void install(State<R> next, ChangeOrigin origin) {
        State<R> old = state;
        state = next;
        rows.changed(old.rows(), next.rows(), origin);
        rangeBacking.notifyChange(old.range(), next.range(), origin);
        queryBacking.notifyChange(old.query(), next.query(), origin);
        countBacking.notifyChange(old.count(), next.count(), origin);
        statusBacking.notifyChange(old.status(), next.status(), origin);
    }

    private record State<E>(@Nullable CoruscoDataStatus status, @Nullable CoruscoDataRange range, @Nullable CoruscoDataQuery query,
                            @Nullable CoruscoDataCount count, List<E> rows, @Nullable CoruscoDataRequest accepted) { }

    private boolean isCurrent(GenerationCounter.Generation generation) {
        return !closed && generations.isCurrent(generation);
    }

    private CoruscoDataRange currentRange() {
        CoruscoDataRange value = range.value();
        return value != null ? value : CoruscoDataRange.EMPTY;
    }

    private CoruscoDataQuery currentQuery() {
        CoruscoDataQuery value = query.value();
        return value != null ? value : CoruscoDataQuery.EMPTY;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Data source is closed");
        }
    }

    private static final class ResettableRows<E extends @NonNull Object> implements ObservableReadableCollection<E> {

        private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
        private final Supplier<List<E>> elements;

        private ResettableRows(Supplier<List<E>> elements) { this.elements = elements; }

        @Override
        public int size() {
            return elements.get().size();
        }

        @Override
        public E get(int index) {
            return elements.get().get(index);
        }

        @Override
        public List<E> snapshot() {
            return elements.get();
        }

        @Override
        public Stream<E> stream() {
            return elements.get().stream();
        }

        @Override
        public Subscription subscribe(ListChangeListener<E> listener) {
            return listeners.addListener(listener);
        }

        private void changed(List<E> oldElements, List<E> copied, ChangeOrigin origin) {
            Objects.requireNonNull(origin, "origin");
            if (oldElements.equals(copied)) {
                return;
            }
            List<ListChange<E>> changes = new ArrayList<>(2);
            if (!oldElements.isEmpty()) {
                changes.add(new ListChange.Cleared<>(oldElements));
            }
            if (!copied.isEmpty()) {
                changes.add(new ListChange.Inserted<>(0, copied));
            }
            ListChangeSet<E> changeSet = new ListChangeSet<>(changes, origin);
            listeners.fireEvent(changeSet, ListChangeListener::listChanged);
        }
    }
}
