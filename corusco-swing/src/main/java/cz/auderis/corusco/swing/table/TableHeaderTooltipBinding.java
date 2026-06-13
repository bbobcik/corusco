package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Resolves table header tooltips from generated column descriptors.
 *
 * <p>The binding follows the table's current visible column order. It converts
 * header view coordinates through Swing's {@link TableColumn#getModelIndex()}
 * before reading the descriptor, so reordered and temporarily hidden columns
 * still resolve the correct tooltip resource.</p>
 *
 * @param <R> row type
 */
public final class TableHeaderTooltipBinding<R> implements Binding {

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final Resources resources;
    private final JTableHeader header;
    private final String originalTooltip;
    private final HeaderTooltipListener listener = new HeaderTooltipListener();
    private boolean closed;

    /**
     * Installs table header tooltip behavior.
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param resources tooltip resources
     * @param <R> row type
     * @return binding
     */
    public static <R> TableHeaderTooltipBinding<R> install(
            JTable table,
            ObservableTableModel<R> model,
            Resources resources
    ) {
        return new TableHeaderTooltipBinding<>(table, model, resources);
    }

    /**
     * Creates and installs table header tooltip behavior.
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param resources tooltip resources
     */
    public TableHeaderTooltipBinding(JTable table, ObservableTableModel<R> model, Resources resources) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.resources = Objects.requireNonNull(resources, "resources");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied model");
        }
        this.header = Objects.requireNonNull(table.getTableHeader(), "tableHeader");
        this.originalTooltip = header.getToolTipText();
        header.addMouseMotionListener(listener);
        header.addMouseListener(listener);
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        header.removeMouseMotionListener(listener);
        header.removeMouseListener(listener);
        header.setToolTipText(originalTooltip);
        closed = true;
    }

    private void updateTooltip(MouseEvent event) {
        if (closed) {
            return;
        }
        int viewIndex = header.columnAtPoint(event.getPoint());
        if (viewIndex < 0) {
            header.setToolTipText(null);
            return;
        }

        TableColumn tableColumn = table.getColumnModel().getColumn(viewIndex);
        int modelIndex = tableColumn.getModelIndex();
        ColumnDescriptor<R, ?> descriptor = model.descriptor().column(modelIndex).descriptor();
        ResourceKey<String> tooltipKey = descriptor.tooltipKey();
        String tooltip = (tooltipKey == null) ? null : resources.find(tooltipKey).orElse(null);
        header.setToolTipText(tooltip);
    }

    private final class HeaderTooltipListener extends MouseAdapter {

        @Override
        public void mouseMoved(MouseEvent event) {
            updateTooltip(event);
        }

        @Override
        public void mouseExited(MouseEvent event) {
            if (!closed) {
                header.setToolTipText(null);
            }
        }
    }
}
