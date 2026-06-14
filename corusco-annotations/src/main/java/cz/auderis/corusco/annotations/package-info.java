/**
 * Root package for the Corusco annotation module.
 *
 * <p>This package intentionally contains only module-level support types such
 * as {@link cz.auderis.corusco.annotations.CoruscoAnnotations}. The annotation
 * vocabulary is split by concern: {@link cz.auderis.corusco.annotations.form}
 * for generated form metadata, {@link cz.auderis.corusco.annotations.table}
 * for generated table metadata, {@link
 * cz.auderis.corusco.annotations.validation} for generated field constraints,
 * {@link cz.auderis.corusco.annotations.command} for action metadata, and
 * {@link cz.auderis.corusco.annotations.help} for tooltip/help-topic
 * metadata.</p>
 *
 * <p>The annotations are source-retained and consumed by compile-time
 * processors through {@code javax.lang.model}. Runtime framework code should
 * use generated keys, descriptors, form models, table columns, and command
 * metadata rather than scanning annotated source types reflectively. Users edit
 * annotated records and methods; generated Java source is an output artifact.</p>
 *
 * <p>Stable ids declared here become resource keys, field keys, action keys,
 * table ids, column ids, and sometimes persisted UI-state ids. Treat id changes
 * as compatibility changes for generated code and saved table preferences.</p>
 */
package cz.auderis.corusco.annotations;
