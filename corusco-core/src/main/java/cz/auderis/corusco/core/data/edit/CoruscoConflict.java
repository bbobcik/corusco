package cz.auderis.corusco.core.data.edit;

import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Persistence conflict for one row key.
 *
 * <p>A conflict reports that a submitted change could not be accepted as-is
 * for a specific key. The attempted change is preserved so a presenter can
 * show exactly what the user tried to save. The current row is optional
 * because some backends can return the latest server row while others can only
 * report that the row changed, disappeared, or failed validation.</p>
 *
 * <p>Problems describe the conflict in the same diagnostic vocabulary used by
 * forms and tables. They may include optimistic-lock messages, server-side
 * validation details, permission failures, or row-specific persistence
 * problems.</p>
 *
 * @param key row key
 * @param attempted attempted change
 * @param currentRow current persisted row, or {@code null}
 * @param problems validation or conflict details
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoConflict<R extends @NonNull Object, K extends @NonNull Object>(
        K key,
        CoruscoEditChange<R, K> attempted,
        @Nullable R currentRow,
        ProblemSet problems
) {

    /**
     * Creates a conflict.
     *
     * <p>The key, attempted change, and problem set are required. The current
     * row may be null when the backend did not provide a replacement row or
     * when the conflict represents deletion.</p>
     *
     * @param key key
     * @param attempted attempted change
     * @param currentRow current persisted row
     * @param problems problems
     */
    public CoruscoConflict {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(attempted, "attempted");
        Objects.requireNonNull(problems, "problems");
    }
}
