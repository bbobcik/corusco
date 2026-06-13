package cz.auderis.corusco.core.value;

import java.util.Objects;
import java.util.List;
import java.util.function.Function;

/**
 * A one-way derived value computed by mapping a source value.
 *
 * <p>The mapped value updates synchronously when its source changes. Closing the
 * mapped value removes the source subscription. Null source values are passed to
 * the mapper.</p>
 *
 * @param <A> source value type
 * @param <B> mapped value type
 */
public final class MappedValue<A, B> extends DerivedValue<B> {

    private MappedValue(ReadableValue<A> source, Function<? super A, ? extends B> mapper) {
        super(() -> mapper.apply(source.value()), List.of(source));
    }

    /**
     * Creates a mapped value.
     *
     * @param source source value
     * @param mapper mapping function, invoked synchronously
     * @param <A> source value type
     * @param <B> mapped value type
     * @return mapped value subscribed to the source
     */
    public static <A, B> MappedValue<A, B> of(
            ReadableValue<A> source,
            Function<? super A, ? extends B> mapper
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mapper, "mapper");
        return new MappedValue<>(source, mapper);
    }
}
