package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.ValueChangeEvent;
import cz.auderis.corusco.core.value.ValueChangeListener;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.problem.ProblemSet;

/** Form with an observable aggregate and owned subscriptions. */
public interface ObservableFormModel<R> extends FormModel<R>, Disposable {
    ReadableValue<FormState> state();

    /** Returns a typed projection. Each listener owns its subscription. */
    default ReadableValue<ProblemSet> fieldProblems(FieldKey<?, ?> key) {
        ReadableValue<FormState> source = state();
        return new ReadableValue<>() {
            @Override public ProblemSet value() { return source.value().problems().filter(ProblemFilter.field(key)); }
            @Override public Subscription subscribe(ValueChangeListener<ProblemSet> listener) {
                return source.subscribe(event -> {
                    ProblemSet before = event.oldValue().problems().filter(ProblemFilter.field(key));
                    ProblemSet after = event.newValue().problems().filter(ProblemFilter.field(key));
                    if (!before.problems().equals(after.problems()))
                        listener.valueChanged(new ValueChangeEvent<>(this, before, after, event.origin()));
                });
            }
        };
    }
}
