package cz.auderis.corusco.core.table;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * {@link Preferences}-backed table state store.
 *
 * <p>Each table id is encoded into a child node under the configured root.
 * Column and sort entries are written as structured preference keys instead
 * of Java serialization, keeping the persisted form explicit and independent
 * of record binary compatibility. The internal store format version is
 * separate from {@link TableState#schemaVersion()}, which belongs to the
 * application's table schema and is available to migration hooks. Instances
 * are not synchronized; use one instance from the UI/lifecycle owner that also
 * calls {@link #flush()}.</p>
 */
public final class PreferencesTableStateStore implements TableStateStore {

    private static final int FORMAT_VERSION = 1;

    private static final String KEY_VERSION = "version";
    private static final String KEY_SCHEMA_VERSION = "schemaVersion";
    private static final String KEY_TABLE_ID = "tableId";
    private static final String KEY_COLUMN_COUNT = "columns.count";
    private static final String KEY_SORT_COUNT = "sort.count";

    private final Preferences root;

    /**
     * Creates a store rooted at the supplied preferences node.
     *
     * @param root root node that owns table-state child nodes
     */
    public PreferencesTableStateStore(Preferences root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    public Optional<TableState> load(String tableId) {
        String requiredTableId = TableIds.requireId(tableId);
        try {
            String nodeName = nodeName(requiredTableId);
            if (!root.nodeExists(nodeName)) {
                return Optional.empty();
            }
            Preferences node = root.node(nodeName);
            return Optional.of(readState(requiredTableId, node));
        } catch (BackingStoreException e) {
            throw new TableStateStoreException("Cannot load table state for " + requiredTableId, e);
        }
    }

    @Override
    public void save(TableState state) {
        Objects.requireNonNull(state, "state");
        try {
            Preferences node = root.node(nodeName(state.tableId()));
            node.clear();
            writeState(node, state);
        } catch (BackingStoreException e) {
            throw new TableStateStoreException("Cannot save table state for " + state.tableId(), e);
        }
    }

    @Override
    public void remove(String tableId) {
        String requiredTableId = TableIds.requireId(tableId);
        try {
            String nodeName = nodeName(requiredTableId);
            if (root.nodeExists(nodeName)) {
                root.node(nodeName).removeNode();
            }
        } catch (BackingStoreException e) {
            throw new TableStateStoreException("Cannot remove table state for " + requiredTableId, e);
        }
    }

    @Override
    public void flush() {
        try {
            root.flush();
        } catch (BackingStoreException e) {
            throw new TableStateStoreException("Cannot flush table state preferences", e);
        }
    }

    private static void writeState(Preferences node, TableState state) {
        node.putInt(KEY_VERSION, FORMAT_VERSION);
        node.putInt(KEY_SCHEMA_VERSION, state.schemaVersion());
        node.put(KEY_TABLE_ID, state.tableId());
        node.putInt(KEY_COLUMN_COUNT, state.columns().size());
        for (int i = 0; i < state.columns().size(); i++) {
            ColumnState column = state.columns().get(i);
            String prefix = "columns." + i + ".";
            node.put(prefix + "id", column.id());
            node.putInt(prefix + "width", column.width());
            node.putInt(prefix + "order", column.order());
            node.putBoolean(prefix + "visible", column.visible());
        }

        node.putInt(KEY_SORT_COUNT, state.sort().size());
        for (int i = 0; i < state.sort().size(); i++) {
            SortState sort = state.sort().get(i);
            String prefix = "sort." + i + ".";
            node.put(prefix + "columnId", sort.columnId());
            node.put(prefix + "direction", sort.direction().name());
            node.putInt(prefix + "priority", sort.priority());
        }
    }

    private static TableState readState(String requestedTableId, Preferences node) {
        int version = requiredInt(node, KEY_VERSION);
        if (version != FORMAT_VERSION) {
            throw malformed(requestedTableId, "unsupported format version " + version);
        }

        String storedTableId = requiredString(node, KEY_TABLE_ID);
        if (!requestedTableId.equals(storedTableId)) {
            throw malformed(requestedTableId, "stored table id does not match node");
        }
        int schemaVersion = optionalNonNegative(
                node,
                requestedTableId,
                KEY_SCHEMA_VERSION,
                TableState.DEFAULT_SCHEMA_VERSION
        );

        int columnCount = nonNegative(node, requestedTableId, KEY_COLUMN_COUNT);
        List<ColumnState> columns = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            String prefix = "columns." + i + ".";
            columns.add(new ColumnState(
                    requiredString(node, prefix + "id"),
                    requiredInt(node, prefix + "width"),
                    requiredInt(node, prefix + "order"),
                    requiredBoolean(node, prefix + "visible")
            ));
        }

        int sortCount = nonNegative(node, requestedTableId, KEY_SORT_COUNT);
        List<SortState> sort = new ArrayList<>(sortCount);
        for (int i = 0; i < sortCount; i++) {
            String prefix = "sort." + i + ".";
            sort.add(new SortState(
                    requiredString(node, prefix + "columnId"),
                    readDirection(requestedTableId, node, prefix + "direction"),
                    requiredInt(node, prefix + "priority")
            ));
        }
        try {
            return new TableState(schemaVersion, storedTableId, columns, sort);
        } catch (IllegalArgumentException e) {
            throw malformed(requestedTableId, e.getMessage());
        }
    }

    private static int nonNegative(Preferences node, String tableId, String key) {
        int value = requiredInt(node, key);
        if (value < 0) {
            throw malformed(tableId, key + " must not be negative");
        }
        return value;
    }

    private static int optionalNonNegative(Preferences node, String tableId, String key, int defaultValue) {
        String value = node.get(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw malformed(tableId, key + " must not be negative");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw malformed(tableId, "invalid integer key " + key);
        }
    }

    private static SortDirection readDirection(String tableId, Preferences node, String key) {
        String value = requiredString(node, key);
        try {
            return SortDirection.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw malformed(tableId, "invalid sort direction " + value);
        }
    }

    private static String requiredString(Preferences node, String key) {
        String value = node.get(key, null);
        if (value == null) {
            throw malformed(node.get(KEY_TABLE_ID, "<unknown>"), "missing key " + key);
        }
        return value;
    }

    private static int requiredInt(Preferences node, String key) {
        String value = requiredString(node, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw malformed(node.get(KEY_TABLE_ID, "<unknown>"), "invalid integer key " + key);
        }
    }

    private static boolean requiredBoolean(Preferences node, String key) {
        String value = requiredString(node, key);
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        throw malformed(node.get(KEY_TABLE_ID, "<unknown>"), "invalid boolean key " + key);
    }

    private static String nodeName(String tableId) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tableId.getBytes(StandardCharsets.UTF_8));
    }

    private static TableStateStoreException malformed(String tableId, String detail) {
        return new TableStateStoreException("Malformed table state for " + tableId + ": " + detail);
    }
}
