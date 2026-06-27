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
