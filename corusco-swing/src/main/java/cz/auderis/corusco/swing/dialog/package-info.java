/**
 * Swing form dialog controllers, lifecycle helpers, and validation/cancel
 * policies.
 *
 * <p>This package coordinates Corusco form models with modal Swing dialog
 * semantics. Start with {@link cz.auderis.corusco.swing.dialog.FormDialog},
 * which owns accept/cancel flow, validation checks, dirty-cancel confirmation,
 * and result creation. The dialog controller consumes a
 * {@link cz.auderis.corusco.core.form.FormModel}; it does not replace the form
 * model or store domain data itself.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.dialog.FormDialogLifecycle} is the cleanup
 * owner for bindings, task services, detachables, and arbitrary disposables
 * created while a dialog is active. {@link
 * cz.auderis.corusco.swing.dialog.FormDialogKeyboardBinding} installs default
 * button and Escape handling. {@link
 * cz.auderis.corusco.swing.dialog.FormDialogValidationBinding} connects
 * problem state to summaries and focus behavior.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.dialog.DirtyState} and
 * {@link cz.auderis.corusco.swing.dialog.CancelConfirmation} let applications
 * decide when cancellation should ask the user before discarding edits. {@link
 * cz.auderis.corusco.swing.dialog.ProblemFocusResolver} maps typed problems to
 * components so failed commit attempts can move focus to useful locations
 * without reflection or string property paths.</p>
 *
 * <p>Dialog helpers are Swing code and should be created, used, and closed on
 * the Event Dispatch Thread. Model validation and result creation remain in the
 * core form layer; this package owns dialog lifecycle, keyboard/focus policy,
 * and component-level presentation.</p>
 */
package cz.auderis.corusco.swing.dialog;
