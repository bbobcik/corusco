package cz.auderis.corusco.core.table;

import java.util.Objects;

/**
 * Shared validation helper for stable table and column identifiers.
 *
 * <p>Table descriptors, persisted table state, and generated metadata all use
 * string ids as compatibility boundaries. This package-private helper keeps the
 * basic non-null and non-blank validation consistent while the public records
 * document the user-visible identity contracts.</p>
 */
final class TableIds {

    private TableIds() {
    }

    static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
