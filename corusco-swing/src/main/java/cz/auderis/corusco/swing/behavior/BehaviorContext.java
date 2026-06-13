package cz.auderis.corusco.swing.behavior;

import javax.swing.JComponent;

/**
 * Installation context supplied to behaviors.
 *
 * <p>The current context is intentionally small: it exposes the target component
 * and owning scope. Later stages can add resources, help services, or
 * diagnostics without changing behavior installation signatures.</p>
 *
 * @param component target component
 * @param scope owning behavior scope
 * @param <C> component type
 */
public record BehaviorContext<C extends JComponent>(C component, BehaviorScope scope) {
}
