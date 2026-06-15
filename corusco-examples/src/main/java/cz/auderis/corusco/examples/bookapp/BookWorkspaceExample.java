package cz.auderis.corusco.examples.bookapp;

import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public final class BookWorkspaceExample {

    private BookWorkspaceExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Book workspace", createContent(), new Dimension(820, 500));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        JPanel panel = new JPanel(new MigLayout("fill, insets 18, gap 12", "[grow][300!]", "[][grow][]"));
        JTable table = new JTable(new Object[][] {
                {"Ada Lovelace", "Active"},
                {"Grace Hopper", "Review"},
                {"Katherine Johnson", "Active"}
        }, new Object[] {"Name", "State"});

        JPanel detail = new JPanel(new MigLayout("fillx, insets 12", "[][grow,fill]", "[][][]push[]"));
        detail.add(new JLabel("Name"));
        detail.add(new JTextField("Ada Lovelace"), "wrap");
        detail.add(new JLabel("State"));
        detail.add(new JTextField("Active"), "wrap");
        detail.add(new JButton("Validate"), "span 2, split 2, align right");
        detail.add(new JButton("Save"));

        panel.add(new JLabel("Customers"), "growx");
        panel.add(new JLabel("Detail"), "growx, wrap");
        panel.add(new JScrollPane(table), "grow");
        panel.add(detail, "growy, wrap");
        panel.add(new JLabel("Ready"), "span 2, growx");
        return panel;
    }
}
