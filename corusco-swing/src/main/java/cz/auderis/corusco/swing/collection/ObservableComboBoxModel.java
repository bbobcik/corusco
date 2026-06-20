package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.ComboBoxModel;
import javax.swing.MutableComboBoxModel;

/**
 * Swing combo-box model backed by a Corusco {@link ObservableList}.
 *
 * <p>This adapter extends {@link ObservableListModel} for Swing's
 * {@link ComboBoxModel} contract. The observable list remains the owner of the
 * option data. Combo-box mutations through {@link MutableComboBoxModel} are
 * routed back to the source list, so user edits and presenter edits share the
 * same observable state and listeners see the normal Corusco list changes.</p>
 *
 * <p>The adapter owns only Swing selection state and the inherited source-list
 * subscription. Selection is stored separately from the source list because
 * Swing's combo-box model allows a selected item that is not currently present.
 * If a source-list change removes the selected value and no equal value remains
 * in the source, the adapter clears selection and fires Swing's standard
 * contents-changed notification for selection.</p>
 *
 * <p>Use the same EDT and lifecycle rules as {@link ObservableListModel}:
 * create and use it on the EDT, ensure source changes are delivered on the EDT,
 * and close the model when the owning component lifecycle ends.</p>
 *
 * @param <E> element type
 */
public final class ObservableComboBoxModel<E> extends ObservableListModel<E> implements MutableComboBoxModel<E> {

    private Object selectedItem;
    private final ObservableList<E> mutableSource;

    /**
     * Creates a combo-box model backed by {@code source}.
     *
     * <p>The inherited constructor subscribes immediately to source changes.
     * The initial selection is {@code null}.</p>
     *
     * @param source observable source list
     */
    public ObservableComboBoxModel(ObservableList<E> source) {
        super(source);
        this.mutableSource = Objects.requireNonNull(source, "source");
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

    /**
     * Sets the selected item and fires Swing selection change events.
     *
     * <p>The item is not required to be present in the source list, following
     * Swing's {@link ComboBoxModel} contract.</p>
     */
    @Override
    public void setSelectedItem(Object anItem) {
        if (Objects.equals(selectedItem, anItem)) {
            return;
        }
        selectedItem = anItem;
        fireContentsChanged(this, -1, -1);
    }

    /**
     * Returns the currently selected item.
     *
     * @return selected item, possibly {@code null}
     */
    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    /**
     * Appends an option to the source list.
     */
    @Override
    public void addElement(E item) {
        mutableSource.add(item);
    }

    /**
     * Removes the first source-list element equal to {@code obj}, if present.
     */
    @Override
    public void removeElement(Object obj) {
        for (int i = 0; i < mutableSource.size(); i++) {
            if (Objects.equals(mutableSource.get(i), obj)) {
                mutableSource.remove(i);
                return;
            }
        }
    }

    @Override
    public void insertElementAt(E item, int index) {
        mutableSource.add(index, item);
    }

    @Override
    public void removeElementAt(int index) {
        mutableSource.remove(index);
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
        for (int i = 0; i < mutableSource.size(); i++) {
            if (Objects.equals(mutableSource.get(i), selectedItem)) {
                return true;
            }
        }
        return false;
    }
}
