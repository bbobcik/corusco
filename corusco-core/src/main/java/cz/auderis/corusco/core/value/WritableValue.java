package cz.auderis.corusco.core.value;

/**
 * A readable value that can be changed synchronously.
 *
 * <p>This is the mutable counterpart to {@link ReadableValue}. It is used by
 * presenters and field models that own scalar state and need to publish changes
 * to bindings or derived values. Callers can provide a {@link ChangeOrigin} so
 * listeners can distinguish user, model, binding, generated, or system updates.</p>
 *
 * <p>Built-in implementations compare old and new values using
 * {@link java.util.Objects#equals(Object, Object)}. Equal values do not emit
 * change events. Values may be set to {@code null}. Implementations should
 * document any different equality or event policy.</p>
 *
 * @param <T> value type
 */
public interface WritableValue<T> extends ReadableValue<T> {

    /**
     * Sets a new value with {@link StandardChangeOrigin#MODEL} as the origin.
     *
     * @param value new value, possibly {@code null}
     */
    default void setValue(T value) {
        setValue(value, StandardChangeOrigin.MODEL);
    }

    /**
     * Sets a new value.
     *
     * @param value new value, possibly {@code null}
     * @param origin non-null origin to attach to emitted events
     */
    void setValue(T value, ChangeOrigin origin);
}
