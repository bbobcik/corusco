package cz.auderis.corusco.examples.miglayout;

import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public final class MigLayoutFormExample {

    private MigLayoutFormExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("MigLayout form", createContent(), new Dimension(520, 260));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        JPanel panel = new JPanel(new MigLayout("fillx, insets 18, gap 8", "[][grow,fill]", "[][][]push[]"));
        panel.add(new JLabel("Name"));
        panel.add(new JTextField("Ada Lovelace"), "wrap");
        panel.add(new JLabel("Type"));
        panel.add(new JComboBox<>(new String[] {"Customer", "Partner", "Internal"}), "wrap");
        panel.add(new JLabel("Status"), "top");
        panel.add(new JTextField("Active"), "wrap");

        JPanel buttons = new JPanel(new MigLayout("insets 0", "[]8[]", "[]"));
        buttons.add(new JButton("Cancel"));
        buttons.add(new JButton("Save"));
        panel.add(buttons, "span 2, align right");
        return panel;
    }
}
