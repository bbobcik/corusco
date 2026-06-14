package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.MasterDetailValue;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates a detachable master-detail value.
 *
 * <p>The example derives detail state from a selected master value and clears
 * ownership through a detachable lifecycle. It is meant for readers modeling
 * master-detail screens before adding Swing selection bindings.</p>
 */
public final class MasterDetailValueExample {

    private MasterDetailValueExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a master-detail lifecycle scenario.
     *
     * @return diagnostics describing active and detached detail loading
     */
    public static List<String> runScenario() {
        SimpleValue<Integer> selectedCustomerId = SimpleValue.of(1);
        AtomicInteger loads = new AtomicInteger();
        MasterDetailValue<Integer, String> detail = MasterDetailValue.of(
                selectedCustomerId,
                id -> "customer-" + id + "-detail-" + loads.incrementAndGet()
        );
        List<String> result = new ArrayList<>();

        // The detail stays unloaded until the active view actually needs it.
        result.add("attached=" + detail.isAttached());
        result.add(detail.value());

        // While attached, changing the master immediately reloads the detail so
        // bound UI can repaint with the new selected customer's data.
        selectedCustomerId.setValue(2, ChangeOrigin.USER);
        result.add(detail.value());

        // When detached, master changes are cheap. The next activation reloads
        // once for the latest master instead of loading every intermediate row.
        detail.detach();
        selectedCustomerId.setValue(3, ChangeOrigin.USER);
        result.add("attached=" + detail.isAttached());
        result.add(detail.value());
        result.add("loads=" + loads.get());
        detail.close();
        return result;
    }
}
