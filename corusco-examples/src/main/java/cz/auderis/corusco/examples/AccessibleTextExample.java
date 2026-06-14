package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.List;
import java.util.Map;
import javax.swing.JTextField;

/**
 * Demonstrates descriptor-derived accessible text for Swing components.
 *
 * <p>The example shows how generated field metadata and resources can populate
 * a component's accessible name and description without duplicating labels in
 * view code. It is intended as a small accessibility scenario for readers
 * wiring generated descriptors into handwritten Swing screens.</p>
 */
public final class AccessibleTextExample {

    private AccessibleTextExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs accessible text behavior and returns accessible context values.
     *
     * @return accessible name and description
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            JTextField nameField = new JTextField();
            Resources resources = Resources.of(Map.of(
                    GeneratedCustomerEditResources.NAME_LABEL.id(), "Customer name",
                    GeneratedCustomerEditResources.NAME_TOOLTIP.id(), "Enter the customer display name"
            ));

            try (BehaviorScope scope = new BehaviorScope()) {
                // Generated field descriptors carry the resource keys; the
                // behavior resolves them without field-name strings or
                // JavaBeans-style property inspection.
                scope.install(nameField, List.of(StandardBehaviors.accessibleText(
                        GeneratedCustomerEditDescriptors.NAME,
                        resources
                )));

                result.set(List.of(
                        nameField.getAccessibleContext().getAccessibleName(),
                        nameField.getAccessibleContext().getAccessibleDescription()
                ));
            }
        });
        return result.get();
    }
}
