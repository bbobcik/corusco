package cz.auderis.corusco.examples;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableHeaderTooltipBinding;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Demonstrates generated table header tooltips.
 *
 * <p>The scenario resolves column header tooltip text from generated table
 * metadata. It is a small reference for connecting descriptor resource keys to
 * Swing table header presentation.</p>
 */
public final class TableHeaderTooltipExample {

    private TableHeaderTooltipExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Installs header tooltip behavior and returns the hovered tooltip text.
     *
     * @return generated header tooltip text
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
                scope.add(TableHeaderTooltipBinding.install(table, model, resources));

                // The binding translates the visible header coordinate back to
                // the generated column descriptor before resolving resources.
                JTableHeader header = table.getTableHeader();
                mouseMoved(header, 5);
                result.set(List.of(header.getToolTipText()));
            }
            rows.close();
        });
        return result.get();
    }

    private static void mouseMoved(JTableHeader header, int x) {
        MouseEvent event = new MouseEvent(
                header,
                MouseEvent.MOUSE_MOVED,
                0L,
                0,
                x,
                5,
                0,
                false
        );
        header.getMouseMotionListeners()[header.getMouseMotionListeners().length - 1].mouseMoved(event);
    }
}
