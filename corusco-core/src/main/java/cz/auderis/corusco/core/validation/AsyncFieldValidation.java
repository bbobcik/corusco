package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.task.GenerationCounter;
import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs asynchronous validation for one field value.
 *
 * <p>The controller observes a semantic value and submits validation work to a
 * {@link TaskService}. Each submission captures a generation token; callbacks
 * for older values are ignored, so slow server-side validation cannot overwrite
 * current field state. The controller is Swing-free and inherits callback
 * threading from the supplied task service.</p>
 *
 * @param <O> owner/model type
 * @param <T> field value type
 */
public final class AsyncFieldValidation<O, T> implements Disposable {

    /**
     * Problem code used when an async validator fails unexpectedly.
     */
    public static final ProblemCode ASYNC_VALIDATION_FAILED =
            ProblemCode.of("validation/async-failed");

    private final FieldKey<O, T> key;
    private final ReadableValue<T> value;
    private final TaskService taskService;
    private final AsyncFieldValidator<O, T> validator;
    private final GenerationCounter generations = new GenerationCounter();
    private final SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.empty());
    private final SimpleValue<Boolean> busy = SimpleValue.of(false);
    private final List<TaskHandle<ProblemSet>> tasks = new CopyOnWriteArrayList<>();
    private final Subscription valueSubscription;
    private boolean closed;

    /**
     * Creates and starts async field validation.
     *
     * @param key typed field key
     * @param value observed field value
     * @param taskService task service
     * @param validator async validator
     * @param <O> owner/model type
     * @param <T> field value type
     * @return async validation controller
     */
    public static <O, T> AsyncFieldValidation<O, T> bind(
            FieldKey<O, T> key,
            ReadableValue<T> value,
            TaskService taskService,
            AsyncFieldValidator<O, T> validator
    ) {
        return new AsyncFieldValidation<>(key, value, taskService, validator);
    }

    /**
     * Creates and starts async field validation.
     *
     * @param key typed field key
     * @param value observed field value
     * @param taskService task service
     * @param validator async validator
     */
    public AsyncFieldValidation(
            FieldKey<O, T> key,
            ReadableValue<T> value,
            TaskService taskService,
            AsyncFieldValidator<O, T> validator
    ) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
        this.taskService = Objects.requireNonNull(taskService, "taskService");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.valueSubscription = value.subscribe(event -> validate(event.newValue(), event.origin()));
        validate(value.value(), ChangeOrigin.SYSTEM);
    }

    /**
     * Returns observable async validation problems.
     *
     * @return problem value
     */
    public ReadableValue<ProblemSet> problems() {
        return problems;
    }

    /**
     * Returns observable async validation busy state for the current field
     * value.
     *
     * @return busy value
     */
    public ReadableValue<Boolean> busy() {
        return busy;
    }

    /**
     * Revalidates the current value.
     */
    public void validateNow() {
        validate(value.value(), ChangeOrigin.MODEL);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        generations.invalidate();
        valueSubscription.close();
        for (TaskHandle<ProblemSet> task : List.copyOf(tasks)) {
            task.cancel();
        }
        tasks.clear();
        busy.setValue(false, ChangeOrigin.SYSTEM);
    }

    private void validate(T fieldValue, ChangeOrigin origin) {
        if (closed) {
            return;
        }
        GenerationCounter.Generation generation = generations.advance();
        busy.setValue(true, origin);
        AtomicReference<TaskHandle<ProblemSet>> handleRef = new AtomicReference<>();
        TaskHandle<ProblemSet> handle;
        try {
            handle = taskService.submit(
                    cancellation -> validator.validate(key, fieldValue, cancellation),
                    new ValidationCallbacks(generation, origin, handleRef)
            );
        } catch (RuntimeException e) {
            completeIfCurrent(generation, origin, failureProblem(e));
            return;
        }
        handleRef.set(handle);
        tasks.add(handle);
        if (handle.isDone()) {
            tasks.remove(handle);
        }
    }

    private void completeIfCurrent(GenerationCounter.Generation generation, ChangeOrigin origin, ProblemSet newProblems) {
        generations.tryAccept(generation, newProblems == null ? ProblemSet.empty() : newProblems, accepted -> {
            problems.setValue(accepted, origin);
            busy.setValue(false, origin);
        });
    }

    private ProblemSet failureProblem(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = error.getClass().getSimpleName();
        }
        return ProblemSet.of(Problem.validation(
                ASYNC_VALIDATION_FAILED,
                ProblemSeverity.ERROR,
                ProblemTarget.field(key),
                message
        ));
    }

    private final class ValidationCallbacks implements TaskCallbacks<ProblemSet> {

        private final GenerationCounter.Generation generation;
        private final ChangeOrigin origin;
        private final AtomicReference<TaskHandle<ProblemSet>> handleRef;

        private ValidationCallbacks(
                GenerationCounter.Generation generation,
                ChangeOrigin origin,
                AtomicReference<TaskHandle<ProblemSet>> handleRef
        ) {
            this.generation = generation;
            this.origin = origin;
            this.handleRef = handleRef;
        }

        @Override
        public void succeeded(ProblemSet result) {
            tasks.remove(handleRef.get());
            completeIfCurrent(generation, origin, result);
        }

        @Override
        public void failed(Throwable error) {
            tasks.remove(handleRef.get());
            completeIfCurrent(generation, origin, failureProblem(error));
        }

        @Override
        public void cancelled() {
            tasks.remove(handleRef.get());
            if (generations.isCurrent(generation)) {
                busy.setValue(false, origin);
            }
        }
    }
}
