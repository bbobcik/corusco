/**
 * Technology-neutral read models for enterprise table data.
 *
 * <h2>Purpose</h2>
 *
 * <p>This package describes windowed data access without choosing JDBC, REST,
 * Kafka, Swing, Glazed Lists, or a repository abstraction. It is the core
 * contract for screens where a table-like view is backed by a queryable data
 * set rather than by a small in-memory list. The package models ranges,
 * filters, sort terms, counts, page status, row identity, and key-based
 * selection; adapters remain responsible for translating those facts to a
 * concrete storage or UI technology.</p>
 *
 * <p>A {@link cz.auderis.corusco.core.data.CoruscoDataRequest} combines a
 * {@link cz.auderis.corusco.core.data.CoruscoDataRange}, a
 * {@link cz.auderis.corusco.core.data.CoruscoDataQuery}, and a refresh
 * generation. A {@link cz.auderis.corusco.core.data.CoruscoDataLoader} turns
 * that request into an immutable
 * {@link cz.auderis.corusco.core.data.CoruscoDataPage}, and a
 * {@link cz.auderis.corusco.core.data.CoruscoDataSource} exposes the current
 * visible window as observable presenter state.</p>
 *
 * <h2>Identity and selection</h2>
 *
 * <p>Rows are identified by stable keys through
 * {@link cz.auderis.corusco.core.data.CoruscoRowIdentity}. Selection and edit
 * staging use those keys instead of current view indexes so sorting,
 * filtering, and virtual paging do not invalidate user intent. This is a
 * deliberate separation from Swing: a {@code JTable} still has view and model
 * row coordinates, but presenter state should carry row keys when selection
 * must survive refreshes, paging, or server-side sorting.</p>
 *
 * <p>{@link cz.auderis.corusco.core.data.CoruscoRowSelection} can represent an
 * empty selection, an explicit set of selected keys, or all rows matching a
 * query except a small excluded-key set. That last mode is the scalable
 * "select all" shape: selecting a million matching rows should not require a
 * million keys in memory merely to preserve the user's intent.</p>
 *
 * <h2>Window ownership</h2>
 *
 * <p>The default source keeps one current immutable row snapshot. Replacing a
 * page swaps the backing list and emits one collection change set for the page
 * replacement, not one event per loaded row. Refreshing keeps old rows visible
 * until a newer page succeeds. Failures are recorded in
 * {@link cz.auderis.corusco.core.data.CoruscoDataStatus}; the old rows remain
 * available so a screen can report "showing previous results" rather than
 * clearing useful data by accident.</p>
 *
 * <h2>Technology boundary</h2>
 *
 * <p>Query and filter records are deliberately small and transport-neutral.
 * They name stable field or column ids and simple operators; they do not claim
 * to be SQL, JPQL, REST query parameters, or an in-memory predicate language.
 * Backend adapters decide which ids and operators they support and how to
 * translate them. Live feeds, backpressure, time-series shapes, and tidy-data
 * tables are outside this package's scope.</p>
 *
 * <h2>Threading model</h2>
 *
 * <p>The package follows the core module's presenter-owned model convention.
 * Observable state changes are synchronous on the callback executor used by
 * the owning {@link cz.auderis.corusco.core.task.TaskService}. The default
 * source does not add hidden synchronization for steady-state reads. Swing
 * applications should use a Swing-aware task service or otherwise ensure that
 * callbacks touching Swing-bound models are delivered on the Event Dispatch
 * Thread.</p>
 */
@org.jspecify.annotations.NullMarked
package cz.auderis.corusco.core.data;
