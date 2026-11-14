package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.task.DefaultTaskService;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultCoruscoDataSourceTest {

    @Test
    void supersedingRequestSettlesFailedWhenPreviousCancellationThrows() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "old"));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        workers.runNext();
        CoruscoDataRequest accepted = source.acceptedRequest();
        source.refresh(StandardChangeOrigin.MODEL);
        tasks.busy().subscribe(event -> { if (!event.newValue()) throw new java.util.concurrent.RejectedExecutionException("cancel observer"); });
        assertThatThrownBy(() -> source.request(new CoruscoDataRange(2, 2), StandardChangeOrigin.USER))
                .isInstanceOf(java.util.concurrent.RejectedExecutionException.class).hasMessage("cancel observer");
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
        assertThat(source.rows().snapshot()).containsExactly(new Row(0, "old"));
        assertThat(source.acceptedRequest()).isEqualTo(accepted);
        assertThat(workers.queue).hasSize(1);
    }

    @Test
    void throwingLoadingListenerSettlesFailedWithLastAcceptedPage() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "old"));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        workers.runNext();
        CoruscoDataRequest accepted = source.acceptedRequest();
        source.status().subscribe(event -> { if (event.newValue().phase() == CoruscoDataPhase.LOADING)
            throw new IllegalStateException("loading observer"); });
        assertThatThrownBy(() -> source.request(new CoruscoDataRange(2, 2), StandardChangeOrigin.USER))
                .hasMessage("loading observer");
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
        assertThat(source.rows().snapshot()).containsExactly(new Row(0, "old"));
        assertThat(source.acceptedRequest()).isEqualTo(accepted);
        assertThat(workers.queue).isEmpty();
    }

    @Test
    void busyObserverRejectionFailsLoadWithoutLateReady() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "late"));
        tasks.busy().subscribe(event -> { if (event.newValue()) throw new java.util.concurrent.RejectedExecutionException("observer"); });
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
        assertThat(source.rows().snapshot()).isEmpty();
    }

    @Test
    void legacyScalarAccessorAcceptsNull() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request));
        List<Object> values = new ArrayList<>();
        source.range().subscribe(event -> values.add(event.newValue()));
        source.range().setValue(null, StandardChangeOrigin.USER);
        assertThat(source.range().value()).isNull();
        assertThat(values).containsExactly((Object) null);
        source.refresh(StandardChangeOrigin.MODEL);
        workers.runNext();
        assertThat(source.acceptedRequest().range()).isEqualTo(CoruscoDataRange.EMPTY);
    }

    @Test
    void equalPageRetainsRowsAndDirectQueryWriteSurvivesCompletion() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "same"));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        workers.runNext();
        Row original = source.rows().get(0);
        source.refresh(StandardChangeOrigin.MODEL);
        CoruscoDataQuery changed = new CoruscoDataQuery(List.of(), List.of(new CoruscoDataSort("id", CoruscoDataSort.Direction.ASCENDING, 0)));
        source.query().setValue(changed, StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(source.rows().get(0)).isSameAs(original);
        assertThat(source.query().value()).isEqualTo(changed);
        assertThat(source.acceptedRequest().query()).isEqualTo(CoruscoDataQuery.EMPTY);
    }

    @Test
    void closeThenRequestFromListenerDropsRequest() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "ok"));
        source.rows().subscribe(event -> { source.close(); source.request(new CoruscoDataRange(4, 2), StandardChangeOrigin.USER); });
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.CLOSED);
        assertThat(workers.queue).isEmpty();
    }

    @Test
    void terminalCallbackQueuedByThrowingListenerStillSettlesLoading() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "ok"));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        boolean[] first = {true};
        source.range().subscribe(event -> {
            if (first[0]) { first[0] = false; workers.runNext(); throw new IllegalStateException("listener"); }
        });
        assertThatThrownBy(() -> source.range().setValue(new CoruscoDataRange(3, 2), StandardChangeOrigin.USER))
                .hasMessage("listener");
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.READY);
    }

    @Test
    void closePublishesClosedWhenCancellationListenerThrows() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "late"));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        tasks.busy().subscribe(event -> { if (!event.newValue()) throw new IllegalStateException("cancel listener"); });
        assertThatThrownBy(source::close).hasMessage("cancel listener");
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.CLOSED);
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.CLOSED);
    }

    @Test
    void reentrantScalarWriteWaitsUntilAcceptedPageNotificationsComplete() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> new CoruscoDataPage<>(List.of(new Row(1, "ok")),
                        new CoruscoDataRange(5, 2), CoruscoDataCount.exact(7)));
        List<String> events = new ArrayList<>();
        source.rows().subscribe(event -> {
            source.range().setValue(new CoruscoDataRange(9, 2), StandardChangeOrigin.USER);
            events.add("rows:" + source.range().value().offset());
        });
        source.range().subscribe(event -> events.add("range:" + event.oldValue().offset() + ":"
                + event.newValue().offset() + ":" + event.origin() + ":" + source.range().value().offset()));
        source.status().subscribe(event -> { if (event.newValue().phase() == CoruscoDataPhase.READY)
            events.add("ready:" + source.range().value().offset()); });
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.MODEL);
        events.clear();
        workers.runNext();
        assertThat(events).containsExactly("rows:5", "range:0:5:MODEL:5", "ready:5", "range:5:9:USER:9");
    }

    @Test
    void closeCancelsQueuedLoadAndNeverPublishesReady() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "late"));
        List<CoruscoDataPhase> phases = new ArrayList<>();
        source.status().subscribe(event -> phases.add(event.newValue().phase()));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
        source.close();
        workers.runNext();
        assertThat(phases).containsExactly(CoruscoDataPhase.LOADING, CoruscoDataPhase.CLOSED);
        assertThat(source.rows().snapshot()).isEmpty();
    }

    @Test
    void legacyMutableAccessorsStillPublishTheirValuesAndOrigins() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "ok"));
        List<Object> changes = new ArrayList<>();
        source.range().subscribe(event -> { changes.add(event.newValue()); changes.add(event.origin()); });
        source.range().setValue(new CoruscoDataRange(9, 3), StandardChangeOrigin.USER);
        assertThat(source.range().value()).isEqualTo(new CoruscoDataRange(9, 3));
        assertThat(changes).containsExactly(new CoruscoDataRange(9, 3), StandardChangeOrigin.USER);
        source.refresh(StandardChangeOrigin.MODEL);
        workers.runNext();
        assertThat(source.acceptedRequest().range()).isEqualTo(new CoruscoDataRange(9, 3));
    }

    @Test
    void requestFromRowListenerStartsAfterAllCurrentNotifications() {
        ManualWorker workers = new ManualWorker();
        DefaultCoruscoDataSource<Row, Long> source = source(DefaultTaskService.of(workers, Runnable::run),
                (request, cancellation) -> page(request, "ok"));
        List<String> phases = new ArrayList<>();
        source.rows().subscribe(event -> {
            phases.add("rows:" + source.status().value().phase());
            source.request(new CoruscoDataRange(4, 2), StandardChangeOrigin.USER);
        });
        source.status().subscribe(event -> phases.add("status:" + event.newValue().phase()));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(phases).containsExactly("status:LOADING", "rows:READY", "status:READY", "status:LOADING");
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.READY);
        assertThat(source.acceptedRequest().range()).isEqualTo(new CoruscoDataRange(4, 2));
    }

    @Test
    void listenerExceptionDiscardsQueuedCloseBeforeNextRequest() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "ok"));
        var subscription = source.status().subscribe(event -> {
            source.close();
            throw new IllegalStateException("listener");
        });
        assertThatThrownBy(() -> source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER))
                .hasMessage("listener");
        subscription.close();
        source.request(new CoruscoDataRange(2, 2), StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.READY);
    }

    @Test
    void rejectedRefreshKeepsAcceptedPageAndCanRetry() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "ok"));
        CoruscoDataRange first = new CoruscoDataRange(0, 2);
        source.request(first, StandardChangeOrigin.USER);
        workers.runNext();
        CoruscoDataRequest accepted = source.acceptedRequest();
        workers.reject = true;
        source.request(new CoruscoDataRange(4, 2), StandardChangeOrigin.USER);
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
        assertThat(source.rows().snapshot()).containsExactly(new Row(0, "ok"));
        assertThat(source.acceptedRequest()).isEqualTo(accepted);
        workers.reject = false;
        source.refresh(StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.READY);
        assertThat(source.acceptedRequest().range()).isEqualTo(new CoruscoDataRange(4, 2));
    }

    @Test
    void reentrantCloseWaitsForPageNotificationsAndSuppressesLateCompletion() {
        ManualWorker workers = new ManualWorker();
        TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
        DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "ok"));
        List<String> phases = new ArrayList<>();
        source.rows().subscribe(event -> { phases.add("rows:" + source.status().value().phase()); source.close(); });
        source.status().subscribe(event -> phases.add("status:" + event.newValue().phase()));
        source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
        workers.runNext();
        assertThat(phases).containsExactly("status:LOADING", "rows:READY", "status:READY", "status:CLOSED");
        assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.CLOSED);
    }

    private static final class ManualWorker extends java.util.concurrent.AbstractExecutorService {
        private final java.util.Queue<Runnable> queue = new java.util.ArrayDeque<>();
        private boolean reject;
        private boolean shutdown;
        @Override public void execute(Runnable task) {
            if (reject || shutdown) throw new java.util.concurrent.RejectedExecutionException();
            queue.add(task);
        }
        void runNext() { queue.remove().run(); }
        @Override public void shutdown() { shutdown = true; }
        @Override public List<Runnable> shutdownNow() { shutdown = true; return List.copyOf(queue); }
        @Override public boolean isShutdown() { return shutdown; }
        @Override public boolean isTerminated() { return shutdown && queue.isEmpty(); }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    }

    @Test
    void rowListenerSeesCompleteAcceptedState() {
        ManualWorker workers = new ManualWorker();
            TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) ->
                    new CoruscoDataPage<>(List.of(new Row(1, "new")), new CoruscoDataRange(5, 2), CoruscoDataCount.exact(7)));
            List<Object> state = new ArrayList<>();
            source.rows().subscribe(event -> {
                state.add(source.range().value());
                state.add(source.totalCount().value());
                state.add(source.status().value().phase());
            });
            source.request(new CoruscoDataRange(0, 2), StandardChangeOrigin.USER);
            workers.runNext();
            assertThat(state).containsExactly(new CoruscoDataRange(5, 2), CoruscoDataCount.exact(7), CoruscoDataPhase.READY);
    }

    @Test
    void initialStateIsIdleAndEmpty() {
        try (TaskService tasks = TaskService.virtualThreads(Runnable::run)) {
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> page(request, "a"));

            assertThat(source.rows().snapshot()).isEmpty();
            assertThat(source.status().value()).isEqualTo(CoruscoDataStatus.INITIAL);
            assertThat(source.range().value()).isEqualTo(CoruscoDataRange.EMPTY);
            assertThat(source.query().value()).isEqualTo(CoruscoDataQuery.EMPTY);
            assertThat(source.totalCount().value()).isEqualTo(CoruscoDataCount.UNKNOWN);
        }
    }

    @Test
    void successfulRequestReplacesRowsWithOneResetEventAndPropagatesOrigin() throws Exception {
        try (TaskService tasks = TaskService.virtualThreads(Runnable::run)) {
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, new CoruscoDataLoader<>() {
                private int calls;

                @Override
                public CoruscoDataPage<Row> load(
                        CoruscoDataRequest request,
                        cz.auderis.corusco.core.task.CancellationToken cancellation
                ) {
                    calls++;
                    return calls == 1 ? page(request, "a", "b") : page(request, "c");
                }
            });
            List<ListChangeSet<Row>> events = new ArrayList<>();
            source.rows().subscribe(events::add);

            source.request(new CoruscoDataRange(20, 5), StandardChangeOrigin.USER);

            awaitRows(source, List.of(new Row(0, "a"), new Row(1, "b")));
            assertThat(source.rows().snapshot()).containsExactly(new Row(0, "a"), new Row(1, "b"));
            assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.READY);
            assertThat(source.totalCount().value()).isEqualTo(CoruscoDataCount.exact(2));
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().origin()).isEqualTo(StandardChangeOrigin.USER);
            assertThat(events.getFirst().changes()).containsExactly(
                    new ListChange.Inserted<>(0, List.of(new Row(0, "a"), new Row(1, "b")))
            );

            source.refresh(StandardChangeOrigin.USER);

            awaitRows(source, List.of(new Row(0, "c")));
            assertThat(events).hasSize(2);
            assertThat(events.get(1).changes()).containsExactly(
                    new ListChange.Cleared<>(List.of(new Row(0, "a"), new Row(1, "b"))),
                    new ListChange.Inserted<>(0, List.of(new Row(0, "c")))
            );
        }
    }

    @Test
    void failureKeepsOldRowsVisibleAndRecordsStatusError() throws Exception {
        try (TaskService tasks = TaskService.virtualThreads(Runnable::run)) {
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, new CoruscoDataLoader<>() {
                private int calls;

                @Override
                public CoruscoDataPage<Row> load(CoruscoDataRequest request, cz.auderis.corusco.core.task.CancellationToken cancellation) {
                    calls++;
                    if (calls == 1) {
                        return page(request, "old");
                    }
                    throw new IllegalStateException("backend down");
                }
            });

            source.request(new CoruscoDataRange(0, 10), StandardChangeOrigin.MODEL);
            awaitRows(source, List.of(new Row(0, "old")));
            source.refresh(StandardChangeOrigin.MODEL);
            awaitPhase(source, CoruscoDataPhase.FAILED);

            assertThat(source.rows().snapshot()).containsExactly(new Row(0, "old"));
            assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.FAILED);
            assertThat(source.status().value().error().message()).isEqualTo("backend down");
        }
    }

    @Test
    void staleCompletionDoesNotReplaceNewerPage() throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
            CountDownLatch firstStarted = new CountDownLatch(1);
            CountDownLatch releaseFirst = new CountDownLatch(1);
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> {
                if (request.refreshGeneration() == 1L) {
                    firstStarted.countDown();
                    releaseFirst.await(2, TimeUnit.SECONDS);
                    return page(request, "stale");
                }
                return page(request, "fresh");
            });

            source.request(new CoruscoDataRange(0, 10), StandardChangeOrigin.MODEL);
            assertThat(firstStarted.await(2, TimeUnit.SECONDS)).isTrue();
            source.request(new CoruscoDataRange(10, 10), StandardChangeOrigin.MODEL);
            releaseFirst.countDown();

            awaitRows(source, List.of(new Row(0, "fresh")));
            assertThat(source.range().value()).isEqualTo(new CoruscoDataRange(10, 10));
            assertThat(source.status().value().refreshGeneration()).isEqualTo(2L);
            tasks.close();
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void closeCancelsAndRejectsFutureRequests() throws Exception {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        try {
            TaskService tasks = DefaultTaskService.of(workers, Runnable::run);
            CountDownLatch started = new CountDownLatch(1);
            DefaultCoruscoDataSource<Row, Long> source = source(tasks, (request, cancellation) -> {
                started.countDown();
                Thread.sleep(10_000L);
                return page(request, "late");
            });

            source.request(new CoruscoDataRange(0, 10), StandardChangeOrigin.MODEL);
            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            source.close();

            assertThat(source.status().value().phase()).isEqualTo(CoruscoDataPhase.CLOSED);
            assertThatThrownBy(() -> source.refresh(StandardChangeOrigin.MODEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
            tasks.close();
        } finally {
            workers.shutdownNow();
        }
    }

    private static DefaultCoruscoDataSource<Row, Long> source(TaskService tasks, CoruscoDataLoader<Row> loader) {
        return new DefaultCoruscoDataSource<>(
                CoruscoRowIdentity.of(Row.class, Long.class, Row::id),
                loader,
                tasks
        );
    }

    private static CoruscoDataPage<Row> page(CoruscoDataRequest request, String... names) {
        List<Row> rows = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            rows.add(new Row(i, names[i]));
        }
        return new CoruscoDataPage<>(rows, request.range(), CoruscoDataCount.exact(rows.size()));
    }

    private static void awaitRows(DefaultCoruscoDataSource<Row, Long> source, List<Row> expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (source.rows().snapshot().equals(expected)) {
                return;
            }
            Thread.sleep(10L);
        }
        assertThat(source.rows().snapshot()).isEqualTo(expected);
    }

    private static void awaitPhase(DefaultCoruscoDataSource<Row, Long> source, CoruscoDataPhase expected)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            CoruscoDataStatus status = source.status().value();
            if (status != null && status.phase() == expected) {
                return;
            }
            Thread.sleep(10L);
        }
        assertThat(source.status().value().phase()).isEqualTo(expected);
    }

    private record Row(long id, String name) {
    }
}
