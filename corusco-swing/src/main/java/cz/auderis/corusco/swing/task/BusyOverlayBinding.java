package cz.auderis.corusco.swing.task;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

/**
 * Binds observable busy state to a {@code JLayer} overlay.
 *
 * <p>Install and close must run on the EDT. Busy value changes may arrive from
 * another thread; the binding schedules the visual update onto the EDT and
 * ignores queued updates after close.</p>
 *
 * @param <C> wrapped Swing component type
 */
public final class BusyOverlayBinding<C extends JComponent> implements Binding {

    private final JLayer<C> layer;
    private final BusyOverlayLayerUI overlayUi;
    private final LayerUI<? super C> previousUi;
    private final Subscription subscription;
    private boolean closed;

    private BusyOverlayBinding(
            JLayer<C> layer,
            ReadableValue<Boolean> busy,
            BusyOverlayLayerUI overlayUi
    ) {
        SwingEdt.requireEdt();
        this.layer = Objects.requireNonNull(layer, "layer");
        Objects.requireNonNull(busy, "busy");
        this.overlayUi = Objects.requireNonNull(overlayUi, "overlayUi");
        this.previousUi = layer.getUI();
        overlayUi.setIdleCursor(layer.getCursor());
        layer.setUI(overlayUi);
        setBusy(Boolean.TRUE.equals(busy.value()));
        this.subscription = busy.subscribe(event -> scheduleBusy(Boolean.TRUE.equals(event.newValue())));
    }

    /**
     * Installs a busy overlay binding with a standard overlay UI.
     *
     * @param layer target {@code JLayer}
     * @param busy observable busy state
     * @param <C> wrapped Swing component type
     * @return installed binding
     */
    public static <C extends JComponent> BusyOverlayBinding<C> install(JLayer<C> layer, ReadableValue<Boolean> busy) {
        return install(layer, busy, new BusyOverlayLayerUI());
    }

    /**
     * Installs a busy overlay binding with an explicit overlay UI.
     *
     * @param layer target {@code JLayer}
     * @param busy observable busy state
     * @param overlayUi overlay UI
     * @param <C> wrapped Swing component type
     * @return installed binding
     */
    public static <C extends JComponent> BusyOverlayBinding<C> install(
            JLayer<C> layer,
            ReadableValue<Boolean> busy,
            BusyOverlayLayerUI overlayUi
    ) {
        return new BusyOverlayBinding<>(layer, busy, overlayUi);
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        subscription.close();
        overlayUi.setBusy(layer, false);
        layer.setUI(previousUi);
        layer.repaint();
    }

    private void scheduleBusy(boolean newBusy) {
        if (SwingUtilities.isEventDispatchThread()) {
            setBusy(newBusy);
        } else {
            SwingEdt.runLater(() -> setBusy(newBusy));
        }
    }

    private void setBusy(boolean newBusy) {
        if (closed) {
            return;
        }
        overlayUi.setBusy(layer, newBusy);
    }
}
