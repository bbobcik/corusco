package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class FormDialogShellTest {

    @Test
    void acceptDelegatesToControllerAndDisposesDialog() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingEdt.runAndWait(() -> {
            FormDialog<TestForm, String> controller = new FormDialog<>(new TestForm("saved"), new JPanel());
            FormDialogShell<TestForm, String> shell = FormDialogShell.create(null, "Customer", controller);

            assertThat(shell.accept()).isTrue();

            assertThat(controller.result().acceptedValue()).contains("saved");
            assertThat(controller.isClosed()).isTrue();
            assertThat(shell.dialog().isDisplayable()).isFalse();
        });
    }

    @Test
    void dirtyCancelRejectionLeavesNativeDialogDisplayable() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            FormDialog<TestForm, String> controller = new FormDialog<>(form, new JPanel(), () -> true, () -> false);
            FormDialogShell<TestForm, String> shell = FormDialogShell.create(null, "Customer", controller);

            assertThat(shell.cancel()).isFalse();

            assertThat(controller.isClosed()).isFalse();
            assertThat(form.resetCalls).isZero();
            assertThat(shell.dialog().isDisplayable()).isTrue();
            shell.close();
        });
    }

    @Test
    void closeBypassesDirtyConfirmationForLifecycleCleanup() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter confirmations = new Counter();
            FormDialog<TestForm, String> controller = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> {
                        confirmations.increment();
                        return false;
                    }
            );
            FormDialogShell<TestForm, String> shell = FormDialogShell.create(null, "Customer", controller);

            shell.close();

            assertThat(controller.isClosed()).isTrue();
            assertThat(form.resetCalls).isEqualTo(1);
            assertThat(confirmations.value()).isZero();
            assertThat(shell.dialog().isDisplayable()).isFalse();
        });
    }

    @Test
    void revertDelegatesToControllerAndDisposesDialog() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm("ignored");
            Counter reverts = new Counter();
            FormDialog<TestForm, String> controller = new FormDialog<>(
                    form,
                    new JPanel(),
                    DirtyState.CLEAN,
                    CancelConfirmation.ALWAYS_CONFIRM,
                    new RevertPolicy() {
                        @Override
                        public boolean canRevert() {
                            return true;
                        }

                        @Override
                        public boolean revert() {
                            reverts.increment();
                            return true;
                        }
                    }
            );
            FormDialogShell<TestForm, String> shell = FormDialogShell.create(null, "Customer", controller);

            assertThat(shell.revert()).isTrue();

            assertThat(reverts.value()).isEqualTo(1);
            assertThat(controller.result().isReverted()).isTrue();
            assertThat(controller.isClosed()).isTrue();
            assertThat(shell.dialog().isDisplayable()).isFalse();
        });
    }

    private static final class TestForm implements FormModel<String> {

        private final String result;
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
            return true;
        }

        @Override
        public void reset() {
            resetCalls++;
        }

        @Override
        public void acceptCurrentValues() {
        }

        @Override
        public String toResult() {
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
}
