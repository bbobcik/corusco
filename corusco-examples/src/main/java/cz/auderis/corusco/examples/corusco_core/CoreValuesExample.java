package cz.auderis.corusco.examples.corusco_core;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.ArrayList;
import java.util.List;

public final class CoreValuesExample {

    private CoreValuesExample() {
        throw new AssertionError("No instances");
    }

    public static List<String> runScenario() {
        SimpleValue<String> selectedCustomer = SimpleValue.of("Ada Lovelace");
        List<String> audit = new ArrayList<>();
        try (SubscriptionScope scope = new SubscriptionScope()) {
            scope.add(selectedCustomer.subscribe(event ->
                    audit.add(event.origin() + ": " + event.oldValue() + " -> " + event.newValue())));
            selectedCustomer.setValue("Grace Hopper", StandardChangeOrigin.USER);
            selectedCustomer.setValue("Katherine Johnson", StandardChangeOrigin.MODEL);
        }
        return audit;
    }
}
