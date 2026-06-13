package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDetailValueTest {

    @Test
    void loadsDetailLazilyAndReusesAttachedMasterCache() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        AtomicInteger loads = new AtomicInteger();
        MasterDetailValue<Integer, String> detail =
                MasterDetailValue.of(master, id -> "detail-" + id + "-" + loads.incrementAndGet());

        assertThat(detail.isAttached()).isFalse();
        assertThat(loads).hasValue(0);

        assertThat(detail.value()).isEqualTo("detail-1-1");
        assertThat(detail.value()).isEqualTo("detail-1-1");

        assertThat(detail.isAttached()).isTrue();
        assertThat(loads).hasValue(1);
    }

    @Test
    void activeMasterChangeReloadsAndNotifiesWithMasterOrigin() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailValue<Integer, String> detail = MasterDetailValue.of(master, id -> "detail-" + id);
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        detail.subscribe(events::add);

        assertThat(detail.value()).isEqualTo("detail-1");
        master.setValue(2, ChangeOrigin.USER);

        assertThat(detail.value()).isEqualTo("detail-2");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().source()).isSameAs(detail);
        assertThat(events.getFirst().oldValue()).isEqualTo("detail-1");
        assertThat(events.getFirst().newValue()).isEqualTo("detail-2");
        assertThat(events.getFirst().origin()).isEqualTo(ChangeOrigin.USER);
    }

    @Test
    void detachedMasterChangeDoesNotLoadUntilAccessed() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        AtomicInteger loads = new AtomicInteger();
        MasterDetailValue<Integer, String> detail =
                MasterDetailValue.of(master, id -> "detail-" + id + "-" + loads.incrementAndGet());
        List<String> observed = new ArrayList<>();
        detail.subscribe(event -> observed.add(event.newValue()));

        assertThat(detail.value()).isEqualTo("detail-1-1");
        detail.detach();
        master.setValue(2, ChangeOrigin.MODEL);

        assertThat(detail.isAttached()).isFalse();
        assertThat(loads).hasValue(1);
        assertThat(observed).isEmpty();
        assertThat(detail.value()).isEqualTo("detail-2-2");
    }

    @Test
    void refreshReloadsCurrentMasterAndNotifiesWhenAttachedValueChanges() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        AtomicInteger revision = new AtomicInteger();
        MasterDetailValue<Integer, String> detail =
                MasterDetailValue.of(master, id -> "detail-" + id + "-r" + revision.incrementAndGet());
        List<String> observed = new ArrayList<>();
        detail.subscribe(event -> observed.add(event.newValue()));

        assertThat(detail.value()).isEqualTo("detail-1-r1");
        assertThat(detail.refresh()).isEqualTo("detail-1-r2");

        assertThat(observed).containsExactly("detail-1-r2");
    }

    @Test
    void closedSubscriptionStopsDetailNotifications() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        MasterDetailValue<Integer, String> detail = MasterDetailValue.of(master, id -> "detail-" + id);
        List<String> observed = new ArrayList<>();
        Subscription subscription = detail.subscribe(event -> observed.add(event.newValue()));

        assertThat(detail.value()).isEqualTo("detail-1");
        master.setValue(2);
        subscription.close();
        subscription.close();
        master.setValue(3);

        assertThat(observed).containsExactly("detail-2");
    }

    @Test
    void closeRemovesMasterSubscriptionAndClearsListeners() {
        SimpleValue<Integer> master = SimpleValue.of(1);
        AtomicInteger loads = new AtomicInteger();
        MasterDetailValue<Integer, String> detail =
                MasterDetailValue.of(master, id -> "detail-" + id + "-" + loads.incrementAndGet());
        List<String> observed = new ArrayList<>();
        detail.subscribe(event -> observed.add(event.newValue()));

        assertThat(detail.value()).isEqualTo("detail-1-1");
        detail.close();
        detail.close();
        master.setValue(2);

        assertThat(detail.isAttached()).isFalse();
        assertThat(detail.value()).isNull();
        assertThat(detail.refresh()).isNull();
        assertThat(loads).hasValue(1);
        assertThat(observed).isEmpty();
        assertThat(detail.subscribe(event -> observed.add(event.newValue()))).isSameAs(Subscription.EMPTY);
    }
}
