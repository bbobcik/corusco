package cz.auderis.corusco.core.collection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappedReadableCollectionTest {

    @Test
    void mapsSortedSetInSourceOrderAndTranslatesChanges() {
        ObservableSortedSet<Integer> source = ObservableSortedSet.of(List.of(3, 1), Comparator.naturalOrder());
        MappedReadableCollection<Integer, String> mapped = MappedReadableCollection.of(source, value -> "v" + value);
        List<ListChangeSet<String>> events = new ArrayList<>();
        mapped.subscribe(events::add);

        source.add(2);
        source.remove(1);
        source.clear();

        assertThat(mapped.snapshot()).isEmpty();
        assertThat(events).hasSize(3);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(1, List.of("v2")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Removed<>(0, List.of("v1")));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Cleared<>(List.of("v2", "v3")));
    }

    @Test
    void nullMappedElementsAreRejected() {
        ObservableSortedSet<Integer> source = ObservableSortedSet.of(List.of(1), Comparator.naturalOrder());

        assertThatThrownBy(() -> MappedReadableCollection.of(source, ignored -> null))
                .isInstanceOf(NullPointerException.class);

        MappedReadableCollection<Integer, String> mapped =
                MappedReadableCollection.of(source, value -> value == 2 ? null : "v" + value);
        assertThatThrownBy(() -> source.add(2))
                .isInstanceOf(NullPointerException.class);
        assertThat(mapped.snapshot()).containsExactly("v1");
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableSortedSet<Integer> source = ObservableSortedSet.of(List.of(1), Comparator.naturalOrder());
        MappedReadableCollection<Integer, String> mapped = MappedReadableCollection.of(source, value -> "v" + value);
        List<ListChangeSet<String>> events = new ArrayList<>();
        mapped.subscribe(events::add);

        mapped.close();
        mapped.close();
        source.add(2);

        assertThat(mapped.snapshot()).containsExactly("v1");
        assertThat(events).isEmpty();
    }
}
