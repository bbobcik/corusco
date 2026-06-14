package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Binding that adds a column-visibility popup menu to a table header.
 *
 * <p>This class is the user-facing control for the column visibility part of
 * {@link TableStateController}. It listens for platform popup triggers on the
 * {@link JTableHeader}, rebuilds a {@link JPopupMenu} from the current captured
 * {@link ColumnState}, and delegates visibility changes back to the controller
 * so hidden {@link javax.swing.table.TableColumn} instances, visual order, and
 * persisted state stay consistent.</p>
 *
 * <p>The binding is EDT-confined. It owns only the header mouse listener and
 * transient menu items; it does not own the table, the model, or the controller.
 * The menu disables the last visible column item to avoid leaving the table
 * with no visible descriptor columns. Close the binding with the surrounding
 * view lifecycle to remove the listener.</p>
 *
 * @param <R> row type
 */
public final class TableHeaderColumnVisibilityMenu<R> implements Binding {

    private final JTable table;
    private final ObservableTableModel<R> model;
    private final TableStateController<R> controller;
    private final JTableHeader header;
    private final MouseAdapter popupListener = new PopupListener();
    private boolean closed;

    /**
     * Installs a visibility popup menu on a table header.
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param controller table state controller for the same table/model pair
     * @param <R> row type
     * @return installed menu binding
     */
    public static <R> TableHeaderColumnVisibilityMenu<R> install(
            JTable table,
            ObservableTableModel<R> model,
            TableStateController<R> controller
    ) {
        return new TableHeaderColumnVisibilityMenu<>(table, model, controller);
    }

    /**
     * Creates and installs a visibility popup menu.
     *
     * <p>The constructor adds the popup mouse listener immediately. The
     * controller and model must describe the same table instance because menu
     * actions use stable column ids captured from both collaborators.</p>
     *
     * @param table Swing table
     * @param model descriptor-backed table model installed in the table
     * @param controller table state controller for the same table/model pair
     * @throws IllegalStateException if called off the EDT
     * @throws IllegalArgumentException if the table does not use the supplied
     *         model
     */
    public TableHeaderColumnVisibilityMenu(
            JTable table,
            ObservableTableModel<R> model,
            TableStateController<R> controller
    ) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table, "table");
        this.model = Objects.requireNonNull(model, "model");
        this.controller = Objects.requireNonNull(controller, "controller");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied model");
        }
        this.header = Objects.requireNonNull(table.getTableHeader(), "tableHeader");
        this.header.addMouseListener(popupListener);
    }

    /**
     * Creates a fresh popup menu reflecting current table visibility.
     *
     * <p>The menu is rebuilt for each popup so item state follows changes made
     * by direct controller calls, restored state, or previous menu actions.</p>
     *
     * @return popup menu
     * @throws IllegalStateException if called off the EDT or after close
     */
    public JPopupMenu createMenu() {
        SwingEdt.requireEdt();
        requireOpen();
        Map<String, ColumnState> statesById = controller.captureState().columns().stream()
                .collect(Collectors.toMap(ColumnState::id, Function.identity()));
        long visibleCount = statesById.values().stream()
                .filter(ColumnState::visible)
                .count();

        JPopupMenu menu = new JPopupMenu();
        for (int modelIndex = 0; modelIndex < model.getColumnCount(); modelIndex++) {
            Column<R, ?> column = model.descriptor().column(modelIndex);
            String columnId = column.descriptor().persistence().id();
            ColumnState state = statesById.get(columnId);
            boolean visible = state == null || state.visible();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(model.getColumnName(modelIndex), visible);
            item.putClientProperty("corusco.columnId", columnId);
            item.setEnabled(!visible || visibleCount > 1);
            item.addActionListener(event -> controller.setColumnVisible(columnId, item.isSelected()));
            menu.add(item);
        }
        return menu;
    }

    /**
     * Removes the header popup listener.
     *
     * <p>The call must run on the EDT and is idempotent. Existing transient
     * popup menu instances are not tracked after they have been shown.</p>
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        header.removeMouseListener(popupListener);
        closed = true;
    }

    private void maybeShowPopup(MouseEvent event) {
        if (!event.isPopupTrigger() || closed) {
            return;
        }
        createMenu().show(event.getComponent(), event.getX(), event.getY());
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Table header visibility menu is closed");
        }
    }

    private final class PopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
    }
}
