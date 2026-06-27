package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionDerivedValueTest {

    @Test
    void derivesScalarFromCollectionSnapshot() {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        CollectionDerivedValue<String, Integer> size = CollectionDerivedValue.size(source);
        List<ValueChangeEvent<Integer>> events = new ArrayList<>();
        size.subscribe(events::add);

        source.add("a", StandardChangeOrigin.USER);
        source.add("b", StandardChangeOrigin.SYSTEM);

        assertThat(size.value()).isEqualTo(2);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).oldValue()).isEqualTo(0);
        assertThat(events.get(0).newValue()).isEqualTo(1);
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(1).oldValue()).isEqualTo(1);
        assertThat(events.get(1).newValue()).isEqualTo(2);
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
    }

    @Test
    void equalDerivedValueIsSilent() {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        CollectionDerivedValue<String, Boolean> hasRows =
                CollectionDerivedValue.of(source, snapshot -> !snapshot.isEmpty());
        List<ValueChangeEvent<Boolean>> events = new ArrayList<>();
        hasRows.subscribe(events::add);

        source.add("a", StandardChangeOrigin.USER);
        source.add("b", StandardChangeOrigin.SYSTEM);

        assertThat(hasRows.value()).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().oldValue()).isFalse();
        assertThat(events.getFirst().newValue()).isTrue();
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.USER);
    }

    @Test
    void derivesScalarFromCollectionStream() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a"));
        CollectionDerivedValue<String, Boolean> hasLongValue =
                CollectionDerivedValue.anyMatch(source, value -> value.length() > 1);
        List<ValueChangeEvent<Boolean>> events = new ArrayList<>();
        hasLongValue.subscribe(events::add);

        source.add("bb", StandardChangeOrigin.SYSTEM);
        source.add("cc", StandardChangeOrigin.USER);

        assertThat(hasLongValue.value()).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().oldValue()).isFalse();
        assertThat(events.getFirst().newValue()).isTrue();
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
    }

    @Test
    void streamMapperDoesNotRequireSnapshot() {
        StreamOnlyCollection source = new StreamOnlyCollection(List.of("a", "bb"));
        CollectionDerivedValue<String, Integer> maxLength = CollectionDerivedValue.fromStream(
                source,
                stream -> stream.mapToInt(String::length).max().orElse(0)
        );

        assertThat(maxLength.value()).isEqualTo(2);
    }

    @Test
    void streamFactoriesCoverCommonPredicatesAndCount() {
        ObservableArrayList<Integer> source = ObservableArrayList.of(List.of(2, 4));

        assertThat(CollectionDerivedValue.count(source).value()).isEqualTo(2L);
        assertThat(CollectionDerivedValue.allMatch(source, value -> value % 2 == 0).value()).isTrue();
        assertThat(CollectionDerivedValue.noneMatch(source, value -> value < 0).value()).isTrue();
    }

    @Test
    void snapshotFactoryReturnsImmutableSnapshots() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a"));
        CollectionDerivedValue<String, List<String>> snapshotValue = CollectionDerivedValue.snapshot(source);

        assertThat(snapshotValue.value()).containsExactly("a");
        assertThatThrownBy(() -> snapshotValue.value().add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mapperReceivesImmutableSnapshot() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a"));

        assertThatThrownBy(() -> CollectionDerivedValue.of(source, snapshot -> {
            snapshot.add("b");
            return snapshot.size();
        })).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        CollectionDerivedValue<String, Boolean> empty = CollectionDerivedValue.isEmpty(source);
        List<ValueChangeEvent<Boolean>> events = new ArrayList<>();
        empty.subscribe(events::add);

        empty.close();
        source.add("a");

        assertThat(empty.value()).isTrue();
        assertThat(events).isEmpty();
    }

    private record StreamOnlyCollection(List<String> elements) implements ObservableReadableCollection<String> {

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public String get(int index) {
            return elements.get(index);
        }

        @Override
        public List<String> snapshot() {
            throw new AssertionError("snapshot must not be used");
        }

        @Override
        public Stream<String> stream() {
            return elements.stream();
        }

        @Override
        public Subscription subscribe(ListChangeListener<String> listener) {
            return Subscription.EMPTY;
        }
    }
}
