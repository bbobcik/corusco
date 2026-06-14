/**
 * Swing list and combo-box adapters for Corusco observable collections.
 *
 * <p>This package connects {@link cz.auderis.corusco.core.collection.ObservableList}
 * to Swing's list model APIs. Start with
 * {@link cz.auderis.corusco.swing.collection.ObservableListModel} when a
 * {@code JList} or other Swing consumer needs a read-only list model backed by
 * an observable source. Use {@link
 * cz.auderis.corusco.swing.collection.ObservableComboBoxModel} when Swing also
 * needs combo-box selection and mutation operations.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.collection.EdtObservableList} is the
 * dispatch boundary for source lists that may be mutated away from the Event
 * Dispatch Thread. It observes source changes and republishes them on the EDT
 * before a Swing model receives them. Without such a boundary, mutate the
 * source list on the EDT while a Swing model is subscribed.</p>
 *
 * <p>The Swing models are adapters, not storage owners. The source
 * {@code ObservableList} remains the data owner; closing the model removes its
 * subscription but does not dispose or clear the source list. Table-specific
 * list adaptation lives in {@code cz.auderis.corusco.swing.table} because
 * table models also need column descriptors, edit routing, and sort/selection
 * concerns.</p>
 */
package cz.auderis.corusco.swing.collection;
