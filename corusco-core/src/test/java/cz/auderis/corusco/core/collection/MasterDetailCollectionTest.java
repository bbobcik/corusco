package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterDetailCollectionTest {

    @Test
    void exposesInitialDetailSnapshotAndSwitchesOnMasterChange() {
        ObservableArrayList<String> first = ObservableArrayList.of(List.of("a", "b"));
        ObservableArrayList<String> second = ObservableArrayList.of(List.of("c"));
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(
                master,
                Map.of(1, first, 2, second)::get
        );
        List<ListChangeSet<String>> events = new ArrayList<>();
        details.subscribe(events::add);

        master.setValue(2, StandardChangeOrigin.USER);

        assertThat(details.snapshot()).containsExactly("c");
        assertThat(details.stream()).containsExactly("c");
        assertThat(details.size()).isEqualTo(1);
        assertThat(details.get(0)).isEqualTo("c");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Cleared<>(List.of("a", "b")),
                new ListChange.Inserted<>(0, List.of("c"))
        );
    }

    @Test
    void nullDetailSourceMeansEmptyCollection() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(master, ignored -> null);

        assertThat(details.size()).isZero();
        assertThat(details.snapshot()).isEmpty();
        assertThat(details.stream()).isEmpty();
        assertThatThrownBy(() -> details.get(0))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void activeDetailChangesAreForwardedWithTheirOrigin() {
        ObservableArrayList<String> first = ObservableArrayList.of(List.of("a"));
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(master, ignored -> first);
        List<ListChangeSet<String>> events = new ArrayList<>();
        details.subscribe(events::add);

        first.add("b", StandardChangeOrigin.SYSTEM);

        assertThat(details.snapshot()).containsExactly("a", "b");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Inserted<>(1, List.of("b")));
    }

    @Test
    void masterSwitchToNullDetailPublishesClearOnly() {
        ObservableArrayList<String> first = ObservableArrayList.of(List.of("a"));
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(
                master,
                value -> value == null ? null : first
        );
        List<ListChangeSet<String>> events = new ArrayList<>();
        details.subscribe(events::add);

        master.setValue(null, StandardChangeOrigin.GENERATED);

        assertThat(details.snapshot()).isEmpty();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.GENERATED);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Cleared<>(List.of("a")));
    }

    @Test
    void equalSnapshotsOnMasterSwitchAreSilent() {
        ObservableArrayList<String> first = ObservableArrayList.of(List.of("a"));
        ObservableArrayList<String> second = ObservableArrayList.of(List.of("a"));
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(
                master,
                value -> value == 1 ? first : second
        );
        List<ListChangeSet<String>> events = new ArrayList<>();
        details.subscribe(events::add);

        master.setValue(2, StandardChangeOrigin.USER);

        assertThat(details.snapshot()).containsExactly("a");
        assertThat(events).isEmpty();
    }

    @Test
    void closeStopsMasterAndDetailEvents() {
        ObservableArrayList<String> first = ObservableArrayList.of(List.of("a"));
        ObservableArrayList<String> second = ObservableArrayList.of(List.of("b"));
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailCollection<Integer, String> details = MasterDetailCollection.of(
                master,
                value -> value == 1 ? first : second
        );
        List<ListChangeSet<String>> events = new ArrayList<>();
        details.subscribe(events::add);

        details.close();
        details.close();
        details.subscribe(events::add);
        first.add("ignored");
        master.setValue(2);

        assertThat(details.snapshot()).isEmpty();
        assertThat(events).isEmpty();
    }
}
