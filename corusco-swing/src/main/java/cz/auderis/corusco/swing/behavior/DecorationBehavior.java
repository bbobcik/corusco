package cz.auderis.corusco.swing.behavior;

import javax.swing.JComponent;

/**
 * Marker interface for behaviors that decorate components without owning the
 * primary value binding.
 *
 * @param <C> target component type
 */
public interface DecorationBehavior<C extends JComponent> extends ViewBehavior<C> {
}
