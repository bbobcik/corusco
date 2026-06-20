package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.table.AbstractTableModel;

/**
 * Swing table model backed by an {@link ObservableReadableCollection} of typed
 * rows.
 *
 * <p>The model is EDT-confined. Construct it on the EDT and either mutate the
 * source on the EDT while this model is subscribed, or wrap the source with
 * {@link cz.auderis.corusco.swing.collection.EdtObservableReadableCollection}
 * before creating this model. Without an explicit dispatcher, off-EDT source
 * changes fail fast instead of firing Swing events on the wrong thread.</p>
 *
 * <p>Cell values are read through typed {@link Column} extractors. Editable
 * models created with {@link #of(ObservableList, TableDescriptor)} replace the
 * row in the source list through the column updater, which supports immutable
 * record rows and generated wither methods. Models created with
 * {@link #readOnly(ObservableReadableCollection, TableDescriptor)} observe any
 * readable collection, report all cells as non-editable, and ignore
 * {@link #setValueAt(Object, int, int)}. Closing the model removes its source
 * subscription and is idempotent.</p>
 *
 * <p>Generated {@code @SwingTable} records create row-specific companions,
 * such as {@code CustomerRowTableDescriptor} with a table-model factory and
 * {@code CustomerRowTableBindings} for installing the model into a
 * {@code JTable} with a binding scope. Applications can also call
 * {@link #of(ObservableList, TableDescriptor)} or
 * {@link #readOnly(ObservableReadableCollection, TableDescriptor)} directly
 * for handwritten table assembly.</p>
 *
 * @param <R> row type
 */
public final class ObservableTableModel<R> extends AbstractTableModel implements Binding {

    private final ObservableReadableCollection<R> rows;
    private final ObservableList<R> mutableRows;
    private final TableDescriptor<R> descriptor;
    private final Subscription subscription;
    private final boolean readOnly;
    private boolean closed;
    private boolean suppressSourceEvents;

    /**
     * Creates a table model.
     *
     * <p>The source list is used for both reads and edit propagation. Editable
     * descriptor columns update rows by replacing the element at the edited
     * model row index.</p>
     *
     * @param rows observable row source
     * @param descriptor table descriptor
     */
    public ObservableTableModel(ObservableList<R> rows, TableDescriptor<R> descriptor) {
        this(rows, rows, descriptor, false);
    }

    private ObservableTableModel(
            ObservableReadableCollection<R> rows,
            ObservableList<R> mutableRows,
            TableDescriptor<R> descriptor,
            boolean readOnly
    ) {
        SwingEdt.requireEdt();
        this.rows = Objects.requireNonNull(rows, "rows");
        this.mutableRows = mutableRows;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.readOnly = readOnly;
        this.subscription = rows.subscribe(this::rowsChanged);
    }

    /**
     * Creates a table model.
     *
     * <p>The returned model can propagate edits for descriptor columns that
     * declare an updater.</p>
     *
     * @param rows observable row source
     * @param descriptor table descriptor
     * @param <R> row type
     * @return table model
     */
    public static <R> ObservableTableModel<R> of(ObservableList<R> rows, TableDescriptor<R> descriptor) {
        return new ObservableTableModel<>(rows, descriptor);
    }

    /**
     * Creates a read-only table model.
     *
     * <p>The returned model observes {@code rows} and exposes descriptor values
     * to Swing, but never edits the source. All cells are non-editable and
     * calls to {@link #setValueAt(Object, int, int)} have no effect.</p>
     *
     * @param rows observable row source
     * @param descriptor table descriptor
     * @param <R> row type
     * @return read-only table model
     */
    public static <R> ObservableTableModel<R> readOnly(
            ObservableReadableCollection<R> rows,
            TableDescriptor<R> descriptor
    ) {
        return new ObservableTableModel<>(rows, null, descriptor, true);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return descriptor.columns().size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return column(columnIndex).descriptor().headerKey().id();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return column(columnIndex).valueType();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return column(columnIndex).value(rows.get(rowIndex));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !readOnly && column(columnIndex).editable();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        SwingEdt.requireEdt();
        if (readOnly) {
            return;
        }
        Column<R, ?> column = column(columnIndex);
        if (!column.editable()) {
            return;
        }
        R updatedRow = column.update(rows.get(rowIndex), value);
        suppressSourceEvents = true;
        try {
            mutableRows.set(rowIndex, updatedRow);
        } finally {
            suppressSourceEvents = false;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Returns table descriptor used by this model.
     *
     * @return descriptor
     */
    public TableDescriptor<R> descriptor() {
        return descriptor;
    }

    /**
     * Returns observable row source used by this model.
     *
     * @return rows
     */
    public ObservableReadableCollection<R> rows() {
        return rows;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        subscription.close();
        closed = true;
    }

    private Column<R, ?> column(int index) {
        return descriptor.column(index);
    }

    private void rowsChanged(ListChangeSet<R> changes) {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        if (suppressSourceEvents) {
            return;
        }
        for (ListChange<R> change : changes.changes()) {
            apply(change);
        }
    }

    private void apply(ListChange<R> change) {
        switch (change) {
            case ListChange.Inserted<R> inserted -> fireTableRowsInserted(
                    inserted.index(),
                    inserted.index() + inserted.elements().size() - 1
            );
            case ListChange.Removed<R> removed -> fireTableRowsDeleted(
                    removed.index(),
                    removed.index() + removed.elements().size() - 1
            );
            case ListChange.Replaced<R> replaced -> fireTableRowsUpdated(replaced.index(), replaced.index());
            case ListChange.Moved<R> moved -> fireTableRowsUpdated(
                    Math.min(moved.fromIndex(), moved.toIndex()),
                    Math.max(moved.fromIndex(), moved.toIndex())
            );
            case ListChange.Cleared<R> cleared -> fireTableRowsDeleted(0, cleared.elements().size() - 1);
        }
    }
}
