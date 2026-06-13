package cz.auderis.corusco.core.lifecycle;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A disposable registration for a listener, callback, or similar attachment.
 *
 * <p>Subscriptions are idempotent: closing a subscription more than once must
 * perform the underlying cleanup at most once. The factory method provided by
 * this interface enforces that behavior for callback-backed registrations.</p>
 */
@FunctionalInterface
public interface Subscription extends Disposable {

    /**
     * A subscription that is already closed and performs no cleanup.
     */
    Subscription EMPTY = () -> {
    };

    /**
     * Creates an idempotent subscription backed by a cleanup callback.
     *
     * @param cleanup cleanup to run the first time the subscription is closed
     * @return an idempotent subscription
     */
    static Subscription of(Disposable cleanup) {
        Objects.requireNonNull(cleanup, "cleanup");
        AtomicBoolean closed = new AtomicBoolean();
        return () -> {
            if (closed.compareAndSet(false, true)) {
                cleanup.close();
            }
        };
    }

    /**
     * Closes this registration.
     *
     * <p>Implementations must be idempotent and release the registered listener
     * or callback at most once.</p>
     */
    @Override
    void close();
}
