package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnPersistence;
import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableState;
import cz.auderis.corusco.core.table.TableStateMigration;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * EDT-confined controller that restores and persists a {@link JTable}'s view state.
 *
 * <p>This type is the bridge between toolkit-neutral table metadata in
 * {@link TableDescriptor}/{@link TableState} and Swing's mutable
 * {@link TableColumnModel}. It does not own row data or cell values. Instead it
 * reads stable column persistence ids from the {@link ObservableTableModel}
 * descriptor, merges saved state with current descriptor defaults, applies
 * column order, visibility, width bounds, and sorter keys to the JTable, then
 * listens for Swing column and sort changes.</p>
 *
 * <p>State writes are debounced on the EDT so resize and drag gestures coalesce
 * into one store write. Programmatic changes such as
 * {@link #setColumnVisible(String, boolean)} also schedule persistence. The
 * controller owns the Swing listeners and the save scheduler; it does not own
 * the table, model, descriptor, or {@link TableStateStore}. Close it with the
 * surrounding view lifecycle to remove listeners, write any pending state, and
 * flush the store. After close, mutation and capture methods reject use.</p>
 *
 * <p>Use this controller only with an {@link ObservableTableModel} installed in
 * the same table. Avoid modifying the column model behind the controller except
 * through JTable gestures or documented controller methods, because direct
 * changes may bypass the persistence assumptions around hidden columns and
 * stable persistence ids.</p>
 *
 * @param <R> row type
 */
public final class TableStateController<R> implements Binding {

    /**
     * Default debounce interval for table state writes triggered by Swing
     * column and sorter events.
     */
    public static final int DEFAULT_SAVE_DELAY_MILLIS = 250;

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final TableStateStore store;
    private final TableDescriptor<R> descriptor;
    private final TableColumnModel columnModel;
    private final Map<String, Column<R, ?>> columnsById;
    private final Map<String, TableColumn> tableColumnsById;
    private final TableColumnModelListener columnListener = new PersistingColumnListener();
    private final RowSorterListener sorterListener = this::sorterChanged;
    private final TableStateSaveScheduler saveScheduler;

    private boolean applying;
    private boolean closed;

    /**
     * Installs a controller for an already configured table/model pair.
     *
     * <p>The controller is active when this method returns: saved state has
     * been applied, listeners are registered, and the current merged state has
     * been written once.</p>
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param <R> row type
     * @return installed controller
     */
    public static <R> TableStateController<R> install(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store
    ) {
        return new TableStateController<>(table, model, store);
    }

    /**
     * Installs a controller with a migration hook for loaded state.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param migration migration hook applied before descriptor merge
     * @param <R> row type
     * @return installed controller
     */
    public static <R> TableStateController<R> install(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            TableStateMigration<R> migration
    ) {
        return new TableStateController<>(table, model, store, migration);
    }

    /**
     * Installs a controller with an explicit event-save debounce interval.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param saveDelayMillis debounce delay for event-triggered saves
     * @param <R> row type
     * @return installed controller
     */
    public static <R> TableStateController<R> install(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            int saveDelayMillis
    ) {
        return new TableStateController<>(table, model, store, saveDelayMillis);
    }

    /**
     * Installs a controller with a migration hook and explicit debounce.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param migration migration hook applied before descriptor merge
     * @param saveDelayMillis debounce delay for event-triggered saves
     * @param <R> row type
     * @return installed controller
     */
    public static <R> TableStateController<R> install(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            TableStateMigration<R> migration,
            int saveDelayMillis
    ) {
        return new TableStateController<>(table, model, store, migration, saveDelayMillis);
    }

    /**
     * Creates and installs a controller.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     */
    public TableStateController(JTable table, ObservableTableModel<R> model, TableStateStore store) {
        this(table, model, store, DEFAULT_SAVE_DELAY_MILLIS);
    }

    /**
     * Creates and installs a controller with a migration hook.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param migration migration hook applied before descriptor merge
     */
    public TableStateController(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            TableStateMigration<R> migration
    ) {
        this(table, model, store, migration, DEFAULT_SAVE_DELAY_MILLIS);
    }

    /**
     * Creates and installs a controller with an explicit debounce interval.
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param saveDelayMillis debounce delay for event-triggered saves
     */
    public TableStateController(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            int saveDelayMillis
    ) {
        this(table, model, store, TableStateMigration.none(), saveDelayMillis);
    }

    /**
     * Creates and installs a controller with a migration hook and debounce.
     *
     * <p>The constructor must run on the EDT. It validates that the table is
     * using the supplied model, discovers the current Swing table columns,
     * applies migrated/merged stored state, registers listeners, and saves the
     * resulting state immediately. The migration hook is applied only to loaded
     * store state before descriptor defaults are merged.</p>
     *
     * @param table Swing table to restore and observe
     * @param model descriptor-backed table model installed in the table
     * @param store table state store
     * @param migration migration hook applied before descriptor merge
     * @param saveDelayMillis debounce delay for event-triggered saves
     * @throws IllegalStateException if called off the EDT
     * @throws IllegalArgumentException if the table does not use the supplied
     *         model or descriptor column ids are duplicated
     */
    public TableStateController(
            JTable table,
            ObservableTableModel<R> model,
            TableStateStore store,
            TableStateMigration<R> migration,
            int saveDelayMillis
    ) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.store = Objects.requireNonNull(store, "store");
        Objects.requireNonNull(migration, "migration");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied model");
        }
        this.descriptor = model.descriptor();
        this.columnModel = table.getColumnModel();
        this.columnsById = descriptorColumns(descriptor);
        this.tableColumnsById = discoverTableColumns();
        this.saveScheduler = new TableStateSaveScheduler(saveDelayMillis, this::writeCurrentState);

        applyState(TableState.merge(descriptor, store.load(descriptor.key().id()).orElse(null), migration));
        columnModel.addColumnModelListener(columnListener);
        if (table.getRowSorter() != null) {
            table.getRowSorter().addRowSorterListener(sorterListener);
        }
        saveNow();
    }

    /**
     * Returns the currently captured table presentation state without writing it.
     *
     * <p>The snapshot includes visible and hidden descriptor columns plus the
     * current sorter keys expressed with stable column persistence ids. It does
     * not include row selection, scroll position, filters, or row data.</p>
     *
     * @return current table state
     * @throws IllegalStateException if called off the EDT
     */
    public TableState captureState() {
        SwingEdt.requireEdt();
        List<ColumnState> columns = captureColumns();
        return new TableState(descriptor.key().id(), columns, captureSort());
    }

    /**
     * Shows or hides one descriptor column by stable persistence id.
     *
     * <p>This programmatic hook is intentionally small. A later header menu can
     * call it without learning how hidden Swing {@link TableColumn} instances
     * are cached and restored. Calling it with the current visibility is a
     * no-op; a real change schedules a debounced save.</p>
     *
     * @param columnId stable column persistence id
     * @param visible whether the column should be visible
     * @throws IllegalStateException if called off the EDT or after close
     * @throws IllegalArgumentException if {@code columnId} is not known by the
     *         descriptor
     */
    public void setColumnVisible(String columnId, boolean visible) {
        SwingEdt.requireEdt();
        requireOpen();
        TableColumn column = requireColumn(columnId);
        boolean currentlyVisible = visibleColumnIds().contains(columnId);
        if (visible == currentlyVisible) {
            return;
        }
        applying = true;
        try {
            if (visible) {
                columnModel.addColumn(column);
                columnModel.moveColumn(columnModel.getColumnCount() - 1, visibleInsertIndex(columnId));
            } else {
                columnModel.removeColumn(column);
            }
        } finally {
            applying = false;
        }
        scheduleSave();
    }

    /**
     * Saves the currently captured state immediately.
     *
     * <p>This cancels any pending delayed save. Event-triggered saves use the
     * configured debounce interval; call this method for explicit user actions
     * or tests that need deterministic persistence at that point.</p>
     *
     * @throws IllegalStateException if called off the EDT or after close
     */
    public void saveNow() {
        SwingEdt.requireEdt();
        requireOpen();
        saveScheduler.saveNow();
    }

    /**
     * Writes any delayed save that has been scheduled but not yet fired.
     *
     * <p>If no delayed save is pending, this method is a no-op. It is useful
     * before assertions or before a surrounding lifecycle performs additional
     * persistence work.</p>
     *
     * @throws IllegalStateException if called off the EDT or after close
     */
    public void flushPendingSaves() {
        SwingEdt.requireEdt();
        requireOpen();
        saveScheduler.flushPending();
    }

    /**
     * Removes installed listeners, writes pending state, and flushes the store.
     *
     * <p>The call must run on the EDT and is idempotent. Close does not dispose
     * the table, model, row sorter, or state store; it only detaches this
     * controller from the table lifecycle.</p>
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        columnModel.removeColumnModelListener(columnListener);
        if (table.getRowSorter() != null) {
            table.getRowSorter().removeRowSorterListener(sorterListener);
        }
        saveScheduler.flushPending();
        store.flush();
        closed = true;
    }

    private void applyState(TableState state) {
        applying = true;
        try {
            removeVisibleColumns();
            List<ColumnState> visibleColumns = state.columns().stream()
                    .filter(ColumnState::visible)
                    .sorted(Comparator.comparingInt(ColumnState::order))
                    .toList();
            for (ColumnState columnState : visibleColumns) {
                TableColumn tableColumn = requireColumn(columnState.id());
                applyColumnBounds(tableColumn, columnState);
                columnModel.addColumn(tableColumn);
            }
            applySort(state.sort());
        } finally {
            applying = false;
        }
    }

    private Map<String, TableColumn> discoverTableColumns() {
        Map<String, TableColumn> result = new LinkedHashMap<>();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            String columnId = columnIdForModelIndex(tableColumn.getModelIndex());
            tableColumn.setIdentifier(columnId);
            applyColumnBounds(tableColumn, defaultColumnState(columnId));
            result.put(columnId, tableColumn);
        }
        return result;
    }

    private void removeVisibleColumns() {
        while (columnModel.getColumnCount() > 0) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
    }

    private void applyColumnBounds(TableColumn tableColumn, ColumnState state) {
        ColumnPersistence persistence = columnsById.get(state.id()).descriptor().persistence();
        tableColumn.setMinWidth(persistence.minWidth());
        tableColumn.setMaxWidth(persistence.maxWidth());
        tableColumn.setPreferredWidth(state.width());
        tableColumn.setWidth(state.width());
    }

    private void applySort(List<SortState> sortStates) {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (sorter == null) {
            return;
        }
        List<RowSorter.SortKey> keys = sortStates.stream()
                .sorted(Comparator.comparingInt(SortState::priority))
                .map(sort -> new RowSorter.SortKey(modelIndex(sort.columnId()), sortOrder(sort.direction())))
                .toList();
        sorter.setSortKeys(keys);
    }

    private List<ColumnState> captureColumns() {
        List<ColumnState> visible = new ArrayList<>();
        Set<String> visibleIds = new HashSet<>();
        for (int viewIndex = 0; viewIndex < columnModel.getColumnCount(); viewIndex++) {
            TableColumn tableColumn = columnModel.getColumn(viewIndex);
            String columnId = columnIdForModelIndex(tableColumn.getModelIndex());
            tableColumnsById.put(columnId, tableColumn);
            visibleIds.add(columnId);
            visible.add(new ColumnState(columnId, tableColumn.getWidth(), viewIndex, true));
        }

        List<ColumnState> hidden = descriptor.columns().stream()
                .map(column -> column.descriptor().persistence().id())
                .filter(columnId -> !visibleIds.contains(columnId))
                .map(columnId -> new ColumnState(columnId, requireColumn(columnId).getWidth(), visible.size(), false))
                .toList();

        List<ColumnState> result = new ArrayList<>(visible.size() + hidden.size());
        result.addAll(visible);
        for (ColumnState hiddenColumn : hidden) {
            result.add(new ColumnState(
                    hiddenColumn.id(),
                    hiddenColumn.width(),
                    result.size(),
                    false
            ));
        }
        return List.copyOf(result);
    }

    private List<SortState> captureSort() {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (sorter == null) {
            return List.of();
        }
        List<SortState> result = new ArrayList<>();
        List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
        for (int i = 0; i < sortKeys.size(); i++) {
            RowSorter.SortKey key = sortKeys.get(i);
            SortDirection direction = sortDirection(key.getSortOrder());
            if (direction == null || key.getColumn() < 0 || key.getColumn() >= descriptor.columns().size()) {
                continue;
            }
            result.add(new SortState(columnIdForModelIndex(key.getColumn()), direction, result.size()));
        }
        return List.copyOf(result);
    }

    private int visibleInsertIndex(String columnId) {
        List<ColumnState> state = captureColumns().stream()
                .sorted(Comparator.comparingInt(ColumnState::order))
                .toList();
        int targetOrder = state.stream()
                .filter(column -> column.id().equals(columnId))
                .findFirst()
                .map(ColumnState::order)
                .orElse(Integer.MAX_VALUE);
        int visibleBefore = 0;
        for (ColumnState columnState : state) {
            if (columnState.id().equals(columnId)) {
                continue;
            }
            if (columnState.visible() && columnState.order() < targetOrder) {
                visibleBefore++;
            }
        }
        return Math.min(visibleBefore, columnModel.getColumnCount() - 1);
    }

    private TableColumn requireColumn(String columnId) {
        TableColumn tableColumn = tableColumnsById.get(columnId);
        if (tableColumn == null) {
            throw new IllegalArgumentException("Unknown table column id: " + columnId);
        }
        return tableColumn;
    }

    private int modelIndex(String columnId) {
        Column<R, ?> column = columnsById.get(columnId);
        if (column == null) {
            throw new IllegalArgumentException("Unknown table column id: " + columnId);
        }
        return descriptor.columns().indexOf(column);
    }

    private String columnIdForModelIndex(int modelIndex) {
        if (modelIndex < 0 || modelIndex >= descriptor.columns().size()) {
            throw new IllegalArgumentException("Unknown table model column index: " + modelIndex);
        }
        return descriptor.column(modelIndex).descriptor().persistence().id();
    }

    private ColumnState defaultColumnState(String columnId) {
        Column<R, ?> column = columnsById.get(columnId);
        ColumnPersistence persistence = column.descriptor().persistence();
        return new ColumnState(
                columnId,
                Math.max(persistence.minWidth(), Math.min(column.descriptor().defaults().width(), persistence.maxWidth())),
                column.descriptor().defaults().order(),
                column.descriptor().defaults().visible()
        );
    }

    private Set<String> visibleColumnIds() {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            result.add(columnIdForModelIndex(columnModel.getColumn(i).getModelIndex()));
        }
        return result;
    }

    private void sorterChanged(RowSorterEvent event) {
        if (!applying && !closed) {
            scheduleSave();
        }
    }

    private void scheduleSave() {
        SwingEdt.requireEdt();
        requireOpen();
        saveScheduler.schedule();
    }

    private void writeCurrentState() {
        store.save(captureState());
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Table state controller is closed");
        }
    }

    private static SortOrder sortOrder(SortDirection direction) {
        return switch (direction) {
            case ASCENDING -> SortOrder.ASCENDING;
            case DESCENDING -> SortOrder.DESCENDING;
        };
    }

    private static SortDirection sortDirection(SortOrder order) {
        return switch (order) {
            case ASCENDING -> SortDirection.ASCENDING;
            case DESCENDING -> SortDirection.DESCENDING;
            default -> null;
        };
    }

    private static <R> Map<String, Column<R, ?>> descriptorColumns(TableDescriptor<R> descriptor) {
        Map<String, Column<R, ?>> result = new HashMap<>();
        for (Column<R, ?> column : descriptor.columns()) {
            String id = column.descriptor().persistence().id();
            if (result.put(id, column) != null) {
                throw new IllegalArgumentException("duplicate descriptor persistence id: " + id);
            }
        }
        return Map.copyOf(result);
    }

    private final class PersistingColumnListener implements TableColumnModelListener {

        @Override
        public void columnAdded(TableColumnModelEvent event) {
            persistIfReady();
        }

        @Override
        public void columnRemoved(TableColumnModelEvent event) {
            persistIfReady();
        }

        @Override
        public void columnMoved(TableColumnModelEvent event) {
            if (event.getFromIndex() != event.getToIndex()) {
                persistIfReady();
            }
        }

        @Override
        public void columnMarginChanged(ChangeEvent event) {
            persistIfReady();
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent event) {
            // Selection state is deliberately outside table layout persistence.
        }

        private void persistIfReady() {
            if (!applying && !closed) {
                scheduleSave();
            }
        }
    }
}
