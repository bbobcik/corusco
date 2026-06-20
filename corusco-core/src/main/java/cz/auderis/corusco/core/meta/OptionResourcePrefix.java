package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import java.util.Objects;

/**
 * Generates resource keys for options of one form field.
 *
 * <p>The prefix is normally derived by generated code from the owning field
 * key, for example {@code customer/type} becomes
 * {@code customer.type.retail.label}. This keeps option labels and descriptions
 * resource-backed without repeating fully qualified resource ids in every
 * generated descriptor.</p>
 *
 * @param prefix resource key prefix
 */
public record OptionResourcePrefix(String prefix) {

    /**
     * Creates an option resource prefix.
     *
     * @param prefix resource key prefix
     */
    public OptionResourcePrefix {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.isBlank()) {
            throw new IllegalArgumentException("prefix must not be blank");
        }
    }

    /**
     * Creates a prefix from a field key.
     *
     * @param fieldKey generated field key
     * @return resource prefix
     */
    public static OptionResourcePrefix of(FieldKey<?, ?> fieldKey) {
        Objects.requireNonNull(fieldKey, "fieldKey");
        return new OptionResourcePrefix(fieldKey.id().replace('/', '.'));
    }

    /**
     * Creates a prefix from a text field key.
     *
     * @param fieldKey generated text field key
     * @return resource prefix
     */
    public static OptionResourcePrefix of(TextFieldKey<?, ?> fieldKey) {
        Objects.requireNonNull(fieldKey, "fieldKey");
        return new OptionResourcePrefix(fieldKey.id().replace('/', '.'));
    }

    /**
     * Creates a label key for an option.
     *
     * @param option option key
     * @return label resource key
     */
    public ResourceKey<String> label(OptionKey option) {
        return facet(option, "label");
    }

    /**
     * Creates a description key for an option.
     *
     * @param option option key
     * @return description resource key
     */
    public ResourceKey<String> description(OptionKey option) {
        return facet(option, "description");
    }

    /**
     * Creates a help key for an option.
     *
     * @param option option key
     * @return help resource key
     */
    public ResourceKey<String> help(OptionKey option) {
        return facet(option, "help");
    }

    private ResourceKey<String> facet(OptionKey option, String facet) {
        Objects.requireNonNull(option, "option");
        return ResourceKey.of(prefix + "." + option.id() + "." + facet, String.class);
    }
}
