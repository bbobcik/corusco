package cz.auderis.corusco.examples.corusco_swing;

import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.examples.book.BookExampleSupport;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.BindingFactory;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public final class BindingBehaviorBookExample {

    private BindingBehaviorBookExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Corusco Swing binding", createContent(), new Dimension(460, 180));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        SimpleValue<String> status = SimpleValue.of("Ready");
        JLabel label = new JLabel();
        BindingScope scope = new BindingScope();
        scope.add(BindingFactory.labelText(label, status));
        status.setValue("Bound status");

        JPanel panel = new JPanel(new MigLayout("fillx, insets 16", "[][grow]", "[]"));
        panel.putClientProperty("book.bindingScope", scope);
        panel.add(new JLabel("Status"));
        panel.add(label, "growx");
        return panel;
    }
}
