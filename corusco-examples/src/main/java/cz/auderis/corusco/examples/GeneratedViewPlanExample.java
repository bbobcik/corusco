package cz.auderis.corusco.examples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.binding.SwingEdt;

/**
 * Demonstrates generated view contracts and behavior plans.
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
                GeneratedCustomerEditBehaviorPlan.install(view, model, scope);
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
