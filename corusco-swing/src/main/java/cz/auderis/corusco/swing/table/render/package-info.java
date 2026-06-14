/**
 * Optional Swing table renderers for high-volume timestamp and state cells.
 *
 * <p>This package solves a narrow presentation problem: some business tables
 * repaint many cells whose values are cheap to identify but relatively
 * repetitive to render. Event logs, audit tables, market-data grids, process
 * monitors, and time-series screens often contain timestamp columns where many
 * adjacent values share the same date, hour, or minute. Status tables often
 * contain boolean or enum values whose visible representation is drawn again
 * and again. The renderers in this package let an application opt into bounded
 * renderer-local caches for those hot cells without changing the table model or
 * the domain row objects.</p>
 *
 * <p>The main entry point is {@link
 * cz.auderis.corusco.swing.table.render.OptimizedTableRenderers}. It installs
 * renderers either as {@link javax.swing.JTable} default renderers for a value
 * type or on a single descriptor-backed column identified by {@link
 * cz.auderis.corusco.core.table.ColumnKey}. Installation returns a {@link
 * cz.auderis.corusco.swing.binding.Binding}; closing that binding restores the
 * renderer that was present before installation. This matches the lifecycle
 * style used by the other Swing table bindings in
 * {@code cz.auderis.corusco.swing.table}.</p>
 *
 * <pre>{@code
 * TimestampRendererOptions options = TimestampRendererOptions.defaults();
 * Binding timestampRenderer = OptimizedTableRenderers.installTimestampRenderer(
 *         table,
 *         model,
 *         OrderEventColumns.RECEIVED_AT_KEY,
 *         options
 * );
 *
 * Binding stateRenderer = OptimizedTableRenderers.installStateRenderer(
 *         table,
 *         OrderEventColumns.STATE_KEY.valueType(),
 *         StateRendererOptions.defaults()
 * );
 *
 * // Later, usually through a BindingScope:
 * timestampRenderer.close();
 * stateRenderer.close();
 * }</pre>
 *
 * <p>The package intentionally belongs to {@code corusco-swing}, not
 * {@code corusco-core}. Core table descriptors define typed values, stable
 * column identities, capabilities, layout defaults, and persistence metadata.
 * They do not know about Swing renderers, fonts, colors, antialiasing,
 * high-DPI transforms, focus borders, or selection painting. This package uses
 * core descriptors only as explicit addresses for Swing installation.</p>
 *
 * <p>All installers and returned bindings are Event Dispatch Thread confined.
 * Create, install, use, and close them on the Swing EDT. Renderer instances are
 * owned by the table or table column on which they are installed; do not share
 * one renderer instance between unrelated tables. Each renderer owns a bounded
 * cache, and the cache key includes visual state such as font, foreground,
 * background, selection, focus, enabled state, row height, and graphics scale.
 * Those keys are deliberately conservative so stale bitmaps are less likely to
 * survive look-and-feel or table-state changes.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.table.render.TimestampRendererOptions}
 * configures rendering of {@code long} or {@link java.lang.Long} epoch values.
 * The timestamp renderer converts the epoch value through an {@link
 * cz.auderis.corusco.swing.table.render.EpochUnit}, a {@link java.time.ZoneId},
 * and a fixed {@link java.time.format.DateTimeFormatter} pattern. The optional
 * bitmap-prefix cache is designed for fixed ISO-like output such as
 * {@code yyyy-MM-dd HH:mm:ss.SSS}; arbitrary formatter patterns still render
 * correctly but may not benefit from prefix segmentation.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.table.render.StateRendererOptions}
 * configures cached rendering for finite state values, primarily booleans and
 * enums. Boolean text is supplied explicitly. Enum values can render either
 * through {@link java.lang.Enum#name()} for stable technical state names or
 * through {@link Object#toString()} when the application owns the display text.
 * The package does not localize state labels by itself; callers should pass
 * already-localized text or use application-specific renderers where dynamic
 * localization is required.</p>
 *
 * <p>These renderers are opt-in performance tools, not replacements for the
 * ordinary Swing renderer pipeline. Use them after profiling or for tables
 * known to repaint very large numbers of repetitive cells. For small tables,
 * editable cells, rich HTML labels, icons, or highly dynamic per-row styling,
 * the default Swing renderers or application-specific renderers are usually
 * clearer. Tests for this package should verify rendering correctness and
 * lifecycle restoration; benchmark results should guide whether bitmap caching
 * is actually useful for a specific workload.</p>
 */
package cz.auderis.corusco.swing.table.render;
