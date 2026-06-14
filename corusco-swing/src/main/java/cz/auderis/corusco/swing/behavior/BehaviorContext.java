package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.help.HelpService;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Runtime information passed to a behavior while it is being installed.
 *
 * <p>A {@link ViewBehavior} is usually created before the final Swing component
 * lifecycle is known. The context supplies the concrete component, the
 * {@link BehaviorScope} that will own the returned binding, and optional
 * application services such as {@link HelpService}. Built-in behaviors use this
 * object to keep factory methods simple while still allowing generated view
 * plans to provide shared services at installation time.</p>
 *
 * <p>The context is an immutable value object, but the objects it refers to are
 * Swing-owned and should be used on the Event Dispatch Thread. Behaviors should
 * not store the context as an application-wide singleton; retain only the
 * collaborators needed by the binding they return.</p>
 *
 * @param component target component
 * @param scope owning behavior scope
 * @param helpService optional help service
 * @param <C> component type
 */
public record BehaviorContext<C extends JComponent>(C component, BehaviorScope scope, HelpService helpService) {

    /**
     * Creates a context without optional application services.
     *
     * @param component target component
     * @param scope owning behavior scope
     */
    public BehaviorContext(C component, BehaviorScope scope) {
        this(component, scope, null);
    }

    /**
     * Returns the help service when the owning behavior scope provides one.
     *
     * @return optional help service
     */
    public Optional<HelpService> helpServiceOptional() {
        return Optional.ofNullable(helpService);
    }
}
