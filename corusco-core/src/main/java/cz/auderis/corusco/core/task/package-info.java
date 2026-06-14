/**
 * Swing-free task execution, cooperative cancellation, and stale-result
 * suppression primitives.
 *
 * <p>This package is for work initiated by a UI or presenter but executed
 * outside the immediate caller flow. Start with
 * {@link cz.auderis.corusco.core.task.TaskService}: it accepts
 * {@link cz.auderis.corusco.core.task.UiTask} bodies, exposes aggregate busy
 * state, and returns a {@link cz.auderis.corusco.core.task.TaskHandle} for each
 * submitted task. {@link cz.auderis.corusco.core.task.DefaultTaskService} is
 * the standard implementation, and {@link
 * cz.auderis.corusco.core.task.TaskCallbacks} groups terminal callbacks.</p>
 *
 * <p>Cancellation is cooperative rather than forced. The owner keeps a
 * {@link cz.auderis.corusco.core.task.CancellationSource}; task code receives a
 * read-only {@link cz.auderis.corusco.core.task.CancellationToken} and checks
 * it at sensible points. {@link
 * cz.auderis.corusco.core.task.TaskCancelledException} is the conventional
 * exception path for task bodies that stop because cancellation was requested.</p>
 *
 * <p>{@link cz.auderis.corusco.core.task.GenerationCounter} solves a related
 * UI problem: stale asynchronous results. A form field, search box, or master
 * selection can advance a generation before starting work; completion handlers
 * compare the generation before publishing the result.</p>
 *
 * <p>The core task APIs do not touch Swing and do not define EDT behavior by
 * themselves. Callback delivery depends on the executor chosen by the task
 * service factory. Swing code normally uses
 * {@code cz.auderis.corusco.swing.task}, which creates services that report
 * completion back on the Event Dispatch Thread.</p>
 */
package cz.auderis.corusco.core.task;
