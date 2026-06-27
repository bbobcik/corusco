package cz.auderis.corusco.examples.showcase;

import com.formdev.flatlaf.FlatClientProperties;
import cz.auderis.corusco.core.dataset.DataColumnDescriptor;
import cz.auderis.corusco.core.dataset.DataColumnKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.table.DataSetFrameTableModel;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableCellTooltipBinding;
import cz.auderis.corusco.swing.table.TableHeaderTooltipBinding;
import java.awt.Dimension;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.miginfocom.swing.MigLayout;

final class ShowcaseUi {

    private ShowcaseUi() {
    }

    static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE, "font: 145% $semibold.font;");
        return label;
    }

    static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:#5f6673;");
        return label;
    }

    static JLabel badge(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE,
                "arc:999; border:6,12,6,12; background:#e8f3ee; foreground:#176b49; font: $semibold.font;");
        return label;
    }

    static JPanel sidebar() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 24 22, gapy 10", "[grow]", "[]4[]18[]8[]8[]8[]push[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:#f6f7f9;");
        JLabel title = new JLabel("Corusco");
        title.putClientProperty(FlatClientProperties.STYLE, "font: 210% $semibold.font;");
        JLabel subtitle = new JLabel("<html>Operations-grade Swing presentation models.</html>");
        subtitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#5f6673;");
        panel.add(title, "growx, wrap");
        panel.add(subtitle, "growx, wrap");
        panel.add(metric("Generated metadata", "Forms, tables, resources"), "growx, wrap");
        panel.add(metric("Runtime bindings", "Commands, dialogs, status"), "growx, wrap");
        panel.add(metric("Data grids", "H2, Glazed Lists, state"), "growx, wrap");
        panel.add(metric("Hot renderers", "Readable epoch timestamps"), "growx, wrap");
        return panel;
    }

    static JPanel metric(String title, String detail) {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 12", "[grow]", "[][]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#ffffff;");
        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: $semibold.font;");
        JLabel detailLabel = new JLabel(detail);
        detailLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#69707d;");
        panel.add(titleLabel, "growx, wrap");
        panel.add(detailLabel, "growx");
        return panel;
    }

    static JPanel metricValue(String title, JLabel value, String detail) {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 14", "[grow]", "[][][]"));
        card(panel);
        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#5f6673; font: 90% $semibold.font;");
        value.putClientProperty(FlatClientProperties.STYLE, "font: 175% $semibold.font;");
        JLabel detailLabel = caption(detail);
        panel.add(titleLabel, "growx, wrap");
        panel.add(value, "growx, wrap");
        panel.add(detailLabel, "growx");
        return panel;
    }

    static void card(JComponent component) {
        component.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#ffffff;");
    }

    static String resolve(Resources resources, ResourceKey<String> key) {
        return resources.find(key).orElse(key.id());
    }

    static <R> void applyTableResources(
            JTable table,
            ObservableTableModel<R> model,
            Resources resources,
            BindingScope scope
    ) {
        table.setFont(table.getFont().deriveFont(11f));
        table.setRowHeight(22);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.putClientProperty(FlatClientProperties.STYLE,
                "selectionBackground:#d7ebff; selectionForeground:#111827;");
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(11f));
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:26; background:#f4f6f8; hoverBackground:#e9edf2;");
        for (int view = 0; view < table.getColumnModel().getColumnCount(); view++) {
            TableColumn column = table.getColumnModel().getColumn(view);
            int modelIndex = column.getModelIndex();
            if (modelIndex >= 0 && modelIndex < model.getColumnCount()) {
                column.setHeaderValue(resolve(resources, model.descriptor().column(modelIndex).descriptor().headerKey()));
            }
        }
        table.getTableHeader().repaint();
        scope.add(TableHeaderTooltipBinding.install(table, model, resources));
        scope.add(TableCellTooltipBinding.install(table, model, resources));
    }

    static <R> void applyDataSetTableResources(
            JTable table,
            DataSetFrameTableModel<R> model,
            Resources resources
    ) {
        table.setFont(table.getFont().deriveFont(11f));
        table.setRowHeight(22);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.putClientProperty(FlatClientProperties.STYLE,
                "selectionBackground:#d7ebff; selectionForeground:#111827;");
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(11f));
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:26; background:#f4f6f8; hoverBackground:#e9edf2;");
        for (int view = 0; view < table.getColumnModel().getColumnCount(); view++) {
            TableColumn column = table.getColumnModel().getColumn(view);
            int modelIndex = column.getModelIndex();
            if (modelIndex >= 0 && modelIndex < model.getColumnCount()) {
                column.setHeaderValue(dataSetHeader(model.column(modelIndex), resources));
            }
        }
        table.getTableHeader().repaint();
    }

    static <R> Binding installColumnRenderer(
            JTable table,
            ObservableTableModel<R> model,
            ColumnKey<R, ?> columnKey,
            TableCellRenderer renderer
    ) {
        int modelIndex = modelIndex(model, columnKey);
        int viewIndex = table.convertColumnIndexToView(modelIndex);
        if (viewIndex < 0) {
            throw new IllegalArgumentException("Column is not visible: " + columnKey);
        }
        TableColumn column = table.getColumnModel().getColumn(viewIndex);
        TableCellRenderer original = column.getCellRenderer();
        column.setCellRenderer(renderer);
        return () -> column.setCellRenderer(original);
    }

    static <R> Binding installDataSetColumnRenderer(
            JTable table,
            DataSetFrameTableModel<R> model,
            DataColumnKey<R, ?> columnKey,
            TableCellRenderer renderer
    ) {
        int modelIndex = dataSetModelIndex(model, columnKey);
        int viewIndex = table.convertColumnIndexToView(modelIndex);
        if (viewIndex < 0) {
            throw new IllegalArgumentException("Column is not visible: " + columnKey);
        }
        TableColumn column = table.getColumnModel().getColumn(viewIndex);
        TableCellRenderer original = column.getCellRenderer();
        column.setCellRenderer(renderer);
        return () -> column.setCellRenderer(original);
    }

    private static <R> int modelIndex(ObservableTableModel<R> model, ColumnKey<R, ?> columnKey) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            Column<R, ?> column = model.descriptor().column(i);
            if (column.key().equals(columnKey)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown column key: " + columnKey);
    }

    private static <R> int dataSetModelIndex(DataSetFrameTableModel<R> model, DataColumnKey<R, ?> columnKey) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.column(i).key().equals(columnKey)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown data column key: " + columnKey);
    }

    private static <R> String dataSetHeader(DataColumnDescriptor<R, ?> column, Resources resources) {
        String id = column.key().id() + "/header";
        return resources.find(ResourceKey.of(id, String.class)).orElse(title(column.sourceMemberName()));
    }

    private static String title(String memberName) {
        String kebab = memberName.replaceAll("(?<!^)([A-Z])", "-$1").toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        for (String part : kebab.split("-")) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(part.isEmpty()
                    ? part
                    : Character.toUpperCase(part.charAt(0)) + part.substring(1));
        }
        return result.toString();
    }
}
