package cz.auderis.corusco.core.task;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Thread-safe token source for suppressing stale asynchronous results.
 *
 * <p>Presentation code often starts asynchronous work for a value that can
 * change again before the work completes. This counter gives that code a small
 * ownership token: call {@link #advance()} when scheduling work for the latest
 * input, capture the returned {@link Generation} with the task, and call
 * {@link #isCurrent(Generation)} or
 * {@link #tryAccept(Generation, Object, Consumer)} before applying the result.
 * Any later advance or invalidation makes older generations stale.</p>
 *
 * <p>The counter is thread-safe because the generation value is atomic, but it
 * does not make the result consumer or the target model thread-safe. Callbacks
 * must still apply accepted results on the thread or executor required by the
 * owning model or Swing binding. Tokens are immutable value objects and can be
 * stored with task callbacks.</p>
 */
public final class GenerationCounter {

    private final AtomicLong generation = new AtomicLong();

    /**
     * Creates a counter starting at generation zero.
     */
    public GenerationCounter() {
    }

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
     * <p>Use this when scheduling a new piece of work whose result should
     * supersede all older work.</p>
     *
     * @return new current generation
     */
    public Generation advance() {
        return new Generation(generation.incrementAndGet());
    }

    /**
     * Invalidates outstanding generations without scheduling new work.
     *
     * <p>Use this during close or reset paths to make already queued callbacks
     * unable to update state.</p>
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
     * <p>The consumer is invoked synchronously on the calling thread when the
     * token matches. If the token is stale, the method returns {@code false}
     * and the consumer is not invoked.</p>
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
     * <p>The numeric value is only meaningful to the counter that produced it.
     * Application code should compare tokens through the owning
     * {@link GenerationCounter}, not by persisting or interpreting the raw
     * value.</p>
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
