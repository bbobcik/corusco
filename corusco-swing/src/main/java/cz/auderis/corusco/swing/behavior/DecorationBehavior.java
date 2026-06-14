package cz.auderis.corusco.swing.behavior;

import javax.swing.JComponent;

/**
 * Marker interface for behaviors that decorate components without owning the
 * primary value binding.
 *
 * <p>Decoration behaviors add secondary UI state such as validation borders,
 * tooltips, accessible text, or busy overlays. A component may have a primary
 * binding and one or more decorations installed through the same
 * {@link BehaviorScope}. Implementors follow the {@link ViewBehavior} lifecycle
 * contract: installation may add listeners or replace component state and must
 * return a binding that removes or restores that state.</p>
 *
 * @param <C> target component type
 */
public interface DecorationBehavior<C extends JComponent> extends ViewBehavior<C> {
}
