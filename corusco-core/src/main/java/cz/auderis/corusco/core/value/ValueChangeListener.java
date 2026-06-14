package cz.auderis.corusco.core.value;

/**
 * Callback notified when an observable value changes.
 *
 * <p>Core observable values invoke listeners synchronously on the thread that
 * performs the mutation unless the value implementation documents a different
 * delivery policy. The listener receives the complete {@link ValueChangeEvent}
 * so it can inspect old and new values without querying the source again.</p>
 *
 * @param <T> value type
 */
@FunctionalInterface
public interface ValueChangeListener<T> {

    /**
     * Handles a value change event.
     *
     * <p>Listeners are invoked synchronously by the mutating call. Listener
     * implementations should return quickly and avoid reentrant mutation unless
     * the owning value documents that pattern.</p>
     *
     * @param event change event
     */
    void valueChanged(ValueChangeEvent<T> event);
}
