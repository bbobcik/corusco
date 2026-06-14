package cz.auderis.corusco.core.key;

import java.util.Objects;

/**
 * Typed identity for a field that belongs to an owner model.
 *
 * <p>The {@code id} is a stable boundary string for generated code internals,
 * diagnostics, serialization, and resource lookup. It is not a public
 * property-path contract. Equality and hash code include the id, owner type,
 * and value type, and are therefore stable for generated key constants.</p>
 *
 * <p>Generated {@code @SwingForm} metadata creates {@code FieldKey} constants
 * in {@code <Form>Fields} for non-text field kinds such as {@code @CheckBox}
 * and {@code @ComboBox}. Text-editable field kinds use
 * {@link TextFieldKey} and can be converted back to a general field key through
 * {@link TextFieldKey#asFieldKey()}. Handwritten code can create equivalent
 * keys with {@link #of(String, Class, Class)} when no generated constant
 * exists.</p>
 *
 * @param id stable non-blank field id
 * @param ownerType owner/model type that declares the field
 * @param valueType field value type
 * @param <O> owner/model type
 * @param <T> field value type
 */
public record FieldKey<O, T>(String id, Class<O> ownerType, Class<T> valueType) {

    /**
     * Creates a field key.
     *
     * @param id stable non-blank field id
     * @param ownerType owner/model type
     * @param valueType field value type
     */
    public FieldKey {
        id = KeyIds.requireId(id);
        Objects.requireNonNull(ownerType, "ownerType");
        Objects.requireNonNull(valueType, "valueType");
    }

    /**
     * Creates a field key for hand-written tests and generated-style code.
     *
     * @param id stable non-blank field id
     * @param ownerType owner/model type
     * @param valueType field value type
     * @param <O> owner/model type
     * @param <T> field value type
     * @return field key
     */
    public static <O, T> FieldKey<O, T> of(String id, Class<O> ownerType, Class<T> valueType) {
        return new FieldKey<>(id, ownerType, valueType);
    }

    @Override
    public String toString() {
        return "FieldKey[" + ownerType.getSimpleName() + "#" + id + ":" + valueType.getSimpleName() + "]";
    }
}
