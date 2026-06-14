package cz.auderis.corusco.core.key;

import java.util.Objects;

/**
 * Typed identity for a text-editable field.
 *
 * <p>A text field key preserves the same owner and value typing as
 * {@link FieldKey}. The stable id remains a boundary identity string for
 * generated descriptors and diagnostics, not an arbitrary property path.</p>
 *
 * <p>Generated {@code @SwingForm} metadata creates {@code TextFieldKey}
 * constants in form-specific fields companions such as
 * {@code CustomerEditFields} for {@code @TextField} and
 * {@code @DateField} record components. Use generated constants for ordinary
 * generated forms; use {@link #of(String, Class, Class)} for handwritten
 * metadata and tests.</p>
 *
 * @param id stable non-blank text field id
 * @param ownerType owner/model type that declares the field
 * @param valueType semantic field value type
 * @param <O> owner/model type
 * @param <T> semantic field value type
 */
public record TextFieldKey<O, T>(String id, Class<O> ownerType, Class<T> valueType) {

    /**
     * Creates a text field key.
     *
     * @param id stable non-blank text field id
     * @param ownerType owner/model type
     * @param valueType semantic field value type
     */
    public TextFieldKey {
        id = KeyIds.requireId(id);
        Objects.requireNonNull(ownerType, "ownerType");
        Objects.requireNonNull(valueType, "valueType");
    }

    /**
     * Creates a text field key for hand-written tests and generated-style code.
     *
     * @param id stable non-blank text field id
     * @param ownerType owner/model type
     * @param valueType semantic field value type
     * @param <O> owner/model type
     * @param <T> semantic field value type
     * @return text field key
     */
    public static <O, T> TextFieldKey<O, T> of(String id, Class<O> ownerType, Class<T> valueType) {
        return new TextFieldKey<>(id, ownerType, valueType);
    }

    /**
     * Returns this text identity as a general field key with the same type
     * relationship.
     *
     * @return matching field key
     */
    public FieldKey<O, T> asFieldKey() {
        return FieldKey.of(id, ownerType, valueType);
    }

    @Override
    public String toString() {
        return "TextFieldKey[" + ownerType.getSimpleName() + "#" + id + ":" + valueType.getSimpleName() + "]";
    }
}
