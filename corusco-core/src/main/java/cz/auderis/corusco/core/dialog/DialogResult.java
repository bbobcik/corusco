package cz.auderis.corusco.core.dialog;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable terminal outcome of a form dialog interaction.
 *
 * <p>Dialog controllers use this Swing-free value to report whether the user
 * accepted a form and, if so, which committed domain object was produced.
 * Accepted results carry the value returned by the form model's commit path.
 * Cancelled results intentionally carry no value so callers cannot accidentally
 * consume a partially edited object after Cancel, Escape, window close, or a
 * dirty-cancel confirmation.</p>
 *
 * <p>The result is one-shot state owned by the dialog interaction, not by the
 * form model. It does not close dialogs or reset forms by itself; it merely
 * records the outcome after the dialog controller has performed validation,
 * active-editor commit, and lifecycle decisions.</p>
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
