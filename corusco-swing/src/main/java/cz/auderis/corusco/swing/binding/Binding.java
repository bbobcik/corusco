package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.lifecycle.Disposable;

/**
 * Disposable connection between a Swing component and a Corusco model/value.
 *
 * <p>Bindings own all Swing listeners and model subscriptions they install.
 * Closing a binding must remove those registrations and is idempotent.</p>
 */
@FunctionalInterface
public interface Binding extends Disposable {

    /**
     * Disposes this binding and removes owned listeners/subscriptions.
     */
    @Override
    void close();
}
