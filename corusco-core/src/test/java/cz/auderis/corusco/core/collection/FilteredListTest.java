package cz.auderis.corusco.core.collection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilteredListTest {

    @Test
    void exposesOnlyAcceptedSourceElements() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3, 4));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);

        assertThat(filtered.size()).isEqualTo(2);
        assertThat(filtered.get(0)).isEqualTo(2);
        assertThat(filtered.snapshot()).containsExactly(2, 4);
    }

    @Test
    void acceptsReadableCollectionSource() {
        ObservableSortedSet<Integer> source = ObservableSortedSet.of(List.of(4, 1, 2), Comparator.naturalOrder());
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);

        source.add(3);
        source.add(6);

        assertThat(filtered.snapshot()).containsExactly(2, 4, 6);
    }

    @Test
    void translatesVisibleInsertReplaceAndRemoveChanges() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3, 4));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        source.add(0, 6);
        source.add(2, 5);
        source.set(3, 8);
        source.set(1, 10);
        source.set(4, 7);
        source.set(5, 9);

        assertThat(filtered.snapshot()).containsExactly(6, 10, 8);
        assertThat(events).hasSize(4);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, List.of(6)));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(1, 2, 8));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Inserted<>(1, List.of(10)));
        assertThat(events.get(3).changes()).containsExactly(new ListChange.Removed<>(3, List.of(4)));
    }

    @Test
    void translatesVisibleMovesUsingFilteredIndices() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 4, 6, 3));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        source.move(3, 1);
        source.move(4, 0);

        assertThat(filtered.snapshot()).containsExactly(6, 2, 4);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Moved<>(2, 0, 6));
    }

    @Test
    void preservesVisibleChangesInsideSourceBatch() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        source.batch(batch -> {
            batch.add(4);
            batch.add(5);
            batch.set(0, 6);
        });

        assertThat(filtered.snapshot()).containsExactly(6, 2, 4);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Inserted<>(1, List.of(4)),
                new ListChange.Inserted<>(0, List.of(6))
        );
    }

    @Test
    void clearPublishesFilteredClearOnlyWhenVisibleElementsExist() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3, 4));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        source.clear();

        assertThat(filtered.snapshot()).isEmpty();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Cleared<>(List.of(2, 4)));
    }

    @Test
    void predicateReplacementPublishesReset() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3, 4));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        filtered.setPredicate(value -> value > 2);

        assertThat(filtered.snapshot()).containsExactly(3, 4);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Cleared<>(List.of(2, 4)),
                new ListChange.Inserted<>(0, List.of(3, 4))
        );
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2));
        FilteredList<Integer> filtered = FilteredList.of(source, value -> value % 2 == 0);
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        filtered.subscribe(events::add);

        filtered.close();
        filtered.close();
        source.add(4);

        assertThat(filtered.snapshot()).containsExactly(2);
        assertThat(events).isEmpty();
    }

    @Test
    void directMutationsFailFast() {
        FilteredList<Integer> filtered = FilteredList.of(ObservableArrayList.empty(), value -> true);

        assertThatThrownBy(() -> filtered.add(1))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
        assertThatThrownBy(() -> filtered.clear())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
    }
}
