package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortedListTest {

    @Test
    void exposesSortedSnapshotOfSourceElements() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(3, 1, 2));
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());

        assertThat(sorted.size()).isEqualTo(3);
        assertThat(sorted.get(0)).isEqualTo(1);
        assertThat(sorted.snapshot()).containsExactly(1, 2, 3);
        assertThat(sorted.stream()).containsExactly(1, 2, 3);
    }

    @Test
    void acceptsReadableCollectionSource() {
        ObservableSortedSet<Integer> source = ObservableSortedSet.of(List.of(1, 2, 3), Comparator.naturalOrder());
        SortedList<Integer> sorted = SortedList.of(source, Comparator.reverseOrder());

        source.add(4);

        assertThat(sorted.snapshot()).containsExactly(4, 3, 2, 1);
    }

    @Test
    void sourceInsertRemoveReplaceMoveAndBatchPublishDeterministicResets() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(3, 1, 2));
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        sorted.subscribe(events::add);

        source.add(0, 0);
        source.remove(2);
        source.set(1, 4);
        source.move(2, 0);
        source.batch(batch -> {
            batch.add(5);
            batch.set(0, 6);
        });

        assertThat(sorted.snapshot()).containsExactly(0, 4, 5, 6);
        assertThat(events).hasSize(4);
        assertThat(events.get(0).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(1, 2, 3)),
                new ListChange.Inserted<>(0, List.of(0, 1, 2, 3))
        );
        assertThat(events.get(1).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(0, 1, 2, 3)),
                new ListChange.Inserted<>(0, List.of(0, 2, 3))
        );
        assertThat(events.get(2).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(0, 2, 3)),
                new ListChange.Inserted<>(0, List.of(0, 2, 4))
        );
        assertThat(events.get(3).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(0, 2, 4)),
                new ListChange.Inserted<>(0, List.of(0, 4, 5, 6))
        );
    }

    @Test
    void comparatorReplacementPublishesResetWhenOrderChanges() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(3, 1, 2));
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        sorted.subscribe(events::add);

        sorted.setComparator(Comparator.reverseOrder());

        assertThat(sorted.snapshot()).containsExactly(3, 2, 1);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Cleared<>(List.of(1, 2, 3)),
                new ListChange.Inserted<>(0, List.of(3, 2, 1))
        );
    }

    @Test
    void preservesSourceOriginAndUsesComparatorReplacementOrigin() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(2, 1));
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        sorted.subscribe(events::add);

        source.add(3, StandardChangeOrigin.USER);
        sorted.setComparator(Comparator.reverseOrder(), StandardChangeOrigin.GENERATED);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(0).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(1, 2)),
                new ListChange.Inserted<>(0, List.of(1, 2, 3))
        );
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.GENERATED);
        assertThat(events.get(1).changes()).containsExactly(
                new ListChange.Cleared<>(List.of(1, 2, 3)),
                new ListChange.Inserted<>(0, List.of(3, 2, 1))
        );
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(2, 1));
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        sorted.subscribe(events::add);

        sorted.close();
        sorted.close();
        source.add(0);

        assertThat(sorted.snapshot()).containsExactly(1, 2);
        assertThat(events).isEmpty();
    }

    @Test
    void directMutationsFailFast() {
        SortedList<Integer> sorted = SortedList.of(ObservableArrayList.<Integer>empty(), Comparator.naturalOrder());

        assertThatThrownBy(() -> sorted.add(1))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
        assertThatThrownBy(sorted::clear)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
    }
}
