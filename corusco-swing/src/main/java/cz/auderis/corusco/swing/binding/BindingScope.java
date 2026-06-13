package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;

/**
 * Owns multiple bindings and closes them in reverse registration order.
 *
 * <p>The scope delegates lifecycle behavior to
 * {@link SubscriptionScope}: closure is idempotent, late additions close
 * immediately, and cleanup failures do not prevent unrelated bindings from
 * closing.</p>
 */
public final class BindingScope implements Binding {

    private final SubscriptionScope scope = new SubscriptionScope();

    /**
     * Adds a binding owned by this scope.
     *
     * @param binding binding to own
     * @param <B> binding type
     * @return the same binding
     */
    public <B extends Binding> B add(B binding) {
        return scope.add(binding);
    }

    /**
     * Indicates whether this scope is closed.
     *
     * @return {@code true} when closed
     */
    public boolean isClosed() {
        return scope.isClosed();
    }

    @Override
    public void close() {
        scope.close();
    }
}
