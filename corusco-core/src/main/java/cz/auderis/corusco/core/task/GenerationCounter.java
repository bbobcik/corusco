package cz.auderis.corusco.core.task;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Thread-safe generation counter for suppressing stale asynchronous results.
 *
 * <p>Call {@link #advance()} when scheduling work for the latest input. Capture
 * the returned {@link Generation} with the task, then call
 * {@link #isCurrent(Generation)} or {@link #tryAccept(Generation, Object, Consumer)}
 * from the callback before applying the result. Any later advance or
 * invalidation makes older generations stale.</p>
 */
public final class GenerationCounter {

    private final AtomicLong generation = new AtomicLong();

    /**
     * Returns the current generation token without changing it.
     *
     * @return current generation
     */
    public Generation current() {
        return new Generation(generation.get());
    }

    /**
     * Advances to a new generation.
     *
     * @return new current generation
     */
    public Generation advance() {
        return new Generation(generation.incrementAndGet());
    }

    /**
     * Invalidates outstanding generations without scheduling new work.
     *
     * @return new current generation
     */
    public Generation invalidate() {
        return advance();
    }

    /**
     * Indicates whether the supplied token is still current.
     *
     * @param token captured generation token
     * @return {@code true} when the token matches the current generation
     */
    public boolean isCurrent(Generation token) {
        Objects.requireNonNull(token, "token");
        return generation.get() == token.value();
    }

    /**
     * Indicates whether the supplied token has become stale.
     *
     * @param token captured generation token
     * @return {@code true} when the token no longer matches the current
     *         generation
     */
    public boolean isStale(Generation token) {
        return !isCurrent(token);
    }

    /**
     * Applies a result only when the supplied generation is still current.
     *
     * @param token captured generation token
     * @param result result to apply, possibly {@code null}
     * @param consumer consumer that applies the result
     * @param <T> result type
     * @return {@code true} when the result was accepted
     */
    public <T> boolean tryAccept(Generation token, T result, Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        if (!isCurrent(token)) {
            return false;
        }
        consumer.accept(result);
        return true;
    }

    /**
     * Immutable generation token captured when work is scheduled.
     *
     * @param value generation value
     */
    public record Generation(long value) {

        /**
         * Creates a generation token.
         *
         * @param value non-negative generation value
         */
        public Generation {
            if (value < 0) {
                throw new IllegalArgumentException("value must not be negative");
            }
        }
    }
}
