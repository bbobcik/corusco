package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlazedObservableListTest {

    @Test
    void exposesWrappedEventListContents() {
        BasicEventList<String> source = eventListOf("a", "b");
        GlazedObservableList<String> adapter = GlazedListsAdapters.observableList(source);

        assertThat(adapter.size()).isEqualTo(2);
        assertThat(adapter.get(1)).isEqualTo("b");
        assertThat(adapter.snapshot()).containsExactly("a", "b");
        assertThat(adapter.source()).isSameAs(source);
    }

    @Test
    void translatesExternalEventListChanges() {
        BasicEventList<String> source = eventListOf("b", "c");
        GlazedObservableList<String> adapter = GlazedObservableList.of(source);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        source.add(0, "a");
        source.set(1, "B");
        source.remove(2);

        assertThat(adapter.snapshot()).containsExactly("a", "B");
        assertThat(events).hasSize(3);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, List.of("a")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(1, "b", "B"));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Removed<>(2, List.of("c")));
    }

    @Test
    void delegatesAdapterMutationsToEventList() {
        BasicEventList<String> source = eventListOf("b", "c");
        GlazedObservableList<String> adapter = GlazedObservableList.of(source);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        adapter.add(0, "a");
        adapter.set(1, "B");
        adapter.move(2, 0);
        adapter.remove(1);

        assertThat(source).containsExactly("c", "B");
        assertThat(events).hasSize(4);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, List.of("a")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(1, "b", "B"));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Moved<>(2, 0, "c"));
        assertThat(events.get(3).changes()).containsExactly(new ListChange.Removed<>(1, List.of("a")));
    }

    @Test
    void reorderingEventsAreReportedAsReset() {
        BasicEventList<String> source = eventListOf("b", "a", "c");
        SortedList<String> sorted = SortedList.create(source);
        GlazedObservableList<String> adapter = GlazedObservableList.of(sorted);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        sorted.setComparator(Comparator.reverseOrder());

        assertThat(adapter.snapshot()).containsExactly("c", "b", "a");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Cleared<>(List.of("a", "b", "c")),
                new ListChange.Inserted<>(0, List.of("c", "b", "a"))
        );
    }

    @Test
    void batchDeliversOneCoruscoChangeSet() {
        BasicEventList<String> source = eventListOf("b", "c");
        GlazedObservableList<String> adapter = GlazedObservableList.of(source);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        adapter.batch(list -> {
            list.add(0, "a");
            list.set(1, "B");
            list.move(2, 0);
        });

        assertThat(source).containsExactly("c", "a", "B");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Inserted<>(0, List.of("a")),
                new ListChange.Replaced<>(1, "b", "B"),
                new ListChange.Moved<>(2, 0, "c")
        );
    }

    @Test
    void closeStopsFutureCoruscoEventsWithoutDisposingSource() {
        BasicEventList<String> source = eventListOf("a");
        GlazedObservableList<String> adapter = GlazedObservableList.of(source);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        adapter.close();
        adapter.close();
        source.add("b");

        assertThat(source).containsExactly("a", "b");
        assertThat(adapter.snapshot()).containsExactly("a", "b");
        assertThat(events).isEmpty();
    }

    @Test
    void supportsNullElements() {
        BasicEventList<String> source = new BasicEventList<>();
        GlazedObservableList<String> adapter = GlazedObservableList.of(source);
        List<ListChangeSet<String>> events = new ArrayList<>();
        adapter.subscribe(events::add);

        adapter.add(null);
        adapter.set(0, "value");

        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, singletonNull()));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(0, null, "value"));
    }

    private static List<String> singletonNull() {
        List<String> result = new ArrayList<>();
        result.add(null);
        return result;
    }

    @SafeVarargs
    private static <E> BasicEventList<E> eventListOf(E... elements) {
        return new BasicEventList<>(new ArrayList<>(List.of(elements)));
    }
}
