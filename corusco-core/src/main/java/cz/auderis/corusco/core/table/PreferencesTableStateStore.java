package cz.auderis.corusco.core.table;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * application's table schema and is available to migration hooks. Each save
 * flushes a replacement snapshot before publishing its slot at the table node.
 * Existing snapshots stored directly in the table node are readable. Instances
 * are not synchronized; use one instance from the UI/lifecycle owner that also
 * calls {@link #flush()}.</p>
 */
public final class PreferencesTableStateStore implements TableStateStore {

    private static final int FORMAT_VERSION = 1;

    private static final String HASHED_NODE_PREFIX = "sha256.";

    private static final String KEY_VERSION = "version";
    private static final String KEY_SCHEMA_VERSION = "schemaVersion";
    private static final String KEY_TABLE_ID = "tableId";
    private static final String KEY_ACTIVE_SLOT = "activeSlot";
    private static final String KEY_COLUMN_COUNT = "columns.count";
    private static final String KEY_SORT_COUNT = "sort.count";
    private static final String FIRST_SLOT = "state-a";
    private static final String SECOND_SLOT = "state-b";

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
            String slot = activeSlot(node, requiredTableId);
            if (slot == null) {
                return node.keys().length == 0
                        ? Optional.empty()
                        : Optional.of(readState(requiredTableId, node));
            }
            if (!node.nodeExists(slot)) {
                throw malformed(requiredTableId, "active snapshot is missing");
            }
            return Optional.of(readState(requiredTableId, node.node(slot)));
        } catch (BackingStoreException | IllegalArgumentException | IllegalStateException e) {
            throw new TableStateStoreException("Cannot load table state for " + requiredTableId, e);
        }
    }

    @Override
    public void save(TableState state) {
        Objects.requireNonNull(state, "state");
        try {
            Preferences node = root.node(nodeName(state.tableId()));
            String previous = node.get(KEY_ACTIVE_SLOT, null);
            String next = FIRST_SLOT.equals(previous) ? SECOND_SLOT : FIRST_SLOT;
            Preferences snapshot = node.node(next);
            snapshot.clear();
            writeState(snapshot, state);
            snapshot.flush();
            activate(node, previous, next);
        } catch (BackingStoreException | IllegalArgumentException | IllegalStateException e) {
            throw new TableStateStoreException("Cannot save table state for " + state.tableId(), e);
        }
    }

    private static String activeSlot(Preferences node, String tableId) {
        String slot = node.get(KEY_ACTIVE_SLOT, null);
        if (slot == null || FIRST_SLOT.equals(slot) || SECOND_SLOT.equals(slot)) {
            return slot;
        }
        throw malformed(tableId, "invalid active snapshot");
    }

    private static void activate(Preferences node, String previous, String next) throws BackingStoreException {
        node.put(KEY_ACTIVE_SLOT, next);
        try {
            node.flush();
        } catch (BackingStoreException failure) {
            try {
                if (previous == null) {
                    node.remove(KEY_ACTIVE_SLOT);
                } else {
                    node.put(KEY_ACTIVE_SLOT, previous);
                }
                node.flush();
            } catch (BackingStoreException | IllegalArgumentException | IllegalStateException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
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
        } catch (BackingStoreException | IllegalArgumentException | IllegalStateException e) {
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

    static String nodeName(String tableId) {
        byte[] idBytes = tableId.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(idBytes);
        if (encoded.length() <= Preferences.MAX_NAME_LENGTH) {
            // Corusco 1.0 - 1.3.0 layout, kept so existing stored state stays readable.
            return encoded;
        }
        // '.' is not part of the URL-safe Base64 alphabet, so hashed names cannot
        // collide with directly encoded ones. The stored table id is verified on
        // load, so a hash collision is reported as malformed state.
        return HASHED_NODE_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(idBytes));
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static TableStateStoreException malformed(String tableId, String detail) {
        return new TableStateStoreException("Malformed table state for " + tableId + ": " + detail);
    }
}
