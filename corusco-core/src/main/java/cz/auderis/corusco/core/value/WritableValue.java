package cz.auderis.corusco.core.value;

/**
 * A readable value that can be changed synchronously.
 *
 * <p>Implementations compare old and new values using
 * {@link java.util.Objects#equals(Object, Object)}. Equal values do not emit
 * change events. Values may be set to {@code null}.</p>
 *
 * @param <T> value type
 */
public interface WritableValue<T> extends ReadableValue<T> {

    /**
     * Sets a new value with {@link ChangeOrigin#MODEL} as the origin.
     *
     * @param value new value, possibly {@code null}
     */
    default void setValue(T value) {
        setValue(value, ChangeOrigin.MODEL);
    }

    /**
     * Sets a new value.
     *
     * @param value new value, possibly {@code null}
     * @param origin non-null origin to attach to emitted events
     */
    void setValue(T value, ChangeOrigin origin);
}
