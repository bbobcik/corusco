package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.key.ComponentKey;

import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Utility for marking Swing components with typed Corusco component keys.
 *
 * <p>Generated view code can expose {@link ComponentKey} constants that name
 * important components without tying tests to field names or reflection. This
 * utility stores such a key in a Swing client property and, when the component
 * has no explicit name, copies the stable key id into
 * {@link JComponent#setName(String)} for diagnostics and screenshots.</p>
 *
 * <p>The client property is the primary contract. Name lookup is only a
 * fallback used by package-local tester code for simple handwritten views. The
 * utility does not install listeners and does not own the component; it only
 * writes metadata to the supplied component, normally during view construction
 * on the EDT.</p>
 */
public final class SwingComponentKeys {

    /**
     * Client-property key that stores a {@link ComponentKey}.
     */
    public static final String COMPONENT_KEY_PROPERTY = "cz.auderis.corusco.componentKey";

    private SwingComponentKeys() {
        throw new AssertionError("No instances");
    }

    /**
     * Marks a component with a typed component key.
     *
     * <p>The component must be an instance of the key's declared component
     * type. The same component is returned so generated builders can mark a
     * component inline while constructing the view tree.</p>
     *
     * @param component component to mark
     * @param key generated or hand-written component key
     * @param <C> component type
     * @return the same component for inline construction
     * @throws IllegalArgumentException if the component does not match the
     *         key's component type
     */
    public static <C extends JComponent> C mark(C component, ComponentKey<C> key) {
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(key, "key");
        if (!key.componentType().isInstance(component)) {
            throw new IllegalArgumentException("Component does not match key type: " + key);
        }
        component.putClientProperty(COMPONENT_KEY_PROPERTY, key);
        if (component.getName() == null || component.getName().isBlank()) {
            component.setName(key.id());
        }
        return component;
    }

    /**
     * Returns the component key stored on a component.
     *
     * @param component component to inspect
     * @return optional component key
     */
    public static Optional<ComponentKey<?>> keyOf(JComponent component) {
        Objects.requireNonNull(component, "component");
        Object value = component.getClientProperty(COMPONENT_KEY_PROPERTY);
        if (value instanceof ComponentKey<?> key) {
            return Optional.of(key);
        }
        return Optional.empty();
    }

    static boolean matches(JComponent component, ComponentKey<?> key) {
        Object marker = component.getClientProperty(COMPONENT_KEY_PROPERTY);
        if (marker instanceof ComponentKey<?>) {
            return key.equals(marker);
        }
        return key.componentType().isInstance(component) && key.id().equals(component.getName());
    }
}
