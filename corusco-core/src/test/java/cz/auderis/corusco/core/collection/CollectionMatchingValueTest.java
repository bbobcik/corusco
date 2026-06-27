package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionMatchingValueTest {

    @Test
    void tracksFirstElementMatchingObservableKey() {
        Row first = new Row(1, "first");
        Row second = new Row(1, "second");
        ObservableArrayList<Row> source = ObservableArrayList.of(List.of(first, second, new Row(2, "other")));
        SimpleValue<Integer> key = SimpleValue.of(1);
        CollectionMatchingValue<Row, Integer> selected = CollectionMatchingValue.of(source, key, Row::id);
        List<ValueChangeEvent<Row>> events = new ArrayList<>();
        selected.subscribe(events::add);

        source.remove(0, StandardChangeOrigin.USER);
        key.setValue(null, StandardChangeOrigin.SYSTEM);
        source.add(0, first, StandardChangeOrigin.MODEL);
        key.setValue(1, StandardChangeOrigin.GENERATED);

        assertThat(selected.value()).isEqualTo(first);
        assertThat(events).hasSize(3);
        assertThat(events.get(0).oldValue()).isEqualTo(first);
        assertThat(events.get(0).newValue()).isEqualTo(second);
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(1).oldValue()).isEqualTo(second);
        assertThat(events.get(1).newValue()).isNull();
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
        assertThat(events.get(2).oldValue()).isNull();
        assertThat(events.get(2).newValue()).isEqualTo(first);
        assertThat(events.get(2).origin()).isEqualTo(StandardChangeOrigin.GENERATED);
    }

    @Test
    void missingKeyProducesNullAndEqualEffectiveValueIsSilent() {
        ObservableArrayList<Row> source = ObservableArrayList.of(List.of(new Row(1, "one")));
        SimpleValue<Integer> key = SimpleValue.of(2);
        CollectionMatchingValue<Row, Integer> selected = CollectionMatchingValue.of(source, key, Row::id);
        List<ValueChangeEvent<Row>> events = new ArrayList<>();
        selected.subscribe(events::add);

        source.add(new Row(3, "three"), StandardChangeOrigin.USER);
        key.setValue(1, StandardChangeOrigin.MODEL);

        assertThat(selected.value()).isEqualTo(new Row(1, "one"));
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().oldValue()).isNull();
        assertThat(events.getFirst().newValue()).isEqualTo(new Row(1, "one"));
        assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.MODEL);
    }

    @Test
    void closeStopsFutureEvents() {
        ObservableArrayList<Row> source = ObservableArrayList.of(List.of(new Row(1, "one")));
        SimpleValue<Integer> key = SimpleValue.of(1);
        CollectionMatchingValue<Row, Integer> selected = CollectionMatchingValue.of(source, key, Row::id);
        List<ValueChangeEvent<Row>> events = new ArrayList<>();
        selected.subscribe(events::add);

        selected.close();
        source.clear();
        key.setValue(null);

        assertThat(selected.value()).isEqualTo(new Row(1, "one"));
        assertThat(events).isEmpty();
    }

    private record Row(int id, String name) {
    }
}
