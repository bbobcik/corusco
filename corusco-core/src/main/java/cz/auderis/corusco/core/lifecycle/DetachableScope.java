package cz.auderis.corusco.core.lifecycle;

import java.util.List;
import java.util.function.Consumer;

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
public final class DetachableScope extends AbstractScope<Detachable> implements Detachable, Disposable {

    /**
     * Creates an empty detachable scope.
     */
    public DetachableScope() {
        super();
    }

    /**
     * Detaches all registered children while keeping them registered.
     */
    @Override
    public void detach() {
        detachChildren();
    }

    @Override
    protected String closeFailureMessage() {
        return "One or more detachables failed to detach";
    }

    private void detachChildren() {
        final List<Detachable> reverseDetachables = childrenSnapshot().reversed();
        if (reverseDetachables.isEmpty()) {
            return;
        }
        final Consumer<Detachable> action = Detachable::detach;
        processChildren(reverseDetachables, action, "One or more detachables failed to detach");
    }

    @Override
    protected void closeChild(Detachable child) {
        child.detach();
    }

    @Override
    protected ScopeException failure(String message) {
        return new DetachmentException(message);
    }

}
