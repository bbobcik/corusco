package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.EventList;

/**
 * Factory methods for Glazed Lists interop adapters.
 */
public final class GlazedListsAdapters {

    private GlazedListsAdapters() {
        throw new AssertionError("No instances");
    }

    /**
     * Adapts a Glazed Lists event list as a Corusco observable list.
     *
     * @param source source event list
     * @param <E> element type
     * @return observable-list adapter
     */
    public static <E> GlazedObservableList<E> observableList(EventList<E> source) {
        return GlazedObservableList.of(source);
    }
}
