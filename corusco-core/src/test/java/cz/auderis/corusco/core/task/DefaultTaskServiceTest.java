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
