package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Read-only detail collection driven by an observable master value.
 *
 * <p>Use this bridge for master-detail presentation models where a scalar
 * selection chooses the collection currently shown as detail rows, choices, or
 * child objects. The master value owns selection state. The loader maps the
 * current master value to the active observable detail collection, or returns
 * {@code null} when no detail collection should be visible.</p>
 *
 * <p>The loader receives the current master value, which may be {@code null},
 * during construction and after every master change. A {@code null} detail
 * collection is treated as an empty read-only collection. The initial detail
 * snapshot is exposed immediately, without emitting an initial event.</p>
 *
 * <p>Master changes replace the active detail subscription. If the visible
 * detail snapshot changes, listeners receive a reset-style change set with the
 * master event origin. Changes from the active detail collection are forwarded
 * unchanged, including their original {@link ListChangeSet#origin()}.</p>
 *
 * <p>The instance owns its master subscription and the active detail
 * subscription. Closing it removes those subscriptions, clears listeners, and
 * makes later subscriptions return {@link Subscription#EMPTY}. The class is not
 * synchronized and inherits the threading assumptions of its master value and
 * active detail collection.</p>
 *
 * @param <M> master value type; master values may be {@code null}
 * @param <E> non-null detail element type
 */
public final class MasterDetailCollection<M, E extends @NonNull Object>
        implements ObservableReadableCollection<E>, Disposable {

    private final ReadableValue<M> master;
    private final Function<? super @Nullable M, ? extends @Nullable ObservableReadableCollection<E>> loader;
    private final Subscription masterSubscription;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private @Nullable ObservableReadableCollection<E> detail;
    private @Nullable Subscription detailSubscription;
    private boolean closed;

    /**
     * Creates a master-detail collection and subscribes it to the master value.
     *
     * @param master master value selecting the active detail collection
     * @param loader detail collection loader; receives nullable master values
     *        and may return {@code null} for an empty detail
     */
    public MasterDetailCollection(
            ReadableValue<M> master,
            Function<? super @Nullable M, ? extends @Nullable ObservableReadableCollection<E>> loader
    ) {
        this.master = Objects.requireNonNull(master, "master");
        this.loader = Objects.requireNonNull(loader, "loader");
        attach(load(master.value()));
        this.masterSubscription = master.subscribe(event -> masterChanged(event.newValue(), event.origin()));
    }

    /**
     * Creates a master-detail collection and subscribes it to the master value.
     *
     * @param master master value selecting the active detail collection
     * @param loader detail collection loader; receives nullable master values
     *        and may return {@code null} for an empty detail
     * @param <M> master value type
     * @param <E> detail element type
     * @return master-detail collection backed by the supplied master and loader
     */
    public static <M, E extends @NonNull Object> MasterDetailCollection<M, E> of(
            ReadableValue<M> master,
            Function<? super @Nullable M, ? extends @Nullable ObservableReadableCollection<E>> loader
    ) {
        return new MasterDetailCollection<>(master, loader);
    }

    @Override
    public int size() {
        ObservableReadableCollection<E> currentDetail = detail;
        return currentDetail == null ? 0 : currentDetail.size();
    }

    @Override
    public E get(int index) {
        ObservableReadableCollection<E> currentDetail = detail;
        if (currentDetail == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return currentDetail.get(index);
    }

    @Override
    public List<E> snapshot() {
        ObservableReadableCollection<E> currentDetail = detail;
        return currentDetail == null ? List.of() : currentDetail.snapshot();
    }

    @Override
    public Stream<E> stream() {
        ObservableReadableCollection<E> currentDetail = detail;
        return currentDetail == null ? Stream.empty() : currentDetail.stream();
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            return Subscription.EMPTY;
        }
        return listeners.addListener(listener);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            masterSubscription.close();
        } finally {
            try {
                detachDetail();
            } finally {
                listeners.clearListeners();
            }
        }
    }

    private void masterChanged(@Nullable M masterValue, ChangeOrigin origin) {
        if (closed) {
            return;
        }
        List<E> previous = snapshot();
        detachDetail();
        attach(load(masterValue));
        List<E> next = snapshot();
        if (previous.equals(next)) {
            return;
        }
        fireReset(previous, next, origin);
    }

    private @Nullable ObservableReadableCollection<E> load(@Nullable M masterValue) {
        return loader.apply(masterValue);
    }

    private void attach(@Nullable ObservableReadableCollection<E> nextDetail) {
        detail = nextDetail;
        if (nextDetail != null) {
            detailSubscription = nextDetail.subscribe(this::fire);
        }
    }

    private void detachDetail() {
        if (detailSubscription != null) {
            detailSubscription.close();
            detailSubscription = null;
        }
        detail = null;
    }

    private void fireReset(List<E> previous, List<E> next, ChangeOrigin origin) {
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!previous.isEmpty()) {
            changes.add(new ListChange.Cleared<>(previous));
        }
        if (!next.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, next));
        }
        if (!changes.isEmpty()) {
            fire(new ListChangeSet<>(changes, origin));
        }
    }

    private void fire(ListChangeSet<E> changeSet) {
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }
}
