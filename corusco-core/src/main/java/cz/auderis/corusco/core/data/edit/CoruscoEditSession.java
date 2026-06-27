package cz.auderis.corusco.core.data.edit;

import cz.auderis.corusco.core.data.CoruscoRowIdentity;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Presenter-owned CRUD edit staging session.
 *
 * <p>An edit session records row-level create, update, and delete intents
 * before a backing store is asked to persist them. It is intentionally
 * technology-neutral: it does not open transactions, call repositories, own
 * HTTP requests, or decide how optimistic locking is performed. Its job is to
 * keep a deterministic keyed edit set and publish observable dirty state for
 * commands and views.</p>
 *
 * <p>The session uses a {@link CoruscoRowIdentity} to extract keys for created
 * and updated rows. Staged changes are stored in a {@link LinkedHashMap}, so
 * snapshot order is stable and follows the first time a key entered the edit
 * set. A later change for the same key replaces the previous staged change
 * instead of appending a duplicate operation. This keeps save payloads compact
 * and makes same-row edit replacement explicit.</p>
 *
 * <p>Every effective mutation publishes a new immutable
 * {@link CoruscoEditSet} snapshot and updates dirty state. Equal snapshots are
 * still passed through {@link SimpleValue}, so listener notification follows
 * the normal value equality rule: no event is emitted when the effective value
 * did not change. The session performs no hidden synchronization and should be
 * mutated by its presenter owner on the same thread that owns bound UI state.</p>
 *
 * @param <R> row type
 * @param <K> key type
 */
public final class CoruscoEditSession<R extends @NonNull Object, K extends @NonNull Object> {

    private final CoruscoRowIdentity<R, K> rowIdentity;
    private final LinkedHashMap<K, CoruscoEditChange<R, K>> changes = new LinkedHashMap<>();
    private final SimpleValue<Boolean> dirty = SimpleValue.of(false);
    private final SimpleValue<CoruscoEditSet<R, K>> editSet = SimpleValue.of(CoruscoEditSet.empty());

    /**
     * Creates an edit session.
     *
     * <p>The identity is retained for all later create and update operations.
     * If an application has different identities for temporary client-created
     * rows and persisted rows, it should model that in the row key type before
     * using the session.</p>
     *
     * @param rowIdentity row identity
     */
    public CoruscoEditSession(CoruscoRowIdentity<R, K> rowIdentity) {
        this.rowIdentity = Objects.requireNonNull(rowIdentity, "rowIdentity");
    }

    /**
     * Returns row identity metadata.
     *
     * @return row identity
     */
    public CoruscoRowIdentity<R, K> rowIdentity() {
        return rowIdentity;
    }

    /**
     * Returns dirty state.
     *
     * <p>The value is {@code true} whenever the current edit set is non-empty.
     * Save and Revert commands normally observe this value for enablement.</p>
     *
     * @return dirty value
     */
    public ReadableValue<Boolean> dirty() {
        return dirty;
    }

    /**
     * Returns immutable edit snapshots.
     *
     * <p>Each published value contains the staged changes in deterministic key
     * order. A save action should read this value once at submission time and
     * pass that immutable snapshot to its persistence layer.</p>
     *
     * @return edit-set value
     */
    public ReadableValue<CoruscoEditSet<R, K>> editSet() {
        return editSet;
    }

    /**
     * Stages a create.
     *
     * <p>The key is extracted from {@code row}. If the same key already has a
     * staged update or delete, the create replaces it. Applications that need
     * stricter state-machine rules can enforce them before calling the session;
     * the session itself keeps only the latest intent per key.</p>
     *
     * @param row row
     * @param origin change origin
     */
    public void create(R row, ChangeOrigin origin) {
        K key = rowIdentity.keyOf(row);
        stage(new CoruscoEditChange<>(CoruscoEditOperation.CREATE, key, row, null), origin);
    }

    /**
     * Stages an update.
     *
     * <p>The row snapshot is stored as supplied. The optional version token is
     * carried through to the save payload so a backing store can perform
     * optimistic concurrency checks.</p>
     *
     * @param row row
     * @param versionToken optional version token
     * @param origin change origin
     */
    public void update(R row, @Nullable CoruscoVersionToken versionToken, ChangeOrigin origin) {
        K key = rowIdentity.keyOf(row);
        stage(new CoruscoEditChange<>(CoruscoEditOperation.UPDATE, key, row, versionToken), origin);
    }

    /**
     * Stages a delete.
     *
     * <p>Delete changes carry the row key and optional version token, but no
     * row snapshot. A later create or update for the same key replaces the
     * delete intent.</p>
     *
     * @param key key
     * @param versionToken optional version token
     * @param origin change origin
     */
    public void delete(K key, @Nullable CoruscoVersionToken versionToken, ChangeOrigin origin) {
        stage(new CoruscoEditChange<>(CoruscoEditOperation.DELETE, key, null, versionToken), origin);
    }

    /**
     * Discards a key's staged change.
     *
     * <p>No event is emitted when the key was not staged. When a staged change
     * is removed, both the edit-set value and dirty value are refreshed with
     * the supplied origin.</p>
     *
     * @param key key
     * @param origin change origin
     */
    public void discard(K key, ChangeOrigin origin) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(origin, "origin");
        if (changes.remove(key) != null) {
            publish(origin);
        }
    }

    /**
     * Clears all staged changes.
     *
     * <p>No event is emitted when the session is already clean. Clearing does
     * not persist or revert external row objects; it only removes staged
     * intents from this session.</p>
     *
     * @param origin change origin
     */
    public void clear(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        if (!changes.isEmpty()) {
            changes.clear();
            publish(origin);
        }
    }

    private void stage(CoruscoEditChange<R, K> change, ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        changes.put(change.key(), change);
        publish(origin);
    }

    private void publish(ChangeOrigin origin) {
        ArrayList<CoruscoEditChange<R, K>> snapshot = new ArrayList<>(changes.size());
        for (Map.Entry<K, CoruscoEditChange<R, K>> entry : changes.entrySet()) {
            snapshot.add(entry.getValue());
        }
        CoruscoEditSet<R, K> newSet = new CoruscoEditSet<>(snapshot);
        editSet.setValue(newSet, origin);
        dirty.setValue(!newSet.isEmpty(), origin);
    }
}
