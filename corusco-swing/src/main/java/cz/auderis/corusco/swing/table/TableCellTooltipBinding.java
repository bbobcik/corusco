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

/**
 * Resolves table body-cell tooltips from generated column descriptors.
 *
 * <p>The binding follows the table's current visible column order. It converts
 * mouse coordinates through Swing's row/column view indices before reading the
 * generated column descriptor, so reordered and temporarily hidden columns still
 * resolve the correct tooltip resource.</p>
 *
 * <p>The binding is EDT-confined and assumes the supplied table uses the
 * supplied {@link ObservableTableModel}. Closing removes the installed mouse
 * listeners and restores the table tooltip that was present before
 * installation.</p>
 *
 * @param <R> row type
 */
public final class TableCellTooltipBinding<R> implements Binding {

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final Resources resources;
    private final String originalTooltip;
    private final CellTooltipListener listener = new CellTooltipListener();
    private boolean closed;

    /**
     * Installs table body-cell tooltip behavior.
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param resources tooltip resources
     * @param <R> row type
     * @return binding
     */
    public static <R> TableCellTooltipBinding<R> install(
            JTable table,
            ObservableTableModel<R> model,
            Resources resources
    ) {
        return new TableCellTooltipBinding<>(table, model, resources);
    }

    /**
     * Creates and installs table body-cell tooltip behavior.
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param resources tooltip resources
     */
    public TableCellTooltipBinding(JTable table, ObservableTableModel<R> model, Resources resources) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.resources = Objects.requireNonNull(resources, "resources");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied model");
        }
        this.originalTooltip = table.getToolTipText();
        table.addMouseMotionListener(listener);
        table.addMouseListener(listener);
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        table.removeMouseMotionListener(listener);
        table.removeMouseListener(listener);
        table.setToolTipText(originalTooltip);
        closed = true;
    }

    private void updateTooltip(MouseEvent event) {
        if (closed) {
            return;
        }
        int viewRow = table.rowAtPoint(event.getPoint());
        int viewColumn = table.columnAtPoint(event.getPoint());
        if (viewRow < 0 || viewColumn < 0) {
            table.setToolTipText(null);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            table.setToolTipText(null);
            return;
        }
        if (modelColumn < 0 || modelColumn >= model.getColumnCount()) {
            table.setToolTipText(null);
            return;
        }

        ColumnDescriptor<R, ?> descriptor = model.descriptor().column(modelColumn).descriptor();
        ResourceKey<String> tooltipKey = descriptor.tooltipKey();
        String tooltip = (tooltipKey == null) ? null : resources.find(tooltipKey).orElse(null);
        table.setToolTipText(tooltip);
    }

    private final class CellTooltipListener extends MouseAdapter {

        @Override
        public void mouseMoved(MouseEvent event) {
            updateTooltip(event);
        }

        @Override
        public void mouseExited(MouseEvent event) {
            if (!closed) {
                table.setToolTipText(null);
            }
        }
    }
}
