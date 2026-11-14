package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;

/**
 * {@link ObservableList} wrapper that delivers listener callbacks on the EDT.
 *
 * <p>The wrapped list remains the storage and mutation owner. This adapter
 * delegates mutations directly to the source list and changes where wrapper
 * subscribers receive source change sets. If the source fires on the EDT,
 * delivery is immediate. If the source fires on another thread, delivery is
 * queued with {@link SwingUtilities#invokeLater(Runnable)}. Change sets are
 * delivered in the order the source raised them.</p>
 *
 * <p>Reads made on the EDT ({@link #size()}, {@link #get(int)},
 * {@link #snapshot()}, {@link #stream()}) use an EDT-side copy of the elements
 * that is initialised from the source snapshot at construction and updated
 * only when a change set is delivered to wrapper subscribers. EDT readers such
 * as Swing models therefore always see a state that agrees with the events
 * they have received, even while a background producer is ahead. Reads made
 * on other threads, and reads inside {@link #batch(ChangeOrigin, Consumer)},
 * go to the source directly. Construct the wrapper before other threads start
 * mutating the source.</p>
 *
 * <p>This class does not make the source list thread-safe. Applications that
 * mutate the source from background threads must still use a source
 * implementation and ownership policy that allow those mutations.</p>
 *
 * <p>Closing the wrapper removes the source subscription, clears wrapper
 * listeners, and suppresses queued callbacks that have not reached the EDT yet.
 * Closing does not close or clear the wrapped source list.</p>
 *
 * @param <E> element type
 */
public final class EdtObservableList<E extends @NonNull Object> implements ObservableList<E>, Disposable {

    private final ObservableList<E> source;
    private final EdtChangeRelay<E> relay;

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable list
     */
    public EdtObservableList(ObservableList<E> source) {
        this.source = Objects.requireNonNull(source, "source");
        this.relay = new EdtChangeRelay<>(source);
    }

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable list
     * @param <E> element type
     * @return EDT-dispatching wrapper
     */
    public static <E extends @NonNull Object> EdtObservableList<E> of(ObservableList<E> source) {
        return new EdtObservableList<>(source);
    }

    @Override
    public int size() {
        return relay.size();
    }

    @Override
    public E get(int index) {
        return relay.get(index);
    }

    @Override
    public List<E> snapshot() {
        return relay.snapshot();
    }

    @Override
    public Stream<E> stream() {
        return relay.stream();
    }

    /**
     * Returns the wrapped source list.
     *
     * @return source list
     */
    public ObservableList<E> source() {
        return source;
    }

    @Override
    public void add(@NonNull E element, ChangeOrigin origin) {
        Objects.requireNonNull(element, "element");
        source.add(element, origin);
    }

    @Override
    public void add(int index, E element, ChangeOrigin origin) {
        Objects.requireNonNull(element, "element");
        source.add(index, element, origin);
    }

    @Override
    public E set(int index, E element, ChangeOrigin origin) {
        Objects.requireNonNull(element, "element");
        return source.set(index, element, origin);
    }

    @Override
    public E remove(int index, ChangeOrigin origin) {
        return source.remove(index, origin);
    }

    @Override
    public void move(int fromIndex, int toIndex, ChangeOrigin origin) {
        source.move(fromIndex, toIndex, origin);
    }

    @Override
    public void clear(ChangeOrigin origin) {
        source.clear(origin);
    }

    @Override
    public void batch(ChangeOrigin origin, Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        relay.withSourceReads(() -> source.batch(origin, ignored -> work.accept(this)));
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        return relay.subscribe(listener);
    }

    @Override
    public void close() {
        relay.close();
    }
}
