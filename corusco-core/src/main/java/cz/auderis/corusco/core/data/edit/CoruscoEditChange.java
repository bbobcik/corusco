package cz.auderis.corusco.core.data.edit;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * One staged row change.
 *
 * <p>A change is a compact, immutable description of one row-level intent in a
 * {@link CoruscoEditSet}. Create and update changes carry a row snapshot.
 * Delete changes carry only the key and optional version token because the row
 * may no longer be available or necessary for deletion.</p>
 *
 * <p>The optional {@link CoruscoVersionToken} is deliberately opaque. The core
 * edit model carries it for optimistic concurrency checks but does not parse
 * or compare it. Backing stores decide whether the token is an ETag, database
 * version, timestamp, vector, or another application-specific value.</p>
 *
 * @param operation operation
 * @param key row key
 * @param row row snapshot, {@code null} for delete
 * @param versionToken optimistic version token, or {@code null}
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoEditChange<R extends @NonNull Object, K extends @NonNull Object>(
        CoruscoEditOperation operation,
        K key,
        @Nullable R row,
        @Nullable CoruscoVersionToken versionToken
) {

    /**
     * Creates a change.
     *
     * <p>The operation and key are required. Create and update operations must
     * carry a row snapshot. Delete operations may carry a null row; callers
     * should not infer a deleted row's previous values from this record.</p>
     *
     * @param operation operation
     * @param key key
     * @param row row snapshot
     * @param versionToken version token
     */
    public CoruscoEditChange {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(key, "key");
        if (operation != CoruscoEditOperation.DELETE && row == null) {
            throw new IllegalArgumentException("create and update changes require a row");
        }
    }
}
