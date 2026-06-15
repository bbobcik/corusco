package cz.auderis.corusco.examples.modern_java;

import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public final class ModernJavaSwingExample {

    public sealed interface LoadState permits LoadState.Idle, LoadState.Loading, LoadState.Loaded {
        record Idle() implements LoadState {
        }

        record Loading(String label) implements LoadState {
        }

        record Loaded(int rows) implements LoadState {
        }
    }

    private ModernJavaSwingExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Modern Java", createContent(new LoadState.Loaded(200)),
                new Dimension(420, 160));
    }

    public static JPanel createContent(LoadState state) {
        BookExampleSupport.requireEdt();
        JPanel panel = new JPanel(new MigLayout("fillx, insets 16", "[][grow]", "[]"));
        panel.add(new JLabel("State"));
        panel.add(new JLabel(describe(state)), "growx");
        return panel;
    }

    public static String describe(LoadState state) {
        return switch (state) {
            case LoadState.Idle ignored -> "Idle";
            case LoadState.Loading loading -> "Loading " + loading.label();
            case LoadState.Loaded loaded -> "Loaded " + loaded.rows() + " rows";
        };
    }
}
