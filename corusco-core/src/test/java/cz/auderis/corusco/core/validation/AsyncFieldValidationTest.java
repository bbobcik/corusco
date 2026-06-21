package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.task.UiTask;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncFieldValidationTest {

    private static final FieldKey<CustomerEdit, String> NAME =
            FieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final ProblemCode NAME_TAKEN = ProblemCode.of("validation/name-taken");

    @Test
    void publishesInitialValidationProblems() {
        SimpleValue<String> name = SimpleValue.of("taken");
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            AsyncFieldValidation<CustomerEdit, String> validation = AsyncFieldValidation.bind(
                    NAME,
                    name,
                    service,
                    (key, value, cancellation) -> problem("Name already used")
            );

            await(() -> !validation.busy().value(), "initial validation finished");

            assertThat(validation.problems().value().problems())
                    .extracting(Problem::message)
                    .containsExactly("Name already used");
        }
    }

    @Test
    void ignoresStaleOutOfOrderResults() throws Exception {
        SimpleValue<String> name = SimpleValue.of("old");
        CountDownLatch oldStarted = new CountDownLatch(1);
        CountDownLatch oldRelease = new CountDownLatch(1);
        CountDownLatch oldFinished = new CountDownLatch(1);
        CountDownLatch newStarted = new CountDownLatch(1);
        CountDownLatch newRelease = new CountDownLatch(1);
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            AsyncFieldValidation<CustomerEdit, String> validation = AsyncFieldValidation.bind(
                    NAME,
                    name,
                    service,
                    (key, value, cancellation) -> {
                        if ("old".equals(value)) {
                            oldStarted.countDown();
                            oldRelease.await(2, TimeUnit.SECONDS);
                            oldFinished.countDown();
                            return problem("old result");
                        }
                        newStarted.countDown();
                        newRelease.await(2, TimeUnit.SECONDS);
                        return ProblemSet.empty();
                    }
            );
            assertThat(oldStarted.await(2, TimeUnit.SECONDS)).isTrue();

            name.setValue("new", StandardChangeOrigin.USER);
            assertThat(newStarted.await(2, TimeUnit.SECONDS)).isTrue();
            newRelease.countDown();
            await(() -> !validation.busy().value(), "new validation finished");

            oldRelease.countDown();
            assertThat(oldFinished.await(2, TimeUnit.SECONDS)).isTrue();
            pauseForCallbackDelivery();

            assertThat(validation.problems().value().isEmpty()).isTrue();
        }
    }

    @Test
    void mapsValidatorFailuresToFieldProblems() {
        SimpleValue<String> name = SimpleValue.of("Alice");
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            AsyncFieldValidation<CustomerEdit, String> validation = AsyncFieldValidation.bind(
                    NAME,
                    name,
                    service,
                    (key, value, cancellation) -> {
                        throw new IllegalStateException("server unavailable");
                    }
            );

            await(() -> !validation.busy().value(), "failed validation finished");

            assertThat(validation.problems().value().problems())
                    .containsExactly(Problem.validation(
                            AsyncFieldValidation.ASYNC_VALIDATION_FAILED,
                            ProblemSeverity.ERROR,
                            ProblemTarget.field(NAME),
                            "server unavailable"
                    ));
        }
    }

    @Test
    void closeCancelsOutstandingTaskAndStopsObservation() throws Exception {
        SimpleValue<String> name = SimpleValue.of("Alice");
        CountDownLatch started = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            AsyncFieldValidation<CustomerEdit, String> validation = AsyncFieldValidation.bind(
                    NAME,
                    name,
                    service,
                    (key, value, cancellation) -> {
                        calls.incrementAndGet();
                        started.countDown();
                        while (!cancellation.isCancellationRequested()) {
                            Thread.sleep(10L);
                        }
                        return ProblemSet.empty();
                    }
            );
            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();

            validation.close();
            name.setValue("Bob", StandardChangeOrigin.USER);
            pauseForCallbackDelivery();

            assertThat(validation.busy().value()).isFalse();
            assertThat(calls).hasValue(1);
        }
    }

    @Test
    void submissionFailureClearsBusyAndPublishesProblem() {
        SimpleValue<String> name = SimpleValue.of("Alice");
        TaskService rejectingService = rejectingService();

        AsyncFieldValidation<CustomerEdit, String> validation = AsyncFieldValidation.bind(
                NAME,
                name,
                rejectingService,
                (key, value, cancellation) -> ProblemSet.empty()
        );

        assertThat(validation.busy().value()).isFalse();
        assertThat(validation.problems().value().problems())
                .extracting(Problem::message)
                .containsExactly("service closed");
    }

    private static ProblemSet problem(String message) {
        return ProblemSet.of(Problem.validation(
                NAME_TAKEN,
                ProblemSeverity.ERROR,
                ProblemTarget.field(NAME),
                message
        ));
    }

    private static TaskService rejectingService() {
        return new TaskService() {
            private final SimpleValue<Boolean> busy = SimpleValue.of(false);

            @Override
            public SimpleValue<Boolean> busy() {
                return busy;
            }

            @Override
            public <T> TaskHandle<T> submit(
                    UiTask<? extends T> task,
                    TaskCallbacks<? super T> callbacks
            ) {
                throw new IllegalStateException("service closed");
            }

            @Override
            public void close() {
            }
        };
    }

    private static void await(Condition condition, String description) {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.isMet()) {
                return;
            }
            pauseForCallbackDelivery();
        }
        throw new AssertionError("Timed out waiting for " + description);
    }

    private static void pauseForCallbackDelivery() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for async validation", e);
        }
    }

    @FunctionalInterface
    private interface Condition {
        boolean isMet();
    }

    private record CustomerEdit(String name) {
    }
}
