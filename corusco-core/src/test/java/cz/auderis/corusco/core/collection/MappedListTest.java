package cz.auderis.corusco.core.collection;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappedListTest {

    @Test
    void exposesMappedSnapshotOfSourceElements() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3));
        MappedList<Integer, String> mapped = MappedList.of(source, value -> "v" + value);

        assertThat(mapped.size()).isEqualTo(3);
        assertThat(mapped.get(1)).isEqualTo("v2");
        assertThat(mapped.snapshot()).containsExactly("v1", "v2", "v3");
    }

    @Test
    void translatesInsertRemoveReplaceMoveAndBatchChanges() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2, 3));
        MappedList<Integer, String> mapped = MappedList.of(source, value -> "v" + value);
        List<ListChangeSet<String>> events = new ArrayList<>();
        mapped.subscribe(events::add);

        source.add(1, 4);
        source.remove(2);
        source.set(0, 5);
        source.move(2, 0);
        source.batch(batch -> {
            batch.add(6);
            batch.set(1, 7);
        });

        assertThat(mapped.snapshot()).containsExactly("v3", "v7", "v4", "v6");
        assertThat(events).hasSize(5);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(1, List.of("v4")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Removed<>(2, List.of("v2")));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Replaced<>(0, "v1", "v5"));
        assertThat(events.get(3).changes()).containsExactly(new ListChange.Moved<>(2, 0, "v3"));
        assertThat(events.get(4).changes()).containsExactly(
                new ListChange.Inserted<>(3, List.of("v6")),
                new ListChange.Replaced<>(1, "v5", "v7")
        );
    }

    @Test
    void clearPublishesMappedClear() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1, 2));
        MappedList<Integer, String> mapped = MappedList.of(source, value -> "v" + value);
        List<ListChangeSet<String>> events = new ArrayList<>();
        mapped.subscribe(events::add);

        source.clear();

        assertThat(mapped.snapshot()).isEmpty();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Cleared<>(List.of("v1", "v2")));
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(1));
        MappedList<Integer, String> mapped = MappedList.of(source, value -> "v" + value);
        List<ListChangeSet<String>> events = new ArrayList<>();
        mapped.subscribe(events::add);

        mapped.close();
        mapped.close();
        source.add(2);

        assertThat(mapped.snapshot()).containsExactly("v1");
        assertThat(events).isEmpty();
    }

    @Test
    void directMutationsFailFast() {
        MappedList<Integer, String> mapped = MappedList.of(ObservableArrayList.empty(), Object::toString);

        assertThatThrownBy(() -> mapped.add("v1"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
        assertThatThrownBy(mapped::clear)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
    }
}
