package cz.auderis.corusco.core.value;

import org.jspecify.annotations.Nullable;

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
 * @param <T> value type; writable values can still be set to {@code null}
 *         through the method contracts unless an implementation documents a
 *         stricter policy
 */
public interface WritableValue<T> extends ReadableValue<T> {

    /**
     * Sets a new value with {@link StandardChangeOrigin#MODEL} as the origin.
     *
     * <p>Use this overload for application-owned model or presenter writes. Use
     * {@link #setValue(Object, ChangeOrigin)} when the caller is forwarding a
     * direct user edit, binding propagation, generated adapter write, framework
     * update, or custom-origin change.</p>
     *
     * @param value new value, possibly {@code null}
     */
    default void setValue(@Nullable T value) {
        setValue(value, StandardChangeOrigin.MODEL);
    }

    /**
     * Sets a new value.
     *
     * <p>When an implementation emits a value-change event, it should attach
     * this origin unchanged unless its own Javadoc explicitly documents a
     * different origin policy.</p>
     *
     * @param value new value, possibly {@code null}
     * @param origin non-null origin to attach to emitted events
     */
    void setValue(@Nullable T value, ChangeOrigin origin);
}
