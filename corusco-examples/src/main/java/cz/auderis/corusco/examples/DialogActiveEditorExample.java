package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Demonstrates active editor commit before a dialog result is created.
 */
public final class DialogActiveEditorExample {

    private DialogActiveEditorExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a table-editing dialog scenario.
     *
     * @return diagnostics describing the committed table value
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            DefaultTableModel rows = new DefaultTableModel(new Object[][] { { "Alice" } }, new Object[] { "Name" });
            JTable table = new JTable(rows);
            JPanel root = new JPanel();
            root.add(table);

            // The table editor still owns the user's text at this point; the
            // table model has not seen the replacement value yet.
            table.editCellAt(0, 0);
            ((JTextField) table.getEditorComponent()).setText("Alicia");
            result.add("beforeOk=" + rows.getValueAt(0, 0));

            // FormDialog commits active editors before asking the form for its
            // result, so generated forms can read from table/list models
            // without special OK-button code.
            FormDialog<TableForm, String> dialog = new FormDialog<>(new TableForm(rows), root);
            dialog.okCommand().execute();

            result.add("accepted=" + dialog.result().acceptedValue().orElseThrow());
            result.add("editing=" + table.isEditing());
        });
        return result;
    }

    private static final class TableForm implements FormModel<String> {

        private final DefaultTableModel rows;

        private TableForm(DefaultTableModel rows) {
            this.rows = rows;
        }

        @Override
        public ProblemSet problems() {
            return ProblemSet.empty();
        }

        @Override
        public boolean isCommittable() {
            return true;
        }

        @Override
        public void reset() {
        }

        @Override
        public void acceptCurrentValues() {
        }

        @Override
        public String toResult() {
            return rows.getValueAt(0, 0).toString();
        }
    }
}
