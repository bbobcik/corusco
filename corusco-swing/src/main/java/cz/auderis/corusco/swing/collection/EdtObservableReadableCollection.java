package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.jspecify.annotations.NonNull;

/**
 * {@link ObservableReadableCollection} wrapper that delivers listener callbacks
 * on the EDT.
 *
 * <p>The wrapper subscribes to source changes immediately. If a source change
 * arrives on a background thread, delivery to this wrapper's listeners is
 * scheduled with {@link SwingUtilities#invokeLater(Runnable)}. Changes already
 * delivered on the EDT are forwarded synchronously. Change sets are delivered
 * in the order the source raised them.</p>
 *
 * <p>Reads made on the EDT use an EDT-side copy of the elements that is
 * initialised from the source snapshot at construction and updated only when
 * a change set is delivered to wrapper subscribers, so EDT readers always see
 * a state consistent with the events they have received. Reads made on other
 * threads go to the source directly. Construct the wrapper before other
 * threads start mutating the source.</p>
 *
 * <p>Closing the wrapper removes the source subscription and clears this
 * wrapper's listener registrations. It does not close, clear, or otherwise own the
 * source collection. Subscribers added after close receive an inert
 * subscription.</p>
 *
 * @param <E> element type
 */
public final class EdtObservableReadableCollection<E extends @NonNull Object>
        implements ObservableReadableCollection<E>, Disposable {

    private final EdtChangeRelay<E> relay;

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable collection
     */
    public EdtObservableReadableCollection(ObservableReadableCollection<E> source) {
        this.relay = new EdtChangeRelay<>(source);
    }

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable collection
     * @param <E> element type
     * @return EDT-dispatching wrapper
     */
    public static <E extends @NonNull Object> EdtObservableReadableCollection<E> of(
            ObservableReadableCollection<E> source
    ) {
        return new EdtObservableReadableCollection<>(source);
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
     * Returns the wrapped source collection.
     *
     * @return source collection
     */
    public ObservableReadableCollection<E> source() {
        return relay.source();
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
