package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.lifecycle.Detachable;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.UiTask;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormDialogLifecycleTest {

    @Test
    void closeReleasesResourcesInReverseOrderAndClosesDialog() {
        SwingEdt.runAndWait(() -> {
            List<String> closed = new ArrayList<>();
            FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm(), new JPanel());
            FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);

            lifecycle.addBinding(trackedBinding("first", closed));
            lifecycle.addDisposable(trackedDisposable("second", closed));

            lifecycle.close();
            lifecycle.close();

            assertThat(closed).containsExactly("second", "first");
            assertThat(dialog.isClosed()).isTrue();
            assertThat(lifecycle.isClosed()).isTrue();
        });
    }

    @Test
    void closeDetachesPresenterResources() {
        SwingEdt.runAndWait(() -> {
            Counter detachCalls = new Counter();
            FormDialogLifecycle lifecycle = FormDialogLifecycle.of(new FormDialog<>(new TestForm(), new JPanel()));

            lifecycle.addDetachable(detachCalls::increment);
            lifecycle.close();

            assertThat(detachCalls.value).isEqualTo(1);
        });
    }

    @Test
    void lateRegistrationsFailClosed() {
        SwingEdt.runAndWait(() -> {
            Counter bindingCalls = new Counter();
            Counter detachCalls = new Counter();
            FormDialogLifecycle lifecycle = FormDialogLifecycle.of(new FormDialog<>(new TestForm(), new JPanel()));

            lifecycle.close();
            lifecycle.addBinding(bindingCalls::increment);
            lifecycle.addDetachable(detachCalls::increment);

            assertThat(bindingCalls.value).isEqualTo(1);
            assertThat(detachCalls.value).isEqualTo(1);
        });
    }

    @Test
    void taskServiceIsClosedWithLifecycle() {
        SwingEdt.runAndWait(() -> {
            TestTaskService service = new TestTaskService();
            FormDialogLifecycle lifecycle = FormDialogLifecycle.of(new FormDialog<>(new TestForm(), new JPanel()));

            lifecycle.addTaskService(service);
            lifecycle.close();

            assertThat(service.closed).isTrue();
        });
    }

    @Test
    void lifecyclesCanBeCreatedAndClosedRepeatedly() {
        SwingEdt.runAndWait(() -> {
            List<FormDialogLifecycle> lifecycles = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                FormDialog<TestForm, String> dialog = new FormDialog<>(new TestForm(), new JPanel());
                FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);
                lifecycle.addBinding(() -> { });
                lifecycle.close();
                lifecycles.add(lifecycle);
            }

            assertThat(lifecycles).allMatch(FormDialogLifecycle::isClosed);
        });
    }

    private static Binding trackedBinding(String name, List<String> closed) {
        return () -> closed.add(name);
    }

    private static Disposable trackedDisposable(String name, List<String> closed) {
        return () -> closed.add(name);
    }

    private static final class Counter {

        private int value;

        private void increment() {
            value++;
        }
    }

    private static final class TestForm implements FormModel<String> {

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
        }

        @Override
        public void acceptCurrentValues() {
        }

        @Override
        public String toResult() {
            return "ok";
        }
    }

    private static final class TestTaskService implements TaskService {

        private final SimpleValue<Boolean> busy = SimpleValue.of(false);
        private boolean closed;

        @Override
        public ReadableValue<Boolean> busy() {
            return busy;
        }

        @Override
        public <T> TaskHandle<T> submit(UiTask<? extends T> task, TaskCallbacks<? super T> callbacks) {
            throw new UnsupportedOperationException("Not needed in lifecycle test");
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
