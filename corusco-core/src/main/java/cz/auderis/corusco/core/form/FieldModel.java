package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.Objects;

/**
 * Semantic field model with dirty and touched state.
 *
 * <p>The model stores a typed {@link FieldKey}, the current semantic value, the
 * original dirty-state baseline, and a touched flag. It is Swing-free and
 * synchronous. Values may be {@code null}.</p>
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
        value.setValue(originalValue, ChangeOrigin.SYSTEM);
        touched.setValue(false, ChangeOrigin.SYSTEM);
        refreshDirty(ChangeOrigin.SYSTEM);
    }

    /**
     * Accepts the current value as the new dirty-state baseline.
     */
    public void acceptCurrentValue() {
        originalValue = value.value();
        refreshDirty(ChangeOrigin.SYSTEM);
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
