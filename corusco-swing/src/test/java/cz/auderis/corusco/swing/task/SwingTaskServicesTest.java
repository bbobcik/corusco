package cz.auderis.corusco.swing.task;

import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwingTaskServicesTest {

    @Test
    void successCallbackRunsOnEdtAndTaskRunsOffEdt() throws Exception {
        try (TaskService service = SwingTaskServices.virtualThreads()) {
            List<String> events = new CopyOnWriteArrayList<>();

            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        events.add("taskEdt=" + SwingUtilities.isEventDispatchThread());
                        return "ok";
                    },
                    TaskCallbacks.onSuccess(result -> events.add(
                            "successEdt=" + SwingUtilities.isEventDispatchThread() + ":" + result
                    ))
            );

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isEqualTo("ok");
            assertThat(events).containsExactly("taskEdt=false", "successEdt=true:ok");
        }
    }

    @Test
    void failureCallbackRunsOnEdt() {
        try (TaskService service = SwingTaskServices.virtualThreads()) {
            List<String> events = new CopyOnWriteArrayList<>();

            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        throw new IllegalStateException("boom");
                    },
                    new TaskCallbacks<>() {
                        @Override
                        public void failed(Throwable error) {
                            events.add("failureEdt=" + SwingUtilities.isEventDispatchThread()
                                    + ":" + error.getClass().getSimpleName());
                        }
                    }
            );

            assertThatThrownBy(() -> handle.completion().join())
                    .hasCauseInstanceOf(IllegalStateException.class);
            assertThat(events).containsExactly("failureEdt=true:IllegalStateException");
        }
    }

    @Test
    void finalBusyEventRunsOnEdt() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (TaskService service = SwingTaskServices.virtualThreads()) {
            List<String> busyEvents = new ArrayList<>();
            TaskHandle<String> handle = service.submit(cancellation -> {
                started.countDown();
                release.await(2, TimeUnit.SECONDS);
                return "done";
            });
            handle.busy().subscribe(event -> busyEvents.add(
                    "busy=" + event.newValue() + ",edt=" + SwingUtilities.isEventDispatchThread()
            ));

            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            release.countDown();

            assertThat(handle.completion().get(2, TimeUnit.SECONDS)).isEqualTo("done");
            assertThat(busyEvents).containsExactly("busy=false,edt=true");
        }
    }
}
