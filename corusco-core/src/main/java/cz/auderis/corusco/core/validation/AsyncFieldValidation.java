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
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lifecycle controller that runs asynchronous validation for one observable field value.
 *
 * <p>This class is used when validation requires work that should not block the
 * caller, for example a server lookup or expensive local check. It subscribes
 * to a semantic {@link ReadableValue}, submits validation work to a
 * {@link TaskService}, and exposes two observable values: current async
 * validation {@link #problems()} and {@link #busy()} state. It is core-level
 * and Swing-free; Swing callers normally choose a task service whose callbacks
 * return on the EDT.</p>
 *
 * <p>Each submitted validation captures a {@link GenerationCounter} token.
 * Completion callbacks for older values are ignored, so a slow validation of a
 * previous value cannot overwrite the problems for the current value. Closing
 * the controller invalidates generations, removes the value subscription,
 * cancels outstanding tasks, clears tracked handles, and publishes not-busy
 * state. Close is idempotent.</p>
 *
 * <p>The controller owns only its subscription to the observed value and the
 * task handles it submits. It does not own the field model, the task service,
 * or the validator. Validator exceptions and task-submission failures are
 * converted to a field-targeted {@link ProblemSeverity#ERROR} problem using
 * {@link #ASYNC_VALIDATION_FAILED}. Cancellation is cooperative and follows the
 * supplied task service's {@link cz.auderis.corusco.core.task.CancellationToken}
 * contract.</p>
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
     * <p>The returned controller immediately validates the current value and
     * then observes future value changes until closed.</p>
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
     * <p>Construction subscribes to the value and immediately schedules
     * validation of the current value with {@link StandardChangeOrigin#SYSTEM}.
     * Later value changes reuse the origin carried by the value event.</p>
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
        validate(value.value(), StandardChangeOrigin.SYSTEM);
    }

    /**
     * Returns observable async validation problems.
     *
     * <p>The returned value is owned by this controller. Subscribers are
     * retained by the returned value according to {@link SimpleValue}'s normal
     * subscription rules and should dispose their subscriptions when no longer
     * needed.</p>
     *
     * @return problem value
     */
    public ReadableValue<ProblemSet> problems() {
        return problems;
    }

    /**
     * Returns observable async validation busy state for the current field value.
     *
     * <p>Busy becomes true when a validation task is submitted for the current
     * generation and false when the current generation completes, fails, is
     * cancelled, or the controller is closed. Older generations do not clear
     * busy for newer work.</p>
     *
     * @return busy value
     */
    public ReadableValue<Boolean> busy() {
        return busy;
    }

    /**
     * Revalidates the current value.
     *
     * <p>The call submits a new generation even if the field value has not
     * changed. Calling this after close has no effect.</p>
     */
    public void validateNow() {
        validate(value.value(), StandardChangeOrigin.MODEL);
    }

    /**
     * Stops observing the value and cancels outstanding validation tasks.
     *
     * <p>The call is idempotent. Already queued task callbacks may still be
     * delivered by the task service, but their generations are invalidated and
     * cannot publish problems after close.</p>
     */
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
        busy.setValue(false, StandardChangeOrigin.SYSTEM);
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
