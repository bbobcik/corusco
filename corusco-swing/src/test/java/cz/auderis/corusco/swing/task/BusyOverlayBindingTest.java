package cz.auderis.corusco.swing.task;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.behavior.BehaviorConflictException;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusyOverlayBindingTest {

    @Test
    void busyLayerConsumesMouseAndKeyInput() {
        SwingEdt.runAndWait(() -> {
            JLayer<JPanel> layer = new JLayer<>(new JPanel());
            BusyOverlayLayerUI ui = new BusyOverlayLayerUI();
            layer.setUI(ui);
            MouseEvent mouse = mouseEvent(layer);
            KeyEvent key = keyEvent(layer);

            ui.eventDispatched(mouse, layer);
            ui.eventDispatched(key, layer);
            assertThat(mouse.isConsumed()).isFalse();
            assertThat(key.isConsumed()).isFalse();

            ui.setBusy(layer, true);
            ui.eventDispatched(mouse, layer);
            ui.eventDispatched(key, layer);

            assertThat(mouse.isConsumed()).isTrue();
            assertThat(key.isConsumed()).isTrue();
            assertThat(layer.getCursor().getType()).isEqualTo(Cursor.WAIT_CURSOR);
        });
    }

    @Test
    void bindingObservesBusyValueAndRestoresPreviousUiOnClose() {
        SwingEdt.runAndWait(() -> {
            SimpleValue<Boolean> busy = SimpleValue.of(false);
            JLayer<JPanel> layer = new JLayer<>(new JPanel());
            layer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            LayerUI<? super JPanel> previousUi = layer.getUI();

            BusyOverlayBinding<JPanel> binding = BusyOverlayBinding.install(layer, busy);
            BusyOverlayLayerUI overlayUi = (BusyOverlayLayerUI) layer.getUI();

            assertThat(overlayUi.isBusy()).isFalse();
            busy.setValue(true, StandardChangeOrigin.MODEL);
            assertThat(overlayUi.isBusy()).isTrue();

            binding.close();
            busy.setValue(false, StandardChangeOrigin.MODEL);

            assertThat(layer.getUI()).isSameAs(previousUi);
            assertThat(layer.getCursor().getType()).isEqualTo(Cursor.CROSSHAIR_CURSOR);
            assertThat(overlayUi.isBusy()).isFalse();
        });
    }

    @Test
    void busyLayerPaintsOverlayOverView() {
        SwingEdt.runAndWait(() -> {
            JLayer<JPanel> layer = new JLayer<>(new JPanel());
            layer.setSize(20, 20);
            BusyOverlayLayerUI ui = new BusyOverlayLayerUI(Color.BLACK, 1.0f);
            layer.setUI(ui);
            ui.setBusy(layer, true);
            BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                ui.paint(graphics, layer);
            } finally {
                graphics.dispose();
            }

            assertThat(image.getRGB(10, 10)).isEqualTo(java.awt.Color.BLACK.getRGB());
        });
    }

    @Test
    void bindingSchedulesOffEdtBusyChangesOntoEdt() throws Exception {
        SimpleValue<Boolean> busy = SimpleValue.of(false);
        Holder<BusyOverlayLayerUI> ui = new Holder<>();
        Holder<BusyOverlayBinding<JPanel>> binding = new Holder<>();
        SwingEdt.runAndWait(() -> {
            JLayer<JPanel> layer = new JLayer<>(new JPanel());
            binding.value = BusyOverlayBinding.install(layer, busy);
            ui.value = (BusyOverlayLayerUI) layer.getUI();
        });

        Thread worker = new Thread(() -> busy.setValue(true, StandardChangeOrigin.MODEL), "busy-overlay-test-worker");
        worker.start();
        worker.join();

        awaitEdt(() -> ui.value.isBusy());
        assertThat(ui.value.isBusy()).isTrue();
        SwingEdt.runAndWait(binding.value::close);
    }

    @Test
    void behaviorInstallsThroughScopeAndPreventsDuplicateOverlay() {
        SwingEdt.runAndWait(() -> {
            SimpleValue<Boolean> busy = SimpleValue.of(true);
            JLayer<JPanel> layer = new JLayer<>(new JPanel());
            BehaviorScope scope = new BehaviorScope();

            scope.install(layer, List.of(StandardBehaviors.busyOverlay(busy)));

            assertThat(((BusyOverlayLayerUI) layer.getUI()).isBusy()).isTrue();
            assertThatThrownBy(() -> scope.install(layer, List.of(StandardBehaviors.busyOverlay(busy))))
                    .isInstanceOf(BehaviorConflictException.class)
                    .hasMessageContaining("busy-overlay");

            scope.close();
            assertThat(layer.getUI()).isNotInstanceOf(BusyOverlayLayerUI.class);
        });
    }

    private static MouseEvent mouseEvent(JLayer<JPanel> layer) {
        return new MouseEvent(
                layer,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                5,
                5,
                1,
                false
        );
    }

    private static KeyEvent keyEvent(JLayer<JPanel> layer) {
        return new KeyEvent(
                layer,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_A,
                'a'
        );
    }

    private static void awaitEdt(Check check) {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            SwingEdt.runAndWait(() -> {
            });
            if (check.isSatisfied()) {
                return;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for EDT update", e);
            }
        }
        throw new AssertionError("Timed out waiting for EDT update");
    }

    @FunctionalInterface
    private interface Check {
        boolean isSatisfied();
    }

    private static final class Holder<T> {
        private T value;
    }
}
