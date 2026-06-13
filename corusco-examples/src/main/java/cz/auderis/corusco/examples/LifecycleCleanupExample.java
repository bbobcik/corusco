package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates deterministic cleanup of listener-style registrations.
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
            Runnable first = () -> {
            };
            Runnable second = () -> {
            };
            listeners.add(first);
            listeners.add(second);
            scope.onClose(() -> listeners.remove(first));
            scope.onClose(() -> listeners.remove(second));
        }
        return listeners.size();
    }
}
