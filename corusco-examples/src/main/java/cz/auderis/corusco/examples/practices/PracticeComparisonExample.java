package cz.auderis.corusco.examples.practices;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

public final class PracticeComparisonExample {

    private PracticeComparisonExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Practice comparison", createContent(), new Dimension(560, 220));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        SimpleValue<String> name = SimpleValue.of("Ada Lovelace");
        JLabel status = new JLabel("Name length: " + name.value().length());
        JTextField field = new JTextField(name.value());

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                name.setValue(field.getText(), ChangeOrigin.USER);
            }
        });
        name.subscribe(event -> status.setText("Name length: " + event.newValue().length()));

        JPanel panel = new JPanel(new MigLayout("fillx, insets 16", "[][grow,fill]", "[][]"));
        panel.add(new JLabel("Model-backed field"));
        panel.add(field, "wrap");
        panel.add(new JLabel("Derived status"));
        panel.add(status, "wrap");
        return panel;
    }
}
