package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EdtObservableListTest {

    @Test
    void deliversBackgroundSourceChangesOnEdt() throws InterruptedException {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        CountDownLatch delivered = new CountDownLatch(1);
        AtomicBoolean deliveredOnEdt = new AtomicBoolean();
        AtomicReference<ListChangeSet<String>> deliveredChanges = new AtomicReference<>();
        edtList.subscribe(changes -> {
            deliveredOnEdt.set(SwingUtilities.isEventDispatchThread());
            deliveredChanges.set(changes);
            delivered.countDown();
        });

        Thread worker = new Thread(
                () -> source.add("background", StandardChangeOrigin.USER),
                "corusco-edt-list-test"
        );
        worker.start();
        worker.join();

        assertThat(delivered.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(deliveredOnEdt).isTrue();
        assertThat(deliveredChanges.get().changes())
                .containsExactly(new ListChange.Inserted<>(0, List.of("background")));
        assertThat(deliveredChanges.get().origin()).isEqualTo(StandardChangeOrigin.USER);
        edtList.close();
    }

    @Test
    void deliversSynchronouslyWhenSourceChangesOnEdt() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.empty();
            EdtObservableList<String> edtList = EdtObservableList.of(source);
            List<ListChangeSet<String>> events = new ArrayList<>();
            edtList.subscribe(events::add);

            source.add("same-thread");

            assertThat(events).hasSize(1);
            assertThat(events.getFirst().changes())
                    .containsExactly(new ListChange.Inserted<>(0, List.of("same-thread")));
            edtList.close();
        });
    }

    @Test
    void preservesBatchedSourceChangeSet() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.empty();
            EdtObservableList<String> edtList = EdtObservableList.of(source);
            List<ListChangeSet<String>> events = new ArrayList<>();
            edtList.subscribe(events::add);

            edtList.batch(StandardChangeOrigin.SYSTEM, list -> {
                list.add("a");
                list.add("b");
                list.set(1, "B");
            });

            assertThat(source.snapshot()).containsExactly("a", "B");
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().changes()).containsExactly(
                    new ListChange.Inserted<>(0, List.of("a")),
                    new ListChange.Inserted<>(1, List.of("b")),
                    new ListChange.Replaced<>(1, "b", "B")
            );
            assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.SYSTEM);
            edtList.close();
        });
    }

    @Test
    void closeSuppressesQueuedEventsAndRemovesSourceSubscription() throws InterruptedException {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        CountDownLatch delivered = new CountDownLatch(1);
        edtList.subscribe(changes -> delivered.countDown());

        SwingEdt.runAndWait(() -> {
            Thread worker = new Thread(() -> source.add("queued"), "corusco-edt-list-close-test");
            worker.start();
            join(worker);
            edtList.close();
        });
        SwingEdt.runAndWait(() -> {
        });
        source.add("after-close");
        SwingEdt.runAndWait(() -> {
        });

        assertThat(delivered.getCount()).isOne();
        assertThat(source.snapshot()).containsExactly("queued", "after-close");
    }

    @Test
    void allowsObservableListModelToObserveBackgroundChangesOnEdt() throws InterruptedException {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        AtomicReference<ObservableListModel<String>> model = new AtomicReference<>();
        CountDownLatch delivered = new CountDownLatch(1);
        AtomicBoolean deliveredOnEdt = new AtomicBoolean();
        AtomicReference<EventRecord> eventRecord = new AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            model.set(ObservableListModel.of(edtList));
            model.get().addListDataListener(new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent event) {
                    deliveredOnEdt.set(SwingUtilities.isEventDispatchThread());
                    eventRecord.set(EventRecord.from(event));
                    delivered.countDown();
                }

                @Override
                public void intervalRemoved(ListDataEvent event) {
                }

                @Override
                public void contentsChanged(ListDataEvent event) {
                }
            });
        });

        Thread worker = new Thread(() -> source.add("row"), "corusco-edt-list-model-test");
        worker.start();
        worker.join();

        assertThat(delivered.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(deliveredOnEdt).isTrue();
        assertThat(eventRecord.get()).isEqualTo(new EventRecord(ListDataEvent.INTERVAL_ADDED, 0, 0));
        SwingEdt.runAndWait(() -> model.get().close());
        edtList.close();
    }

    @Test
    void readableWrapperDeliversBackgroundSourceChangesOnEdt() throws InterruptedException {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        EdtObservableReadableCollection<String> edtCollection = EdtObservableReadableCollection.of(source);
        CountDownLatch delivered = new CountDownLatch(1);
        AtomicBoolean deliveredOnEdt = new AtomicBoolean();
        AtomicReference<ListChangeSet<String>> deliveredChanges = new AtomicReference<>();
        edtCollection.subscribe(changes -> {
            deliveredOnEdt.set(SwingUtilities.isEventDispatchThread());
            deliveredChanges.set(changes);
            delivered.countDown();
        });

        Thread worker = new Thread(() -> source.add("background"), "corusco-edt-readable-test");
        worker.start();
        worker.join();

        assertThat(delivered.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(deliveredOnEdt).isTrue();
        assertThat(deliveredChanges.get().changes())
                .containsExactly(new ListChange.Inserted<>(0, List.of("background")));
        assertThat(edtCollection.get(0)).isEqualTo("background");
        edtCollection.close();
    }

    @Test
    void wrappersDelegateStreamReadsToSource() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("a", "b"));
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        EdtObservableReadableCollection<String> edtCollection = EdtObservableReadableCollection.of(source);

        assertThat(edtList.stream()).containsExactly("a", "b");
        assertThat(edtCollection.stream()).containsExactly("a", "b");

        edtList.close();
        edtCollection.close();
    }

    private record EventRecord(int type, int index0, int index1) {

        static EventRecord from(ListDataEvent event) {
            return new EventRecord(event.getType(), event.getIndex0(), event.getIndex1());
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for worker thread", e);
        }
    }

    @Test
    void edtReadsMatchDeliveredEventsWhileBackgroundProducerIsAhead() throws Exception {
        ObservableArrayList<Integer> source = ObservableArrayList.empty();
        EdtObservableList<Integer> edtList = EdtObservableList.of(source);
        assertEdtReadsFollowDeliveredEvents(source, edtList, edtList::size, edtList::get, edtList::snapshot);
        edtList.close();
    }

    @Test
    void readableWrapperEdtReadsMatchDeliveredEventsWhileBackgroundProducerIsAhead() throws Exception {
        ObservableArrayList<Integer> source = ObservableArrayList.empty();
        EdtObservableReadableCollection<Integer> edtCollection = EdtObservableReadableCollection.of(source);
        assertEdtReadsFollowDeliveredEvents(
                source,
                edtCollection,
                edtCollection::size,
                edtCollection::get,
                edtCollection::snapshot
        );
        edtCollection.close();
    }

    @Test
    void edtMutationsInterleavedWithQueuedBackgroundChangesStayOrdered() throws Exception {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        CountDownLatch backgroundDone = new CountDownLatch(1);
        List<String> mirroredByEvents = new ArrayList<>();
        edtList.subscribe(changes -> changes.changes().forEach(change -> {
            if (change instanceof ListChange.Inserted<String> inserted) {
                mirroredByEvents.addAll(inserted.index(), inserted.elements());
            }
        }));

        SwingEdt.runAndWait(() -> {
            Thread worker = new Thread(() -> {
                source.add("background");
                backgroundDone.countDown();
            }, "corusco-edt-list-order-test");
            worker.start();
            try {
                assertThat(backgroundDone.await(2, TimeUnit.SECONDS)).isTrue();
                worker.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            // The background change is still queued for the EDT here.
            edtList.add("edt");
            assertThat(edtList.snapshot()).containsExactly("background", "edt");
        });
        SwingEdt.runAndWait(() -> assertThat(mirroredByEvents).containsExactly("background", "edt"));
        edtList.close();
    }

    private static void assertEdtReadsFollowDeliveredEvents(
            ObservableArrayList<Integer> source,
            cz.auderis.corusco.core.collection.ObservableReadableCollection<Integer> wrapper,
            java.util.function.IntSupplier size,
            java.util.function.IntFunction<Integer> get,
            java.util.function.Supplier<List<Integer>> snapshot
    ) throws Exception {
        int total = 2_000;
        List<Integer> delivered = new ArrayList<>();
        List<String> mismatches = new java.util.concurrent.CopyOnWriteArrayList<>();
        CountDownLatch allDelivered = new CountDownLatch(1);
        wrapper.subscribe(changes -> {
            for (ListChange<Integer> change : changes.changes()) {
                if (change instanceof ListChange.Inserted<Integer> inserted) {
                    delivered.addAll(inserted.index(), inserted.elements());
                }
            }
            if (size.getAsInt() != delivered.size()) {
                mismatches.add("size " + size.getAsInt() + " after " + delivered.size() + " delivered");
            } else if (get.apply(delivered.size() - 1).intValue() != delivered.getLast()) {
                mismatches.add("last element differs at " + delivered.size());
            }
            if (delivered.size() == total) {
                allDelivered.countDown();
            }
        });

        Thread producer = new Thread(() -> {
            for (int i = 0; i < total; i++) {
                source.add(i);
            }
        }, "corusco-edt-list-producer");
        producer.start();
        producer.join();

        assertThat(allDelivered.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(mismatches).isEmpty();
        SwingEdt.runAndWait(() -> assertThat(snapshot.get()).isEqualTo(delivered));
    }
}
