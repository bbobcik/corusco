package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Demonstrates generated view contracts and behavior plans.
 *
 * <p>The example shows how generated metadata can describe a view in terms of
 * typed components and behaviors, leaving actual Swing component instances to
 * the view implementation. It is a bridge between annotation processing and the
 * behavior-scope APIs.</p>
 */
public final class GeneratedViewPlanExample {

    private GeneratedViewPlanExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs a generated behavior plan on a headless Swing view.
     *
     * @return model and component details
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            GeneratedCustomerEditFormModel model = new GeneratedCustomerEditFormModel(new GeneratedCustomerEdit(
                    "Alice",
                    new BigDecimal("10.00"),
                    30,
                    LocalDate.parse("2026-01-01"),
                    GeneratedCustomerType.RETAIL,
                    true
            ));
            GeneratedCustomerView view = new GeneratedCustomerView();

            try (BehaviorScope scope = new BehaviorScope()) {
                // Generated plans are deliberately direct: they call generated
                // view accessors and install ordinary reusable behaviors.
                installGeneratedCustomerEditBehaviorPlan(view, model, scope);
                view.nameField.setText("Bob");
                view.activeBox.setSelected(false);

                result.set(List.of(
                        model.name.value(),
                        Boolean.toString(model.active.value().value()),
                        view.nameField.getToolTipText() == null ? "no-tooltip" : view.nameField.getToolTipText()
                ));
            }
        });
        return result.get();
    }

    private static void installGeneratedCustomerEditBehaviorPlan(
            GeneratedCustomerEditView view,
            GeneratedCustomerEditFormModel model,
            BehaviorScope scope
    ) {
        // Keep the example source-set self-contained: javac cannot reliably
        // resolve a type generated from the same source set during attribution,
        // so this helper mirrors the generated plan's direct installation code.
        scope.install(view.nameField(), List.of(
                StandardBehaviors.textFieldBinding(model.name),
                StandardBehaviors.validationTooltip(model.name.problemSet()),
                StandardBehaviors.validationBorder(model.name.problemSet()),
                StandardBehaviors.selectAllOnFocus()
        ));
        scope.install(view.creditLimitField(), List.of(
                StandardBehaviors.textFieldBinding(model.creditLimit),
                StandardBehaviors.validationTooltip(model.creditLimit.problemSet()),
                StandardBehaviors.validationBorder(model.creditLimit.problemSet()),
                StandardBehaviors.selectAllOnFocus()
        ));
        scope.install(view.ageField(), List.of(
                StandardBehaviors.textFieldBinding(model.age),
                StandardBehaviors.validationTooltip(model.age.problemSet()),
                StandardBehaviors.validationBorder(model.age.problemSet()),
                StandardBehaviors.selectAllOnFocus()
        ));
        scope.install(view.validFromField(), List.of(
                StandardBehaviors.textFieldBinding(model.validFrom),
                StandardBehaviors.validationTooltip(model.validFrom.problemSet()),
                StandardBehaviors.validationBorder(model.validFrom.problemSet()),
                StandardBehaviors.selectAllOnFocus()
        ));
        scope.install(view.activeBox(), List.of(StandardBehaviors.checkBoxBinding(model.active)));
    }

    private static final class GeneratedCustomerView extends JPanel implements GeneratedCustomerEditView {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = new JTextField();
        private final JTextField creditLimitField = new JTextField();
        private final JTextField ageField = new JTextField();
        private final JTextField validFromField = new JTextField();
        private final JComboBox<GeneratedCustomerType> typeCombo = new JComboBox<>(GeneratedCustomerType.values());
        private final JCheckBox activeBox = new JCheckBox();

        @Override
        public JTextField nameField() {
            return nameField;
        }

        @Override
        public JTextField creditLimitField() {
            return creditLimitField;
        }

        @Override
        public JTextField ageField() {
            return ageField;
        }

        @Override
        public JTextField validFromField() {
            return validFromField;
        }

        @Override
        public JComboBox<GeneratedCustomerType> typeCombo() {
            return typeCombo;
        }

        @Override
        public JCheckBox activeBox() {
            return activeBox;
        }
    }
}
