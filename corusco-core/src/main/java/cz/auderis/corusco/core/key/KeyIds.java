package cz.auderis.corusco.core.key;

import java.util.Objects;

/**
 * Shared validation helper for stable key identifiers in this package.
 *
 * <p>The public key records expose their own user-facing contracts. This
 * package-private helper keeps the non-null and non-blank id checks identical
 * across action, component, field, resource, text-field, and help-topic keys
 * without becoming a public API entry point.</p>
 */
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
