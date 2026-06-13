package cz.auderis.corusco.core.lifecycle;

/**
 * A model or adapter that can release cached presentation data without being
 * permanently closed.
 *
 * <p>Detaching is a lifecycle hint for inactive views and presenters. A
 * detached object remains reusable unless a more specific implementation says
 * otherwise; the next access may attach again by reloading or rebuilding the
 * released state. Implementations should make {@link #detach()} idempotent and
 * should not use it as a listener-cleanup replacement for {@link Disposable}
 * ownership.</p>
 */
@FunctionalInterface
public interface Detachable {

    /**
     * Releases cached state that can be loaded again later.
     */
    void detach();
}
