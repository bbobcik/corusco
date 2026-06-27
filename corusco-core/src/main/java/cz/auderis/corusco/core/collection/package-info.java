/**
 * Observable collection models, change descriptions, and derived collection
 * views for Corusco presentation state.
 *
 * <p>This package is the list-oriented counterpart to
 * {@link cz.auderis.corusco.core.value}. It is the right place to start when a
 * presenter, form model, table model, or adapter needs ordered data that can be
 * observed without depending on Swing. The central read contract is
 * {@link cz.auderis.corusco.core.collection.ObservableReadableCollection}: it
 * exposes ordered indexed reads, immediate stream traversal, immutable
 * snapshots, and listener registration.
 * {@link cz.auderis.corusco.core.collection.ObservableList} extends that
 * contract with explicit mutation methods and batching for application-owned
 * lists.</p>
 *
 * <p>{@link cz.auderis.corusco.core.collection.ObservableArrayList} is the
 * default in-memory list implementation for application-owned rows or choices.
 * {@link cz.auderis.corusco.core.collection.ObservableSortedSet} owns unique
 * sorted contents while exposing the same ordered read surface for read-only
 * views.
 * Mutating it produces {@link cz.auderis.corusco.core.collection.ListChange}
 * instances grouped into a
 * {@link cz.auderis.corusco.core.collection.ListChangeSet}. Listeners implement
 * {@link cz.auderis.corusco.core.collection.ListChangeListener} and remain
 * registered until the returned
 * {@link cz.auderis.corusco.core.lifecycle.Subscription} is closed.</p>
 *
 * <p>Each delivered {@code ListChangeSet} also carries a {@link
 * cz.auderis.corusco.core.value.ChangeOrigin}. Convenience mutators use {@link
 * cz.auderis.corusco.core.value.StandardChangeOrigin#MODEL}. Call the
 * origin-aware overloads when a list mutation represents user input, binding
 * propagation, generated code, framework work, or another stable custom
 * origin. Derived views preserve the origin of source changes; local view
 * resets such as predicate or comparator replacement accept their own origin.</p>
 *
 * <p>Core collection implementations use {@link
 * cz.auderis.corusco.core.lifecycle.ListenerSet} for their own listener
 * storage. Registering the same listener instance twice has set semantics, and
 * a change event is delivered to the listener snapshot captured before
 * dispatch starts.</p>
 *
 * <p>Use {@link
 * cz.auderis.corusco.core.collection.ObservableReadableCollection#stream()}
 * for immediate traversal when a durable copy is unnecessary, such as
 * {@code anyMatch}, {@code findFirst}, {@code map}, or {@code count}. Use
 * {@link cz.auderis.corusco.core.collection.ObservableReadableCollection#snapshot()}
 * or {@link
 * cz.auderis.corusco.core.collection.ObservableReadableCollection#snapshotStream()}
 * when code needs a stable point-in-time view that survives later source
 * mutation.</p>
 *
 * <p>Use {@link cz.auderis.corusco.core.collection.FilteredList},
 * {@link cz.auderis.corusco.core.collection.SortedList},
 * {@link cz.auderis.corusco.core.collection.MappedReadableCollection}, and
 * {@link cz.auderis.corusco.core.collection.MappedList} when a view needs a
 * read-only projection of another observable collection, for example a search
 * result, sorted row list, filtered choice list, or display-object mapping. Use
 * {@link cz.auderis.corusco.core.collection.LoadableList} when a presenter owns
 * a row cache loaded from an external source and needs a
 * {@link cz.auderis.corusco.core.lifecycle.Detachable} cleanup boundary.</p>
 *
 * <p>Use {@link cz.auderis.corusco.core.collection.CollectionItemValue},
 * {@link cz.auderis.corusco.core.collection.CollectionMatchingValue}, and
 * {@link cz.auderis.corusco.core.collection.CollectionDerivedValue} when a
 * collection needs to feed scalar observable state, such as the current row,
 * selected-key row, row count, empty flag, stream predicate result, or
 * snapshot-derived label. Use {@link
 * cz.auderis.corusco.core.collection.MasterDetailCollection} when a scalar
 * master value chooses the active detail collection.</p>
 *
 * <p>The package is deliberately Swing-free. Swing list and combo-box adapters
 * live in {@code cz.auderis.corusco.swing.collection}; table adapters live in
 * {@code cz.auderis.corusco.swing.table}. Core list notifications are delivered
 * according to the list implementation, typically synchronously on the mutating
 * thread. They are not automatically marshalled to the Swing Event Dispatch
 * Thread.</p>
 *
 * <p>A common usage flow is to create an observable list or sorted set in a
 * presenter, pass it to a derived view or Swing adapter, mutate it through the
 * owner API, and close subscriptions or detachable owners when the view is
 * disposed. Observable collections do not accept null elements; mapping
 * functions must likewise return non-null values. Avoid exposing mutable
 * implementation internals directly; use streams for immediate inspection and
 * snapshots for retained contents. Public collection type parameters use
 * explicit JSpecify non-null bounds to match that runtime contract.</p>
 */
@NullMarked
package cz.auderis.corusco.core.collection;

import org.jspecify.annotations.NullMarked;
