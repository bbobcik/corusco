package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionItemValueTest {

    @Test
    void tracksCollectionItemAtObservableIndex() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a", "b"));
        SimpleValue<Integer> index = SimpleValue.of(1);
        CollectionItemValue<String> item = CollectionItemValue.of(source, index);
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        item.subscribe(events::add);

        source.set(1, "B", StandardChangeOrigin.USER);
        index.setValue(5, StandardChangeOrigin.SYSTEM);
        source.add("c", StandardChangeOrigin.MODEL);
        index.setValue(0, StandardChangeOrigin.GENERATED);

        assertThat(item.value()).isEqualTo("a");
        assertThat(events).hasSize(3);
        assertThat(events.get(0).oldValue()).isEqualTo("b");
        assertThat(events.get(0).newValue()).isEqualTo("B");
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(1).oldValue()).isEqualTo("B");
        assertThat(events.get(1).newValue()).isNull();
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
        assertThat(events.get(2).oldValue()).isNull();
        assertThat(events.get(2).newValue()).isEqualTo("a");
        assertThat(events.get(2).origin()).isEqualTo(StandardChangeOrigin.GENERATED);
    }

    @Test
    void nullAndOutOfRangeIndexRepresentNoItem() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a"));
        SimpleValue<Integer> index = SimpleValue.empty();
        CollectionItemValue<String> item = CollectionItemValue.of(source, index);
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        item.subscribe(events::add);

        index.setValue(-1);
        index.setValue(0, StandardChangeOrigin.USER);
        source.clear(StandardChangeOrigin.SYSTEM);

        assertThat(item.value()).isNull();
        assertThat(events).hasSize(2);
        assertThat(events.get(0).newValue()).isEqualTo("a");
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(1).oldValue()).isEqualTo("a");
        assertThat(events.get(1).newValue()).isNull();
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
    }

    @Test
    void closeStopsFutureEventsAndSubscriptions() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a"));
        SimpleValue<Integer> index = SimpleValue.of(0);
        CollectionItemValue<String> item = CollectionItemValue.of(source, index);
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        item.subscribe(events::add);

        item.close();
        item.close();
        item.subscribe(events::add);
        source.set(0, "A");
        index.setValue(null);

        assertThat(item.value()).isEqualTo("a");
        assertThat(events).isEmpty();
    }
}
