package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.StandardChangeOrigin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadableListTest {

    @Test
    void loadsLazilyAndReusesCacheUntilDetached() {
        AtomicInteger loads = new AtomicInteger();
        LoadableList<String> list = LoadableList.of(() -> List.of("row-" + loads.incrementAndGet()));

        assertThat(list.isAttached()).isFalse();
        assertThat(loads).hasValue(0);

        assertThat(list.snapshot()).containsExactly("row-1");
        assertThat(list.get(0)).isEqualTo("row-1");
        assertThat(loads).hasValue(1);

        list.detach();

        assertThat(list.isAttached()).isFalse();
        assertThat(list.snapshot()).containsExactly("row-2");
        assertThat(loads).hasValue(2);
    }

    @Test
    void streamLoadsLazilyAndReusesAttachedCache() {
        AtomicInteger loads = new AtomicInteger();
        LoadableList<String> list = LoadableList.of(() -> List.of("row-" + loads.incrementAndGet()));

        assertThat(list.isAttached()).isFalse();
        assertThat(list.stream()).containsExactly("row-1");
        assertThat(list.stream()).containsExactly("row-1");

        assertThat(list.isAttached()).isTrue();
        assertThat(loads).hasValue(1);
    }

    @Test
    void mutationsLoadCacheAndPublishOrdinaryListChanges() {
        LoadableList<String> list = LoadableList.of(() -> List.of("alpha"));
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        list.add("beta");
        list.set(0, "ALPHA");
        list.remove(1);

        assertThat(list.snapshot()).containsExactly("ALPHA");
        assertThat(events).hasSize(3);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(1, List.of("beta")));
        assertThat(events.get(1).changes()).containsExactly(new ListChange.Replaced<>(0, "alpha", "ALPHA"));
        assertThat(events.get(2).changes()).containsExactly(new ListChange.Removed<>(1, List.of("beta")));
    }

    @Test
    void refreshPublishesResetOnlyWhenAttachedSnapshotChanges() {
        AtomicReference<List<String>> source = new AtomicReference<>(List.of("alpha", "beta"));
        LoadableList<String> list = LoadableList.of(source::get);
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        assertThat(list.snapshot()).containsExactly("alpha", "beta");
        assertThat(list.refresh()).containsExactly("alpha", "beta");
        source.set(List.of("gamma"));
        assertThat(list.refresh()).containsExactly("gamma");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Cleared<>(List.of("alpha", "beta")),
                new ListChange.Inserted<>(0, List.of("gamma"))
        );
    }

    @Test
    void refreshAndMutationsPublishSuppliedOrigins() {
        AtomicReference<List<String>> source = new AtomicReference<>(List.of("alpha"));
        LoadableList<String> list = LoadableList.of(source::get);
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        assertThat(list.snapshot()).containsExactly("alpha");
        list.add("beta", StandardChangeOrigin.USER);
        source.set(List.of("gamma"));
        list.refresh(StandardChangeOrigin.SYSTEM);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).origin()).isEqualTo(StandardChangeOrigin.USER);
        assertThat(events.get(0).changes()).containsExactly(new ListChange.Inserted<>(1, List.of("beta")));
        assertThat(events.get(1).origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
        assertThat(events.get(1).changes()).containsExactly(
                new ListChange.Cleared<>(List.of("alpha", "beta")),
                new ListChange.Inserted<>(0, List.of("gamma"))
        );
    }

    @Test
    void refreshWhileDetachedLoadsWithoutPublishingReset() {
        AtomicReference<List<String>> source = new AtomicReference<>(List.of("alpha"));
        LoadableList<String> list = LoadableList.of(source::get);
        List<ListChangeSet<String>> events = new ArrayList<>();
        list.subscribe(events::add);

        source.set(List.of("beta"));
        assertThat(list.refresh()).containsExactly("beta");

        assertThat(list.isAttached()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    void detachPreservesSubscribersForLaterMutations() {
        LoadableList<String> list = LoadableList.of(() -> List.of("alpha"));
        List<ListChangeSet<String>> events = new ArrayList<>();
        Subscription subscription = list.subscribe(events::add);

        assertThat(list.snapshot()).containsExactly("alpha");
        list.detach();
        list.add("beta");
        subscription.close();
        subscription.close();
        list.add("gamma");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(new ListChange.Inserted<>(1, List.of("beta")));
    }

    @Test
    void batchPassesStableWrapperAndDeliversOneChangeSet() {
        LoadableList<String> list = LoadableList.of(() -> List.of("alpha"));
        List<ListChangeSet<String>> events = new ArrayList<>();
        List<ObservableList<String>> suppliedLists = new ArrayList<>();
        list.subscribe(events::add);

        list.batch(batch -> {
            suppliedLists.add(batch);
            batch.add("beta");
            batch.set(0, "ALPHA");
        });

        assertThat(suppliedLists).containsExactly(list);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().changes()).containsExactly(
                new ListChange.Inserted<>(1, List.of("beta")),
                new ListChange.Replaced<>(0, "alpha", "ALPHA")
        );
    }

    @Test
    void nullLoadedElementsAreRejected() {
        LoadableList<String> list = LoadableList.of(LoadableListTest::singletonNull);

        assertThatThrownBy(list::snapshot)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void detachAllowsCachedRowsToBeGarbageCollectedWherePractical() {
        DetachedRows detached = detachedRows();

        assertThat(detached.list().isAttached()).isFalse();
        assertThat(awaitCleared(detached.reference())).isTrue();
    }

    private static DetachedRows detachedRows() {
        AtomicReference<List<Payload>> source = new AtomicReference<>(List.of(new Payload(new byte[256 * 1024])));
        LoadableList<Payload> list = LoadableList.of(source::get);
        Payload payload = list.get(0);
        WeakReference<Payload> reference = new WeakReference<>(payload);

        payload = null;
        source.set(List.of());
        list.detach();
        return new DetachedRows(list, reference);
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

    private static List<String> singletonNull() {
        List<String> result = new ArrayList<>();
        result.add(null);
        return result;
    }

    private record Payload(byte[] data) {
    }

    private record DetachedRows(LoadableList<Payload> list, WeakReference<Payload> reference) {
    }
}
