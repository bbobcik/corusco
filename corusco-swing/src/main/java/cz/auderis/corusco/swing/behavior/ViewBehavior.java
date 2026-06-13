package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.swing.binding.Binding;
import javax.swing.JComponent;

/**
 * Reusable component extension.
 *
 * <p>A behavior installs listeners, bindings, decorators, or key mappings on a
 * Swing component and returns a disposable handle. Installation must run on the
 * EDT through {@link BehaviorScope}.</p>
 *
 * @param <C> target component type
 */
public interface ViewBehavior<C extends JComponent> {

    /**
     * Returns behavior metadata for ordering and conflict checks.
     *
     * @return behavior descriptor
     */
    BehaviorDescriptor descriptor();

    /**
     * Installs the behavior.
     *
     * @param context installation context
     * @return disposable installed behavior
     */
    Binding install(BehaviorContext<C> context);
}
