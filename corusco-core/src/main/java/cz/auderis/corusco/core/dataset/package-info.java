/**
 * Fixed-schema data-set descriptors and request models.
 *
 * <p>This package is the Swing-free schema layer for generated time-series and
 * tidy-data sources. A data set is identified by a {@link
 * cz.auderis.corusco.core.dataset.DataSetKey}, described by a {@link
 * cz.auderis.corusco.core.dataset.DataSetDescriptor}, and composed from
 * ordered {@link cz.auderis.corusco.core.dataset.DataColumnDescriptor}
 * instances. The descriptors are intended to be generated from records whose
 * structure is known at compile time, so downstream code can avoid maps,
 * reflection, bean-property lookup, and per-cell schema discovery in hot
 * paths.</p>
 *
 * <p>The core vocabulary is deliberately semantic rather than presentation
 * oriented. Column roles identify time axes, dimensions, measures, quality
 * columns, sequence columns, and auxiliary metadata. Missing-value and quality
 * policies state how values should be interpreted. Unit metadata gives adapters
 * enough information for labels, exports, and request validation without
 * making core responsible for unit conversion.</p>
 *
 * <p>Aggregation and downsampling are represented as request metadata only.
 * {@link cz.auderis.corusco.core.dataset.AggregationRequest} can validate that
 * a request is meaningful for a descriptor, but it does not execute the
 * request. SQL adapters, generated columnar frames, file readers, or service
 * clients decide how to perform the operation.</p>
 *
 * <p>The package does not render charts, own Swing table state, define external
 * JSON names, or prescribe storage formats. Generated companions and adapters
 * should consume these descriptors as the schema authority, then layer UI,
 * serialization, persistence, or analytics behavior on top.</p>
 */
@org.jspecify.annotations.NullMarked
package cz.auderis.corusco.core.dataset;
