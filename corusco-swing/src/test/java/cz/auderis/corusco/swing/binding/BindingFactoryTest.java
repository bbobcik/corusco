package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BindingFactoryTest {

    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final TextFieldKey<CustomerEdit, String> NOTES =
            TextFieldKey.of("customer/notes", CustomerEdit.class, String.class);
    private static final FieldKey<CustomerEdit, Boolean> ACTIVE =
            FieldKey.of("customer/active", CustomerEdit.class, Boolean.class);

    @Test
    void textFieldEditUpdatesRawTextSemanticValueAndProblemState() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> model =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            JTextField field = new JTextField();
            Binding binding = BindingFactory.textField(field, model);

            field.setText("20.00");
            assertThat(model.rawText().value()).isEqualTo("20.00");
            assertThat(model.value()).isEqualByComparingTo("20.00");
            assertThat(model.problems().isEmpty()).isTrue();

            field.setText("bad");
            assertThat(model.rawText().value()).isEqualTo("bad");
            assertThat(model.value()).isEqualByComparingTo("20.00");
            assertThat(model.problems().hasErrors()).isTrue();

            binding.close();
            field.setText("30.00");
            assertThat(model.rawText().value()).isEqualTo("bad");
        });
    }

    @Test
    void modelRawTextChangeUpdatesTextFieldWithoutFeedbackLoop() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> model =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            JTextField field = new JTextField();
            Binding binding = BindingFactory.textField(field, model);

            model.setRawText("15.00", ChangeOrigin.MODEL);

            assertThat(field.getText()).isEqualTo("15.00");
            assertThat(model.value()).isEqualByComparingTo("15.00");
            binding.close();
        });
    }

    @Test
    void textAreaBindingUsesTheSameTextModelContract() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, String> model =
                    new TextFieldModel<>(NOTES, "initial", Converters.string());
            JTextArea area = new JTextArea();
            Binding binding = BindingFactory.textArea(area, model);

            area.setText("updated");

            assertThat(model.rawText().value()).isEqualTo("updated");
            assertThat(model.value()).isEqualTo("updated");
            binding.close();
        });
    }

    @Test
    void checkboxAndReadableValueBindingsUpdateComponentsAndModels() {
        SwingEdt.runAndWait(() -> {
            FieldModel<CustomerEdit, Boolean> active = new FieldModel<>(ACTIVE, false);
            JCheckBox checkBox = new JCheckBox();
            Binding selected = BindingFactory.selected(checkBox, active);

            checkBox.setSelected(true);
            assertThat(active.value().value()).isTrue();

            active.setValue(false, ChangeOrigin.MODEL);
            assertThat(checkBox.isSelected()).isFalse();

            SimpleValue<String> labelText = SimpleValue.of("ready");
            JLabel label = new JLabel();
            Binding labelBinding = BindingFactory.labelText(label, labelText);
            labelText.setValue("done", ChangeOrigin.MODEL);
            assertThat(label.getText()).isEqualTo("done");

            SimpleValue<Boolean> enabledValue = SimpleValue.of(true);
            JButton button = new JButton();
            Binding enabled = BindingFactory.enabled(button, enabledValue);
            enabledValue.setValue(false, ChangeOrigin.MODEL);
            assertThat(button.isEnabled()).isFalse();

            selected.close();
            labelBinding.close();
            enabled.close();
        });
    }

    @Test
    void validationTooltipAndBorderReflectProblemStateAndRestoreOnClose() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> model =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            JTextField field = new JTextField();
            Border original = field.getBorder();
            Binding tooltip = BindingFactory.validationTooltip(field, model.problemSet());
            Binding border = BindingFactory.validationBorder(field, model.problemSet());

            model.setRawText("bad", ChangeOrigin.USER);

            assertThat(field.getToolTipText()).isEqualTo("Expected BigDecimal");
            assertThat(field.getBorder()).isNotSameAs(original);

            model.reset();
            assertThat(field.getToolTipText()).isNull();
            assertThat(field.getBorder()).isSameAs(original);

            model.setRawText("bad", ChangeOrigin.USER);
            border.close();
            tooltip.close();
            assertThat(field.getBorder()).isSameAs(original);
        });
    }

    @Test
    void bindingScopeClosesOwnedBindings() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, String> model =
                    new TextFieldModel<>(NOTES, "initial", Converters.string());
            JTextArea area = new JTextArea();
            BindingScope scope = new BindingScope();
            scope.add(BindingFactory.textArea(area, model));

            scope.close();
            area.setText("ignored");

            assertThat(model.rawText().value()).isEqualTo("initial");
            assertThat(scope.isClosed()).isTrue();
        });
    }

    @Test
    void commitActiveEditorSucceedsWithoutActiveEditorInsideRoot() {
        SwingEdt.runAndWait(() -> assertThat(SwingEditors.commitActiveEditor(new JPanel())).isTrue());
    }

    @Test
    void modelCallbacksFailFastOutsideEdt() {
        SimpleValue<String> text = SimpleValue.of("initial");

        SwingEdt.runAndWait(() -> BindingFactory.labelText(new JLabel(), text));

        assertThatThrownBy(() -> text.setValue("off-edt", ChangeOrigin.MODEL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Swing binding code must run on the EDT");
    }

    private record CustomerEdit(BigDecimal creditLimit, String notes, boolean active) {
    }
}
