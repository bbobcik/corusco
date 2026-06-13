package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.key.ComponentKey;

import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Swing-side tagging convention for generated {@link ComponentKey} constants.
 *
 * <p>The marker is stored as a client property so tests and generated helpers
 * can locate components without reflection or JavaBeans property paths. The
 * component name is set to the key id when it is currently blank, which keeps
 * screenshots and ad-hoc diagnostics readable without making name lookup the
 * primary contract.</p>
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
     * @param component component to mark
     * @param key generated or hand-written component key
     * @param <C> component type
     * @return the same component for inline construction
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
