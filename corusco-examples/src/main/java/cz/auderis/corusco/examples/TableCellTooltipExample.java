package cz.auderis.corusco.examples;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableCellTooltipBinding;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;

/**
 * Demonstrates generated table cell tooltips.
 */
public final class TableCellTooltipExample {

    private TableCellTooltipExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs cell tooltip behavior and returns the hovered tooltip text.
     *
     * @return generated cell tooltip text
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
                    new GeneratedCustomerRow("Acme", 2)
            )));
            GlazedObservableList<GeneratedCustomerRow> rows = GlazedListsAdapters.observableList(eventList);
            Resources resources = Resources.of(Map.of(
                    GeneratedCustomerRowTableResources.NAME_TOOLTIP.id(), "Customer display name"
            ));

            JTable table = new JTable();
            try (BindingScope scope = new BindingScope()) {
                ObservableTableModel<GeneratedCustomerRow> model =
                        GeneratedCustomerRowTableBindings.installModel(table, rows, scope);
                scope.add(TableCellTooltipBinding.install(table, model, resources));

                // Generated table bindings install descriptor-backed columns;
                // the tooltip binding uses that descriptor instead of string
                // property paths when resolving the column resource key.
                configureTable(table);

                // Swing reports the hovered cell in view coordinates. The
                // binding converts the column back to the generated model
                // index before looking up the tooltip resource.
                mouseMoved(table, 5, 5);
                result.set(List.of(table.getToolTipText()));
            }
            rows.close();
        });
        return result.get();
    }

    private static void configureTable(JTable table) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setWidth(75);
            table.getColumnModel().getColumn(i).setPreferredWidth(75);
        }
        table.setRowHeight(20);
        table.setSize(150, 20);
    }

    private static void mouseMoved(JTable table, int x, int y) {
        MouseEvent event = new MouseEvent(
                table,
                MouseEvent.MOUSE_MOVED,
                0L,
                0,
                x,
                y,
                0,
                false
        );
        for (var listener : table.getMouseMotionListeners()) {
            listener.mouseMoved(event);
        }
    }
}
