package cz.auderis.corusco.core.key;

import java.util.Objects;

/**
 * Typed identity for a view component.
 *
 * <p>This key is generic and Swing-free. Swing integration can use
 * {@code ComponentKey<JTextField>} or similar types without making core depend
 * on {@code java.desktop}. Equality and hash code include id and component
 * type.</p>
 *
 * @param id stable non-blank component id
 * @param componentType component type
 * @param <C> component type
 */
public record ComponentKey<C>(String id, Class<C> componentType) {

    /**
     * Creates a component key.
     *
     * @param id stable non-blank component id
     * @param componentType component type
     */
    public ComponentKey {
        id = KeyIds.requireId(id);
        Objects.requireNonNull(componentType, "componentType");
    }

    /**
     * Creates a component key for hand-written tests and generated-style code.
     *
     * @param id stable non-blank component id
     * @param componentType component type
     * @param <C> component type
     * @return component key
     */
    public static <C> ComponentKey<C> of(String id, Class<C> componentType) {
        return new ComponentKey<>(id, componentType);
    }

    @Override
    public String toString() {
        return "ComponentKey[" + id + ":" + componentType.getSimpleName() + "]";
    }
}
