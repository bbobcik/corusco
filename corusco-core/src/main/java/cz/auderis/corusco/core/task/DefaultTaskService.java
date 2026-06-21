package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes UI-initiated background work and delivers callbacks on a configured
 * executor.
 *
 * <p>The service separates task execution from UI callback delivery. Task
 * bodies run on a worker {@link ExecutorService}; success, failure,
 * cancellation callbacks, and final busy-state updates run through the supplied
 * callback executor. Swing applications should pass an executor that dispatches
 * to the Event Dispatch Thread when callbacks update Swing state.</p>
 *
 * <p>The service owns the worker executor only when created through
 * {@link #virtualThreads(Executor)}. Instances created with
 * {@link #of(ExecutorService, Executor)} cancel outstanding handles on close
 * but leave the caller-owned executor service open. Closing is idempotent and
 * causes later submissions to fail with {@link IllegalStateException}.</p>
 */
public final class DefaultTaskService implements TaskService {

    private final ExecutorService workerExecutor;
    private final Executor callbackExecutor;
    private final boolean ownsWorkerExecutor;
    private final SimpleValue<Boolean> busy = SimpleValue.of(false);
    private final AtomicInteger activeTasks = new AtomicInteger();
    private final List<TaskHandleImpl<?>> handles = new CopyOnWriteArrayList<>();
    private boolean closed;

    /**
     * Creates a service backed by Java virtual threads.
     *
     * @param callbackExecutor executor used for callbacks and final busy-state
     *         delivery
     * @return task service
     */
    public static DefaultTaskService virtualThreads(Executor callbackExecutor) {
        return new DefaultTaskService(Executors.newVirtualThreadPerTaskExecutor(), callbackExecutor, true);
    }

    /**
     * Creates a service backed by explicit executors.
     *
     * @param workerExecutor executor used for task bodies
     * @param callbackExecutor executor used for callbacks and final busy-state
     *         delivery
     * @return task service
     */
    public static DefaultTaskService of(ExecutorService workerExecutor, Executor callbackExecutor) {
        return new DefaultTaskService(workerExecutor, callbackExecutor, false);
    }

    private DefaultTaskService(ExecutorService workerExecutor, Executor callbackExecutor, boolean ownsWorkerExecutor) {
        this.workerExecutor = Objects.requireNonNull(workerExecutor, "workerExecutor");
        this.callbackExecutor = Objects.requireNonNull(callbackExecutor, "callbackExecutor");
        this.ownsWorkerExecutor = ownsWorkerExecutor;
    }

    @Override
    public ReadableValue<Boolean> busy() {
        return busy;
    }

    @Override
    public <T> TaskHandle<T> submit(UiTask<? extends T> task, TaskCallbacks<? super T> callbacks) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(callbacks, "callbacks");
        if (closed) {
            throw new IllegalStateException("TaskService is closed");
        }
        CancellationSource cancellation = new CancellationSource();
        TaskHandleImpl<T> handle = new TaskHandleImpl<>(cancellation, callbacks);
        handles.add(handle);
        markStarted(handle);
        Future<?> workerFuture = workerExecutor.submit(() -> runTask(task, handle));
        handle.workerFuture = workerFuture;
        if (cancellation.isCancellationRequested()) {
            workerFuture.cancel(true);
        }
        return handle;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (TaskHandleImpl<?> handle : List.copyOf(handles)) {
            handle.cancel();
        }
        if (ownsWorkerExecutor) {
            workerExecutor.shutdownNow();
        }
    }

    private <T> void runTask(UiTask<? extends T> task, TaskHandleImpl<T> handle) {
        try {
            handle.cancellation.token().throwIfCancellationRequested();
            T result = task.run(handle.cancellation.token());
            handle.cancellation.token().throwIfCancellationRequested();
            finish(handle, TaskOutcome.success(result));
        } catch (TaskCancelledException | CancellationException e) {
            finish(handle, TaskOutcome.cancelled());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handle.cancellation.cancel();
            finish(handle, TaskOutcome.cancelled());
        } catch (Throwable e) {
            finish(handle, TaskOutcome.failure(e));
        }
    }

    private void markStarted(TaskHandleImpl<?> handle) {
        handle.busy.setValue(true, StandardChangeOrigin.SYSTEM);
        if (activeTasks.getAndIncrement() == 0) {
            busy.setValue(true, StandardChangeOrigin.SYSTEM);
        }
    }

    private <T> void finish(TaskHandleImpl<T> handle, TaskOutcome<T> outcome) {
        if (!handle.finishStarted.compareAndSet(false, true)) {
            return;
        }
        try {
            callbackExecutor.execute(() -> finishOnCallbackExecutor(handle, outcome));
        } catch (RejectedExecutionException e) {
            finishOnCallbackExecutor(handle, TaskOutcome.failure(e));
        }
    }

    private <T> void finishOnCallbackExecutor(TaskHandleImpl<T> handle, TaskOutcome<T> outcome) {
        try {
            handle.busy.setValue(false, StandardChangeOrigin.SYSTEM);
            if (activeTasks.decrementAndGet() == 0) {
                busy.setValue(false, StandardChangeOrigin.SYSTEM);
            }
            handles.remove(handle);
            switch (outcome.state()) {
                case SUCCESS -> {
                    handle.callbacks.succeeded(outcome.result());
                    handle.completion.complete(outcome.result());
                }
                case FAILURE -> {
                    handle.callbacks.failed(outcome.error());
                    handle.completion.completeExceptionally(outcome.error());
                }
                case CANCELLED -> {
                    handle.callbacks.cancelled();
                    handle.completion.cancel(false);
                }
            }
        } catch (Throwable e) {
            handle.completion.completeExceptionally(e);
            throw e;
        }
    }

    private enum TaskState {
        SUCCESS,
        FAILURE,
        CANCELLED
    }

    private record TaskOutcome<T>(TaskState state, T result, Throwable error) {

        private static <T> TaskOutcome<T> success(T result) {
            return new TaskOutcome<>(TaskState.SUCCESS, result, null);
        }

        private static <T> TaskOutcome<T> failure(Throwable error) {
            return new TaskOutcome<>(TaskState.FAILURE, null, Objects.requireNonNull(error, "error"));
        }

        private static <T> TaskOutcome<T> cancelled() {
            return new TaskOutcome<>(TaskState.CANCELLED, null, null);
        }
    }

    private final class TaskHandleImpl<T> implements TaskHandle<T> {

        private final CancellationSource cancellation;
        private final TaskCallbacks<? super T> callbacks;
        private final SimpleValue<Boolean> busy = SimpleValue.of(false);
        private final CompletableFuture<T> completion = new CompletableFuture<>();
        private final AtomicBoolean finishStarted = new AtomicBoolean();
        private volatile Future<?> workerFuture;

        private TaskHandleImpl(CancellationSource cancellation, TaskCallbacks<? super T> callbacks) {
            this.cancellation = cancellation;
            this.callbacks = callbacks;
        }

        @Override
        public ReadableValue<Boolean> busy() {
            return busy;
        }

        @Override
        public CancellationToken cancellationToken() {
            return cancellation.token();
        }

        @Override
        public CompletableFuture<T> completion() {
            return completion;
        }

        @Override
        public void cancel() {
            cancellation.cancel();
            Future<?> future = workerFuture;
            if (future != null) {
                future.cancel(true);
            }
            finish(this, TaskOutcome.cancelled());
        }

        @Override
        public boolean isDone() {
            return completion.isDone();
        }
    }
}
