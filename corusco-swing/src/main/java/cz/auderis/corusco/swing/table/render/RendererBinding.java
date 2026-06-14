package cz.auderis.corusco.swing.table.render;

import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Binding that restores the renderer replaced by an installer.
 *
 * <p>The binding captures only the restoration action. It does not own the
 * table, model, or renderer lifecycle beyond undoing the renderer mutation when
 * closed on the EDT.</p>
 */
final class RendererBinding implements Binding {

    private final Runnable restore;
    private boolean closed;

    private RendererBinding(Runnable restore) {
        this.restore = Objects.requireNonNull(restore, "restore");
    }

    static RendererBinding defaultRenderer(
            JTable table,
            Class<?> valueType,
            TableCellRenderer original
    ) {
        return new RendererBinding(() -> table.setDefaultRenderer(valueType, original));
    }

    static RendererBinding columnRenderer(TableColumn column, TableCellRenderer original) {
        return new RendererBinding(() -> column.setCellRenderer(original));
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        restore.run();
    }
}
