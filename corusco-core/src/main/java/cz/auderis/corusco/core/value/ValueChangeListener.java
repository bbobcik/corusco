package cz.auderis.corusco.core.value;

/**
 * Listener for synchronous value change events.
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
