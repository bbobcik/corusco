package cz.auderis.corusco.examples.book;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Shared setup for book companion examples.
 */
public final class BookExampleSupport {

    private BookExampleSupport() {
        throw new AssertionError("No instances");
    }

    public static void installLookAndFeel() {
        FlatLightLaf.setup();
        UIManager.put("Component.focusWidth", 1);
    }

    public static JInternalFrame frame(String title, JComponent content, Dimension size) {
        requireEdt();
        installLookAndFeel();
        JInternalFrame frame = new JInternalFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(content);
        frame.setPreferredSize(size);
        frame.pack();
        return frame;
    }

    public static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Book examples must be constructed on the EDT");
        }
    }
}
