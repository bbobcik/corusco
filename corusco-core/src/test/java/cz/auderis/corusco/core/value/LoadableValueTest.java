package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Subscription;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoadableValueTest {

    @Test
    void loadsLazilyAndReusesCacheUntilDetached() {
        AtomicInteger loads = new AtomicInteger();
        LoadableValue<String> value = LoadableValue.of(() -> "loaded-" + loads.incrementAndGet());

        assertThat(value.isAttached()).isFalse();
        assertThat(loads).hasValue(0);

        assertThat(value.value()).isEqualTo("loaded-1");
        assertThat(value.value()).isEqualTo("loaded-1");
        assertThat(value.isAttached()).isTrue();
        assertThat(loads).hasValue(1);

        value.detach();

        assertThat(value.isAttached()).isFalse();
        assertThat(value.value()).isEqualTo("loaded-2");
        assertThat(loads).hasValue(2);
    }

    @Test
    void invalidateReleasesCacheWithoutEagerReload() {
        AtomicInteger loads = new AtomicInteger();
        LoadableValue<String> value = LoadableValue.of(() -> "loaded-" + loads.incrementAndGet());

        assertThat(value.value()).isEqualTo("loaded-1");

        value.invalidate();
        value.invalidate();

        assertThat(value.isAttached()).isFalse();
        assertThat(loads).hasValue(1);
        assertThat(value.value()).isEqualTo("loaded-2");
    }

    @Test
    void refreshReloadsAndNotifiesWhenEffectiveValueChanges() {
        AtomicReference<String> source = new AtomicReference<>("first");
        LoadableValue<String> value = LoadableValue.of(source::get);
        List<ValueChangeEvent<String>> events = new ArrayList<>();
        value.subscribe(events::add);

        assertThat(value.value()).isEqualTo("first");
        source.set("first");
        assertThat(value.refresh()).isEqualTo("first");
        source.set("second");
        assertThat(value.refresh()).isEqualTo("second");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().source()).isSameAs(value);
        assertThat(events.getFirst().oldValue()).isEqualTo("first");
        assertThat(events.getFirst().newValue()).isEqualTo("second");
        assertThat(events.getFirst().origin()).isEqualTo(ChangeOrigin.MODEL);
    }

    @Test
    void refreshOnDetachedValueLoadsWithoutChangeEvent() {
        AtomicInteger loads = new AtomicInteger();
        LoadableValue<String> value = LoadableValue.of(() -> "loaded-" + loads.incrementAndGet());
        List<String> observed = new ArrayList<>();
        value.subscribe(event -> observed.add(event.newValue()));

        assertThat(value.refresh()).isEqualTo("loaded-1");

        assertThat(value.isAttached()).isTrue();
        assertThat(observed).isEmpty();
    }

    @Test
    void closingSubscriptionStopsRefreshNotifications() {
        AtomicReference<String> source = new AtomicReference<>("first");
        LoadableValue<String> value = LoadableValue.of(source::get);
        List<String> observed = new ArrayList<>();
        Subscription subscription = value.subscribe(event -> observed.add(event.newValue()));

        assertThat(value.value()).isEqualTo("first");
        source.set("second");
        value.refresh();
        subscription.close();
        subscription.close();
        source.set("third");
        value.refresh();

        assertThat(observed).containsExactly("second");
    }

    @Test
    void detachAllowsCachedValueToBeGarbageCollectedWherePractical() {
        DetachedPayload detached = detachedPayload();

        assertThat(detached.value().isAttached()).isFalse();
        assertThat(awaitCleared(detached.reference())).isTrue();
    }

    private static DetachedPayload detachedPayload() {
        AtomicReference<Payload> source = new AtomicReference<>(new Payload(new byte[256 * 1024]));
        LoadableValue<Payload> value = LoadableValue.of(source::get);
        Payload payload = value.value();
        WeakReference<Payload> reference = new WeakReference<>(payload);

        payload = null;
        source.set(null);
        value.detach();
        return new DetachedPayload(value, reference);
    }

    private static boolean awaitCleared(WeakReference<?> reference) {
        for (int attempt = 0; attempt < 20 && reference.get() != null; attempt++) {
            System.gc();
            byte[] pressure = new byte[512 * 1024];
            pressure[0] = 1;
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return reference.get() == null;
    }

    private record Payload(byte[] data) {
    }

    private record DetachedPayload(LoadableValue<Payload> value, WeakReference<Payload> reference) {
    }
}
