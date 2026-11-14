package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GeneratedValidationDecorationTest {
    @Test void semanticValidationUpdatesGeneratedTooltipAndDetaches() {
        SwingEdt.runAndWait(() -> {
            var form = new GeneratedCustomerEditFormModel(new GeneratedCustomerEdit(
                    "Alice", BigDecimal.TEN, 30, LocalDate.parse("2026-01-01"), GeneratedCustomerType.RETAIL, true));
            var view = new View();
            try (var scope = new BehaviorScope()) {
                GeneratedCustomerEditBehaviorPlan.install(view, new GeneratedCustomerEditPresentationModel(form), scope);
                view.name.setText("");
                assertThat(form.name.problems().isEmpty()).isTrue();
                assertThat(view.name.getToolTipText()).isNotBlank();
                view.name.setText("Bob");
                assertThat(view.name.getToolTipText()).isNull();
            }
            form.name.setRawText("", cz.auderis.corusco.core.value.StandardChangeOrigin.MODEL);
            assertThat(view.name.getToolTipText()).isNull();
            form.close();
        });
    }
    private static final class View implements GeneratedCustomerEditView {
        final JTextField name = new JTextField();
        final JTextField credit = new JTextField();
        final JTextField age = new JTextField();
        final JTextField date = new JTextField();
        final JComboBox<GeneratedCustomerType> type = new JComboBox<>(GeneratedCustomerType.values());
        final JCheckBox active = new JCheckBox();
        public JTextField nameField() { return name; }
        public JTextField creditLimitField() { return credit; }
        public JTextField ageField() { return age; }
        public JTextField validFromField() { return date; }
        public JComboBox<GeneratedCustomerType> typeCombo() { return type; }
        public JCheckBox activeBox() { return active; }
    }
}
