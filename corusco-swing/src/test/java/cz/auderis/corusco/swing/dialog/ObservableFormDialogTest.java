package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.AbstractObservableFormModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.math.BigDecimal;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import cz.auderis.corusco.swing.binding.BindingFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ObservableFormDialogTest {
    @Test void invalidEditUpdatesCommandsSummaryAndDefaultDirtyState() {
        SwingEdt.runAndWait(() -> {
            Form form = new Form();
            var dialog = new FormDialog<>(form, new JPanel());
            var label = new JLabel("original");
            var binding = FormDialogValidationBinding.install(dialog, label);
            form.field.setRawText("bad", StandardChangeOrigin.USER);
            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.dirtyState().isDirty()).isTrue();
            assertThat(label.getText()).isNotBlank();
            binding.close();
            assertThat(label.getText()).isEqualTo("original");
            dialog.close();
            form.close();
        });
    }
    @Test void reentrantResetDefersOnlyLockedDocumentWrite() {
        Form form = new Form();
        JTextField text = new JTextField();
        SwingEdt.runAndWait(() -> {
            BindingFactory.textField(text, form.field);
            form.state().subscribe(event -> { if (!event.newValue().committable()) form.reset(); });
            text.setText("");
            assertThat(form.state().value().committable()).isTrue();
            assertThat(form.field.value()).isEqualByComparingTo("10");
        });
        SwingEdt.runAndWait(() -> assertThat(text.getText()).isEqualTo("10"));
    }
    @Test void disposedBindingDoesNotApplyPendingDocumentWrite() {
        Form form = new Form();
        JTextField text = new JTextField();
        SwingEdt.runAndWait(() -> {
            var binding = BindingFactory.textField(text, form.field);
            form.state().subscribe(event -> { if (!event.newValue().committable()) form.reset(); });
            text.setText("");
            binding.close();
        });
        SwingEdt.runAndWait(() -> assertThat(text.getText()).isEmpty());
    }
    @Test void pendingDocumentWriteUsesLatestModelValue() {
        Form form = new Form();
        JTextField text = new JTextField();
        SwingEdt.runAndWait(() -> {
            BindingFactory.textField(text, form.field);
            form.state().subscribe(event -> { if (!event.newValue().committable()) form.reset(); });
            text.setText("");
            form.field.setRawText("20", StandardChangeOrigin.MODEL);
            assertThat(text.getText()).isEqualTo("20");
        });
        SwingEdt.runAndWait(() -> assertThat(text.getText()).isEqualTo("20"));
    }
    @Test void writeToDifferentDocumentRemainsSynchronous() {
        Form first = new Form();
        Form second = new Form();
        JTextField firstText = new JTextField();
        JTextField secondText = new JTextField();
        SwingEdt.runAndWait(() -> {
            var firstBinding = BindingFactory.textField(firstText, first.field);
            var secondBinding = BindingFactory.textField(secondText, second.field);
            first.state().subscribe(event -> {
                second.field.setRawText("20", StandardChangeOrigin.MODEL);
                assertThat(secondText.getText()).isEqualTo("20");
            });
            firstText.setText("");
            firstBinding.close(); secondBinding.close();
        });
    }
    @Test void sharedDocumentBindingsDeferReentrantReset() {
        Form form = new Form();
        JTextField first = new JTextField();
        JTextField second = new JTextField();
        SwingEdt.runAndWait(() -> {
            second.setDocument(first.getDocument());
            BindingFactory.textField(first, form.field);
            BindingFactory.textField(second, form.field);
            form.state().subscribe(event -> { if (!event.newValue().committable()) form.reset(); });
            first.setText("");
            assertThat(form.state().value().committable()).isTrue();
        });
        SwingEdt.runAndWait(() -> assertThat(first.getText()).isEqualTo("10"));
    }
    private static final class Form extends AbstractObservableFormModel<BigDecimal> {
        final TextFieldModel<Form, BigDecimal> field = register(new TextFieldModel<>(
                TextFieldKey.of("number", Form.class, BigDecimal.class), BigDecimal.TEN,
                Converters.bigDecimal(EmptyTextPolicy.REJECT)));
        @Override protected BigDecimal createResult() { return field.value(); }
    }
}
