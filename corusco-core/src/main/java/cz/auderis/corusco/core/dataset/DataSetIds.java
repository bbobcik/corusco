package cz.auderis.corusco.core.dataset;

import java.util.Objects;

final class DataSetIds {

    private DataSetIds() {
    }

    static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (!id.matches("[A-Za-z0-9][A-Za-z0-9._/-]*")) {
            throw new IllegalArgumentException("id must contain only letters, digits, dots, underscores, dashes, or slashes");
        }
        return id;
    }
}
