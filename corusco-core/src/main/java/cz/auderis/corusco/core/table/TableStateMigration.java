package cz.auderis.corusco.core.table;

import java.util.List;
import java.util.Objects;

/**
 * Hook for transforming loaded table state before descriptor merge.
 *
 * <p>The hook is intentionally explicit: stores only load immutable
 * {@link TableState} snapshots, and callers decide which migration chain is
 * appropriate for the current application schema. Implementations should be
 * deterministic and Swing-free. After migration, {@link TableState#merge}
 * still applies descriptor filtering, width clamping, missing-column insertion,
 * and sort filtering.</p>
 *
 * @param <R> row type associated with the target descriptor
 */
@FunctionalInterface
public interface TableStateMigration<R> {

    /**
     * Migrates loaded state toward the current descriptor schema.
     *
     * @param descriptor current descriptor
     * @param stored loaded state
     * @return migrated state
     */
    TableState migrate(TableDescriptor<R> descriptor, TableState stored);

    /**
     * Returns a no-op migration.
     *
     * @param <R> row type
     * @return no-op migration
     */
    static <R> TableStateMigration<R> none() {
        return (descriptor, stored) -> stored;
    }

    /**
     * Chains this migration with another one.
     *
     * @param next next migration
     * @return chained migration
     */
    default TableStateMigration<R> andThen(TableStateMigration<R> next) {
        Objects.requireNonNull(next, "next");
        return (descriptor, stored) -> next.migrate(descriptor, migrate(descriptor, stored));
    }

    /**
     * Chains migrations in the supplied order.
     *
     * @param migrations migrations to run
     * @param <R> row type
     * @return chained migration
     */
    static <R> TableStateMigration<R> chain(List<? extends TableStateMigration<R>> migrations) {
        Objects.requireNonNull(migrations, "migrations");
        TableStateMigration<R> result = none();
        for (TableStateMigration<R> migration : migrations) {
            result = result.andThen(Objects.requireNonNull(migration, "migration"));
        }
        return result;
    }
}
