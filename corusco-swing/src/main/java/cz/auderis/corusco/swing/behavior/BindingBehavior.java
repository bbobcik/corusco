package cz.auderis.corusco.swing.behavior;

import javax.swing.JComponent;

/**
 * Marker interface for behaviors that install the primary model/component
 * binding for a Swing component.
 *
 * <p>A binding behavior owns the main data flow between a Corusco model and a
 * component, for example a text field value binding. {@link BehaviorScope}
 * treats these behaviors specially so a component cannot accidentally receive
 * two competing primary bindings. Implementors still follow the normal
 * {@link ViewBehavior} contract: installation should create listeners or Swing
 * state and return a {@link cz.auderis.corusco.swing.binding.Binding} that
 * removes them.</p>
 *
 * @param <C> target component type
 */
public interface BindingBehavior<C extends JComponent> extends ViewBehavior<C> {
}
