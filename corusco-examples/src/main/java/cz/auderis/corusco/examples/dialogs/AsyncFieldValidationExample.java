package cz.auderis.corusco.examples.dialogs;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.validation.AsyncFieldValidation;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates asynchronous validation bound to a single field value.
 *
 * <p>The scenario connects a text field model to an asynchronous validator and
 * shows how newer edits supersede stale validation results. It is useful for
 * readers adding server-side or otherwise delayed validation to a form while
 * keeping problem reporting in the Corusco model layer.</p>
 */
public final class AsyncFieldValidationExample {

    private static final FieldKey<CustomerEdit, String> NAME =
            FieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final ProblemCode NAME_TAKEN = ProblemCode.of("validation/name-taken");

    private AsyncFieldValidationExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a field validation scenario with out-of-order async completion.
     *
     * @return diagnostics describing the accepted current result
     */
    public static List<String> runScenario() {
        SimpleValue<String> name = SimpleValue.of("old");
        CountDownLatch oldStarted = new CountDownLatch(1);
        CountDownLatch oldRelease = new CountDownLatch(1);
        CountDownLatch oldFinished = new CountDownLatch(1);
        CountDownLatch newStarted = new CountDownLatch(1);
        CountDownLatch newRelease = new CountDownLatch(1);
        List<String> result = new ArrayList<>();

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
                            return problem("old value is already taken");
                        }
                        newStarted.countDown();
                        newRelease.await(2, TimeUnit.SECONDS);
                        return ProblemSet.empty();
                    }
            );
            await(oldStarted);

            // Editing the field starts a newer validation generation while the
            // first request is still running. The controller keeps the field
            // busy until this current generation finishes.
            name.setValue("new", ChangeOrigin.USER);
            await(newStarted);
            result.add("busyAfterEdit=" + validation.busy().value());

            // The current value completes first and clears the presentation
            // problems. When the older request finishes later, its generation
            // is stale and cannot overwrite the accepted result.
            newRelease.countDown();
            awaitNotBusy(validation);
            oldRelease.countDown();
            await(oldFinished);
            result.add("messages=" + messages(validation.problems().value()));
            result.add("busyAfterCurrent=" + validation.busy().value());
        }
        return result;
    }

    private static ProblemSet problem(String message) {
        return ProblemSet.of(Problem.validation(
                NAME_TAKEN,
                ProblemSeverity.ERROR,
                ProblemTarget.field(NAME),
                message
        ));
    }

    private static List<String> messages(ProblemSet problems) {
        return problems.problems().stream()
                .map(Problem::message)
                .toList();
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for validation step");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for validation step", e);
        }
    }

    private static void awaitNotBusy(AsyncFieldValidation<?, ?> validation) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (!validation.busy().value()) {
                return;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for validation", e);
            }
        }
        throw new IllegalStateException("Timed out waiting for validation");
    }

    private record CustomerEdit(String name) {
    }
}
