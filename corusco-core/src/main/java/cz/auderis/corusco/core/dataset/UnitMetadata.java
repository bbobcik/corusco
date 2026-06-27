package cz.auderis.corusco.core.dataset;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Small unit descriptor for a data-set column.
 *
 * <p>The unit id is stable metadata for descriptors and adapters. It may be a
 * display symbol such as {@code USD}, {@code ms}, or {@code shares}, or a more
 * formal application-specific unit id. Corusco core stores the metadata but
 * deliberately does not perform unit conversion, dimensional analysis, or unit
 * compatibility checks.</p>
 *
 * <p>The optional quantity kind can group compatible units at a higher level,
 * for example {@code money}, {@code duration}, {@code mass}, or
 * {@code energy}. Adapters can use that value for labels, export metadata, or
 * request validation.</p>
 *
 * @param id stable unit id or symbol
 * @param quantityKind optional quantity kind
 */
public record UnitMetadata(String id, @Nullable String quantityKind) {

    /**
     * Creates unit metadata.
     *
     * @param id stable unit id or symbol
     * @param quantityKind optional quantity kind
     */
    public UnitMetadata {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (quantityKind != null && quantityKind.isBlank()) {
            throw new IllegalArgumentException("quantityKind must not be blank");
        }
    }

    /**
     * Creates unit metadata without a quantity kind.
     *
     * @param id stable unit id or symbol
     * @return unit metadata
     */
    public static UnitMetadata of(String id) {
        return new UnitMetadata(id, null);
    }
}
