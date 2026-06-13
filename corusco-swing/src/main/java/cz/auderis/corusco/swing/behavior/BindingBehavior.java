package cz.auderis.corusco.swing.behavior;

import javax.swing.JComponent;

/**
 * Marker interface for behaviors that install primary model/component bindings.
 *
 * @param <C> target component type
 */
public interface BindingBehavior<C extends JComponent> extends ViewBehavior<C> {
}
