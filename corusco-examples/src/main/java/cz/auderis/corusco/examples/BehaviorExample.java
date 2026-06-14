package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.help.DefaultHelpService;
import cz.auderis.corusco.core.help.HelpRequest;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Demonstrates behavior-based Swing component extension.
 *
 * <p>The example installs several behaviors into a scope to show how Corusco
 * separates primary bindings, decorations, and interactions. It gives readers a
 * compact view of the behavior lifecycle before they move to generated view
 * plans.</p>
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
            List<HelpRequest> helpRequests = new ArrayList<>();
            DefaultHelpService helpService = new DefaultHelpService(helpRequests::add);
            Resources resources = Resources.of(Map.of(
                    GeneratedCustomerRowTableResources.NAME_TOOLTIP.id(), "Customer display name"
            ));
            SimpleValue<String> disabledReason = SimpleValue.of("Save is disabled until the field is valid");

            try (BehaviorScope scope = new BehaviorScope(helpService)) {
                // Generated behavior plans should eventually emit this kind of
                // ordered behavior list. The scope performs ordering, conflict
                // checks, and deterministic cleanup.
                scope.install(field, List.of(
                        StandardBehaviors.textFieldBinding(creditLimit),
                        StandardBehaviors.composedTooltip(
                                creditLimit.problemSet(),
                                disabledReason,
                                resources.resolve(GeneratedCustomerRowTableResources.NAME_TOOLTIP, ""),
                                GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic() != null
                        ),
                        StandardBehaviors.validationBorder(creditLimit.problemSet()),
                        StandardBehaviors.selectAllOnFocus(),
                        StandardBehaviors.helpOnF1(GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic())
                ));

                field.setText("bad");
                // Help behavior uses Swing input/action maps, so generated
                // help topics can be dispatched without raw key listeners.
                triggerF1(field);
                result.set(List.of(
                        creditLimit.rawText().value(),
                        creditLimit.value().toPlainString(),
                        field.getToolTipText(),
                        helpRequests.getFirst().topic().id()
                ));
            }
        });
        return result.get();
    }

    private static void triggerF1(JTextField field) {
        KeyStroke f1 = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0);
        Object actionKey = field.getInputMap(JComponent.WHEN_FOCUSED).get(f1);
        Action action = field.getActionMap().get(actionKey);
        action.actionPerformed(new ActionEvent(field, ActionEvent.ACTION_PERFORMED, "help"));
    }

    private record CustomerEdit(BigDecimal creditLimit) {
    }
}
