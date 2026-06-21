package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.WritableValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelListener;

/**
 * Binds {@link JTable} row selection to Corusco observable values.
 *
 * <p>The binding is EDT-confined. User-originated table selection updates the
 * selected model-row index and optional row value with
 * {@link StandardChangeOrigin#USER}. Presenter-originated changes to the
 * model-row value select the corresponding JTable view row with
 * {@link StandardChangeOrigin#MODEL} already carried by the value event.</p>
 *
 * <p>Selection is single-row oriented. The JTable may technically allow
 * multiple selected rows, but this binding tracks the lead selected row exposed
 * by {@link JTable#getSelectedRow()}. Sorting and filtering are handled through
 * JTable's view/model index conversion APIs. Configure any row sorter before
 * creating the binding; if the table receives a replacement sorter, close and
 * recreate the binding. Closing removes all installed Swing and value
 * listeners.</p>
 *
 * @param <R> row type
 */
public final class TableSelectionBinding<R> implements Binding {

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final WritableValue<Integer> selectedModelRow;
    private final WritableValue<R> selectedRow;
    private final SubscriptionScope scope = new SubscriptionScope();
    private RowSorter<?> attachedSorter;
    private boolean updatingFromTable;
    private boolean updatingFromValue;
    private boolean closed;

    private TableSelectionBinding(
            JTable table,
            ObservableTableModel<R> model,
            WritableValue<Integer> selectedModelRow,
            WritableValue<R> selectedRow
    ) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.selectedModelRow = Objects.requireNonNull(selectedModelRow, "selectedModelRow");
        this.selectedRow = selectedRow;
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied ObservableTableModel");
        }
        installListeners();
        updateValuesFromTable(StandardChangeOrigin.MODEL);
    }

    /**
     * Binds table selection to a selected model-row value.
     *
     * @param table Swing table
     * @param model observable table model installed on the table
     * @param selectedModelRow selected model-row value, whose value may be {@code null}
     * @param <R> row type
     * @return binding
     */
    public static <R> TableSelectionBinding<R> bind(
            JTable table,
            ObservableTableModel<R> model,
            WritableValue<Integer> selectedModelRow
    ) {
        return new TableSelectionBinding<>(table, model, selectedModelRow, null);
    }

    /**
     * Binds table selection to selected model-row and row values.
     *
     * @param table Swing table
     * @param model observable table model installed on the table
     * @param selectedModelRow selected model-row value, whose value may be {@code null}
     * @param selectedRow selected row value, whose value may be {@code null}
     * @param <R> row type
     * @return binding
     */
    public static <R> TableSelectionBinding<R> bind(
            JTable table,
            ObservableTableModel<R> model,
            WritableValue<Integer> selectedModelRow,
            WritableValue<R> selectedRow
    ) {
        return new TableSelectionBinding<>(
                table,
                model,
                selectedModelRow,
                Objects.requireNonNull(selectedRow, "selectedRow")
        );
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        scope.close();
    }

    private void installListeners() {
        ListSelectionListener selectionListener = event -> {
            SwingEdt.requireEdt();
            if (!event.getValueIsAdjusting() && !updatingFromValue) {
                updateValuesFromTable(StandardChangeOrigin.USER);
            }
        };
        table.getSelectionModel().addListSelectionListener(selectionListener);
        scope.onClose(() -> table.getSelectionModel().removeListSelectionListener(selectionListener));

        TableModelListener tableModelListener = event -> refreshAfterStructureChange();
        model.addTableModelListener(tableModelListener);
        scope.onClose(() -> model.removeTableModelListener(tableModelListener));

        RowSorterListener rowSorterListener = this::sorterChanged;
        if (table.getRowSorter() != null) {
            attachedSorter = table.getRowSorter();
            attachedSorter.addRowSorterListener(rowSorterListener);
        }
        scope.onClose(() -> {
            if (attachedSorter != null) {
                attachedSorter.removeRowSorterListener(rowSorterListener);
            }
        });

        Subscription selectionSubscription = selectedModelRow.subscribe(event -> {
            SwingEdt.requireEdt();
            if (!updatingFromTable) {
                selectModelRow(event.newValue());
            }
        });
        scope.add(selectionSubscription);
    }

    private void sorterChanged(RowSorterEvent event) {
        SwingEdt.requireEdt();
        refreshAfterStructureChange();
    }

    private void refreshAfterStructureChange() {
        SwingEdt.requireEdt();
        if (updatingFromValue) {
            return;
        }
        Integer selected = selectedModelRow.value();
        if (selected == null || selected < 0 || selected >= model.getRowCount()) {
            clearTableSelection();
            updateValues(null, null, StandardChangeOrigin.MODEL);
            return;
        }
        selectModelRow(selected);
        updateValuesFromTable(StandardChangeOrigin.MODEL);
    }

    private void updateValuesFromTable(ChangeOrigin origin) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            updateValues(null, null, origin);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        R row = modelRow >= 0 && modelRow < model.getRowCount() ? model.readableRows().get(modelRow) : null;
        updateValues(modelRow, row, origin);
    }

    private void updateValues(Integer modelRow, R row, ChangeOrigin origin) {
        updatingFromTable = true;
        try {
            selectedModelRow.setValue(modelRow, origin);
            if (selectedRow != null) {
                selectedRow.setValue(row, origin);
            }
        } finally {
            updatingFromTable = false;
        }
    }

    private void selectModelRow(Integer modelRow) {
        SwingEdt.requireEdt();
        updatingFromValue = true;
        try {
            if (modelRow == null || modelRow < 0 || modelRow >= model.getRowCount()) {
                clearTableSelection();
                updateValues(null, null, StandardChangeOrigin.MODEL);
                return;
            }
            int viewRow = table.convertRowIndexToView(modelRow);
            if (viewRow < 0) {
                clearTableSelection();
                updateValues(modelRow, model.readableRows().get(modelRow), StandardChangeOrigin.MODEL);
                return;
            }
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            if (selectedRow != null) {
                selectedRow.setValue(model.readableRows().get(modelRow), StandardChangeOrigin.MODEL);
            }
        } finally {
            updatingFromValue = false;
        }
    }

    private void clearTableSelection() {
        table.clearSelection();
    }
}
