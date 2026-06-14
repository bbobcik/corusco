package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.swing.binding.Binding;
import javax.swing.JComponent;

/**
 * Reusable extension that installs one component responsibility into a Swing view.
 *
 * <p>A behavior is Corusco's unit of generated or handwritten Swing assembly.
 * It can bind a model to a component, add validation decoration, install help
 * key strokes, attach command actions, or add other listeners and client
 * properties. A {@link BehaviorScope} reads the behavior descriptor, orders
 * behaviors by phase, checks conflicts, and owns the binding returned from
 * installation.</p>
 *
 * <p>Implementations should be lightweight and explicit about ownership.
 * Installation must run on the Swing Event Dispatch Thread, may mutate the
 * target component, and must return a {@link Binding} that removes listeners,
 * restores replaced state, or closes nested subscriptions. Implementations
 * should not assume they are reusable across unrelated components unless all
 * captured state is immutable or intentionally shared.</p>
 *
 * @param <C> target component type
 */
public interface ViewBehavior<C extends JComponent> {

    /**
     * Returns behavior metadata for ordering, duplicate checks, and diagnostics.
     *
     * @return behavior descriptor
     */
    BehaviorDescriptor descriptor();

    /**
     * Installs the behavior into the supplied component context.
     *
     * <p>The returned binding becomes owned by the caller, normally a
     * {@link BehaviorScope}. If installation has already changed Swing state and
     * then fails, the implementation should either restore that state before
     * throwing or document why the failure is not recoverable.</p>
     *
     * @param context installation context
     * @return disposable installed behavior, not {@code null}
     */
    Binding install(BehaviorContext<C> context);
}
