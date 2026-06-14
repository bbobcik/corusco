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
 * Binding that connects an observable busy flag to a {@link JLayer} overlay.
 *
 * <p>Use this binding when a presenter or task service exposes busy state and a
 * Swing view should block interaction while background work is active. The
 * binding replaces the layer's current {@link LayerUI} with a
 * {@link BusyOverlayLayerUI}, forwards the initial and subsequent busy values
 * to that UI, and restores the previous UI when closed.</p>
 *
 * <p>Installation and close are EDT-confined because they mutate Swing
 * component state. The observed {@link ReadableValue} may deliver changes from
 * another thread; the binding schedules visual updates back onto the EDT and
 * ignores queued updates after close. It owns only the subscription and the
 * temporary layer UI installation, not the wrapped component, layer, or busy
 * value itself.</p>
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
     * <p>The overlay UI is installed immediately and retained until
     * {@link #close()} restores the previous UI.</p>
     *
     * @param layer target {@code JLayer}
     * @param busy observable busy state
     * @param overlayUi overlay UI
     * @param <C> wrapped Swing component type
     * @return installed binding
     * @throws IllegalStateException if called off the EDT
     */
    public static <C extends JComponent> BusyOverlayBinding<C> install(
            JLayer<C> layer,
            ReadableValue<Boolean> busy,
            BusyOverlayLayerUI overlayUi
    ) {
        return new BusyOverlayBinding<>(layer, busy, overlayUi);
    }

    /**
     * Unsubscribes from busy changes and restores the layer's previous UI.
     *
     * <p>The call must run on the EDT and is idempotent. Close forces the
     * overlay into the not-busy state before restoring the previous UI.</p>
     */
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
