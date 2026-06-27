package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.table.TableDescriptor;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Binds table metadata to stable row identity.
 *
 * <p>A {@link TableDescriptor} explains the column contract for a row type.
 * A {@link CoruscoRowIdentity} explains how the same row type is keyed for
 * selection, edit staging, refresh continuity, and data-source interaction.
 * This record packages the two facts together for table-data bridges without
 * introducing a Swing table model or backend repository.</p>
 *
 * <p>The constructor verifies that both descriptors name the same row type.
 * This catches accidental combinations such as using a customer table
 * descriptor with an order-row identity before a presenter can route selection
 * or edits to the wrong key space.</p>
 *
 * @param table table descriptor
 * @param rowIdentity row identity
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoTableDataDescriptor<R extends @NonNull Object, K extends @NonNull Object>(
        TableDescriptor<R> table,
        CoruscoRowIdentity<R, K> rowIdentity
) {

    /**
     * Creates a descriptor.
     *
     * <p>Both components are retained as immutable metadata. The record does
     * not own row data and does not subscribe to any data source.</p>
     *
     * @param table table descriptor
     * @param rowIdentity row identity
     */
    public CoruscoTableDataDescriptor {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(rowIdentity, "rowIdentity");
        if (!table.key().rowType().equals(rowIdentity.rowType())) {
            throw new IllegalArgumentException("table row type does not match row identity");
        }
    }
}
