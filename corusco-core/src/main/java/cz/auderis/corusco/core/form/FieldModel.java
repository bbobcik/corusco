package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.Objects;

/**
 * Semantic field model with dirty and touched state.
 *
 * <p>This is the basic model object for a field whose value is already in its
 * semantic Java type. It stores a typed {@link FieldKey}, the current semantic
 * value, the original dirty-state baseline, and a touched flag. Form models use
 * it directly for non-text fields and indirectly through {@link TextFieldModel}
 * for text fields that also need parse state.</p>
 *
 * <p>The field owns three observable values: current value, dirty state, and
 * touched state. Mutations are synchronous, may accept {@code null} values, and
 * notify subscribers through the underlying value contracts. The model is
 * Swing-free and not synchronized; Swing bindings must apply their own EDT
 * rules when they write into it.</p>
 *
 * <p>Calling {@link #acceptCurrentValue()} changes only the dirty-state
 * baseline. Calling {@link #reset()} restores the current value to that
 * baseline and clears touched state. The field does not run validation by
 * itself; form models and validators decide how semantic values become
 * problems.</p>
 *
 * @param <O> owner/model type
 * @param <T> field value type
 */
public class FieldModel<O, T> {

    private final FieldKey<O, T> key;
    private final SimpleValue<T> value;
    private final SimpleValue<Boolean> dirty = SimpleValue.of(false);
    private final SimpleValue<Boolean> touched = SimpleValue.of(false);
    private T originalValue;

    /**
     * Creates a field model.
     *
     * @param key typed field key
     * @param originalValue original semantic value, possibly {@code null}
     */
    public FieldModel(FieldKey<O, T> key, T originalValue) {
        this.key = Objects.requireNonNull(key, "key");
        this.originalValue = originalValue;
        this.value = SimpleValue.of(originalValue);
    }

    /**
     * Returns the typed field key.
     *
     * @return field key
     */
    public FieldKey<O, T> key() {
        return key;
    }

    /**
     * Returns the observable current semantic value.
     *
     * @return writable value
     */
    public ReadableValue<T> value() {
        return value;
    }

    /**
     * Returns observable dirty state.
     *
     * @return dirty value
     */
    public ReadableValue<Boolean> dirty() {
        return dirty;
    }

    /**
     * Returns observable touched state.
     *
     * @return touched value
     */
    public ReadableValue<Boolean> touched() {
        return touched;
    }

    /**
     * Changes the semantic value and marks the field touched.
     *
     * @param newValue new semantic value, possibly {@code null}
     * @param origin change origin
     */
    public void setValue(T newValue, ChangeOrigin origin) {
        value.setValue(newValue, origin);
        touched.setValue(true, origin);
        refreshDirty(origin);
    }

    /**
     * Resets current value and touched state to the original baseline.
     */
    public void reset() {
        value.setValue(originalValue, StandardChangeOrigin.SYSTEM);
        touched.setValue(false, StandardChangeOrigin.SYSTEM);
        refreshDirty(StandardChangeOrigin.SYSTEM);
    }

    /**
     * Accepts the current value as the new dirty-state baseline.
     */
    public void acceptCurrentValue() {
        originalValue = value.value();
        refreshDirty(StandardChangeOrigin.SYSTEM);
    }

    void markTouched(ChangeOrigin origin) {
        touched.setValue(true, origin);
    }

    /**
     * Returns whether the current semantic value differs from the original
     * baseline.
     *
     * @return dirty flag
     */
    public boolean isDirty() {
        return dirty.value();
    }

    /**
     * Returns whether this field has been touched.
     *
     * @return touched flag
     */
    public boolean isTouched() {
        return touched.value();
    }

    private void refreshDirty(ChangeOrigin origin) {
        dirty.setValue(!Objects.equals(originalValue, value.value()), origin);
    }
}
