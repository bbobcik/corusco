package cz.auderis.corusco.core.lifecycle;

/**
 * A resource that can be disposed without checked exceptions.
 *
 * <p>Implementations should be idempotent unless their Javadoc states a
 * stronger contract. Calling {@link #close()} releases resources owned by this
 * object, such as listeners, callbacks, or temporary state. Implementations may
 * throw unchecked exceptions when cleanup fails.</p>
 */
@FunctionalInterface
public interface Disposable extends AutoCloseable {

    /**
     * Disposes this resource.
     *
     * <p>The method does not declare checked exceptions so callers can use it in
     * listener and UI cleanup paths without wrapping boilerplate.</p>
     */
    @Override
    void close();
}
