package cz.auderis.corusco.swing.behavior;

import java.util.List;
import javax.swing.JComponent;

/**
 * Creates behaviors for a component.
 *
 * <p>Generated behavior plans can use factories to resolve application-specific
 * behavior instances while keeping installation ordering in
 * {@link BehaviorScope}.</p>
 *
 * @param <C> component type
 */
@FunctionalInterface
public interface BehaviorFactory<C extends JComponent> {

    /**
     * Creates behaviors for the supplied component.
     *
     * @param component target component
     * @return behaviors to install
     */
    List<ViewBehavior<? super C>> create(C component);
}
