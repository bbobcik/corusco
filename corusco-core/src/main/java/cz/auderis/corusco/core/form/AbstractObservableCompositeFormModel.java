package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A fixed composition of observable forms. Close to detach child listeners.
 *
 * <p>A child belongs to at most one live composite parent. Nested composites
 * are supported. Close each composite to release its ownership.</p>
 */
public abstract class AbstractObservableCompositeFormModel<R> extends AbstractCompositeFormModel<R> implements ObservableFormModel<R> {
    private final List<ObservableFormModel<?>> observableChildren;
    private final List<Subscription> subscriptions = new ArrayList<>();
    private SimpleValue<FormState> state;
    private boolean batching;
    private boolean changed;
    private boolean closed;
    private final FormMutations ownMutations = new FormMutations();
    private FormMutations mutations = ownMutations;
    private final List<Runnable> detachRouting = new ArrayList<>();

    protected AbstractObservableCompositeFormModel(List<? extends ObservableFormModel<?>> children) {
        super(children);
        observableChildren = List.copyOf(children);
        FormOwnership.reserve(this, observableChildren);
        try {
            for (var child : observableChildren) ensureIdle(child);
            for (var child : observableChildren) attach(child, mutations, detachRouting);
        } catch (RuntimeException | Error failure) {
            detachRoutes();
            throw failure;
        }
    }
    protected AbstractObservableCompositeFormModel(ObservableFormModel<?>... children) {
        this(Arrays.asList(Objects.requireNonNull(children, "children")));
    }
    @Override public final ReadableValue<FormState> state() {
        if (closed) throw new IllegalStateException("Composite form is disposed");
        if (state == null) {
            for (var child : observableChildren) {
                if (FormOwnership.disposed(child)) throw new IllegalStateException("Child form is disposed");
            }
            state = SimpleValue.of(snapshot(0));
            if (!closed) for (var child : observableChildren) subscriptions.add(child.state().subscribe(e -> update()));
        }
        return state;
    }
    private FormState snapshot(long revision) {
        ProblemSet problems = problems();
        boolean dirty = observableChildren.stream().anyMatch(child -> child.state().value().dirty());
        return new FormState(problems, !problems.hasErrors(), dirty, revision);
    }
    private void update() {
        if (closed || state == null) return;
        if (batching) { changed = true; return; }
        state.setValue(snapshot(state.value().revision() + 1), StandardChangeOrigin.MODEL);
    }
    @Override public void reset() { mutations.runOwned(this, "reset", this::resetNow); }
    @Override public void acceptCurrentValues() { mutations.runOwned(this, "accept", this::acceptNow); }
    private void resetNow() {
        batching = true; changed = false;
        try {
            for (var child : observableChildren) {
                mutations.invokeChild(child, "reset", child::reset);
            }
            resetParentState();
        } finally { finishBatch(); }
    }
    private void acceptNow() {
        batching = true; changed = false;
        try {
            for (var child : observableChildren) {
                mutations.invokeChild(child, "accept", child::acceptCurrentValues);
            }
            acceptParentCurrentValues();
        } finally { finishBatch(); }
    }
    private void finishBatch() {
        batching = false;
        if (state == null || closed) return;
        FormState before = state.value();
        FormState after = snapshot(before.revision());
        if (changed || before.dirty() != after.dirty() || before.committable() != after.committable()
                || !before.problems().problems().equals(after.problems().problems())) update();
    }
    private static void attach(ObservableFormModel<?> child, FormMutations owner, List<Runnable> detach) {
        if (child instanceof AbstractObservableCompositeFormModel<?> composite) {
            FormMutations previous = composite.mutations;
            composite.mutations = owner;
            detach.add(() -> {
                if (composite.mutations == owner) {
                    composite.mutations = previous;
                    if (composite.closed && previous == composite.ownMutations) composite.detachRoutes();
                }
            });
            for (var nested : composite.observableChildren) attach(nested, owner, detach);
        } else if (child instanceof AbstractFormModel<?> form) {
            FormMutations previous = form.mutations();
            form.useMutations(owner);
            detach.add(() -> { if (form.mutations() == owner) form.useMutations(previous); });
        }
    }
    private static void ensureIdle(ObservableFormModel<?> child) {
        if (child instanceof AbstractObservableCompositeFormModel<?> composite) {
            if (composite.mutations.isRunning()) throw new IllegalStateException("Cannot attach a form during mutation");
            for (var nested : composite.observableChildren) ensureIdle(nested);
        } else if (child instanceof AbstractFormModel<?> form && form.mutations().isRunning()) {
            throw new IllegalStateException("Cannot attach a form during mutation");
        }
    }
    @Override public void close() {
        if (closed) return;
        FormOwnership.requireUnowned(this);
        closed = true; subscriptions.forEach(Subscription::close); subscriptions.clear();
        mutations.afterDrain(this::detachRoutes);
    }
    private void detachRoutes() {
        for (int i = detachRouting.size() - 1; i >= 0; i--) detachRouting.get(i).run();
        detachRouting.clear();
        FormOwnership.release(this, observableChildren);
    }
    final boolean isDisposed() { return closed; }
}
