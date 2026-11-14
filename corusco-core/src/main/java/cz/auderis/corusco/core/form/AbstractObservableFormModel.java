package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.List;

/** Observable field-backed form; initialization is deferred until state is requested. */
public abstract class AbstractObservableFormModel<R> extends AbstractFormModel<R> implements ObservableFormModel<R> {
    private final List<Subscription> subscriptions = new ArrayList<>();
    private SimpleValue<FormState> state;
    private boolean batching;
    private boolean changed;
    private boolean closed;

    @Override public final ReadableValue<FormState> state() {
        if (state == null) {
            state = SimpleValue.of(snapshot(0));
            if (!closed) {
                for (var field : textFields) subscriptions.add(field.completed().subscribe(e -> update()));
                for (var field : fields) if (!isOwnedByTextField(field)) subscriptions.add(field.completed().subscribe(e -> update()));
            }
        }
        return state;
    }
    @Override protected final void fieldRegistered(FieldModel<?, ?> field) { field.useMutations(mutations()); }
    @Override protected final void textFieldRegistered(TextFieldModel<?, ?> field) { field.useMutations(mutations()); }
    private FormState snapshot(long revision) {
        ProblemSet problems = problems();
        boolean dirty = fields.stream().anyMatch(FieldModel::isDirty)
                || textFields.stream().anyMatch(f -> f.parseState().value() instanceof ParseState.Failed<?>);
        return new FormState(problems, !problems.hasErrors(), dirty, revision);
    }
    private void update() {
        if (closed || state == null) return;
        if (batching) { changed = true; return; }
        state.setValue(snapshot(state.value().revision() + 1), StandardChangeOrigin.MODEL);
    }
    @Override protected void beginBatch() { batching = true; changed = false; }
    @Override protected void endBatch() { batching = false; if (changed) update(); }
    @Override public void close() {
        if (closed) return;
        FormOwnership.requireUnowned(this);
        closed = true; subscriptions.forEach(Subscription::close); subscriptions.clear();
    }
    final boolean isDisposed() { return closed; }
}
