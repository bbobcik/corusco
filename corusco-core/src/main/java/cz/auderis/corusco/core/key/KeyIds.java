package cz.auderis.corusco.core.key;

import java.util.Objects;

final class KeyIds {

    private KeyIds() {
    }

    static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
