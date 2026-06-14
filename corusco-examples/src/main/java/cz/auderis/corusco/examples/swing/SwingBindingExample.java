package cz.auderis.corusco.examples.swing;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Demonstrates headless-safe Swing bindings.
 *
 * <p>The example binds Swing components to core field models and then closes
 * the returned bindings. It focuses on listener ownership and model/component
 * synchronization without showing a native window.</p>
 */
public final class SwingBindingExample {

    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final FieldKey<CustomerEdit, Boolean> ACTIVE =
            FieldKey.of("customer/active", CustomerEdit.class, Boolean.class);

    private SwingBindingExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Exercises Swing bindings on the EDT and returns model diagnostics.
     *
     * @return raw text, semantic value, active value, and label text
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> creditLimit =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            FieldModel<CustomerEdit, Boolean> active = new FieldModel<>(ACTIVE, false);
            JTextField creditLimitField = new JTextField();
            JCheckBox activeBox = new JCheckBox();
            JLabel rawPreview = new JLabel();

            try (BindingScope scope = new BindingScope()) {
                // The scope owns all Swing listeners and value subscriptions,
                // matching the cleanup path a view/behavior lifecycle uses
                // when a panel is removed.
                scope.add(BindingFactory.textField(creditLimitField, creditLimit));
                scope.add(BindingFactory.selected(activeBox, active));
                scope.add(BindingFactory.labelText(rawPreview, creditLimit.rawText()));

                // Swing edits flow into the model synchronously on the EDT.
                creditLimitField.setText("25.00");
                activeBox.setSelected(true);

                result.set(List.of(
                        creditLimit.rawText().value(),
                        creditLimit.value().toPlainString(),
                        String.valueOf(active.value().value()),
                        rawPreview.getText()
                ));
            }
        });
        return result.get();
    }

    private record CustomerEdit(BigDecimal creditLimit, boolean active) {
    }
}
