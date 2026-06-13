package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JTextField;

/**
 * Demonstrates behavior-based Swing component extension.
 */
public final class BehaviorExample {

    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);

    private BehaviorExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs behaviors on a text field and returns model diagnostics.
     *
     * @return raw text, semantic value, and tooltip text
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> creditLimit =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            JTextField field = new JTextField();

            try (BehaviorScope scope = new BehaviorScope()) {
                // Generated behavior plans should eventually emit this kind of
                // ordered behavior list. The scope performs ordering, conflict
                // checks, and deterministic cleanup.
                scope.install(field, List.of(
                        StandardBehaviors.textFieldBinding(creditLimit),
                        StandardBehaviors.validationTooltip(creditLimit.problemSet()),
                        StandardBehaviors.validationBorder(creditLimit.problemSet()),
                        StandardBehaviors.selectAllOnFocus()
                ));

                field.setText("bad");
                result.set(List.of(
                        creditLimit.rawText().value(),
                        creditLimit.value().toPlainString(),
                        field.getToolTipText()
                ));
            }
        });
        return result.get();
    }

    private record CustomerEdit(BigDecimal creditLimit) {
    }
}
