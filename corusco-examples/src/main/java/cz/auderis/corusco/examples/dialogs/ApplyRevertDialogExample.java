package cz.auderis.corusco.examples.dialogs;

import cz.auderis.corusco.core.form.ComponentStateModel;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.CancelConfirmation;
import cz.auderis.corusco.swing.dialog.DirtyState;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogActionState;
import cz.auderis.corusco.swing.dialog.RevertPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;

/**
 * Demonstrates Apply-Cancel and optional Revert semantics.
 *
 * <p>The scenario keeps two baselines visible. The form baseline moves after
 * Apply, so Apply-Cancel returns the last applied value. A separate pre-dialog
 * snapshot backs Revert and can restore the opening state when the application
 * supports that operation.</p>
 */
public final class ApplyRevertDialogExample {

    private ApplyRevertDialogExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs Apply-Cancel and Revert flows.
     *
     * @return diagnostics describing dialog action state and terminal results
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            SettingsForm form = new SettingsForm("original");
            Snapshot openingSnapshot = form.snapshot();
            DirtyState currentBaselineDirty = form::isDirty;
            DirtyState preDialogDirty = () -> !Objects.equals(openingSnapshot, form.snapshot());
            SnapshotRevertPolicy revertPolicy = new SnapshotRevertPolicy(form, openingSnapshot);
            FormDialog<SettingsForm, Settings> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    currentBaselineDirty,
                    CancelConfirmation.ALWAYS_CONFIRM,
                    revertPolicy
            );
            FormDialogActionState actions = new FormDialogActionState(dialog, currentBaselineDirty, preDialogDirty);

            form.setName("applied");
            actions.refresh();
            result.add("applyBefore=" + enabled(actions.applyAction()));
            result.add("revertBefore=" + enabled(actions.revertAction()));

            dialog.apply();
            actions.refresh();
            result.add("applyAfter=" + enabled(actions.applyAction()));
            result.add("revertAfter=" + enabled(actions.revertAction()));

            form.setName("discarded");
            dialog.cancel();
            result.add("cancelAccepted=" + dialog.result().isAccepted());
            result.add("cancelValue=" + dialog.result().acceptedValue().orElseThrow().name());

            SettingsForm revertForm = new SettingsForm("original");
            Snapshot revertSnapshot = revertForm.snapshot();
            FormDialog<SettingsForm, Settings> revertDialog = new FormDialog<>(
                    revertForm,
                    new JPanel(),
                    revertForm::isDirty,
                    CancelConfirmation.ALWAYS_CONFIRM,
                    new SnapshotRevertPolicy(revertForm, revertSnapshot)
            );

            revertForm.setName("changed");
            result.add("reverted=" + revertDialog.revert());
            result.add("revertResult=" + revertDialog.result().isReverted());
            result.add("revertedName=" + revertForm.name);
        });
        return result;
    }

    private static boolean enabled(ComponentStateModel state) {
        return state.enabled().value();
    }

    private record Settings(String name) {
    }

    private record Snapshot(String name) {
    }

    private static final class SnapshotRevertPolicy implements RevertPolicy {

        private final SettingsForm form;
        private final Snapshot snapshot;

        private SnapshotRevertPolicy(SettingsForm form, Snapshot snapshot) {
            this.form = form;
            this.snapshot = snapshot;
        }

        @Override
        public boolean canRevert() {
            return !Objects.equals(snapshot, form.snapshot());
        }

        @Override
        public boolean revert() {
            form.name = snapshot.name();
            form.acceptCurrentValues();
            return true;
        }
    }

    private static final class SettingsForm implements FormModel<Settings> {

        private String name;
        private String baseline;

        private SettingsForm(String name) {
            this.name = name;
            this.baseline = name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private boolean isDirty() {
            return !Objects.equals(name, baseline);
        }

        private Snapshot snapshot() {
            return new Snapshot(name);
        }

        @Override
        public ProblemSet problems() {
            return ProblemSet.empty();
        }

        @Override
        public boolean isCommittable() {
            return true;
        }

        @Override
        public void reset() {
            name = baseline;
        }

        @Override
        public void acceptCurrentValues() {
            baseline = name;
        }

        @Override
        public Settings toResult() {
            return new Settings(name);
        }
    }
}
