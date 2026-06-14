package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;

/**
 * Lifecycle owner for a group of Swing bindings.
 *
 * <p>Most generated or presenter-built views install several independent
 * bindings: value synchronization, validation decoration, command actions,
 * tooltips, and component listeners. A binding scope lets that view keep one
 * close handle instead of manually remembering each binding. It uses the same
 * reverse-order cleanup semantics as {@link SubscriptionScope}, which matters
 * when later bindings depend on state installed by earlier ones.</p>
 *
 * <p>The scope owns only the {@link Binding} handles added to it. It does not
 * own the Swing components, models, or resources captured by those bindings.
 * Closing is idempotent, late additions are closed immediately by the
 * underlying subscription scope, and cleanup failures are aggregated by that
 * lifecycle implementation. Because bindings normally touch Swing components,
 * create, add to, and close this scope on the Event Dispatch Thread unless all
 * contained bindings document another rule.</p>
 */
public final class BindingScope implements Binding {

    private final SubscriptionScope scope = new SubscriptionScope();

    /**
     * Creates an empty binding scope.
     */
    public BindingScope() {
    }

    /**
     * Adds a binding owned by this scope.
     *
     * <p>If the scope is already closed, the supplied binding is closed
     * immediately by the delegated lifecycle scope. The same binding instance
     * should not be added to multiple active owners.</p>
     *
     * @param binding binding to own
     * @param <B> binding type
     * @return the same binding
     */
    public <B extends Binding> B add(B binding) {
        return scope.add(binding);
    }

    /**
     * Indicates whether this scope has already been closed.
     *
     * @return {@code true} when closed
     */
    public boolean isClosed() {
        return scope.isClosed();
    }

    /**
     * Closes all owned bindings in reverse registration order.
     *
     * <p>Repeated calls are allowed. Any cleanup failure policy is inherited
     * from {@link SubscriptionScope}.</p>
     */
    @Override
    public void close() {
        scope.close();
    }
}
