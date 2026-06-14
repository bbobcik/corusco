/**
 * Root package for Swing integration built on Corusco core models.
 *
 * <p>Use {@link cz.auderis.corusco.swing.binding} for disposable component
 * bindings, {@link cz.auderis.corusco.swing.collection} for Swing list and
 * combo-box models backed by observable lists,
 * {@link cz.auderis.corusco.swing.table} for descriptor-backed table models
 * and persisted column state, {@link cz.auderis.corusco.swing.dialog} for
 * modal form semantics, and {@link cz.auderis.corusco.swing.command} for
 * adapting command metadata to Swing actions.</p>
 *
 * <p>Swing APIs in this package family are generally EDT-confined. Construct,
 * mutate, and close bindings and controllers on the Event Dispatch Thread
 * unless an individual type documents a stronger or weaker rule. Background
 * task helpers in {@link cz.auderis.corusco.swing.task} provide explicit
 * boundaries for worker-thread results entering Swing.</p>
 */
package cz.auderis.corusco.swing;
