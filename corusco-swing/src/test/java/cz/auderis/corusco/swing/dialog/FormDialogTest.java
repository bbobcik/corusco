package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FormDialogTest {

    @Test
    void acceptCommitsResultAndClosesController() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("saved");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThat(dialog.accept()).isTrue();

            assertThat(dialog.result().isAccepted()).isTrue();
            assertThat(dialog.result().acceptedValue()).contains("saved");
            assertThat(dialog.isClosed()).isTrue();
            assertThat(form.toResultCalls).isEqualTo(1);
            assertThat(form.acceptCalls).isEqualTo(1);
            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.applyCommand().isEnabled()).isFalse();
            assertThat(dialog.cancelCommand().isEnabled()).isFalse();
        });
    }

    @Test
    void cancelClosesWithoutCreatingResult() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            dialog.cancel();
            dialog.cancel();

            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.isClosed()).isTrue();
            assertThat(form.toResultCalls).isZero();
            assertThat(form.acceptCalls).isZero();
            assertThat(form.resetCalls).isEqualTo(1);
        });
    }

    @Test
    void applyCommitsBaselineWithoutClosing() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("applied");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThat(dialog.apply()).isTrue();

            assertThat(dialog.isClosed()).isFalse();
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.lastAppliedResult()).contains("applied");
            assertThat(form.toResultCalls).isEqualTo(1);
            assertThat(form.acceptCalls).isEqualTo(1);
            assertThat(dialog.okCommand().isEnabled()).isTrue();
            assertThat(dialog.applyCommand().isEnabled()).isTrue();
        });
    }

    @Test
    void applyThenCancelClosesAsAcceptedWithLastAppliedResult() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("applied");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThat(dialog.apply()).isTrue();
            form.result = "edited-after-apply";
            assertThat(dialog.cancel()).isTrue();

            assertThat(dialog.result().isAccepted()).isTrue();
            assertThat(dialog.result().acceptedValue()).contains("applied");
            assertThat(dialog.lastAppliedResult()).contains("applied");
            assertThat(form.resetCalls).isEqualTo(1);
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void failedApplyDoesNotReplaceLastAppliedResult() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("first");
            TestFormDialog dialog = new TestFormDialog(form, true);

            assertThat(dialog.apply()).isTrue();
            form.result = "second";
            dialog.editorCommit = false;

            assertThat(dialog.apply()).isFalse();
            assertThat(dialog.lastAppliedResult()).contains("first");

            assertThat(dialog.cancel()).isTrue();
            assertThat(dialog.result().acceptedValue()).contains("first");
            assertThat(form.toResultCalls).isEqualTo(1);
        });
    }

    @Test
    void commandExecutionUsesDialogSemantics() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("saved");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            dialog.okCommand().execute();

            assertThat(dialog.result().acceptedValue()).contains("saved");
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void cleanCancelDoesNotAskConfirmationButResetsForm() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter confirmationCalls = new Counter();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> false,
                    () -> {
                        confirmationCalls.increment();
                        return true;
                    }
            );

            assertThat(dialog.cancel()).isTrue();

            assertThat(confirmationCalls.value()).isZero();
            assertThat(form.resetCalls).isEqualTo(1);
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void dirtyCancelRejectedKeepsDialogOpenAndDoesNotReset() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter confirmationCalls = new Counter();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> {
                        confirmationCalls.increment();
                        return false;
                    }
            );

            assertThat(dialog.cancel()).isFalse();

            assertThat(confirmationCalls.value()).isEqualTo(1);
            assertThat(form.resetCalls).isZero();
            assertThat(dialog.isClosed()).isFalse();
            assertThat(dialog.cancelCommand().isEnabled()).isTrue();
        });
    }

    @Test
    void dirtyCancelConfirmedClosesAndResets() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter confirmationCalls = new Counter();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> {
                        confirmationCalls.increment();
                        return true;
                    }
            );

            assertThat(dialog.cancel()).isTrue();
            assertThat(dialog.cancel()).isTrue();

            assertThat(confirmationCalls.value()).isEqualTo(1);
            assertThat(form.resetCalls).isEqualTo(1);
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void cancelCommandHonorsDirtyConfirmation() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> false
            );

            dialog.cancelCommand().execute();

            assertThat(dialog.isClosed()).isFalse();
            assertThat(form.resetCalls).isZero();
        });
    }

    @Test
    void closeForcesCleanupWithoutDirtyConfirmation() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter confirmationCalls = new Counter();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> {
                        confirmationCalls.increment();
                        return false;
                    }
            );

            dialog.close();

            assertThat(dialog.isClosed()).isTrue();
            assertThat(form.resetCalls).isEqualTo(1);
            assertThat(confirmationCalls.value()).isZero();
        });
    }

    @Test
    void closeAfterApplyStillForcesCancelledLifecycleCleanup() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("applied");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThat(dialog.apply()).isTrue();
            dialog.close();

            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.result().acceptedValue()).isEmpty();
            assertThat(form.resetCalls).isEqualTo(1);
        });
    }

    @Test
    void nonCommittableFormDisablesCommitCommandsAndBlocksAccept() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("blocked");
            form.committable = false;
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            dialog.okCommand().execute();

            assertThat(dialog.accept()).isFalse();
            assertThat(dialog.apply()).isFalse();
            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.applyCommand().isEnabled()).isFalse();
            assertThat(dialog.isClosed()).isFalse();
            assertThat(form.toResultCalls).isZero();
            assertThat(dialog.result().isAccepted()).isFalse();
        });
    }

    @Test
    void rejectedEditorCommitBlocksAcceptAndApply() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("blocked");
            TestFormDialog dialog = new TestFormDialog(form, false);

            assertThat(dialog.accept()).isFalse();
            assertThat(dialog.apply()).isFalse();

            assertThat(dialog.isClosed()).isFalse();
            assertThat(form.toResultCalls).isZero();
            assertThat(form.acceptCalls).isZero();
            assertThat(dialog.result().isAccepted()).isFalse();
        });
    }

    @Test
    void rejectingTableEditorBlocksAcceptAndKeepsDialogOpen() {
        SwingEdt.runAndWait(() -> {
            JPanel root = new JPanel();
            DefaultTableModel model = new DefaultTableModel(new Object[][] { { "Alice" } }, new Object[] { "Name" });
            JTable table = new JTable(model);
            table.setDefaultEditor(Object.class, new RejectingCellEditor());
            root.add(table);
            table.editCellAt(0, 0);
            ((JTextField) table.getEditorComponent()).setText("Alicia");
            FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm("blocked"), root);

            assertThat(dialog.accept()).isFalse();

            assertThat(dialog.isClosed()).isFalse();
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(table.isEditing()).isTrue();
            assertThat(model.getValueAt(0, 0)).isEqualTo("Alice");
        });
    }

    @Test
    void nullResultDoesNotAcceptBaseline() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm(null);
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThatNullPointerException()
                    .isThrownBy(dialog::accept)
                    .withMessageContaining("formModel.toResult()");

            assertThat(dialog.isClosed()).isFalse();
            assertThat(form.acceptCalls).isZero();
            assertThat(dialog.result().isAccepted()).isFalse();
        });
    }

    @Test
    void refreshCommandStateReflectsChangedCommittability() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("saved");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            form.committable = false;
            dialog.refreshCommandState();

            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.applyCommand().isEnabled()).isFalse();
            assertThat(dialog.cancelCommand().isEnabled()).isTrue();
        });
    }

    @Test
    void unsupportedRevertIsDisabledAndDoesNotClose() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("saved");
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());

            assertThat(dialog.revertCommand().isEnabled()).isFalse();
            assertThat(dialog.revert()).isFalse();

            assertThat(dialog.isClosed()).isFalse();
            assertThat(dialog.result().isReverted()).isFalse();
            assertThat(form.resetCalls).isZero();
        });
    }

    @Test
    void revertUsesPolicyAndClosesAsRevertedWithoutResettingBaseline() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("applied");
            Counter revertCalls = new Counter();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    CancelConfirmation.ALWAYS_CONFIRM,
                    new RevertPolicy() {
                        @Override
                        public boolean canRevert() {
                            return true;
                        }

                        @Override
                        public boolean revert() {
                            revertCalls.increment();
                            return true;
                        }
                    }
            );

            assertThat(dialog.apply()).isTrue();
            assertThat(dialog.revertCommand().isEnabled()).isTrue();
            assertThat(dialog.revert()).isTrue();

            assertThat(revertCalls.value()).isEqualTo(1);
            assertThat(dialog.result().isReverted()).isTrue();
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.lastAppliedResult()).isEmpty();
            assertThat(form.resetCalls).isZero();
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void actionStateFollowsCommittabilityAndDirtyPolicies() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("saved");
            MutableDirtyState currentBaselineDirty = new MutableDirtyState(true);
            MutableDirtyState preDialogDirty = new MutableDirtyState(true);
            MutableRevertPolicy revertPolicy = new MutableRevertPolicy(true);
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    currentBaselineDirty,
                    CancelConfirmation.ALWAYS_CONFIRM,
                    revertPolicy
            );
            FormDialogActionState actionState = new FormDialogActionState(
                    dialog,
                    currentBaselineDirty,
                    preDialogDirty
            );

            assertThat(actionState.applyAction().enabled().value()).isTrue();
            assertThat(actionState.revertAction().enabled().value()).isTrue();

            currentBaselineDirty.dirty = false;
            actionState.refresh();
            assertThat(actionState.applyAction().enabled().value()).isFalse();
            assertThat(actionState.revertAction().enabled().value()).isTrue();

            preDialogDirty.dirty = false;
            actionState.refresh();
            assertThat(actionState.revertAction().enabled().value()).isFalse();

            currentBaselineDirty.dirty = true;
            preDialogDirty.dirty = true;
            form.committable = false;
            revertPolicy.canRevert = false;
            actionState.refresh();
            assertThat(actionState.applyAction().enabled().value()).isFalse();
            assertThat(actionState.revertAction().enabled().value()).isFalse();
        });
    }

    private static final class TestFormDialog extends FormDialog<TestForm, String> {

        private boolean editorCommit;

        private TestFormDialog(TestForm form, boolean editorCommit) {
            super(form, new JPanel());
            this.editorCommit = editorCommit;
        }

        @Override
        protected boolean commitActiveEditor() {
            return editorCommit;
        }
    }

    private static final class RejectingCellEditor extends DefaultCellEditor {

        private RejectingCellEditor() {
            super(new JTextField());
        }

        @Override
        public boolean stopCellEditing() {
            return false;
        }
    }

    private static final class TestForm implements FormModel<String> {

        private String result;
        private boolean committable = true;
        private int toResultCalls;
        private int acceptCalls;
        private int resetCalls;

        private TestForm(String result) {
            this.result = result;
        }

        @Override
        public ProblemSet problems() {
            return ProblemSet.empty();
        }

        @Override
        public boolean isCommittable() {
            return committable;
        }

        @Override
        public void reset() {
            resetCalls++;
        }

        @Override
        public void acceptCurrentValues() {
            acceptCalls++;
        }

        @Override
        public String toResult() {
            toResultCalls++;
            return result;
        }
    }

    private static final class Counter {

        private int value;

        private void increment() {
            value++;
        }

        private int value() {
            return value;
        }
    }

    private static final class MutableDirtyState implements DirtyState {

        private boolean dirty;

        private MutableDirtyState(boolean dirty) {
            this.dirty = dirty;
        }

        @Override
        public boolean isDirty() {
            return dirty;
        }
    }

    private static final class MutableRevertPolicy implements RevertPolicy {

        private boolean canRevert;

        private MutableRevertPolicy(boolean canRevert) {
            this.canRevert = canRevert;
        }

        @Override
        public boolean canRevert() {
            return canRevert;
        }

        @Override
        public boolean revert() {
            return canRevert;
        }
    }
}
