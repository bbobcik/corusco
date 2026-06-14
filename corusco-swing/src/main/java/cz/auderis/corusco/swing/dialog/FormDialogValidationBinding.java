package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Binds a form dialog's problem state to validation summary and focus behavior.
 *
 * <p>This binding is the Swing presentation companion to {@link FormDialog}'s
 * commit checks. It reads {@link cz.auderis.corusco.core.form.FormModel#problems()}
 * from the dialog's model, writes a compact summary into a {@link JLabel}, and
 * can ask a {@link ProblemFocusResolver} for the component that should receive
 * focus for the most severe problem. It does not perform validation itself and
 * does not change dialog result state.</p>
 *
 * <p>The binding is Event Dispatch Thread confined and intentionally
 * pull-based. Call {@link #refresh()} after form state changes or after a
 * generated presenter runs validation. This matches the current synchronous
 * form-model contract and avoids hiding refresh timing in a listener that the
 * form model does not expose.</p>
 *
 * <p>Instances retain the dialog, summary label, and resolver until closed.
 * Closing restores the label text captured during installation and prevents
 * later refresh/focus work. Use {@link FormDialogLifecycle} when the validation
 * binding should be closed with the rest of a dialog view.</p>
 */
public final class FormDialogValidationBinding implements Binding {

    private final FormDialog<?, ?> dialog;
    private final JLabel summaryLabel;
    private final ProblemFocusResolver focusResolver;
    private final String originalText;
    private boolean closed;

    private FormDialogValidationBinding(
            FormDialog<?, ?> dialog,
            JLabel summaryLabel,
            ProblemFocusResolver focusResolver
    ) {
        SwingEdt.requireEdt();
        this.dialog = Objects.requireNonNull(dialog, "dialog");
        this.summaryLabel = Objects.requireNonNull(summaryLabel, "summaryLabel");
        this.focusResolver = Objects.requireNonNull(focusResolver, "focusResolver");
        this.originalText = summaryLabel.getText();
        refresh();
    }

    /**
     * Installs validation summary behavior without focus targets.
     *
     * @param dialog dialog controller whose form problems are displayed, not
     *         {@code null}
     * @param summaryLabel label whose text is owned by the binding until close,
     *         not {@code null}
     * @return installed binding; close it to restore the original label text
     * @throws IllegalStateException if called off the EDT
     */
    public static FormDialogValidationBinding install(FormDialog<?, ?> dialog, JLabel summaryLabel) {
        return install(dialog, summaryLabel, ProblemFocusResolver.NONE);
    }

    /**
     * Installs validation summary and focus behavior.
     *
     * @param dialog dialog controller whose form problems are displayed, not
     *         {@code null}
     * @param summaryLabel label whose text is owned by the binding until close,
     *         not {@code null}
     * @param focusResolver resolver used by {@link #focusFirstProblem()}, not
     *         {@code null}
     * @return installed binding; close it to restore the original label text
     * @throws IllegalStateException if called off the EDT
     */
    public static FormDialogValidationBinding install(
            FormDialog<?, ?> dialog,
            JLabel summaryLabel,
            ProblemFocusResolver focusResolver
    ) {
        return new FormDialogValidationBinding(dialog, summaryLabel, focusResolver);
    }

    /**
     * Refreshes summary text from the form's current problems.
     *
     * <p>The summary is blank when there are no problems, the first
     * severity-ordered problem message when there is one problem, or a count
     * plus the first message when there are multiple problems. Calling this
     * method after {@link #close()} has no effect.</p>
     *
     * @throws IllegalStateException if called off the EDT
     */
    public void refresh() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        summaryLabel.setText(summaryText(dialog.formModel().problems()));
    }

    /**
     * Attempts to focus the first severity-ordered problem with a resolvable
     * component.
     *
     * <p>The method asks the resolver for each problem in severity-descending
     * order and calls {@link JComponent#requestFocusInWindow()} on the first
     * component it finds. It does not change validation state and does not show
     * any dialog by itself.</p>
     *
     * @return {@code true} when focus was requested successfully
     * @throws IllegalStateException if called off the EDT
     */
    public boolean focusFirstProblem() {
        SwingEdt.requireEdt();
        if (closed) {
            return false;
        }
        List<Problem> problems = dialog.formModel().problems().bySeverityDescending();
        for (Problem problem : problems) {
            JComponent component = focusResolver.resolve(problem).orElse(null);
            if (component != null) {
                return component.requestFocusInWindow();
            }
        }
        return false;
    }

    /**
     * Restores the summary label text captured during installation.
     *
     * @throws IllegalStateException if called off the EDT
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        summaryLabel.setText(originalText);
    }

    private static String summaryText(ProblemSet problems) {
        if (problems.isEmpty()) {
            return "";
        }
        List<Problem> ordered = problems.bySeverityDescending();
        Problem first = ordered.getFirst();
        if (ordered.size() == 1) {
            return first.message();
        }
        return ordered.size() + " problems: " + first.message();
    }
}
