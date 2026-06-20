/**
 * Rich desktop showcase for evaluating Corusco in a business Swing application.
 *
 * <p>The showcase is intentionally larger than the focused examples in sibling
 * packages. It uses showcase-local {@code @CoruscoForm}, {@code @CoruscoTable},
 * {@code @Column}, validation, help, and {@code @UiAction} annotations, then
 * consumes the generated form models, table descriptors, resource keys, Swing
 * bindings, and command factories in the Swing UI. Runtime code is responsible
 * for layout, renderer installation, and data loading; generated metadata owns
 * stable ids, descriptors, validators, bindings, and action resources.</p>
 *
 * <p>The application shell presents generated form metadata, typed table
 * descriptors, validation, commands, resources, status text, accessibility,
 * table-state persistence, Glazed Lists row sources, optimized renderers,
 * background tasks, busy overlays, dialogs, and an H2-backed time-series table
 * in one decision-oriented desktop demo.</p>
 */
@cz.auderis.corusco.annotations.SwingCompanionPackage
package cz.auderis.corusco.examples.showcase;
