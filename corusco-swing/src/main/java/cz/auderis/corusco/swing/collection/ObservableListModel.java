package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.AbstractListModel;

/**
 * Swing {@link javax.swing.ListModel} backed by an {@link ObservableList}.
 *
 * <p>The adapter is EDT-confined. Construct it on the EDT and either mutate the
 * source list on the EDT while this adapter is subscribed, or wrap the source
 * with {@link EdtObservableList} before creating this model. Without an
 * explicit dispatcher, this class intentionally fails fast instead of firing
 * Swing events on a background thread.</p>
 *
 * <p>Closing the adapter removes its source-list subscription. Closing is
 * idempotent, and a closed adapter still exposes the source's current contents
 * but no longer fires Swing list events.</p>
 *
 * @param <E> element type
 */
public class ObservableListModel<E> extends AbstractListModel<E> implements Binding {

    private final ObservableList<E> source;
    private final Subscription subscription;
    private boolean closed;

    /**
     * Creates a list model backed by {@code source}.
     *
     * @param source observable source list
     */
    public ObservableListModel(ObservableList<E> source) {
        SwingEdt.requireEdt();
        this.source = Objects.requireNonNull(source, "source");
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a list model backed by {@code source}.
     *
     * @param source observable source list
     * @param <E> element type
     * @return list model
     */
    public static <E> ObservableListModel<E> of(ObservableList<E> source) {
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
     * Returns the observable source list.
     *
     * @return source list
     */
    protected final ObservableList<E> source() {
        return source;
    }

    /**
     * Applies a delivered source-list change set to this Swing model.
     *
     * @param changes source-list changes
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

    @Override
    public void close() {
        if (closed) {
            return;
        }
        subscription.close();
        closed = true;
    }
}
