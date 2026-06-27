package cz.auderis.corusco.core.data.edit;

import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Result of saving an edit set.
 *
 * <p>This record is the persistence-layer response shape for a submitted
 * {@link CoruscoEditSet}. It separates accepted rows, row-level conflicts,
 * and general problems because real saves can be partial: one row may save,
 * another may fail optimistic locking, and the backend may also report a
 * request-level validation problem.</p>
 *
 * <p>The result does not mutate a {@link CoruscoEditSession}. A presenter
 * decides how to apply a successful result, whether to clear staged changes,
 * how to display conflicts, and whether server-returned rows should replace
 * current rows in a data source.</p>
 *
 * @param savedRows rows accepted by the backing store
 * @param conflicts row conflicts
 * @param problems general validation or persistence problems
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoSaveResult<R extends @NonNull Object, K extends @NonNull Object>(
        List<R> savedRows,
        List<CoruscoConflict<R, K>> conflicts,
        ProblemSet problems
) {

    /**
     * Creates a result.
     *
     * <p>Rows and conflicts are copied into immutable lists. The supplied
     * {@link ProblemSet} is already immutable.</p>
     *
     * @param savedRows saved rows
     * @param conflicts conflicts
     * @param problems problems
     */
    public CoruscoSaveResult {
        savedRows = List.copyOf(Objects.requireNonNull(savedRows, "savedRows"));
        conflicts = List.copyOf(Objects.requireNonNull(conflicts, "conflicts"));
        Objects.requireNonNull(problems, "problems");
    }

    /**
     * Indicates whether the save completed without conflicts or problems.
     *
     * <p>A successful result may still contain saved rows. It means the save
     * produced no conflicts and no general problems; it does not say whether
     * the caller should clear or refresh local state.</p>
     *
     * @return true if successful
     */
    public boolean successful() {
        return conflicts.isEmpty() && problems.isEmpty();
    }
}
