package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.DerivedValue;
import cz.auderis.corusco.core.value.MappedValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates simple and derived observable values with scoped cleanup.
 */
public final class ObservableValueExample {

    private ObservableValueExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a small value example and returns observed labels.
     *
     * @return labels observed before scoped cleanup
     */
    public static List<String> runValueScenario() {
        SimpleValue<String> firstName = SimpleValue.of("Ada");
        SimpleValue<String> lastName = SimpleValue.of("Lovelace");
        DerivedValue<String> fullName = DerivedValue.of(
                () -> firstName.value() + " " + lastName.value(),
                firstName,
                lastName
        );
        MappedValue<String, String> greeting = MappedValue.of(fullName, name -> "Hello, " + name);
        List<String> observed = new ArrayList<>();

        try (SubscriptionScope scope = new SubscriptionScope()) {
            scope.add(fullName);
            scope.add(greeting);
            scope.add(greeting.subscribe(event -> observed.add(event.newValue())));
            firstName.setValue("Grace", ChangeOrigin.USER);
        }

        firstName.setValue("Ignored", ChangeOrigin.USER);
        return List.copyOf(observed);
    }
}
