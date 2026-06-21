package cz.auderis.corusco.core.lifecycle;

import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Callback-backed subscription that claims and clears its cleanup atomically.
 *
 * <p>The cleanup reference itself is the one-shot state. Closing performs an
 * atomic get-and-set to {@code null}; the thread that receives the non-null
 * cleanup runs it, and all later callers observe an already closed
 * subscription.</p>
 */
final class IdempotentSubscription implements Subscription {

    private static final VarHandle CLEANUP;
    static {
        try {
            CLEANUP = MethodHandles
                    .lookup()
                    .findVarHandle(IdempotentSubscription.class, "cleanup", Disposable.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile @Nullable Disposable cleanup;

    IdempotentSubscription(Disposable cleanup) {
        this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
    }

    @Override
    public void close() {
        final Disposable cleanup = (Disposable) CLEANUP.getAndSet(this, null);
        if (null != cleanup) {
            cleanup.close();
        }
    }

}
