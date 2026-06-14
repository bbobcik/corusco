package cz.auderis.corusco.swing.behavior;

import java.util.List;
import javax.swing.JComponent;

/**
 * Creates the behaviors that should be installed for one Swing component.
 *
 * <p>This interface is the bridge between generated view plans and
 * application-specific presenter code. A factory can capture models, commands,
 * resources, or services and produce the {@link ViewBehavior} instances that a
 * {@link BehaviorScope} will order and install for the component. The factory
 * itself does not install listeners or mutate Swing state; that work belongs to
 * the behavior's {@link ViewBehavior#install(BehaviorContext)} method.</p>
 *
 * <p>Factories are normally invoked on the EDT as part of view construction.
 * Implementations may return an empty list when a component has no generated
 * behavior. Returned behavior instances are observed by the owning behavior
 * scope until that scope is closed.</p>
 *
 * @param <C> component type
 */
@FunctionalInterface
public interface BehaviorFactory<C extends JComponent> {

    /**
     * Creates behaviors for the supplied component.
     *
     * @param component target component
     * @return behaviors to install; may be empty, but not {@code null}
     */
    List<ViewBehavior<? super C>> create(C component);
}
