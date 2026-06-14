/**
 * Toolkit-neutral dialog result values shared by Swing and non-Swing code.
 *
 * <p>This package contains {@link cz.auderis.corusco.core.dialog.DialogResult},
 * the small result object used to distinguish accepted dialog output from
 * cancellation. It lets form models, presenters, examples, and tests express
 * modal outcomes without referencing {@code javax.swing.JDialog} or any other
 * toolkit class.</p>
 *
 * <p>Swing dialog controllers and lifecycle helpers live in
 * {@code cz.auderis.corusco.swing.dialog}. Those classes decide when a result
 * is produced, when validation blocks acceptance, and when cancellation should
 * be confirmed. This package only represents the final outcome value that can
 * cross architectural boundaries.</p>
 */
package cz.auderis.corusco.core.dialog;
