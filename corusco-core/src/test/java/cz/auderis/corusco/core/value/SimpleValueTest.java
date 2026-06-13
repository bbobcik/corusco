package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleValueTest {

    @Test
    void settingDifferentValueNotifiesOnceWithEventDetails() {
        SimpleValue<String> value = SimpleValue.of("old");
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        value.subscribe(events::add);

        value.setValue("new", ChangeOrigin.USER);

        assertThat(events).hasSize(1);
        ValueChangeEvent<String> event = events.getFirst();
        assertThat(event.source()).isSameAs(value);
        assertThat(event.oldValue()).isEqualTo("old");
        assertThat(event.newValue()).isEqualTo("new");
        assertThat(event.origin()).isEqualTo(ChangeOrigin.USER);
    }

    @Test
    void settingEqualValueDoesNotNotify() {
        SimpleValue<String> value = SimpleValue.of("same");
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        value.subscribe(events::add);

        value.setValue("same", ChangeOrigin.MODEL);

        assertThat(events).isEmpty();
    }

    @Test
    void nullValuesAreSupported() {
        SimpleValue<String> value = SimpleValue.empty();
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        value.subscribe(events::add);

        value.setValue("present", ChangeOrigin.MODEL);
        value.setValue(null, ChangeOrigin.SYSTEM);
        value.setValue(null, ChangeOrigin.SYSTEM);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).oldValue()).isNull();
        assertThat(events.get(0).newValue()).isEqualTo("present");
        assertThat(events.get(1).oldValue()).isEqualTo("present");
        assertThat(events.get(1).newValue()).isNull();
    }

    @Test
    void closingSubscriptionStopsNotifications() {
        SimpleValue<Integer> value = SimpleValue.of(1);
        List<Integer> observed = new ArrayList<>();
        Subscription subscription = value.subscribe(event -> observed.add(event.newValue()));

        value.setValue(2);
        subscription.close();
        subscription.close();
        value.setValue(3);

        assertThat(observed).containsExactly(2);
    }

    @Test
    void removingListenerDuringDispatchDoesNotSkipUnrelatedListeners() {
        SimpleValue<Integer> value = SimpleValue.of(0);
        List<String> observed = new ArrayList<>();
        Subscription[] firstSubscription = new Subscription[1];
        firstSubscription[0] = value.subscribe(event -> {
            observed.add("first:" + event.newValue());
            firstSubscription[0].close();
        });
        value.subscribe(event -> observed.add("second:" + event.newValue()));

        value.setValue(1);
        value.setValue(2);

        assertThat(observed).containsExactly("first:1", "second:1", "second:2");
    }

    @Test
    void blankCustomOriginIsRejected() {
        assertThatThrownBy(() -> ChangeOrigin.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
    }
}
