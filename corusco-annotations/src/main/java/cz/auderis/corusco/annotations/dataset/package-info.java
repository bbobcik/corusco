/**
 * Compile-time annotations for generated fixed-schema data-set descriptors.
 *
 * <p>These annotations describe semantic time-series and tidy-data columns on
 * records whose structure is known at compile time. The processor turns them
 * into core data-set descriptor companions and generated columnar frames.
 * Runtime code should consume those generated companions rather than scanning
 * annotations reflectively.</p>
 *
 * <p>The annotations are separate from {@code @CoruscoTable} and
 * {@code @Column}. Table annotations describe Swing/presentation behavior such
 * as headers, widths, visibility, and persistence. Data-set annotations
 * describe schema semantics such as time axes, dimensions, measures, units,
 * missing values, quality state, and supported aggregations. A record may use
 * both when the same source type should support generated tables and generated
 * data-set descriptors.</p>
 *
 * <p>Enum-valued annotation attributes use the core dataset vocabulary
 * directly. This keeps source annotations and generated runtime descriptors
 * aligned: there is one {@code DataColumnRole}, one {@code MissingPolicy}, one
 * {@code QualityPolicy}, and one {@code AggregationFunction} model.</p>
 */
package cz.auderis.corusco.annotations.dataset;
