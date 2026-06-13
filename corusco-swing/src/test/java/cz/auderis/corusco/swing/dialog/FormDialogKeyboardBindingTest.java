package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormDialogKeyboardBindingTest {

    @Test
    void escapeRunsDialogCancelCommand() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            FormDialog<TestForm, String> dialog = new FormDialog<>(form, new JPanel());
            JRootPane rootPane = new JRootPane();
            FormDialogKeyboardBinding binding = FormDialogKeyboardBinding.install(rootPane, dialog);

            triggerEscape(rootPane);

            assertThat(dialog.isClosed()).isTrue();
            assertThat(form.resetCalls).isEqualTo(1);
            binding.close();
        });
    }

    @Test
    void escapeHonorsDirtyCancelVeto() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            FormDialog<TestForm, String> dialog = new FormDialog<>(
                    form,
                    new JPanel(),
                    () -> true,
                    () -> false
            );
            JRootPane rootPane = new JRootPane();
            FormDialogKeyboardBinding binding = FormDialogKeyboardBinding.install(rootPane, dialog);

            triggerEscape(rootPane);

            assertThat(dialog.isClosed()).isFalse();
            assertThat(form.resetCalls).isZero();
            binding.close();
        });
    }

    @Test
    void installSetsAndRestoresDefaultButton() {
        SwingEdt.runAndWait(() -> {
            FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm(), new JPanel());
            JRootPane rootPane = new JRootPane();
            JButton previous = new JButton("Previous");
            JButton ok = new JButton("OK");
            rootPane.setDefaultButton(previous);

            FormDialogKeyboardBinding binding = FormDialogKeyboardBinding.install(rootPane, dialog, ok);

            assertThat(rootPane.getDefaultButton()).isSameAs(ok);

            binding.close();
            binding.close();

            assertThat(rootPane.getDefaultButton()).isSameAs(previous);
        });
    }

    @Test
    void closeRestoresPreviousEscapeMappingAndAction() {
        SwingEdt.runAndWait(() -> {
            FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm(), new JPanel());
            JRootPane rootPane = new JRootPane();
            KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            Action previousAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                }
            };
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "previous-escape");
            rootPane.getActionMap().put("corusco.dialog.cancel", previousAction);

            FormDialogKeyboardBinding binding = FormDialogKeyboardBinding.install(rootPane, dialog);
            binding.close();

            assertThat(rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape))
                    .isEqualTo("previous-escape");
            assertThat(rootPane.getActionMap().get("corusco.dialog.cancel")).isSameAs(previousAction);
        });
    }

    @Test
    void closeRemovesEscapeMappingWhenNoneExisted() {
        SwingEdt.runAndWait(() -> {
            FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm(), new JPanel());
            JRootPane rootPane = new JRootPane();
            KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

            FormDialogKeyboardBinding binding = FormDialogKeyboardBinding.install(rootPane, dialog);
            assertThat(rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape)).isNotNull();

            binding.close();

            assertThat(rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape)).isNull();
            assertThat(rootPane.getActionMap().get("corusco.dialog.cancel")).isNull();
        });
    }

    private static void triggerEscape(JRootPane rootPane) {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object actionKey = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape);
        Action action = rootPane.getActionMap().get(actionKey);
        action.actionPerformed(new ActionEvent(rootPane, ActionEvent.ACTION_PERFORMED, "escape"));
    }

    private static final class TestForm implements FormModel<String> {

        private int resetCalls;

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
            return "ok";
        }
    }
}
