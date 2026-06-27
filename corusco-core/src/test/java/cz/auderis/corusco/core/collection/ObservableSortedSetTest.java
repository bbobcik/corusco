package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservableSortedSetTest {

    @Test
    void exposesSortedUniqueSnapshotThroughReadableCollectionContract() {
        ObservableReadableCollection<Integer> set = ObservableSortedSet.of(
                List.of(3, 1, 2, 1),
                Comparator.naturalOrder()
        );

        assertThat(set.size()).isEqualTo(3);
        assertThat(set.snapshot()).containsExactly(1, 2, 3);
        assertThat(set.stream()).containsExactly(1, 2, 3);
    }

    @Test
    void comparatorDefinesUniqueness() {
        ObservableSortedSet<String> set = ObservableSortedSet.empty(String.CASE_INSENSITIVE_ORDER);
        List<ListChangeSet<String>> events = new ArrayList<>();
        set.subscribe(events::add);

        boolean firstAdded = set.add("alpha");
        boolean duplicateAdded = set.add("ALPHA");

        assertThat(firstAdded).isTrue();
        assertThat(duplicateAdded).isFalse();
        assertThat(set.contains("ALPHA")).isTrue();
        assertThat(set.snapshot()).containsExactly("alpha");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes())
                .containsExactly(new ListChange.Inserted<>(0, List.of("alpha")));
    }

    @Test
    void addRemoveAndClearPublishEventsInSortedCoordinates() {
        ObservableSortedSet<Integer> set = ObservableSortedSet.of(List.of(2, 4), Comparator.naturalOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        set.subscribe(events::add);

        set.add(1);
        set.add(3);
        set.remove(2);
        set.clear();
        set.clear();

        assertThat(set.snapshot()).isEmpty();
        assertThat(events).hasSize(4);
        assertThat(events.get(0).changes())
                .containsExactly(new ListChange.Inserted<>(0, List.of(1)));
        assertThat(events.get(1).changes())
                .containsExactly(new ListChange.Inserted<>(2, List.of(3)));
        assertThat(events.get(2).changes())
                .containsExactly(new ListChange.Removed<>(1, List.of(2)));
        assertThat(events.get(3).changes())
                .containsExactly(new ListChange.Cleared<>(List.of(1, 3, 4)));
    }

    @Test
    void eventIndicesFollowComparatorOrder() {
        ObservableSortedSet<Integer> set = ObservableSortedSet.of(List.of(4, 2), Comparator.reverseOrder());
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        set.subscribe(events::add);

        set.add(3);
        set.remove(2);

        assertThat(set.snapshot()).containsExactly(4, 3);
        assertThat(events.get(0).changes())
                .containsExactly(new ListChange.Inserted<>(1, List.of(3)));
        assertThat(events.get(1).changes())
                .containsExactly(new ListChange.Removed<>(2, List.of(2)));
    }

    @Test
    void removeUsesComparatorEquivalentStoredElement() {
        ObservableSortedSet<String> set = ObservableSortedSet.of(
                List.of("alpha", "beta"),
                String.CASE_INSENSITIVE_ORDER
        );
        List<ListChangeSet<String>> events = new ArrayList<>();
        set.subscribe(events::add);

        boolean removed = set.remove("ALPHA");

        assertThat(removed).isTrue();
        assertThat(set.snapshot()).containsExactly("beta");
        assertThat(events.getFirst().changes())
                .containsExactly(new ListChange.Removed<>(0, List.of("alpha")));
    }

    @Test
    void nullElementsAreRejectedEvenWhenComparatorAllowsThem() {
        Comparator<String> nullFriendlyComparator = Comparator.nullsFirst(String::compareTo);
        ObservableSortedSet<String> set = ObservableSortedSet.empty(nullFriendlyComparator);

        assertThatThrownBy(() -> set.add(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> set.contains(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> set.remove(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ObservableSortedSet.of(singletonNull(), nullFriendlyComparator))
                .isInstanceOf(NullPointerException.class);

        assertThat(set.snapshot()).isEmpty();
    }

    @Test
    void listViewExposesReadOnlyIndexedAccessAndSetEvents() {
        ObservableSortedSet<Integer> set = ObservableSortedSet.of(List.of(3, 1), Comparator.naturalOrder());
        ObservableList<Integer> view = set.asList();
        List<ListChangeSet<Integer>> events = new ArrayList<>();
        view.subscribe(events::add);

        set.add(2);

        assertThat(view.size()).isEqualTo(3);
        assertThat(view.get(0)).isEqualTo(1);
        assertThat(view.snapshot()).containsExactly(1, 2, 3);
        assertThat(view.stream()).containsExactly(1, 2, 3);
        assertThat(events.getFirst().changes())
                .containsExactly(new ListChange.Inserted<>(1, List.of(2)));
        assertThatThrownBy(() -> view.add(4))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
        assertThatThrownBy(() -> view.move(0, 1))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("read-only");
    }

    @Test
    void listenerRemovalDuringDispatchAffectsFutureEventsOnly() {
        ObservableSortedSet<Integer> set = ObservableSortedSet.empty(Comparator.naturalOrder());
        List<String> events = new ArrayList<>();
        Subscription[] first = new Subscription[1];
        first[0] = set.subscribe(changes -> {
            events.add("first:" + changes.changes().size());
            first[0].close();
        });
        set.subscribe(changes -> events.add("second:" + changes.changes().size()));

        set.add(1);
        set.add(2);

        assertThat(events).containsExactly("first:1", "second:1", "second:1");
    }

    private static List<String> singletonNull() {
        List<String> result = new ArrayList<>();
        result.add(null);
        return result;
    }
}
