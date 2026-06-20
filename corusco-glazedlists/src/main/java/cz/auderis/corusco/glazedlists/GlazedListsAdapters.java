package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.EventList;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;

/**
 * Entry point for adapting Glazed Lists collections to Corusco collection APIs.
 *
 * <p>Use this utility at the boundary between Corusco observable collections
 * and Glazed Lists. {@link #observableList(EventList)} adapts an existing
 * Glazed Lists source as a mutable Corusco observable list.
 * {@link #eventListMirror(ObservableReadableCollection)} mirrors a Corusco
 * readable collection into a read-only Glazed Lists event list.</p>
 *
 * <p>The returned objects follow the detailed contracts documented by
 * {@link GlazedObservableList} and {@link GlazedReadableCollectionMirror}.
 * Both register listeners and must be closed when no longer needed. Source
 * ownership stays with the caller; adapters remove subscriptions on close but
 * do not dispose application-owned collections.</p>
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

    /**
     * Mirrors a Corusco readable collection into a read-only Glazed Lists event
     * list.
     *
     * <p>The returned mirror owns an internal event list and must be closed
     * when no longer needed. Ownership of the source collection stays with the
     * caller.</p>
     *
     * @param source source observable collection, not {@code null}
     * @param <E> element type
     * @return event-list mirror
     */
    public static <E> GlazedReadableCollectionMirror<E> eventListMirror(
            ObservableReadableCollection<E> source
    ) {
        return GlazedReadableCollectionMirror.of(source);
    }
}
