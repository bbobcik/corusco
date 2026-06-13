package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.ComboBoxModel;
import javax.swing.MutableComboBoxModel;

/**
 * Swing {@link ComboBoxModel} backed by an {@link ObservableList}.
 *
 * <p>The same EDT and lifecycle rules as {@link ObservableListModel} apply.
 * The adapter implements {@link MutableComboBoxModel} by routing mutations back
 * to the source list, so combo-box edits and application list edits share one
 * observable state owner.</p>
 *
 * @param <E> element type
 */
public final class ObservableComboBoxModel<E> extends ObservableListModel<E> implements MutableComboBoxModel<E> {

    private Object selectedItem;

    /**
     * Creates a combo-box model backed by {@code source}.
     *
     * @param source observable source list
     */
    public ObservableComboBoxModel(ObservableList<E> source) {
        super(source);
    }

    /**
     * Creates a combo-box model backed by {@code source}.
     *
     * @param source observable source list
     * @param <E> element type
     * @return combo-box model
     */
    public static <E> ObservableComboBoxModel<E> of(ObservableList<E> source) {
        return new ObservableComboBoxModel<>(source);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (Objects.equals(selectedItem, anItem)) {
            return;
        }
        selectedItem = anItem;
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void addElement(E item) {
        source().add(item);
    }

    @Override
    public void removeElement(Object obj) {
        for (int i = 0; i < source().size(); i++) {
            if (Objects.equals(source().get(i), obj)) {
                source().remove(i);
                return;
            }
        }
    }

    @Override
    public void insertElementAt(E item, int index) {
        source().add(index, item);
    }

    @Override
    public void removeElementAt(int index) {
        source().remove(index);
    }

    @Override
    protected void sourceChanged(ListChangeSet<E> changes) {
        SwingEdt.requireEdt();
        boolean selectionRemoved = selectedItem != null
                && removesSelectedValue(changes)
                && !sourceContainsSelection();
        super.sourceChanged(changes);
        if (selectionRemoved) {
            setSelectedItem(null);
        }
    }

    private boolean removesSelectedValue(ListChangeSet<E> changes) {
        for (ListChange<E> change : changes.changes()) {
            switch (change) {
                case ListChange.Removed<E> removed -> {
                    if (removed.elements().stream().anyMatch(element -> Objects.equals(element, selectedItem))) {
                        return true;
                    }
                }
                case ListChange.Cleared<E> cleared -> {
                    if (cleared.elements().stream().anyMatch(element -> Objects.equals(element, selectedItem))) {
                        return true;
                    }
                }
                case ListChange.Replaced<E> replaced -> {
                    if (Objects.equals(replaced.oldElement(), selectedItem)
                            && !Objects.equals(replaced.newElement(), selectedItem)) {
                        return true;
                    }
                }
                case ListChange.Inserted<E> ignored -> {
                }
                case ListChange.Moved<E> ignored -> {
                }
            }
        }
        return false;
    }

    private boolean sourceContainsSelection() {
        for (int i = 0; i < source().size(); i++) {
            if (Objects.equals(source().get(i), selectedItem)) {
                return true;
            }
        }
        return false;
    }
}
