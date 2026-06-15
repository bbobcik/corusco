package cz.auderis.corusco.examples.refresh;

import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import net.miginfocom.swing.MigLayout;

public final class RefreshShellExample {

    private RefreshShellExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Swing refresh", createContent(), new Dimension(720, 420));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        JPanel panel = new JPanel(new MigLayout("fill, insets 16, gap 10", "[grow]", "[][grow][]"));
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("Refresh"));
        toolbar.add(new JButton("Save"));

        JPanel form = new JPanel(new MigLayout("fillx, insets 0", "[][grow,fill]", "[][]"));
        form.add(new JLabel("Customer"));
        form.add(new JTextField("Ada Lovelace"), "wrap");
        form.add(new JLabel("Filter"));
        form.add(new JTextField(), "wrap");

        JTable table = new JTable(new Object[][] {
                {"Ada Lovelace", "Active"},
                {"Grace Hopper", "Review"},
                {"Katherine Johnson", "Active"}
        }, new Object[] {"Name", "State"});

        panel.add(toolbar, "growx, wrap");
        panel.add(form, "growx, wrap");
        panel.add(new JScrollPane(table), "grow, wrap");
        panel.add(new JLabel("Ready"), "growx");
        return panel;
    }
}
