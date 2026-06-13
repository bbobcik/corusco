package cz.auderis.corusco.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Owns detachable children for a presenter or view activation lifecycle.
 *
 * <p>A detachable scope is reusable until it is closed. Calling
 * {@link #detach()} releases cached state in registered children but keeps the
 * registrations for later activation cycles. Calling {@link #close()} detaches
 * once more, clears the registrations, and marks the scope closed.</p>
 *
 * <p>Children are detached in reverse registration order. If one child fails,
 * the scope still attempts to detach every remaining child and then throws a
 * {@link DetachmentException} containing individual failures as suppressed
 * exceptions.</p>
 */
public final class DetachableScope implements Detachable, Disposable {

    private final List<Detachable> children = new ArrayList<>();
    private boolean closed;

    /**
     * Registers a detachable child.
     *
     * <p>If the scope has already been closed, the child is detached
     * immediately and is not retained. This fail-closed behavior prevents a
     * late-created loadable model from keeping expensive cached data after the
     * presenter lifecycle has ended.</p>
     *
     * @param child child to detach with this scope
     * @param <D> child type
     * @return the same child for fluent registration
     */
    public <D extends Detachable> D add(D child) {
        Objects.requireNonNull(child, "child");
        if (closed) {
            child.detach();
            return child;
        }
        children.add(child);
        return child;
    }

    /**
     * Indicates whether this scope has been permanently closed.
     *
     * @return {@code true} after {@link #close()} has been called
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Detaches all registered children while keeping them registered.
     */
    @Override
    public void detach() {
        detachChildren();
    }

    /**
     * Detaches all children, clears registrations, and marks the scope closed.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        try {
            detachChildren();
        } finally {
            children.clear();
            closed = true;
        }
    }

    private void detachChildren() {
        DetachmentException failure = null;
        for (int index = children.size() - 1; index >= 0; index--) {
            try {
                children.get(index).detach();
            } catch (RuntimeException | Error e) {
                if (failure == null) {
                    failure = new DetachmentException("One or more detachables failed to detach");
                }
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
