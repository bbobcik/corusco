package cz.auderis.corusco.examples;

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
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Swing example windows must be created on the EDT");
        }

        JInternalFrame frame = new JInternalFrame("Corusco baseline");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(createContent());
        frame.pack();
        return frame;
    }

    /**
     * Creates the example content pane.
     *
     * @return a panel containing baseline example content
     */
    public static JPanel createContent() {
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
        SwingUtilities.invokeAndWait(BaselineSwingExample::createWindow);
    }
}
