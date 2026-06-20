/**
 * Optional Glazed Lists interoperability for Corusco observable collections.
 *
 * <h2>When To Use This Package</h2>
 *
 * <p>Use this package when an application needs to bridge Corusco observable
 * collections and Glazed Lists {@code EventList} instances. It supports both
 * directions: exposing an application-owned {@code EventList} through
 * Corusco's {@code ObservableList} API, and mirroring a Corusco readable
 * collection into a read-only {@code EventList}. This is common during
 * migration or in applications that already rely on Glazed Lists sorting,
 * filtering, or event infrastructure.</p>
 *
 * <p>Do not use this package just to create a normal Corusco list. If the
 * application does not already need Glazed Lists, start with
 * {@code cz.auderis.corusco.core.collection.ObservableArrayList} or another
 * core collection type.</p>
 *
 * <p>The package is an integration adapter, not a second collection model for
 * all Corusco code. Its value is highest when Glazed Lists already owns
 * filtering, sorting, event delivery, or shared data structures that other
 * application code depends on.</p>
 *
 * <h2>Mental Model</h2>
 *
 * <p>For {@code EventList -> ObservableList} adaptation, the Glazed Lists
 * {@code EventList} remains the storage owner. Corusco does not copy the
 * elements into a separate list. Instead, the adapter presents a Corusco
 * observable-list view over the existing event list.</p>
 *
 * <p>For {@code ObservableReadableCollection -> EventList} mirroring,
 * {@link cz.auderis.corusco.glazedlists.GlazedReadableCollectionMirror} owns
 * an internal event list initialized from the Corusco source order and keeps
 * it synchronized from Corusco change events. Callers receive a read-only
 * Glazed Lists view; the Corusco source remains the owner.</p>
 *
 * <p>{@link cz.auderis.corusco.glazedlists.GlazedObservableList} is the
 * mutable adapter from Glazed Lists to Corusco.
 * {@link cz.auderis.corusco.glazedlists.GlazedReadableCollectionMirror} is the
 * read-only mirror from Corusco to Glazed Lists.
 * {@link cz.auderis.corusco.glazedlists.GlazedListsAdapters} is the small
 * factory entry point.</p>
 *
 * <p>Think of the adapter as a view over the event list. Reading through the
 * adapter reads the source. Mutating through the adapter mutates the source.
 * Closing the adapter disconnects listener translation; it does not clear,
 * close, or otherwise own the source list.</p>
 *
 * <pre>{@code
 * EventList<CustomerRow> eventList = new BasicEventList<>();
 * GlazedObservableList<CustomerRow> rows =
 *         GlazedListsAdapters.observableList(eventList);
 *
 * rows.subscribe(changeSet -> tableAudit.record(changeSet));
 * eventList.add(new CustomerRow("Ada", true));
 *
 * rows.close();
 * }</pre>
 *
 * <pre>{@code
 * ObservableReadableCollection<CustomerRow> sortedRows = sortedCustomers;
 * GlazedReadableCollectionMirror<CustomerRow> mirror =
 *         GlazedListsAdapters.eventListMirror(sortedRows);
 *
 * EventList<CustomerRow> readOnlyEventList = mirror.eventList();
 *
 * mirror.close();
 * }</pre>
 *
 * <h2>Event Translation</h2>
 *
 * <p>{@link cz.auderis.corusco.glazedlists.GlazedObservableList} listens to
 * Glazed Lists events and translates them into Corusco
 * {@link cz.auderis.corusco.core.collection.ListChangeSet} notifications.
 * Inserts, deletes, updates, moves, clears, and reorder-like changes are
 * delivered through the Corusco observable-list contract.</p>
 *
 * <p>{@link cz.auderis.corusco.glazedlists.GlazedReadableCollectionMirror}
 * performs the reverse translation. It applies Corusco inserts, removals,
 * replacements, moves, and clears to its internal event list using source
 * coordinates.</p>
 *
 * <p>Corusco listeners are retained until their returned subscriptions are
 * closed. Closing the adapter removes the adapter's listener from the source
 * event list. Closing the mirror removes its listener from the Corusco source.
 * Closing either object does not dispose the source collection, because
 * ownership remains with the application.</p>
 *
 * <p>Batching follows the Corusco observable-list contract. A batch can group
 * several adapter mutations into one Corusco change delivery. Glazed Lists
 * still controls the underlying source-list semantics and event production.</p>
 *
 * <h2>Locking And Threading</h2>
 *
 * <p>Adapter reads and direct adapter mutations use the Glazed Lists
 * read/write lock. Mirror updates lock the internally owned event list while
 * applying Corusco changes. External code that mutates a shared
 * {@code EventList} should still follow Glazed Lists' own locking and
 * threading rules.</p>
 *
 * <p>If the adapted list is later connected to Swing, Swing rules still apply.
 * Mutations that affect Swing models must be delivered on the Event Dispatch
 * Thread or through a documented EDT boundary. The adapter does not make
 * arbitrary background changes safe for Swing.</p>
 *
 * <p>When in doubt, decide where the boundary belongs before wiring the list to
 * Swing. Some applications keep all table-facing mutations on the EDT. Others
 * perform Glazed Lists work elsewhere and publish results through an explicit
 * EDT adapter. The important part is that the boundary is visible.</p>
 *
 * <h2>Where It Fits</h2>
 *
 * <p>Core Corusco intentionally does not depend on Glazed Lists. This package
 * keeps that dependency at the edge. Application code can use Glazed Lists
 * where it is already useful and still consume Corusco table models, list
 * bindings, or generated table helpers that expect an observable list.</p>
 *
 * <p>Use the adapter at the boundary between the existing Glazed Lists data
 * source and Corusco consumers. Avoid spreading Glazed Lists types throughout
 * code that only needs Corusco's observable-list contract.</p>
 *
 * <p>This boundary keeps optional dependencies optional. Most Corusco packages
 * can remain unaware of Glazed Lists, while applications that need Glazed
 * Lists can still feed Corusco table and list consumers, or feed Glazed Lists
 * consumers from Corusco readable collections.</p>
 *
 * <h2>Migration Pattern</h2>
 *
 * <p>During migration from Glazed Lists to Corusco, keep the existing
 * {@code EventList} as the source of truth. Wrap it once near the presentation
 * boundary, pass the Corusco observable-list view to Corusco consumers, and
 * close the adapter with the view or presenter that owns the integration.</p>
 *
 * <p>During migration in the opposite direction, keep the Corusco collection as
 * the source of truth. Create one mirror near the Glazed Lists boundary, pass
 * the read-only event list to Glazed Lists consumers, and close the mirror with
 * the owner of that boundary.</p>
 *
 * <p>Avoid wrapping the same source list repeatedly for the same screen unless
 * each adapter has a clear owner and cleanup path. Multiple adapters mean
 * multiple source listeners and can make event flow harder to reason about.</p>
 *
 * <h2>Testing</h2>
 *
 * <p>Adapter tests should verify both directions that matter to the
 * application. Mutating the source event list should notify Corusco listeners.
 * Mutating through the adapter should update the source event list and notify
 * listeners according to the observable-list contract. Mirror tests should
 * verify initialization order, source-change translation, read-only event-list
 * exposure, and close-time detachment.</p>
 *
 * <p>Tests should also close subscriptions and the adapter. This confirms that
 * listeners are not retained after the view or presenter using the adapter has
 * been disposed.</p>
 *
 * <p>When testing Swing consumers of an adapted list, separate adapter
 * correctness from Swing-thread correctness. First verify event translation at
 * the adapter level. Then verify that the Swing boundary receives changes on
 * the EDT according to the application's chosen dispatch policy.</p>
 */
package cz.auderis.corusco.glazedlists;
