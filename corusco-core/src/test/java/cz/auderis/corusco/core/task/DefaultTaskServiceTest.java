package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.value.ValueChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultTaskServiceTest {

    @Test
    void closeCancelsAllTasksEvenWhenCancellationCallbacksThrow() {
        ManualWorker workers = new ManualWorker();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<String> callbacks = new ArrayList<>();
        TaskHandle<String> first = service.submit(c -> "first", new TaskCallbacks<String>() {
            @Override public void cancelled() { callbacks.add("first"); throw new IllegalStateException("first failure"); }
        });
        TaskHandle<String> second = service.submit(c -> "second", new TaskCallbacks<String>() {
            @Override public void cancelled() { callbacks.add("second"); throw new IllegalArgumentException("second failure"); }
        });
        assertThatThrownBy(service::close).isInstanceOf(IllegalStateException.class)
                .hasMessage("first failure")
                .satisfies(error -> assertThat(error.getSuppressed()).singleElement()
                        .isInstanceOf(IllegalArgumentException.class)
                        .satisfies(suppressed -> assertThat(suppressed).hasMessage("second failure")));
        assertThat(callbacks).containsExactly("first", "second");
        assertThat(first.completion()).isCompletedExceptionally();
        assertThat(second.completion()).isCompletedExceptionally();
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void closeBeforeAcceptanceOverridesEarlySuccess() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService workers = new java.util.concurrent.AbstractExecutorService() {
            @Override public void execute(Runnable task) {
                task.run(); ran.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            }
            @Override public void shutdown() { }
            @Override public List<Runnable> shutdownNow() { return List.of(); }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        };
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            var submission = caller.submit(() -> service.submit(c -> "early"));
            assertThat(ran.await(2, TimeUnit.SECONDS)).isTrue();
            service.close(); release.countDown();
            TaskHandle<String> handle = submission.get(2, TimeUnit.SECONDS);
            assertThatThrownBy(() -> handle.completion().join()).isInstanceOf(CancellationException.class);
        } finally { release.countDown(); caller.shutdownNow(); }
    }

    @Test
    void closeDuringRejectedSubmissionDoesNotDeliverCancellation() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService workers = new java.util.concurrent.AbstractExecutorService() {
            @Override public void execute(Runnable task) {
                entered.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
                throw new java.util.concurrent.RejectedExecutionException();
            }
            @Override public void shutdown() { }
            @Override public List<Runnable> shutdownNow() { return List.of(); }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        };
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        ExecutorService caller = Executors.newSingleThreadExecutor();
        List<String> callbacks = new java.util.concurrent.CopyOnWriteArrayList<>();
        try {
            var submission = caller.submit(() -> service.submit(c -> "never", new TaskCallbacks<String>() {
                @Override public void cancelled() { callbacks.add("cancelled"); }
            }));
            assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
            service.close(); release.countDown();
            assertThatThrownBy(() -> submission.get(2, TimeUnit.SECONDS)).hasCauseInstanceOf(java.util.concurrent.RejectedExecutionException.class);
            assertThat(callbacks).isEmpty();
            assertThat(service.busy().value()).isFalse();
        } finally { release.countDown(); caller.shutdownNow(); }
    }

    @Test
    void throwingBusyStartListenerDoesNotStrandAcceptedTask() {
        ManualWorker workers = new ManualWorker();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<String> callbacks = new ArrayList<>();
        service.busy().subscribe(event -> { if (event.newValue()) throw new IllegalStateException("busy"); });
        TaskHandle<String> handle = service.submit(c -> "ok", TaskCallbacks.onSuccess(callbacks::add));
        workers.runNext();
        assertThatThrownBy(() -> handle.completion().join()).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(callbacks).isEmpty();
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void busyObserverRejectedExecutionExceptionIsAcceptedTaskFailure() {
        ManualWorker workers = new ManualWorker();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<Throwable> failures = new ArrayList<>();
        service.busy().subscribe(event -> { if (event.newValue()) throw new java.util.concurrent.RejectedExecutionException("observer"); });
        TaskHandle<String> handle = service.submit(c -> "late", new TaskCallbacks<String>() {
            @Override public void failed(Throwable error) { failures.add(error); }
        });
        workers.runNext();
        assertThat(handle.completion()).isCompletedExceptionally();
        assertThat(failures).singleElement().isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void failureCallbackRejectionDoesNotEscapeAcceptedSubmission() {
        ManualWorker workers = new ManualWorker();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        service.busy().subscribe(event -> { if (event.newValue()) throw new IllegalStateException("observer"); });
        TaskHandle<String> handle = service.submit(c -> "late", new TaskCallbacks<String>() {
            @Override public void failed(Throwable error) { throw new java.util.concurrent.RejectedExecutionException("callback"); }
        });
        assertThatThrownBy(() -> handle.completion().join())
                .hasCauseInstanceOf(java.util.concurrent.RejectedExecutionException.class);
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void rejectedExecutionExceptionFromCallbackDoesNotFinishTwice() {
        ManualWorker workers = new ManualWorker();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        TaskHandle<String> first = service.submit(c -> "first", TaskCallbacks.onSuccess(result -> {
            throw new java.util.concurrent.RejectedExecutionException("callback");
        }));
        TaskHandle<String> second = service.submit(c -> "second");
        workers.runNext();
        assertThat(first.completion()).isCompletedExceptionally();
        assertThat(service.busy().value()).isTrue();
        workers.runNext();
        assertThat(second.completion().join()).isEqualTo("second");
        assertThat(service.busy().value()).isFalse();
    }

    private static final class ManualWorker extends java.util.concurrent.AbstractExecutorService {
        private final java.util.Queue<Runnable> queue = new java.util.ArrayDeque<>();
        @Override public void execute(Runnable task) { queue.add(task); }
        void runNext() { queue.remove().run(); }
        @Override public void shutdown() { }
        @Override public List<Runnable> shutdownNow() { return List.copyOf(queue); }
        @Override public boolean isShutdown() { return false; }
        @Override public boolean isTerminated() { return false; }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
    }

    @Test
    void closeWhileWorkerSubmissionIsBlockedCancelsAcceptedTask() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService workers = new java.util.concurrent.AbstractExecutorService() {
            @Override public void execute(Runnable task) {
                entered.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            }
            @Override public void shutdown() { }
            @Override public List<Runnable> shutdownNow() { return List.of(); }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        };
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        ExecutorService caller = Executors.newSingleThreadExecutor();
        List<String> callbacks = new java.util.concurrent.CopyOnWriteArrayList<>();
        try {
            var submission = caller.submit(() -> service.submit(c -> "never", new TaskCallbacks<String>() {
                @Override public void cancelled() { callbacks.add("cancelled"); }
            }));
            assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
            service.close();
            release.countDown();
            TaskHandle<String> handle = submission.get(2, TimeUnit.SECONDS);
            assertThat(handle.isDone()).isTrue();
            assertThatThrownBy(() -> handle.completion().join()).isInstanceOf(CancellationException.class);
            assertThat(callbacks).containsExactly("cancelled");
            assertThat(service.busy().value()).isFalse();
        } finally { release.countDown(); caller.shutdownNow(); }
    }

    @Test
    void inlineTaskClosingServiceIsCancelled() {
        ExecutorService workers = new java.util.concurrent.AbstractExecutorService() {
            @Override public void execute(Runnable task) { task.run(); }
            @Override public void shutdown() { }
            @Override public List<Runnable> shutdownNow() { return List.of(); }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        };
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<String> callbacks = new ArrayList<>();
        TaskHandle<String> handle = service.submit(c -> { service.close(); return "late"; }, new TaskCallbacks<String>() {
            @Override public void cancelled() { callbacks.add("cancelled"); }
        });
        assertThatThrownBy(() -> handle.completion().join()).isInstanceOf(CancellationException.class);
        assertThat(callbacks).containsExactly("cancelled");
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void inlineWorkerFinishesAfterSubmissionIsAccountedFor() {
        ExecutorService workers = new java.util.concurrent.AbstractExecutorService() {
            @Override public void execute(Runnable task) { task.run(); }
            @Override public void shutdown() { }
            @Override public List<Runnable> shutdownNow() { return List.of(); }
            @Override public boolean isShutdown() { return false; }
            @Override public boolean isTerminated() { return false; }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        };
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<Boolean> events = new ArrayList<>();
        service.busy().subscribe(event -> events.add(event.newValue()));
        TaskHandle<String> handle = service.submit(c -> "done");
        assertThat(handle.completion().join()).isEqualTo("done");
        assertThat(events).containsExactly(true, false);
        assertThat(service.busy().value()).isFalse();
    }

    @Test
    void rejectionDoesNotNotifyBusyListenerThatWouldCloseService() {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        workers.shutdown();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<Boolean> busyEvents = new ArrayList<>();
        service.busy().subscribe(event -> { busyEvents.add(event.newValue()); service.close(); });
        assertThatThrownBy(() -> service.submit(c -> "never"))
                .isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
        assertThat(busyEvents).isEmpty();
        assertThat(service.busy().value()).isFalse();
        assertThatThrownBy(() -> service.submit(c -> "still rejected"))
                .isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
    }

    @Test
    void rejectedSubmissionWhileAnotherTaskIsActivePreservesBusyUntilCompletion() throws Exception {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        try {
            TaskHandle<String> first = service.submit(c -> { started.countDown(); release.await(); return "ok"; });
            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            workers.shutdown();
            assertThatThrownBy(() -> service.submit(c -> "rejected"))
                    .isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
            assertThat(service.busy().value()).isTrue();
            release.countDown();
            assertThat(first.completion().get(2, TimeUnit.SECONDS)).isEqualTo("ok");
            assertThat(service.busy().value()).isFalse();
        } finally { release.countDown(); workers.shutdownNow(); }
    }

    @Test
    void rejectedSubmissionDoesNotLeaveServiceBusyOrInvokeCallbacks() {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        workers.shutdown();
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        List<String> callbacks = new ArrayList<>();
        assertThatThrownBy(() -> service.submit(c -> "never", new TaskCallbacks<String>() {
            @Override public void failed(Throwable error) { callbacks.add("failed"); }
            @Override public void cancelled() { callbacks.add("cancelled"); }
        })).isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
        assertThat(service.busy().value()).isFalse();
        service.close();
        assertThat(callbacks).isEmpty();
    }

    @Test
    void submittedTaskRunsOffCallerThread() throws Exception {
        String callerThread = Thread.currentThread().getName();
        ExecutorService workers = Executors.newSingleThreadExecutor(r -> new Thread(r, "corusco-worker"));
        try {
            TaskService service = DefaultTaskService.of(workers, Runnable::run);

            TaskHandle<String> handle = service.submit(cancellation -> Thread.currentThread().getName());

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isEqualTo("corusco-worker");
            assertThat(handle.completion().get()).isNotEqualTo(callerThread);
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void virtualThreadFactoryRunsTasksOnVirtualThreads() throws Exception {
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            TaskHandle<Boolean> handle = service.submit(cancellation -> Thread.currentThread().isVirtual());

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void callbacksRunThroughConfiguredCallbackExecutor() throws Exception {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        ExecutorService callbacks = Executors.newSingleThreadExecutor(r -> new Thread(r, "corusco-callback"));
        try {
            TaskService service = DefaultTaskService.of(workers, callbacks);
            List<String> callbackThreads = new ArrayList<>();

            TaskHandle<String> handle = service.submit(
                    cancellation -> "ok",
                    TaskCallbacks.onSuccess(result -> callbackThreads.add(Thread.currentThread().getName() + ":" + result))
            );

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isEqualTo("ok");
            assertThat(callbackThreads).containsExactly("corusco-callback:ok");
        } finally {
            workers.shutdownNow();
            callbacks.shutdownNow();
        }
    }

    @Test
    void busyValuesTrackRunningTask() throws Exception {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            TaskService service = DefaultTaskService.of(workers, Runnable::run);
            List<Boolean> serviceBusy = new ArrayList<>();
            service.busy().subscribe(event -> serviceBusy.add(event.newValue()));

            TaskHandle<String> handle = service.submit(cancellation -> {
                started.countDown();
                release.await(2, TimeUnit.SECONDS);
                return "done";
            });
            List<Boolean> taskBusy = new ArrayList<>();
            handle.busy().subscribe((ValueChangeEvent<Boolean> event) -> taskBusy.add(event.newValue()));

            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(service.busy().value()).isTrue();
            assertThat(handle.busy().value()).isTrue();

            release.countDown();

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isEqualTo("done");
            assertThat(service.busy().value()).isFalse();
            assertThat(handle.busy().value()).isFalse();
            assertThat(serviceBusy).containsExactly(true, false);
            assertThat(taskBusy).containsExactly(false);
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void cancellationSuppressesSuccessAndFailureCallbacks() {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        try {
            TaskService service = DefaultTaskService.of(workers, Runnable::run);
            List<String> callbacks = new ArrayList<>();
            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        started.countDown();
                        try {
                            Thread.sleep(10_000L);
                        } catch (InterruptedException e) {
                            interrupted.countDown();
                            throw e;
                        }
                        return "late";
                    },
                    new TaskCallbacks<>() {
                        @Override
                        public void succeeded(String result) {
                            callbacks.add("success:" + result);
                        }

                        @Override
                        public void failed(Throwable error) {
                            callbacks.add("failure:" + error.getClass().getSimpleName());
                        }

                        @Override
                        public void cancelled() {
                            callbacks.add("cancelled");
                        }
                    }
            );

            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            handle.cancel();

            assertThatThrownBy(() -> handle.completion().join()).isInstanceOf(CancellationException.class);
            assertThat(handle.cancellationToken().isCancellationRequested()).isTrue();
            assertThat(interrupted.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(callbacks).containsExactly("cancelled");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void failureCallbackReceivesTaskError() {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        try {
            TaskService service = DefaultTaskService.of(workers, Runnable::run);
            List<String> callbacks = new ArrayList<>();

            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        throw new IllegalStateException("boom");
                    },
                    new TaskCallbacks<>() {
                        @Override
                        public void failed(Throwable error) {
                            callbacks.add(error.getClass().getSimpleName() + ":" + error.getMessage());
                        }
                    }
            );

            assertThatThrownBy(() -> handle.completion().join())
                    .hasCauseInstanceOf(IllegalStateException.class);
            assertThat(callbacks).containsExactly("IllegalStateException:boom");
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void closeCancelsRunningTasksAndRejectsNewSubmissions() throws Exception {
        ExecutorService workers = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        TaskService service = DefaultTaskService.of(workers, Runnable::run);
        try {
            TaskHandle<String> handle = service.submit(cancellation -> {
                started.countDown();
                Thread.sleep(10_000L);
                return "late";
            });
            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();

            service.close();

            assertThatThrownBy(() -> handle.completion().join()).isInstanceOf(CancellationException.class);
            assertThatThrownBy(() -> service.submit(cancellation -> "new"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        } finally {
            workers.shutdownNow();
        }
    }
}
