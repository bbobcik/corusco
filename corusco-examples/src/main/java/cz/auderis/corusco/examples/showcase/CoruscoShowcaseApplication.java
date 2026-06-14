package cz.auderis.corusco.examples.showcase;

import com.formdev.flatlaf.FlatLightLaf;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Runnable desktop showcase for evaluating Corusco in a business Swing app.
 */
public final class CoruscoShowcaseApplication {

    private CoruscoShowcaseApplication() {
        throw new AssertionError("No instances");
    }

    /**
     * Starts the showcase application.
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Component.arc", 6);
        SwingUtilities.invokeLater(() -> {
            ShowcaseRuntime runtime = new ShowcaseRuntime();
            JFrame frame = new JFrame("Corusco Showcase");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setJMenuBar(runtime.menuBar());
            frame.setContentPane(runtime.layer());
            frame.setMinimumSize(new Dimension(1220, 780));
            frame.setLocationByPlatform(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent event) {
                    runtime.close();
                }
            });
            frame.pack();
            frame.setVisible(true);
        });
    }

    /**
     * Runs a non-window scenario that exercises the showcase composition.
     *
     * @return diagnostics for tests and documentation checks
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            ShowcaseRuntime runtime = new ShowcaseRuntime();
            try {
                result.set(runtime.diagnostics());
            } finally {
                runtime.close();
            }
        });
        return result.get();
    }
}
