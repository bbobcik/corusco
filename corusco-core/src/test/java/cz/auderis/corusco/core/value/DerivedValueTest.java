package cz.auderis.corusco.core.value;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DerivedValueTest {

    @Test
    void derivedValueUpdatesWhenDependencyChanges() {
        SimpleValue<String> first = SimpleValue.of("Ada");
        SimpleValue<String> last = SimpleValue.of("Lovelace");
        DerivedValue<String> fullName = DerivedValue.of(
                () -> first.value() + " " + last.value(),
                first,
                last
        );
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        fullName.subscribe(events::add);

        first.setValue("Grace", ChangeOrigin.USER);

        assertThat(fullName.value()).isEqualTo("Grace Lovelace");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().oldValue()).isEqualTo("Ada Lovelace");
        assertThat(events.getFirst().newValue()).isEqualTo("Grace Lovelace");
        assertThat(events.getFirst().origin()).isEqualTo(ChangeOrigin.USER);
    }

    @Test
    void derivedValueDoesNotNotifyWhenComputedValueIsEqual() {
        SimpleValue<Integer> number = SimpleValue.of(1);
        DerivedValue<String> parity = DerivedValue.of(
                () -> number.value() % 2 == 0 ? "even" : "odd",
                number
        );
        List<String> observed = new ArrayList<>();
        parity.subscribe(event -> observed.add(event.newValue()));

        number.setValue(3, ChangeOrigin.MODEL);
        number.setValue(4, ChangeOrigin.MODEL);

        assertThat(observed).containsExactly("even");
    }

    @Test
    void closingDerivedValueStopsDependencyNotifications() {
        SimpleValue<Integer> number = SimpleValue.of(1);
        DerivedValue<Integer> doubled = DerivedValue.of(() -> number.value() * 2, number);
        List<Integer> observed = new ArrayList<>();
        doubled.subscribe(event -> observed.add(event.newValue()));

        doubled.close();
        doubled.close();
        number.setValue(2);

        assertThat(doubled.value()).isEqualTo(2);
        assertThat(observed).isEmpty();
    }

    @Test
    void mappedValueUpdatesFromSource() {
        SimpleValue<String> name = SimpleValue.of("corusco");
        MappedValue<String, Integer> length = MappedValue.of(name, value -> value == null ? 0 : value.length());
        List<Integer> observed = new ArrayList<>();
        length.subscribe(event -> observed.add(event.newValue()));

        name.setValue("core", ChangeOrigin.GENERATED);
        name.setValue(null, ChangeOrigin.SYSTEM);

        assertThat(length.value()).isZero();
        assertThat(observed).containsExactly(4, 0);
    }

    @Test
    void mappedValueEventsUseMappedValueAsSource() {
        SimpleValue<String> name = SimpleValue.of("corusco");
        MappedValue<String, Integer> length = MappedValue.of(name, String::length);
        List<ValueChangeEvent<Integer>> events = new ArrayList<>();
        length.subscribe(events::add);

        name.setValue("core", ChangeOrigin.MODEL);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().source()).isSameAs(length);
    }
}
