package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.ComponentStateModel;
import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.meta.OptionDescriptor;
import cz.auderis.corusco.core.meta.OptionKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
    private static final FieldKey<CustomerEdit, AuthenticationMode> AUTHENTICATION_MODE =
            FieldKey.of("customer/authentication-mode", CustomerEdit.class, AuthenticationMode.class);
    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ProblemCode RANGE = ProblemCode.of("range");

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

            model.setRawText("15.00", StandardChangeOrigin.MODEL);

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

            active.setValue(false, StandardChangeOrigin.MODEL);
            assertThat(checkBox.isSelected()).isFalse();

            SimpleValue<String> labelText = SimpleValue.of("ready");
            JLabel label = new JLabel();
            Binding labelBinding = BindingFactory.labelText(label, labelText);
            labelText.setValue("done", StandardChangeOrigin.MODEL);
            assertThat(label.getText()).isEqualTo("done");

            SimpleValue<Boolean> enabledValue = SimpleValue.of(true);
            JButton button = new JButton();
            Binding enabled = BindingFactory.enabled(button, enabledValue);
            enabledValue.setValue(false, StandardChangeOrigin.MODEL);
            assertThat(button.isEnabled()).isFalse();

            selected.close();
            labelBinding.close();
            enabled.close();
        });
    }

    @Test
    void radioGroupBindingUpdatesButtonsAndModel() {
        SwingEdt.runAndWait(() -> {
            FieldModel<CustomerEdit, AuthenticationMode> authenticationMode =
                    new FieldModel<>(AUTHENTICATION_MODE, AuthenticationMode.PASSWORD);
            JPanel panel = new JPanel();
            JRadioButton password = new JRadioButton("Password");
            password.setActionCommand("password");
            JRadioButton certificate = new JRadioButton("Certificate");
            certificate.setActionCommand("certificate");
            ButtonGroup group = new ButtonGroup();
            group.add(password);
            group.add(certificate);
            panel.add(password);
            panel.add(certificate);

            Binding binding = BindingFactory.radioGroup(panel, authenticationMode, List.of(
                    option(AuthenticationMode.PASSWORD, "password"),
                    option(AuthenticationMode.CERTIFICATE, "certificate")
            ));

            assertThat(password.isSelected()).isTrue();
            assertThat(certificate.isSelected()).isFalse();

            certificate.doClick();
            assertThat(authenticationMode.value().value()).isEqualTo(AuthenticationMode.CERTIFICATE);

            authenticationMode.setValue(AuthenticationMode.PASSWORD, StandardChangeOrigin.MODEL);
            assertThat(password.isSelected()).isTrue();
            assertThat(certificate.isSelected()).isFalse();

            binding.close();
            certificate.doClick();
            assertThat(authenticationMode.value().value()).isEqualTo(AuthenticationMode.PASSWORD);
        });
    }

    @Test
    void componentStateBindingAppliesPresentationStateAndRestoresOnClose() {
        SwingEdt.runAndWait(() -> {
            ComponentStateModel state = new ComponentStateModel();
            JTextField field = new JTextField();
            field.setEnabled(true);
            field.setVisible(true);
            field.setEditable(true);

            Binding binding = BindingFactory.componentState(field, state);

            state.enabled().setValue(false, StandardChangeOrigin.MODEL);
            assertThat(field.isEnabled()).isFalse();

            state.enabled().setValue(true, StandardChangeOrigin.MODEL);
            state.relevant().setValue(false, StandardChangeOrigin.MODEL);
            assertThat(field.isEnabled()).isFalse();
            assertThat(field.isVisible()).isFalse();

            state.relevant().setValue(true, StandardChangeOrigin.MODEL);
            state.protectedValue().setValue(true, StandardChangeOrigin.MODEL);
            assertThat(field.isEditable()).isFalse();

            state.protectedValue().setValue(false, StandardChangeOrigin.MODEL);
            state.busy().setValue(true, StandardChangeOrigin.MODEL);
            assertThat(field.isEnabled()).isFalse();
            assertThat(field.isEditable()).isFalse();

            binding.close();
            assertThat(field.isEnabled()).isTrue();
            assertThat(field.isVisible()).isTrue();
            assertThat(field.isEditable()).isTrue();
        });
    }

    @Test
    void statusTextPublishesWhileFocusedAndRestoresPreviousText() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            JLabel status = new JLabel("Ready");
            int initialFocusListenerCount = field.getFocusListeners().length;
            Binding binding = BindingFactory.statusText(field, status, "Enter customer name");

            focusGained(field);
            assertThat(status.getText()).isEqualTo("Enter customer name");

            focusLost(field);
            assertThat(status.getText()).isEqualTo("Ready");

            binding.close();
            assertThat(field.getFocusListeners()).hasSize(initialFocusListenerCount);
        });
    }

    @Test
    void observableStatusTextUpdatesOnlyWhileFocusedAndRestoresOnClose() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            JLabel status = new JLabel("Idle");
            SimpleValue<String> statusText = SimpleValue.of("Initial guidance");
            Binding binding = BindingFactory.statusText(field, status, statusText);

            statusText.setValue("Changed before focus", StandardChangeOrigin.MODEL);
            assertThat(status.getText()).isEqualTo("Idle");

            focusGained(field);
            assertThat(status.getText()).isEqualTo("Changed before focus");

            statusText.setValue("Changed while focused", StandardChangeOrigin.MODEL);
            assertThat(status.getText()).isEqualTo("Changed while focused");

            binding.close();
            assertThat(status.getText()).isEqualTo("Idle");

            statusText.setValue("Ignored after close", StandardChangeOrigin.MODEL);
            assertThat(status.getText()).isEqualTo("Idle");
        });
    }

    @Test
    void accessibleTextSetsAndRestoresAccessibleContextValues() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            field.getAccessibleContext().setAccessibleName("previous-name");
            field.getAccessibleContext().setAccessibleDescription("previous-description");

            Binding binding = BindingFactory.accessibleText(field, "Customer name", "Enter the display name");

            assertThat(field.getAccessibleContext().getAccessibleName()).isEqualTo("Customer name");
            assertThat(field.getAccessibleContext().getAccessibleDescription()).isEqualTo("Enter the display name");

            binding.close();
            assertThat(field.getAccessibleContext().getAccessibleName()).isEqualTo("previous-name");
            assertThat(field.getAccessibleContext().getAccessibleDescription()).isEqualTo("previous-description");
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

            model.setRawText("bad", StandardChangeOrigin.USER);

            assertThat(field.getToolTipText()).isEqualTo("Expected BigDecimal");
            assertThat(field.getBorder()).isNotSameAs(original);

            model.reset();
            assertThat(field.getToolTipText()).isNull();
            assertThat(field.getBorder()).isSameAs(original);

            model.setRawText("bad", StandardChangeOrigin.USER);
            border.close();
            tooltip.close();
            assertThat(field.getBorder()).isSameAs(original);
            assertThat(field.getToolTipText()).isNull();
        });
    }

    @Test
    void composedTooltipUsesPolicyOrderingAndRestoresPreviousTooltip() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            field.setToolTipText("original");
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.of(
                    problem(RANGE, ProblemSeverity.WARNING, "Credit limit should be reviewed"),
                    problem(REQUIRED, ProblemSeverity.ERROR, "Name is required")
            ));
            SimpleValue<String> disabledReason = SimpleValue.of("Save is disabled");

            Binding tooltip = BindingFactory.composedTooltip(
                    field,
                    problems,
                    disabledReason,
                    "Customer display name",
                    true
            );

            assertThat(field.getToolTipText()).isEqualTo("""
                    Name is required
                    Save is disabled
                    Customer display name
                    Press F1 for help""");

            disabledReason.setValue("Save waits for validation", StandardChangeOrigin.MODEL);
            assertThat(field.getToolTipText()).contains("Save waits for validation");

            problems.setValue(ProblemSet.empty(), StandardChangeOrigin.MODEL);
            assertThat(field.getToolTipText()).isEqualTo("""
                    Save waits for validation
                    Customer display name
                    Press F1 for help""");

            tooltip.close();
            assertThat(field.getToolTipText()).isEqualTo("original");
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

        assertThatThrownBy(() -> text.setValue("off-edt", StandardChangeOrigin.MODEL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Swing binding code must run on the EDT");
    }

    private record CustomerEdit(BigDecimal creditLimit, String notes, boolean active, AuthenticationMode authenticationMode) {
    }

    private enum AuthenticationMode {
        PASSWORD,
        CERTIFICATE
    }

    private static Problem problem(ProblemCode code, ProblemSeverity severity, String message) {
        return Problem.validation(code, severity, ProblemTarget.form(), message);
    }

    private static OptionDescriptor<AuthenticationMode> option(AuthenticationMode value, String key) {
        return OptionDescriptor.of(
                value,
                OptionKey.of(key),
                ResourceKey.of("customer.authentication-mode." + key + ".label", String.class),
                ResourceKey.of("customer.authentication-mode." + key + ".description", String.class),
                ResourceKey.of("customer.authentication-mode." + key + ".help", String.class)
        );
    }

    private static void focusGained(JTextField field) {
        field.getFocusListeners()[field.getFocusListeners().length - 1]
                .focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
    }

    private static void focusLost(JTextField field) {
        field.getFocusListeners()[field.getFocusListeners().length - 1]
                .focusLost(new FocusEvent(field, FocusEvent.FOCUS_LOST));
    }
}
