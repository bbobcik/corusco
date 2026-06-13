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
 * Binds a form dialog's current problems to validation-summary UI behavior.
 *
 * <p>The binding is EDT-bound and intentionally pull-based: call
 * {@link #refresh()} after form state changes. This matches the current
 * synchronous {@link cz.auderis.corusco.core.form.FormModel} contract and
 * leaves generated presenters free to decide when a dialog should refresh its
 * summary.</p>
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
     * @param dialog dialog controller
     * @param summaryLabel summary label
     * @return installed binding
     */
    public static FormDialogValidationBinding install(FormDialog<?, ?> dialog, JLabel summaryLabel) {
        return install(dialog, summaryLabel, ProblemFocusResolver.NONE);
    }

    /**
     * Installs validation summary and focus behavior.
     *
     * @param dialog dialog controller
     * @param summaryLabel summary label
     * @param focusResolver problem focus resolver
     * @return installed binding
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
     * @return {@code true} when focus was requested successfully
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
