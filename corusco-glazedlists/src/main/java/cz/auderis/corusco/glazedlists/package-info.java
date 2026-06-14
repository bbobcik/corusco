/**
 * Optional Glazed Lists interoperability for Corusco observable collections.
 *
 * <p>This module is for applications that already use Glazed Lists
 * {@code EventList} instances but want to consume Corusco observable-list APIs.
 * Start with {@link cz.auderis.corusco.glazedlists.GlazedListsAdapters}, whose
 * factory methods create adapters without moving the source data into a
 * separate Corusco list. {@link cz.auderis.corusco.glazedlists.GlazedObservableList}
 * is the concrete adapter, and {@link cz.auderis.corusco.glazedlists.CoruscoGlazedLists}
 * is only a small module marker/diagnostics utility.</p>
 *
 * <p>The wrapped {@code EventList} remains the storage owner. The adapter
 * translates Glazed Lists events into Corusco
 * {@link cz.auderis.corusco.core.collection.ListChangeSet} notifications and
 * uses the Glazed Lists read/write lock for adapter reads and direct adapter
 * mutations. Closing the adapter removes its listener; it does not dispose the
 * source event list.</p>
 *
 * <p>Core Corusco intentionally does not depend on Glazed Lists. Use this
 * package only at the integration boundary. If the adapted list is later bound
 * to Swing, the same EDT considerations as other observable lists apply.</p>
 */
package cz.auderis.corusco.glazedlists;
