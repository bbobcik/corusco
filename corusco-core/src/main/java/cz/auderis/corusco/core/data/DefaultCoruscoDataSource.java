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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

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
 * @param <R> row type
 * @param <K> key type
 */
public final class DefaultCoruscoDataSource<R extends @NonNull Object, K extends @NonNull Object>
        implements CoruscoDataSource<R, K> {

    private final CoruscoRowIdentity<R, K> rowIdentity;
    private final CoruscoDataLoader<R> loader;
    private final TaskService taskService;
    private final ResettableRows<R> rows = new ResettableRows<>();
    private final SimpleValue<CoruscoDataStatus> status = SimpleValue.of(CoruscoDataStatus.INITIAL);
    private final SimpleValue<CoruscoDataRange> range = SimpleValue.of(CoruscoDataRange.EMPTY);
    private final SimpleValue<CoruscoDataQuery> query = SimpleValue.of(CoruscoDataQuery.EMPTY);
    private final SimpleValue<CoruscoDataCount> totalCount = SimpleValue.of(CoruscoDataCount.UNKNOWN);
    private final GenerationCounter generations = new GenerationCounter();
    private TaskHandle<?> currentTask;
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

    @Override
    public void request(CoruscoDataRange range, ChangeOrigin origin) {
        request(range, currentQuery(), origin);
    }

    @Override
    public void request(CoruscoDataRange newRange, CoruscoDataQuery newQuery, ChangeOrigin origin) {
        Objects.requireNonNull(newRange, "range");
        Objects.requireNonNull(newQuery, "query");
        Objects.requireNonNull(origin, "origin");
        ensureOpen();
        GenerationCounter.Generation generation = generations.advance();
        long requestGeneration = generation.value();
        CoruscoDataRequest request = new CoruscoDataRequest(newRange, newQuery, requestGeneration);
        range.setValue(newRange, origin);
        query.setValue(newQuery, origin);
        status.setValue(new CoruscoDataStatus(CoruscoDataPhase.LOADING, requestGeneration, null), origin);
        TaskHandle<?> previousTask = currentTask;
        if (previousTask != null && !previousTask.isDone()) {
            previousTask.cancel();
        }
        currentTask = taskService.submit(
                cancellation -> loader.load(request, cancellation),
                callbacks(request, generation, origin)
        );
    }

    @Override
    public void refresh(ChangeOrigin origin) {
        request(currentRange(), currentQuery(), origin);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        TaskHandle<?> task = currentTask;
        if (task != null) {
            task.cancel();
        }
        GenerationCounter.Generation closedGeneration = generations.invalidate();
        status.setValue(
                new CoruscoDataStatus(CoruscoDataPhase.CLOSED, closedGeneration.value(), null),
                StandardChangeOrigin.SYSTEM
        );
    }

    private TaskCallbacks<CoruscoDataPage<R>> callbacks(
            CoruscoDataRequest request,
            GenerationCounter.Generation generation,
            ChangeOrigin origin
    ) {
        return new TaskCallbacks<>() {
            @Override
            public void succeeded(CoruscoDataPage<R> page) {
                if (!isCurrent(generation)) {
                    return;
                }
                rows.reset(page.rows(), origin);
                range.setValue(page.range(), origin);
                totalCount.setValue(page.totalCount(), origin);
                status.setValue(new CoruscoDataStatus(CoruscoDataPhase.READY, request.refreshGeneration(), null), origin);
            }

            @Override
            public void failed(Throwable error) {
                if (!isCurrent(generation)) {
                    return;
                }
                status.setValue(new CoruscoDataStatus(
                        CoruscoDataPhase.FAILED,
                        request.refreshGeneration(),
                        CoruscoDataError.of(error)
                ), origin);
            }

            @Override
            public void cancelled() {
                if (!isCurrent(generation) || closed) {
                    return;
                }
                status.setValue(new CoruscoDataStatus(CoruscoDataPhase.IDLE, request.refreshGeneration(), null), origin);
            }
        };
    }

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
        private List<E> elements = List.of();

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public E get(int index) {
            return elements.get(index);
        }

        @Override
        public List<E> snapshot() {
            return elements;
        }

        @Override
        public Stream<E> stream() {
            return elements.stream();
        }

        @Override
        public Subscription subscribe(ListChangeListener<E> listener) {
            return listeners.addListener(listener);
        }

        private void reset(List<E> newElements, ChangeOrigin origin) {
            Objects.requireNonNull(origin, "origin");
            List<E> oldElements = elements;
            List<E> copied = List.copyOf(Objects.requireNonNull(newElements, "newElements"));
            if (oldElements.equals(copied)) {
                return;
            }
            elements = copied;
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
