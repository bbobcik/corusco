package cz.auderis.corusco.core.data.edit;

/**
 * CRUD edit operation.
 *
 * <p>The operation names the persistence intent represented by a
 * {@link CoruscoEditChange}. The core edit model does not prescribe how these
 * intents map to SQL statements, REST methods, command messages, or repository
 * calls.</p>
 */
public enum CoruscoEditOperation {
    /**
     * Create a new row using the supplied row snapshot.
     */
    CREATE,
    /**
     * Update an existing row using the supplied row snapshot.
     */
    UPDATE,
    /**
     * Delete an existing row by key.
     */
    DELETE
}
