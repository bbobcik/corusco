package cz.auderis.corusco.core.data.edit;

import java.util.Objects;

/**
 * Opaque optimistic-version token.
 *
 * <p>Version tokens carry backing-store concurrency state through Corusco
 * edit records without making the core module understand that state. A token
 * might contain an HTTP ETag, database row version, revision string, timestamp
 * encoding, or another application-specific value. Core code treats it as an
 * opaque non-blank string.</p>
 *
 * @param value token value
 */
public record CoruscoVersionToken(String value) {

    /**
     * Creates a token.
     *
     * <p>Blank tokens are rejected because they are indistinguishable from
     * missing concurrency information in logs, tests, and persistence
     * adapters.</p>
     *
     * @param value token value
     */
    public CoruscoVersionToken {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
