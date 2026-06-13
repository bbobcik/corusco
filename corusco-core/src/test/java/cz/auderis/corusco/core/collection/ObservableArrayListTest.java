package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservableArrayListTest {

    @Test
    void reportsPreciseSingleChanges() {
        ObservableArrayList<String> list = ObservableArrayList.of(List.of("b", "c"));
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        list.add(0, "a");
        list.set(1, "B");
        list.move(2, 0);
        list.remove(1);
        list.clear();

        assertThat(events).hasSize(5);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, List.of("a")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(1, "b", "B"));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Moved<>(2, 0, "c"));
        assertThat(events.get(3).changes()).containsExactly(new ListChange.Removed<>(1, List.of("a")));
        assertThat(events.get(4).changes()).containsExactly(new ListChange.Cleared<>(List.of("c", "B")));
        assertThat(list.snapshot()).isEmpty();
    }

    @Test
    void preservesBatchChangeOrder() {
        ObservableArrayList<String> list = ObservableArrayList.empty();
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        list.batch(batch -> {
            batch.add("a");
            batch.add("b");
            batch.set(1, "B");
        });

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Inserted<>(0, List.of("a")),
                new ListChange.Inserted<>(1, List.of("b")),
                new ListChange.Replaced<>(1, "b", "B")
        );
        assertThat(list.snapshot()).containsExactly("a", "B");
    }

    @Test
    void listenerRemovalDuringDispatchAffectsFutureEventsOnly() {
        ObservableArrayList<String> list = ObservableArrayList.empty();
        List<String> events = new ArrayList<>();
        Subscription[] first = new Subscription[1];
        first[0] = list.subscribe(changes -> {
            events.add("first:" + changes.changes().size());
            first[0].close();
        });
        list.subscribe(changes -> events.add("second:" + changes.changes().size()));

        list.add("a");
        list.add("b");

        assertThat(events).containsExactly("first:1", "second:1", "second:1");
    }

    @Test
    void snapshotIsImmutable() {
        ObservableArrayList<String> list = ObservableArrayList.of(List.of("a"));

        List<String> snapshot = list.snapshot();

        assertThatThrownBy(() -> snapshot.add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(list.snapshot()).containsExactly("a");
    }

    @Test
    void nullElementsAreReportedConsistently() {
        ObservableArrayList<String> list = ObservableArrayList.empty();
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        list.add(null);
        String removed = list.remove(0);

        assertThat(removed).isNull();
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(0, singletonNull()));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Removed<>(0, singletonNull()));
    }

    @Test
    void noOpMoveAndEmptyClearDoNotEmitEvents() {
        ObservableArrayList<String> list = ObservableArrayList.of(List.of("a"));
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        list.move(0, 0);
        list.clear();
        list.clear();

        assertThat(events).containsExactly(new ListChangeSet<>(List.of(new ListChange.Cleared<>(List.of("a")))));
    }

    private static List<String> singletonNull() {
        List<String> result = new ArrayList<>();
        result.add(null);
        return result;
    }
}
