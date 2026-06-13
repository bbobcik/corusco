package cz.auderis.corusco.swing.binding;

import java.text.NumberFormat;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwingEditorsTest {

    @Test
    void formattedFieldCommitFailureBlocksDialogCommit() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            RejectingFormattedTextField field = new RejectingFormattedTextField();
            root.add(field);

            assertThat(SwingEditors.commitActiveEditor(root, field)).isFalse();
        });
    }

    @Test
    void spinnerCommitUsesSpinnerEditorApi() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
            root.add(spinner);
            JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            textField.setText("7");

            assertThat(SwingEditors.commitActiveEditor(root, textField)).isTrue();

            assertThat(spinner.getValue()).isEqualTo(7);
        });
    }

    @Test
    void invalidSpinnerTextBlocksDialogCommit() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
            root.add(spinner);
            JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            textField.setText("not a number");

            assertThat(SwingEditors.commitActiveEditor(root, textField)).isFalse();
            assertThat(spinner.getValue()).isEqualTo(1);
        });
    }

    @Test
    void editingTableBelowRootIsStoppedWithoutFocusOwner() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            DefaultTableModel model = new DefaultTableModel(new Object[][] { { "Alice" } }, new Object[] { "Name" });
            JTable table = new JTable(model);
            root.add(table);

            table.editCellAt(0, 0);
            ((JTextField) table.getEditorComponent()).setText("Alicia");

            assertThat(SwingEditors.commitActiveEditor(root, null)).isTrue();

            assertThat(table.isEditing()).isFalse();
            assertThat(model.getValueAt(0, 0)).isEqualTo("Alicia");
        });
    }

    @Test
    void rejectingTableEditorBlocksDialogCommit() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            DefaultTableModel model = new DefaultTableModel(new Object[][] { { "Alice" } }, new Object[] { "Name" });
            JTable table = new JTable(model);
            table.setDefaultEditor(Object.class, new RejectingCellEditor());
            root.add(table);

            table.editCellAt(0, 0);
            ((JTextField) table.getEditorComponent()).setText("Alicia");

            assertThat(SwingEditors.commitActiveEditor(root, null)).isFalse();

            assertThat(table.isEditing()).isTrue();
            assertThat(model.getValueAt(0, 0)).isEqualTo("Alice");
        });
    }

    @Test
    void focusOutsideRootIsIgnoredButRootTablesStillCommit() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            JPanel otherRoot = new JPanel();
            RejectingFormattedTextField outsideField = new RejectingFormattedTextField();
            otherRoot.add(outsideField);

            assertThat(SwingEditors.commitActiveEditor(root, outsideField)).isTrue();
        });
    }

    @Test
    void nullRootFailsClearly() {
        SwingEdt.runAndWait(() ->
                assertThatNullPointerException()
                        .isThrownBy(() -> SwingEditors.commitActiveEditor(null))
                        .withMessageContaining("root")
        );
    }

    @Test
    void publicCommitRequiresEdt() {
        assertThatThrownBy(() -> SwingEditors.commitActiveEditor(new JPanel()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");
    }

    private static final class RejectingFormattedTextField extends JFormattedTextField {

        private RejectingFormattedTextField() {
            super(NumberFormat.getIntegerInstance());
        }

        @Override
        public void commitEdit() throws java.text.ParseException {
            throw new java.text.ParseException("Rejected for test", 0);
        }
    }

    private static final class RejectingCellEditor extends DefaultCellEditor {

        private RejectingCellEditor() {
            super(new JTextField());
        }

        @Override
        public boolean stopCellEditing() {
            return false;
        }
    }
}
