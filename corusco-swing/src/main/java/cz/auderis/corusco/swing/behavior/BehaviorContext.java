package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.help.HelpService;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Installation context supplied to behaviors.
 *
 * <p>The current context is intentionally small: it exposes the target component
 * owning scope, and optional application services used by interaction
 * behaviors.</p>
 *
 * @param component target component
 * @param scope owning behavior scope
 * @param helpService optional help service
 * @param <C> component type
 */
public record BehaviorContext<C extends JComponent>(C component, BehaviorScope scope, HelpService helpService) {

    /**
     * Creates a context without optional services.
     *
     * @param component target component
     * @param scope owning behavior scope
     */
    public BehaviorContext(C component, BehaviorScope scope) {
        this(component, scope, null);
    }

    /**
     * Returns the optional help service.
     *
     * @return help service
     */
    public Optional<HelpService> helpServiceOptional() {
        return Optional.ofNullable(helpService);
    }
}
