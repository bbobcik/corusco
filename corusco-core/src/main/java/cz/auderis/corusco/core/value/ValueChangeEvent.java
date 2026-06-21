package cz.auderis.corusco.core.value;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Immutable event delivered synchronously after an effective value change.
 *
 * <p>The event carries the value source, old value, new value, and
 * {@link ChangeOrigin}. Values may be {@code null}; the source and origin are
 * always non-null.</p>
 *
 * @param source value that emitted the event
 * @param oldValue value before the change, possibly {@code null}
 * @param newValue value after the change, possibly {@code null}
 * @param origin origin supplied by the mutating code
 * @param <T> value type
 */
public record ValueChangeEvent<T>(

        ReadableValue<T> source,

        @Nullable
        T oldValue,

        @Nullable
        T newValue,

        ChangeOrigin origin
) {

    /**
     * Creates a value change event.
     *
     * @param source value that emitted the event
     * @param oldValue value before the change, possibly {@code null}
     * @param newValue value after the change, possibly {@code null}
     * @param origin change origin
     */
    public ValueChangeEvent {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(origin, "origin");
    }
}
