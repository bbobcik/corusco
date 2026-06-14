/**
 * Typed resource lookup APIs for generated and handwritten presentation
 * metadata.
 *
 * <p>This package resolves {@link cz.auderis.corusco.core.key.ResourceKey}
 * instances to runtime values. Start with
 * {@link cz.auderis.corusco.core.resource.Resources}, the lookup contract used
 * by command adapters, descriptor-derived labels, tooltips, accessible text,
 * and examples. {@link cz.auderis.corusco.core.resource.MapResources} is the
 * simple immutable implementation for tests, examples, and small applications.</p>
 *
 * <p>Resource keys are typed. A lookup verifies that the returned value matches
 * the key's declared value type, so metadata can safely ask for a string,
 * icon-like value, or another supported resource type without downcasting from
 * an untyped map. Missing optional resources are represented separately from
 * configuration errors; {@link cz.auderis.corusco.core.resource.ResourceException}
 * reports required misses or type mismatches.</p>
 *
 * <p>The package does not prescribe localization, reload behavior, resource
 * bundle structure, or Swing rendering. Applications can bridge it to their own
 * localization infrastructure. Swing code should resolve resources at the UI
 * boundary and keep stable ids separate from user-visible text.</p>
 */
package cz.auderis.corusco.core.resource;
