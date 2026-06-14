package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.EventList;

/**
 * Entry point for adapting Glazed Lists collections to Corusco collection APIs.
 *
 * <p>Use this utility when an application already owns data in a Glazed Lists
 * {@link EventList} and wants to reuse Corusco consumers such as observable
 * list bindings or typed table models. The adapter keeps the EventList as the
 * storage owner and presents a Corusco {@code ObservableList} view; it does not
 * copy the list into a separate model or take responsibility for disposing the
 * EventList.</p>
 *
 * <p>The returned adapters follow the detailed contract documented by
 * {@link GlazedObservableList}: direct adapter reads and mutations use the
 * EventList read/write lock, Glazed Lists events are translated to Corusco
 * change sets, listeners are retained until their subscriptions are closed,
 * and the adapter itself must be closed to remove its source-list listener.
 * External code that mutates the same EventList must still follow Glazed
 * Lists' own threading and locking rules.</p>
 */
public final class GlazedListsAdapters {

    private GlazedListsAdapters() {
        throw new AssertionError("No instances");
    }

    /**
     * Adapts a Glazed Lists event list as a Corusco observable list.
     *
     * <p>The returned adapter registers a listener on the source list and must
     * be closed when no longer needed. Ownership of the source list stays with
     * the caller.</p>
     *
     * @param source source event list, not {@code null}
     * @param <E> element type
     * @return observable-list adapter
     */
    public static <E> GlazedObservableList<E> observableList(EventList<E> source) {
        return GlazedObservableList.of(source);
    }
}
