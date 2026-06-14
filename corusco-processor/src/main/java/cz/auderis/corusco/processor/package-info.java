/**
 * Annotation processor implementation for Corusco generated metadata.
 *
 * <p>This package is primarily for build tools and maintainers. Applications
 * normally use {@link cz.auderis.corusco.processor.CoruscoAnnotationProcessor}
 * as an annotation processor and do not call processor internals directly.
 * {@link cz.auderis.corusco.processor.CoruscoProcessor} is a marker utility;
 * package-private specification and writer types encode the generated-source
 * contracts used by tests.</p>
 *
 * <p>The processor reads source-retained annotations from
 * {@code cz.auderis.corusco.annotations} through {@code javax.lang.model}. It
 * validates annotation placement and combinations, then writes deterministic
 * Java source containing typed keys, form metadata, table descriptors,
 * validation metadata, and action descriptors. Runtime reflection is
 * intentionally outside the supported path.</p>
 *
 * <p>Generated code is meant to be consumed by
 * {@code cz.auderis.corusco.core} and {@code cz.auderis.corusco.swing}. Users
 * should edit annotated source records and methods, not the generated output.
 * Processor tests should use the public test support module rather than
 * depending on private writer details unless they are verifying a generated
 * contract directly.</p>
 */
package cz.auderis.corusco.processor;
