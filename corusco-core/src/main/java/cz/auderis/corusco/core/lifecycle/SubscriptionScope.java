package cz.auderis.corusco.core.lifecycle;

/**
 * Owns a group of disposable registrations and closes them deterministically.
 *
 * <p>A scope is intended for a single ownership context such as a presenter,
 * behavior, or test fixture. Registration and close state are synchronized, but
 * child cleanup still runs on the calling thread and must obey each child's
 * own threading rules.</p>
 *
 * <p>Children are closed in reverse registration order. Closing the scope is
 * idempotent. If one child fails, the scope still closes remaining children and
 * then throws a {@link ScopeException} containing the cleanup
 * failures as suppressed exceptions. Adding a child after the scope is closed
 * closes that child immediately and returns it.</p>
 */
public final class SubscriptionScope extends AbstractScope<Disposable> implements Disposable {

    /**
     * Creates an empty subscription scope.
     */
    public SubscriptionScope() {
        super();
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

    @Override
    protected void closeChild(Disposable child) {
        child.close();
    }

}
