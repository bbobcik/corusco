package cz.auderis.corusco.core.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Persisted visual state for a table.
 *
 * <p>The state is immutable and Swing-free. It stores stable table/column ids,
 * not Swing column instances. Use {@link #defaults(TableDescriptor)} to create
 * descriptor-derived state and {@link #merge(TableDescriptor, TableState)} to
 * reconcile stored state with the current descriptor after generated columns
 * are added, removed, reordered, or have new width bounds.</p>
 *
 * @param schemaVersion application table-state schema version
 * @param tableId stable table id
 * @param columns immutable visual column states
 * @param sort immutable sort states
 */
public record TableState(int schemaVersion, String tableId, List<ColumnState> columns, List<SortState> sort) {

    /**
     * Default application table-state schema version.
     */
    public static final int DEFAULT_SCHEMA_VERSION = 1;

    /**
     * Creates table state with the default schema version.
     *
     * @param tableId stable table id
     * @param columns visual column states
     * @param sort sort states
     */
    public TableState(String tableId, List<ColumnState> columns, List<SortState> sort) {
        this(DEFAULT_SCHEMA_VERSION, tableId, columns, sort);
    }

    /**
     * Creates table state.
     *
     * @param schemaVersion application table-state schema version
     * @param tableId stable table id
     * @param columns visual column states
     * @param sort sort states
     */
    public TableState {
        if (schemaVersion < 0) {
            throw new IllegalArgumentException("schemaVersion must not be negative");
        }
        tableId = TableIds.requireId(tableId);
        columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        sort = List.copyOf(Objects.requireNonNull(sort, "sort"));
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be empty");
        }
        requireUniqueColumnIds(columns);
    }

    /**
     * Creates default state from a descriptor.
     *
     * @param descriptor table descriptor
     * @param <R> row type
     * @return descriptor-derived state
     */
    public static <R> TableState defaults(TableDescriptor<R> descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        descriptorColumns(descriptor);
        List<ColumnState> states = descriptor.columns().stream()
                .map(TableState::defaultColumnState)
                .sorted(Comparator.comparingInt(ColumnState::order))
                .toList();
        return new TableState(descriptor.key().id(), normalizeOrder(states), List.of());
    }

    /**
     * Merges stored state after applying a migration hook.
     *
     * @param descriptor current table descriptor
     * @param stored optional stored state, may be {@code null}
     * @param migration migration hook
     * @param <R> row type
     * @return merged state
     */
    public static <R> TableState merge(
            TableDescriptor<R> descriptor,
            TableState stored,
            TableStateMigration<R> migration
    ) {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(migration, "migration");
        if (stored == null) {
            return defaults(descriptor);
        }
        return merge(descriptor, migration.migrate(descriptor, stored));
    }

    /**
     * Merges stored state with the current descriptor.
     *
     * <p>Known stored columns keep their visibility and relative order, with
     * widths clamped to current descriptor bounds. Unknown stored columns are
     * ignored. Columns missing from stored state are appended in descriptor
     * default order. Sort state is retained only for columns still present in
     * the descriptor.</p>
     *
     * @param descriptor current table descriptor
     * @param stored optional stored state, may be {@code null}
     * @param <R> row type
     * @return merged state
     */
    public static <R> TableState merge(TableDescriptor<R> descriptor, TableState stored) {
        Objects.requireNonNull(descriptor, "descriptor");
        if (stored == null || !descriptor.key().id().equals(stored.tableId())) {
            return defaults(descriptor);
        }

        Map<String, Column<?, ?>> descriptorColumns = descriptorColumns(descriptor);
        Set<String> emitted = new HashSet<>();
        List<ColumnState> merged = new ArrayList<>();
        List<ColumnState> storedColumns = stored.columns().stream()
                .sorted(Comparator.comparingInt(ColumnState::order))
                .toList();
        for (ColumnState storedColumn : storedColumns) {
            Column<?, ?> descriptorColumn = descriptorColumns.get(storedColumn.id());
            if (descriptorColumn == null) {
                continue;
            }
            merged.add(mergeColumn(storedColumn, descriptorColumn, merged.size()));
            emitted.add(storedColumn.id());
        }

        descriptor.columns().stream()
                .filter(column -> !emitted.contains(column.descriptor().persistence().id()))
                .sorted(Comparator.comparingInt(column -> column.descriptor().defaults().order()))
                .map(TableState::defaultColumnState)
                .forEach(merged::add);

        List<ColumnState> normalizedColumns = normalizeOrder(merged);
        Set<String> knownIds = new HashSet<>(descriptorColumns.keySet());
        List<SortState> retainedSort = stored.sort().stream()
                .filter(sortState -> knownIds.contains(sortState.columnId()))
                .sorted(Comparator.comparingInt(SortState::priority))
                .map(sortState -> new SortState(sortState.columnId(), sortState.direction(), sortState.priority()))
                .toList();
        return new TableState(stored.schemaVersion(), descriptor.key().id(), normalizedColumns, normalizeSort(retainedSort));
    }

    /**
     * Returns a copy with a different schema version.
     *
     * @param schemaVersion new schema version
     * @return copied state
     */
    public TableState withSchemaVersion(int schemaVersion) {
        return new TableState(schemaVersion, tableId, columns, sort);
    }

    private static ColumnState defaultColumnState(Column<?, ?> column) {
        ColumnDescriptor<?, ?> descriptor = column.descriptor();
        ColumnDefaults defaults = descriptor.defaults();
        return new ColumnState(
                descriptor.persistence().id(),
                clamp(defaults.width(), descriptor.persistence()),
                defaults.order(),
                defaults.visible()
        );
    }

    private static ColumnState mergeColumn(ColumnState stored, Column<?, ?> descriptorColumn, int order) {
        ColumnPersistence persistence = descriptorColumn.descriptor().persistence();
        return stored.withWidthAndOrder(clamp(stored.width(), persistence), order);
    }

    private static <R> Map<String, Column<?, ?>> descriptorColumns(TableDescriptor<R> descriptor) {
        Map<String, Column<?, ?>> result = new LinkedHashMap<>();
        for (Column<R, ?> column : descriptor.columns()) {
            String id = column.descriptor().persistence().id();
            if (result.put(id, column) != null) {
                throw new IllegalArgumentException("duplicate descriptor persistence id: " + id);
            }
        }
        return result;
    }

    private static int clamp(int width, ColumnPersistence persistence) {
        return Math.max(persistence.minWidth(), Math.min(width, persistence.maxWidth()));
    }

    private static List<ColumnState> normalizeOrder(List<ColumnState> columns) {
        List<ColumnState> normalized = new ArrayList<>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            ColumnState column = columns.get(i);
            normalized.add(column.withWidthAndOrder(column.width(), i));
        }
        return List.copyOf(normalized);
    }

    private static List<SortState> normalizeSort(List<SortState> sort) {
        List<SortState> normalized = new ArrayList<>(sort.size());
        for (int i = 0; i < sort.size(); i++) {
            SortState item = sort.get(i);
            normalized.add(new SortState(item.columnId(), item.direction(), i));
        }
        return List.copyOf(normalized);
    }

    private static void requireUniqueColumnIds(List<ColumnState> columns) {
        Set<String> ids = new HashSet<>();
        for (ColumnState column : columns) {
            if (!ids.add(column.id())) {
                throw new IllegalArgumentException("duplicate column state id: " + column.id());
            }
        }
    }
}
