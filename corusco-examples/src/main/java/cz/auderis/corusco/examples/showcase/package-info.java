/**
 * Rich desktop showcase for evaluating Corusco in a business Swing application.
 *
 * <p>The showcase is intentionally larger than the focused examples in sibling
 * packages. It uses showcase-local {@code @SwingForm}, {@code @SwingTable},
 * {@code @Column}, validation, help, and {@code @UiAction} annotations, then
 * consumes the generated form models, table descriptors, resource keys, and
 * command factories in the Swing UI. Runtime code is responsible for layout,
 * bindings, renderer installation, and data loading; generated metadata owns
 * stable ids, descriptors, validators, and action resources.</p>
 *
 * <p>The application shell presents generated form metadata, typed table
 * descriptors, validation, commands, resources, status text, accessibility,
 * table-state persistence, Glazed Lists row sources, optimized renderers,
 * background tasks, busy overlays, dialogs, and an H2-backed time-series table
 * in one decision-oriented desktop demo.</p>
 */
package cz.auderis.corusco.examples.showcase;
