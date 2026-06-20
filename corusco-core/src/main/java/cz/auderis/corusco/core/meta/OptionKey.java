package cz.auderis.corusco.core.meta;

import java.util.Objects;

/**
 * Stable identity for an option inside an option-bearing editor.
 *
 * <p>An option key is not display text. Labels, descriptions, and help topics
 * are resource-backed metadata owned by {@link OptionDescriptor}.</p>
 *
 * @param id stable non-blank option id
 */
public record OptionKey(String id) {

    /**
     * Creates an option key.
     *
     * @param id stable non-blank option id
     */
    public OptionKey {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates an option key.
     *
     * @param id stable non-blank option id
     * @return option key
     */
    public static OptionKey of(String id) {
        return new OptionKey(id);
    }

    @Override
    public String toString() {
        return "OptionKey[" + id + "]";
    }
}
