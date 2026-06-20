package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.TableCellProblems;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Placeholder binding for table cell validation feedback.
 *
 * <p>The binding wraps the table's current default renderers for descriptor
 * column value classes. During rendering it converts view row/column indices to
 * model indices, looks up problems targeted at {@code row + ColumnKey}, and
 * applies a tooltip plus a simple severity-colored border. It is intentionally
 * small; richer renderers can later use the same typed targeting helpers.</p>
 *
 * <p>The binding is EDT-confined and assumes the supplied table uses the
 * supplied {@link ObservableTableModel}. Row matching follows
 * {@link TableCellProblems}: row objects are compared with {@code equals}.
 * Closing restores the original default renderers and removes the problem
 * subscription.</p>
 *
 * @param <R> row type
 */
public final class TableCellValidationBinding<R> implements Binding {

    private static final String ORIGINAL_BORDER_PROPERTY =
            "cz.auderis.corusco.swing.table.originalCellBorder";

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final ReadableValue<ProblemSet> problems;
    private final Map<Class<?>, TableCellRenderer> originalRenderers = new LinkedHashMap<>();
    private final Subscription subscription;
    private boolean closed;

    private TableCellValidationBinding(
            JTable table,
            ObservableTableModel<R> model,
            ReadableValue<ProblemSet> problems
    ) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.problems = Objects.requireNonNull(problems, "problems");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied ObservableTableModel");
        }
        installRenderers();
        this.subscription = problems.subscribe(event -> {
            SwingEdt.requireEdt();
            table.repaint();
        });
    }

    /**
     * Installs validation cell decoration for a table model.
     *
     * @param table Swing table
     * @param model observable table model installed on the table
     * @param problems readable problem set
     * @param <R> row type
     * @return binding
     */
    public static <R> TableCellValidationBinding<R> bind(
            JTable table,
            ObservableTableModel<R> model,
            ReadableValue<ProblemSet> problems
    ) {
        return new TableCellValidationBinding<>(table, model, problems);
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        subscription.close();
        originalRenderers.forEach(table::setDefaultRenderer);
        table.repaint();
    }

    private void installRenderers() {
        for (Column<R, ?> column : model.descriptor().columns()) {
            Class<?> valueType = column.valueType();
            originalRenderers.computeIfAbsent(valueType, type -> {
                TableCellRenderer original = table.getDefaultRenderer(type);
                table.setDefaultRenderer(type, new ProblemCellRenderer(original));
                return original;
            });
        }
    }

    private Problem problemAt(int viewRow, int viewColumn) {
        if (viewRow < 0 || viewColumn < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        if (modelRow < 0 || modelRow >= model.getRowCount()
                || modelColumn < 0 || modelColumn >= model.getColumnCount()) {
            return null;
        }
        R row = model.readableRows().get(modelRow);
        Column<R, ?> column = model.descriptor().column(modelColumn);
        return TableCellProblems.mostSevere(problems.value(), row, column.key());
    }

    private final class ProblemCellRenderer implements TableCellRenderer {

        private final TableCellRenderer delegate;

        private ProblemCellRenderer(TableCellRenderer delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Problem problem = problemAt(row, column);
            if (component instanceof JComponent jComponent) {
                decorate(jComponent, problem);
            }
            return component;
        }

        private void decorate(JComponent component, Problem problem) {
            component.setToolTipText(problem == null ? null : problem.message());
            if (problem == null) {
                restoreBorder(component);
                return;
            }
            component.putClientProperty(ORIGINAL_BORDER_PROPERTY, component.getBorder());
            component.setBorder(BorderFactory.createLineBorder(color(problem.severity())));
        }

        private void restoreBorder(JComponent component) {
            Object original = component.getClientProperty(ORIGINAL_BORDER_PROPERTY);
            if (original instanceof Border border) {
                component.setBorder(border);
                component.putClientProperty(ORIGINAL_BORDER_PROPERTY, null);
            }
        }

        private Color color(ProblemSeverity severity) {
            return severity == ProblemSeverity.ERROR ? Color.RED : Color.ORANGE;
        }
    }
}
