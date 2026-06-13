package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.value.LoadableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates a detachable lazy value.
 */
public final class LoadableValueExample {

    private LoadableValueExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a lazy load, detach, and refresh scenario.
     *
     * @return diagnostics describing load and cache behavior
     */
    public static List<String> runScenario() {
        AtomicInteger loads = new AtomicInteger();
        LoadableValue<String> customerName = LoadableValue.of(() -> "customer-" + loads.incrementAndGet());
        List<String> result = new ArrayList<>();

        // Construction is cheap: a presenter can keep the model field without
        // hitting a repository or server until the UI actually asks for data.
        result.add("attached=" + customerName.isAttached());
        result.add(customerName.value());
        result.add(customerName.value());

        // Detach belongs to view/presenter lifecycle cleanup. It releases the
        // cached value but leaves the model reusable for a later activation.
        customerName.detach();
        result.add("attached=" + customerName.isAttached());
        result.add(customerName.value());

        // Refresh is the explicit stale-data path. It reloads immediately and
        // would notify subscribers if the effective value changes.
        result.add(customerName.refresh());
        result.add("loads=" + loads.get());
        return result;
    }
}
