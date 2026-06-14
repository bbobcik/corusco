package cz.auderis.corusco.examples.swing;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Minimal Swing example used by the repository baseline.
 *
 * <p>The example builds Swing components on the Event Dispatch Thread and does
 * not show a native top-level window, so it can participate in automated
 * headless builds.</p>
 */
public final class BaselineSwingExample {

    private BaselineSwingExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Creates the example window on the Event Dispatch Thread.
     *
     * @return a configured, hidden internal frame
     */
    public static JInternalFrame createWindow() {
        // Keep Swing construction on the EDT even in a smoke example; later
        // bindings and behaviors will rely on the same discipline.
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Swing example windows must be created on the EDT");
        }

        // Use an internal frame instead of JFrame so automated headless builds
        // can construct the component tree without opening a native window.
        JInternalFrame frame = new JInternalFrame("Corusco baseline");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(createContent());
        // Pack now so layout errors surface during the smoke test, not only
        // when someone eventually displays the example.
        frame.pack();
        return frame;
    }

    /**
     * Creates the example content pane.
     *
     * @return a panel containing baseline example content
     */
    public static JPanel createContent() {
        // Keep the baseline content deliberately boring; it proves module and
        // Swing wiring without implying framework APIs that do not exist yet.
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Corusco Swing baseline"), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the example window on the Event Dispatch Thread.
     *
     * @param args ignored
     * @throws InvocationTargetException when EDT execution fails
     * @throws InterruptedException when waiting for EDT execution is interrupted
     */
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        // A real application would show a top-level window here. This entry
        // point only exercises EDT-safe construction.
        SwingUtilities.invokeAndWait(BaselineSwingExample::createWindow);
    }
}
