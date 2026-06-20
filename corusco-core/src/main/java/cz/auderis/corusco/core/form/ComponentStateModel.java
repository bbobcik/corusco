package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.value.WritableValue;

/**
 * Observable presentation state for a generated form control or UI region.
 *
 * <p>This model is intentionally separate from {@link FieldModel}. It describes
 * how a control should currently be presented: enabled, visible, editable,
 * relevant, busy, protected, valid, and optional problem summary. It is not a
 * semantic field value and must not be committed into a form result.</p>
 */
public final class ComponentStateModel {

    private final SimpleValue<Boolean> enabled = SimpleValue.of(true);
    private final SimpleValue<Boolean> visible = SimpleValue.of(true);
    private final SimpleValue<Boolean> editable = SimpleValue.of(true);
    private final SimpleValue<Boolean> required = SimpleValue.of(false);
    private final SimpleValue<Boolean> relevant = SimpleValue.of(true);
    private final SimpleValue<Boolean> busy = SimpleValue.of(false);
    private final SimpleValue<Boolean> protectedValue = SimpleValue.of(false);
    private final SimpleValue<Boolean> valid = SimpleValue.of(true);
    private final SimpleValue<String> problemSummary = SimpleValue.empty();

    /**
     * Returns enabled state.
     *
     * @return enabled value
     */
    public WritableValue<Boolean> enabled() {
        return enabled;
    }

    /**
     * Returns visible state.
     *
     * @return visible value
     */
    public WritableValue<Boolean> visible() {
        return visible;
    }

    /**
     * Returns editable state.
     *
     * @return editable value
     */
    public WritableValue<Boolean> editable() {
        return editable;
    }

    /**
     * Returns required-now state.
     *
     * @return required value
     */
    public WritableValue<Boolean> required() {
        return required;
    }

    /**
     * Returns relevance state.
     *
     * @return relevant value
     */
    public WritableValue<Boolean> relevant() {
        return relevant;
    }

    /**
     * Returns busy state.
     *
     * @return busy value
     */
    public WritableValue<Boolean> busy() {
        return busy;
    }

    /**
     * Returns protected-value presentation state.
     *
     * @return protected value
     */
    public WritableValue<Boolean> protectedValue() {
        return protectedValue;
    }

    /**
     * Returns validity presentation state.
     *
     * @return valid value
     */
    public WritableValue<Boolean> valid() {
        return valid;
    }

    /**
     * Returns optional problem summary text.
     *
     * @return problem summary value
     */
    public WritableValue<String> problemSummary() {
        return problemSummary;
    }
}
