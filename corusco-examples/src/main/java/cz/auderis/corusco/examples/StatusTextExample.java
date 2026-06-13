package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Demonstrates focus-scoped status-bar text.
 */
public final class StatusTextExample {

    private static final ResourceKey<String> NAME_STATUS =
            ResourceKey.of("customer/name/status", String.class);

    private StatusTextExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs status text behavior and returns status-label snapshots.
     *
     * @return status text before focus, during focus, and after focus
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            JTextField nameField = new JTextField();
            JLabel status = new JLabel("Ready");
            Resources resources = Resources.of(Map.of(
                    NAME_STATUS.id(), "Enter the customer display name"
            ));

            try (BehaviorScope scope = new BehaviorScope()) {
                // Status text is resolved through a stable generated-style key;
                // no public field name or reflective property path is involved.
                String statusText = resources.require(NAME_STATUS);
                scope.install(nameField, List.of(StandardBehaviors.statusText(status, statusText)));

                String beforeFocus = status.getText();
                // The status behavior owns the shared label only while its
                // component is focused, then restores the previous text.
                focusGained(nameField);
                String duringFocus = status.getText();
                focusLost(nameField);

                result.set(List.of(beforeFocus, duringFocus, status.getText()));
            }
        });
        return result.get();
    }

    private static void focusGained(JTextField field) {
        field.getFocusListeners()[field.getFocusListeners().length - 1]
                .focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
    }

    private static void focusLost(JTextField field) {
        field.getFocusListeners()[field.getFocusListeners().length - 1]
                .focusLost(new FocusEvent(field, FocusEvent.FOCUS_LOST));
    }
}
