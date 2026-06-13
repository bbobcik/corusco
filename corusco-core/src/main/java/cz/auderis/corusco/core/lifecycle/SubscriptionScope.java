package cz.auderis.corusco.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Owns a group of disposable registrations and closes them deterministically.
 *
 * <p>A scope is intended for a single ownership context such as a presenter,
 * behavior, or test fixture. It is not synchronized; callers that share a scope
 * across threads must provide their own coordination.</p>
 *
 * <p>Children are closed in reverse registration order. Closing the scope is
 * idempotent. If one child fails, the scope still closes remaining children and
 * then throws a {@link SubscriptionScopeException} containing the cleanup
 * failures as suppressed exceptions. Adding a child after the scope is closed
 * closes that child immediately and returns it.</p>
 */
public final class SubscriptionScope implements Disposable {

    private final List<Disposable> children = new ArrayList<>();
    private boolean closed;

    /**
     * Registers a disposable child owned by this scope.
     *
     * <p>If this scope is already closed, the child is closed immediately. This
     * makes late registration fail closed and avoids leaking listener-style
     * registrations created during teardown.</p>
     *
     * @param child child resource to register
     * @param <D> child type
     * @return the same child for fluent registration
     */
    public <D extends Disposable> D add(D child) {
        Objects.requireNonNull(child, "child");
        if (closed) {
            child.close();
            return child;
        }
        children.add(child);
        return child;
    }

    /**
     * Registers a cleanup callback as an idempotent subscription.
     *
     * @param cleanup cleanup to run when this scope closes
     * @return the created subscription
     */
    public Subscription onClose(Disposable cleanup) {
        return add(Subscription.of(cleanup));
    }

    /**
     * Indicates whether this scope has been closed.
     *
     * @return {@code true} after {@link #close()} has been called
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Closes all owned children in reverse registration order.
     *
     * <p>Each child is closed at most once by this scope. If multiple children
     * fail, all failures are attached as suppressed exceptions to the thrown
     * {@link SubscriptionScopeException}.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        List<Disposable> closing = List.copyOf(children);
        children.clear();

        SubscriptionScopeException failure = null;
        for (int index = closing.size() - 1; index >= 0; index--) {
            try {
                closing.get(index).close();
            } catch (RuntimeException | Error e) {
                if (failure == null) {
                    failure = new SubscriptionScopeException("One or more subscriptions failed to close");
                }
                failure.addSuppressed(e);
            }
        }

        if (failure != null) {
            throw failure;
        }
    }
}
