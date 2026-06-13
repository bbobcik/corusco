package cz.auderis.corusco.core.dialog;

import java.util.Objects;
import java.util.Optional;

/**
 * Terminal result of a form dialog.
 *
 * <p>The result is immutable and Swing-free. Accepted results carry the
 * committed domain value; cancelled results intentionally carry no value so
 * callers cannot accidentally read a partially edited object.</p>
 *
 * @param <R> committed result type
 */
public sealed interface DialogResult<R> permits DialogResult.Accepted, DialogResult.Cancelled {

    /**
     * Creates an accepted dialog result.
     *
     * @param value committed value
     * @param <R> committed result type
     * @return accepted result
     */
    static <R> DialogResult<R> accepted(R value) {
        return new Accepted<>(value);
    }

    /**
     * Creates a cancelled dialog result.
     *
     * @param <R> committed result type
     * @return cancelled result
     */
    static <R> DialogResult<R> cancelled() {
        return new Cancelled<>();
    }

    /**
     * Indicates whether this result is accepted.
     *
     * @return {@code true} for accepted results
     */
    boolean isAccepted();

    /**
     * Returns the committed value for accepted results.
     *
     * @return optional committed value
     */
    Optional<R> acceptedValue();

    /**
     * Accepted dialog result.
     *
     * @param value committed value
     * @param <R> committed result type
     */
    record Accepted<R>(R value) implements DialogResult<R> {

        /**
         * Creates an accepted result.
         *
         * @param value committed value
         */
        public Accepted {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public boolean isAccepted() {
            return true;
        }

        @Override
        public Optional<R> acceptedValue() {
            return Optional.of(value);
        }
    }

    /**
     * Cancelled dialog result.
     *
     * @param <R> committed result type
     */
    record Cancelled<R>() implements DialogResult<R> {

        @Override
        public boolean isAccepted() {
            return false;
        }

        @Override
        public Optional<R> acceptedValue() {
            return Optional.empty();
        }
    }
}
