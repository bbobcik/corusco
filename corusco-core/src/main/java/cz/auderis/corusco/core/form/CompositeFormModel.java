package cz.auderis.corusco.core.form;

import java.util.List;

/**
 * Form model composed from a fixed ordered set of child form models.
 *
 * <p>Composite forms keep multi-section and multi-tab dialog semantics in the
 * core form layer without depending on any Swing view classes. Child ordering
 * is semantic: problem aggregation, reset, and baseline acceptance must all
 * follow this order so tests and presentation code can rely on deterministic
 * behavior.</p>
 *
 * @param <R> committed result type
 */
public interface CompositeFormModel<R> extends FormModel<R> {

    /**
     * Returns child form models in registration order.
     *
     * @return immutable ordered child list
     */
    List<? extends FormModel<?>> children();
}
