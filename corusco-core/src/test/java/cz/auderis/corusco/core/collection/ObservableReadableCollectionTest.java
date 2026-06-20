package cz.auderis.corusco.core.collection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservableReadableCollectionTest {

    @Test
    void observableListCanBeConsumedThroughReadableCollectionContract() {
        ObservableArrayList<String> list = ObservableArrayList.of(List.of("a"));
        ObservableReadableCollection<String> readable = list;
        List<ListChangeSet<String>> events = new ArrayList<>();

        readable.subscribe(events::add);
        list.add("b");

        assertThat(readable.size()).isEqualTo(2);
        assertThat(readable.get(1)).isEqualTo("b");
        assertThat(readable.snapshot()).containsExactly("a", "b");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes())
                .containsExactly(new ListChange.Inserted<>(1, List.of("b")));
    }

    @Test
    void observableSortedSetCanBeConsumedThroughReadableCollectionContract() {
        ObservableReadableCollection<Integer> readable = ObservableSortedSet.of(
                List.of(3, 1, 2),
                Comparator.naturalOrder()
        );

        assertThat(readable.size()).isEqualTo(3);
        assertThat(readable.get(0)).isEqualTo(1);
        assertThat(readable.snapshot()).containsExactly(1, 2, 3);
    }
}
