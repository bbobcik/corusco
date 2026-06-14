package cz.auderis.corusco.examples.collections;

import cz.auderis.corusco.core.collection.MappedList;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.collection.SortedList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Demonstrates mapped and sorted observable-list views.
 *
 * <p>The source list remains the mutation owner. Derived views subscribe to it
 * and expose read-only presentation shapes: a sorted row view and a mapped
 * label view. This keeps presenter storage, display ordering, and display text
 * concerns separate without runtime reflection or JavaBeans property paths.</p>
 */
public final class TransformedListExample {

    private TransformedListExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a transformed-list scenario.
     *
     * @return snapshots after each step
     */
    public static List<String> runScenario() {
        ObservableArrayList<CustomerRow> source = ObservableArrayList.of(List.of(
                new CustomerRow("Beta", 2),
                new CustomerRow("Alpha", 5)
        ));
        SortedList<CustomerRow> sorted = SortedList.of(source, Comparator.comparing(CustomerRow::name));
        MappedList<CustomerRow, String> labels = MappedList.of(source, row -> row.name() + ":" + row.orders());
        List<String> snapshots = new ArrayList<>();

        snapshots.add(names(sorted.snapshot()));
        snapshots.add(String.join(",", labels.snapshot()));

        source.add(new CustomerRow("Apex", 1));
        source.set(1, new CustomerRow("Delta", 6));
        snapshots.add(names(sorted.snapshot()));
        snapshots.add(String.join(",", labels.snapshot()));

        sorted.setComparator(Comparator.comparing(CustomerRow::orders).reversed());
        snapshots.add(names(sorted.snapshot()));

        sorted.close();
        labels.close();
        source.add(new CustomerRow("Closed", 99));
        snapshots.add(names(sorted.snapshot()));
        snapshots.add(String.join(",", labels.snapshot()));
        return snapshots;
    }

    private static String names(List<CustomerRow> rows) {
        List<String> names = new ArrayList<>();
        for (CustomerRow row : rows) {
            names.add(row.name());
        }
        return String.join(",", names);
    }

    /**
     * Example row type.
     *
     * @param name customer name
     * @param orders order count
     */
    public record CustomerRow(String name, int orders) {
    }
}
