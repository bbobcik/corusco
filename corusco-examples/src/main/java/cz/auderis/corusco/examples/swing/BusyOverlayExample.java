package cz.auderis.corusco.examples.swing;

import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.task.BusyOverlayLayerUI;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayer;
import javax.swing.JPanel;

/**
 * Demonstrates the Swing busy overlay behavior.
 *
 * <p>The scenario wraps a component in a {@code JLayer}, connects it to an
 * observable busy value, and verifies that the overlay can be installed and
 * removed deterministically. It is a headless-safe counterpart to the visual
 * busy indicator used by task-driven Swing screens.</p>
 */
public final class BusyOverlayExample {

    private BusyOverlayExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a small busy overlay scenario.
     *
     * @return diagnostics describing overlay state and input blocking
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            SimpleValue<Boolean> busy = SimpleValue.of(false);
            JPanel formPanel = new JPanel();
            JLayer<JPanel> layer = new JLayer<>(formPanel);

            try (BehaviorScope scope = new BehaviorScope()) {
                // The form is wrapped explicitly because JLayer owns event
                // interception and overlay painting. Generated views can make
                // this wrapping decision at container boundaries.
                scope.install(layer, List.of(StandardBehaviors.busyOverlay(busy)));
                BusyOverlayLayerUI overlay = (BusyOverlayLayerUI) layer.getUI();
                result.add("initialBusy=" + overlay.isBusy());

                // The same observable busy value can come from TaskService or
                // AsyncFieldValidation. The binding updates the overlay without
                // mutating child component enabled states.
                busy.setValue(true, StandardChangeOrigin.MODEL);
                MouseEvent event = new MouseEvent(
                        layer,
                        MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(),
                        0,
                        1,
                        1,
                        1,
                        false
                );
                overlay.eventDispatched(event, layer);
                result.add("busyConsumesInput=" + event.isConsumed());
            }
            result.add("restored=" + !(layer.getUI() instanceof BusyOverlayLayerUI));
        });
        return result;
    }
}
