package cz.auderis.corusco.examples.core;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates deterministic cleanup of listener-style registrations.
 *
 * <p>The scenario registers subscriptions in a lifecycle scope and closes them
 * in predictable order. It gives readers a compact pattern for presenter or
 * dialog cleanup when several models retain listeners.</p>
 */
public final class LifecycleCleanupExample {

    private LifecycleCleanupExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Registers two listeners in a scope, closes the scope, and returns the
     * remaining listener count.
     *
     * @return listener count after scoped cleanup
     */
    public static int remainingListenersAfterCleanup() {
        List<Runnable> listeners = new ArrayList<>();
        try (SubscriptionScope scope = new SubscriptionScope()) {
            // The list stands in for an external event source that keeps strong
            // references to listeners until explicitly unregistered.
            Runnable first = () -> {
            };
            Runnable second = () -> {
            };
            listeners.add(first);
            listeners.add(second);
            // Register cleanup immediately after attachment. This keeps the
            // ownership rule local: whoever adds a listener also records how to
            // remove it.
            scope.onClose(() -> listeners.remove(first));
            scope.onClose(() -> listeners.remove(second));
        }
        // Leaving the try block closes the scope and proves no listener remains
        // registered after the owner is disposed.
        return listeners.size();
    }
}
