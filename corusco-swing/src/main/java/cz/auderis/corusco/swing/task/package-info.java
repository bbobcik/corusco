/**
 * Swing adapters for asynchronous task services and busy-state presentation.
 *
 * <p>This package is the Swing boundary for task-related UI behavior. Start
 * with {@link cz.auderis.corusco.swing.task.SwingTaskServices} when a presenter
 * or dialog needs a {@link cz.auderis.corusco.core.task.TaskService} whose
 * background work runs away from the UI thread but whose terminal callbacks are
 * delivered on the Event Dispatch Thread. Use {@link
 * cz.auderis.corusco.swing.task.BusyOverlayBinding} when a busy value should
 * control a {@code javax.swing.JLayer} overlay.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.task.BusyOverlayLayerUI} is the layer UI
 * that paints the translucent cover and consumes input while busy. The binding
 * observes a Corusco readable value, updates the layer UI, and returns a
 * disposable handle that must be closed when the view is disposed. These
 * operations touch Swing components and belong on the Event Dispatch Thread.</p>
 *
 * <p>The underlying task concepts live in
 * {@code cz.auderis.corusco.core.task}. This package does not change the
 * cooperative cancellation model; it only supplies Swing callback dispatch and
 * visual busy feedback. A typical dialog flow is to create a Swing task
 * service, bind its busy value to a layer overlay, submit work from a command,
 * and register both the binding and service with the dialog lifecycle.</p>
 */
package cz.auderis.corusco.swing.task;
