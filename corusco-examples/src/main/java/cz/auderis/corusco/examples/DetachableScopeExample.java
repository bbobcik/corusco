package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.collection.LoadableList;
import cz.auderis.corusco.core.lifecycle.DetachableScope;
import cz.auderis.corusco.core.value.LoadableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates grouping detachable models under one presenter lifecycle.
 *
 * <p>The example registers detachable objects in a scope and closes them from a
 * single owner. It highlights reverse-order cleanup and the reason presenters
 * should own listener-like resources instead of leaving each model to be closed
 * ad hoc.</p>
 */
public final class DetachableScopeExample {

    private DetachableScopeExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a detachable presenter lifecycle scenario.
     *
     * @return diagnostics describing attachment and reload behavior
     */
    public static List<String> runScenario() {
        AtomicInteger customerLoads = new AtomicInteger();
        AtomicInteger rowLoads = new AtomicInteger();
        LoadableValue<String> selectedCustomer = LoadableValue.of(() -> "customer-" + customerLoads.incrementAndGet());
        LoadableList<String> orders = LoadableList.of(() -> List.of("order-" + rowLoads.incrementAndGet()));
        List<String> result = new ArrayList<>();

        try (DetachableScope scope = new DetachableScope()) {
            scope.add(selectedCustomer);
            scope.add(orders);

            // The presenter can register all detachable models once, then use
            // them normally while the view is active.
            result.add(selectedCustomer.value());
            result.add(String.join(",", orders.snapshot()));

            // Deactivation detaches every registered cache without closing the
            // scope. The same model objects can be used again after reactivation.
            scope.detach();
            result.add("valueAttached=" + selectedCustomer.isAttached());
            result.add("rowsAttached=" + orders.isAttached());
            result.add(selectedCustomer.value());
            result.add(String.join(",", orders.snapshot()));
        }

        result.add("valueLoads=" + customerLoads.get());
        result.add("rowLoads=" + rowLoads.get());
        return result;
    }
}
