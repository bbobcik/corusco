package cz.auderis.corusco.core.table;

import java.util.Objects;

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
