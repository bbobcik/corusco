package cz.auderis.corusco.examples.core;

import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.DerivedValue;
import cz.auderis.corusco.core.value.MappedValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates simple and derived observable values with scoped cleanup.
 *
 * <p>The example creates writable and derived values, subscribes to changes,
 * and disposes those subscriptions through a scope. It is the basic value-model
 * scenario that underlies field models and Swing bindings.</p>
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
        // SimpleValue is the mutable edge. Later form fields and commands can
        // build richer behavior around the same synchronous notification rule.
        SimpleValue<String> firstName = SimpleValue.of("Ada");
        SimpleValue<String> lastName = SimpleValue.of("Lovelace");
        // Derived values keep read-only presentation state close to the data it
        // depends on, without forcing a Swing dependency into core.
        DerivedValue<String> fullName = DerivedValue.of(
                () -> firstName.value() + " " + lastName.value(),
                firstName,
                lastName
        );
        // MappedValue is the common one-source case; it is useful for labels,
        // command enablement text, and other one-way projections.
        MappedValue<String, String> greeting = MappedValue.of(fullName, name -> "Hello, " + name);
        List<String> observed = new ArrayList<>();

        try (SubscriptionScope scope = new SubscriptionScope()) {
            // The scope owns both derived subscriptions and the external
            // observer registration, so the whole example tears down together.
            scope.add(fullName);
            scope.add(greeting);
            scope.add(greeting.subscribe(event -> observed.add(event.newValue())));
            firstName.setValue("Grace", StandardChangeOrigin.USER);
        }

        // This change proves the scoped subscriptions were removed; it should
        // not append another greeting after cleanup.
        firstName.setValue("Ignored", StandardChangeOrigin.USER);
        return List.copyOf(observed);
    }
}
