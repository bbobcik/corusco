/**
 * Observable list models, change descriptions, and derived collection views for
 * Corusco presentation state.
 *
 * <p>This package is the list-oriented counterpart to
 * {@link cz.auderis.corusco.core.value}. It is the right place to start when a
 * presenter, form model, table model, or adapter needs ordered data that can be
 * observed without depending on Swing. The central contract is
 * {@link cz.auderis.corusco.core.collection.ObservableList}: it exposes indexed
 * reads, explicit mutation methods, immutable snapshots, batching, and listener
 * registration.</p>
 *
 * <p>{@link cz.auderis.corusco.core.collection.ObservableArrayList} is the
 * default in-memory implementation for application-owned rows or choices.
 * Mutating it produces {@link cz.auderis.corusco.core.collection.ListChange}
 * instances grouped into a
 * {@link cz.auderis.corusco.core.collection.ListChangeSet}. Listeners implement
 * {@link cz.auderis.corusco.core.collection.ListChangeListener} and remain
 * registered until the returned
 * {@link cz.auderis.corusco.core.lifecycle.Subscription} is closed.</p>
 *
 * <p>Use {@link cz.auderis.corusco.core.collection.FilteredList} when a view
 * needs a read-only projection of another observable list, for example a search
 * result or filtered choice list. Use
 * {@link cz.auderis.corusco.core.collection.LoadableList} when a presenter owns
 * a row cache loaded from an external source and needs a
 * {@link cz.auderis.corusco.core.lifecycle.Detachable} cleanup boundary.</p>
 *
 * <p>The package is deliberately Swing-free. Swing list and combo-box adapters
 * live in {@code cz.auderis.corusco.swing.collection}; table adapters live in
 * {@code cz.auderis.corusco.swing.table}. Core list notifications are delivered
 * according to the list implementation, typically synchronously on the mutating
 * thread. They are not automatically marshalled to the Swing Event Dispatch
 * Thread.</p>
 *
 * <p>A common usage flow is to create an observable list in a presenter, pass it
 * to a derived view or Swing adapter, mutate it through the list API, and close
 * subscriptions or detachable owners when the view is disposed. Avoid exposing
 * mutable implementation internals directly; use snapshots when code only needs
 * to inspect current contents.</p>
 */
package cz.auderis.corusco.core.collection;
