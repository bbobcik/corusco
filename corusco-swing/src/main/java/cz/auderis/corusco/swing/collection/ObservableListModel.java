package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.AbstractListModel;

/**
 * Swing {@link javax.swing.ListModel} adapter over a Corusco
 * {@link ObservableReadableCollection}.
 *
 * <p>The adapter lets Swing list components display the same observable source
 * collection used by presenters and other Corusco models. The source remains
 * the data owner. This class only subscribes to source structural changes and
 * translates them into Swing {@code ListDataEvent}s with matching indices.</p>
 *
 * <p>The adapter is EDT-confined. Construct it on the EDT and either mutate the
 * source on the EDT while this adapter is subscribed, or wrap the source with
 * {@link EdtObservableReadableCollection} before creating this model. Without
 * an explicit dispatcher, this class intentionally fails fast instead of
 * firing Swing events on a background thread.</p>
 *
 * <p>Closing the adapter removes its source subscription. Closing is
 * idempotent, and a closed adapter still exposes the source's current contents
 * but no longer fires Swing list events. It does not close, clear, or otherwise
 * own the source collection.</p>
 *
 * @param <E> element type
 */
public class ObservableListModel<E> extends AbstractListModel<E> implements Binding {

    private final ObservableReadableCollection<E> source;
    private final Subscription subscription;
    private boolean closed;

    /**
     * Creates a list model backed by {@code source}.
     *
     * <p>The constructor subscribes immediately. The source collection should not be
     * mutated off the EDT unless it already dispatches changes on the EDT.</p>
     *
     * @param source observable source collection
     */
    public ObservableListModel(ObservableReadableCollection<E> source) {
        SwingEdt.requireEdt();
        this.source = Objects.requireNonNull(source, "source");
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a list model backed by {@code source}.
     *
     * @param source observable source collection
     * @param <E> element type
     * @return list model
     */
    public static <E> ObservableListModel<E> of(ObservableReadableCollection<E> source) {
        return new ObservableListModel<>(source);
    }

    @Override
    public int getSize() {
        return source.size();
    }

    @Override
    public E getElementAt(int index) {
        return source.get(index);
    }

    /**
     * Returns the observable source collection.
     *
     * @return source collection
     */
    protected final ObservableReadableCollection<E> source() {
        return source;
    }

    /**
     * Applies a delivered source-collection change set to this Swing model.
     *
     * <p>Subclasses may override when they need additional state maintenance
     * before or after Swing events, as {@link ObservableComboBoxModel} does for
     * selection. Implementations must preserve EDT confinement.</p>
     *
     * @param changes source-collection changes
     */
    protected void sourceChanged(ListChangeSet<E> changes) {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        for (ListChange<E> change : changes.changes()) {
            apply(change);
        }
    }

    private void apply(ListChange<E> change) {
        switch (change) {
            case ListChange.Inserted<E> inserted -> fireIntervalAdded(
                    this,
                    inserted.index(),
                    inserted.index() + inserted.elements().size() - 1
            );
            case ListChange.Removed<E> removed -> fireIntervalRemoved(
                    this,
                    removed.index(),
                    removed.index() + removed.elements().size() - 1
            );
            case ListChange.Replaced<E> replaced -> fireContentsChanged(this, replaced.index(), replaced.index());
            case ListChange.Moved<E> moved -> fireContentsChanged(
                    this,
                    Math.min(moved.fromIndex(), moved.toIndex()),
                    Math.max(moved.fromIndex(), moved.toIndex())
            );
            case ListChange.Cleared<E> cleared -> fireIntervalRemoved(this, 0, cleared.elements().size() - 1);
        }
    }

    /**
     * Removes the source subscription.
     *
     * <p>The call is idempotent. It should be made on the same Swing lifecycle
     * path that disposes the list component using this model.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        subscription.close();
        closed = true;
    }
}
