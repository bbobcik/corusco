package cz.auderis.corusco.swing.task;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;

/**
 * {@code JLayer} UI that paints and enforces a busy overlay.
 *
 * <p>When busy, the layer paints a translucent cover and consumes input events
 * before they reach the wrapped view. The UI is intentionally animation-free so
 * tests and generated views get deterministic lifecycle behavior. It is used by
 * {@link BusyOverlayBinding} and {@link cz.auderis.corusco.swing.behavior.StandardBehaviors#busyOverlay}
 * to block interaction while background work is active.</p>
 *
 * <p>Instances are mutable Swing UI delegates and should be accessed on the
 * event dispatch thread. The UI does not own the wrapped component or the
 * {@code JLayer}; callers install and uninstall it through normal Swing
 * component lifecycle.</p>
 */
public final class BusyOverlayLayerUI extends LayerUI<JComponent> {

    private static final long serialVersionUID = 1L;

    private static final long BLOCKED_EVENT_MASK =
            AWTEvent.MOUSE_EVENT_MASK
                    | AWTEvent.MOUSE_MOTION_EVENT_MASK
                    | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                    | AWTEvent.KEY_EVENT_MASK
                    | AWTEvent.FOCUS_EVENT_MASK;

    private final Color overlayColor;
    private final float alpha;
    private Cursor idleCursor = Cursor.getDefaultCursor();
    private boolean busy;

    /**
     * Creates a busy overlay UI with a standard translucent dark overlay.
     */
    public BusyOverlayLayerUI() {
        this(new Color(40, 44, 52), 0.28f);
    }

    /**
     * Creates a busy overlay UI.
     *
     * @param overlayColor overlay paint color, not {@code null}
     * @param alpha overlay opacity from 0.0 to 1.0
     * @throws IllegalArgumentException if {@code alpha} is outside
     *         {@code 0.0..1.0}
     */
    public BusyOverlayLayerUI(Color overlayColor, float alpha) {
        this.overlayColor = Objects.requireNonNull(overlayColor, "overlayColor");
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException("alpha must be between 0.0 and 1.0");
        }
        this.alpha = alpha;
    }

    /**
     * Indicates whether the overlay is active.
     *
     * @return {@code true} when busy
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * Sets the cursor restored when the layer is not busy.
     *
     * @param idleCursor cursor to use outside busy state
     */
    public void setIdleCursor(Cursor idleCursor) {
        this.idleCursor = Objects.requireNonNull(idleCursor, "idleCursor");
    }

    /**
     * Sets busy state and repaints the layer when it changes.
     *
     * @param layer owning layer
     * @param busy new busy state
     */
    public void setBusy(JLayer<? extends JComponent> layer, boolean busy) {
        Objects.requireNonNull(layer, "layer");
        if (this.busy == busy) {
            return;
        }
        this.busy = busy;
        layer.setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : idleCursor);
        layer.repaint();
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        @SuppressWarnings("unchecked")
        JLayer<? extends JComponent> layer = (JLayer<? extends JComponent>) component;
        layer.setLayerEventMask(BLOCKED_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent component) {
        @SuppressWarnings("unchecked")
        JLayer<? extends JComponent> layer = (JLayer<? extends JComponent>) component;
        layer.setLayerEventMask(0L);
        layer.setCursor(idleCursor);
        super.uninstallUI(component);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        super.paint(graphics, component);
        if (!busy) {
            return;
        }
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            Composite oldComposite = copy.getComposite();
            copy.setComposite(AlphaComposite.SrcOver.derive(alpha));
            copy.setColor(overlayColor);
            copy.fillRect(0, 0, component.getWidth(), component.getHeight());
            copy.setComposite(oldComposite);
        } finally {
            copy.dispose();
        }
    }

    @Override
    public void eventDispatched(AWTEvent event, JLayer<? extends JComponent> layer) {
        if (busy && event instanceof InputEvent inputEvent) {
            inputEvent.consume();
        }
    }
}
