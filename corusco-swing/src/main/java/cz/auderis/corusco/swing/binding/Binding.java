package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.lifecycle.Disposable;

/**
 * Disposable connection between a Swing component and a Corusco model or value.
 *
 * <p>A binding is the lifecycle handle returned after Swing integration code
 * installs listeners, model subscriptions, document filters, borders, actions,
 * or other component state. Callers keep the handle for as long as the view is
 * alive and close it when the component is removed, a dialog is disposed, or a
 * behavior scope is closed.</p>
 *
 * <p>Implementations own only the registrations and state they install. Closing
 * a binding must remove those registrations, restore any replaced component
 * state described by the binding, and be idempotent. Unless a specific binding
 * documents another rule, close it on the Swing event dispatch thread because
 * cleanup touches Swing components.</p>
 */
@FunctionalInterface
public interface Binding extends Disposable {

    /**
     * Disposes this binding and removes owned listeners/subscriptions.
     */
    @Override
    void close();
}
