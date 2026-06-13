package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservableListModelTest {

    @Test
    void exposesSourceContentsAndTranslatesPreciseEvents() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.of(List.of("b", "c"));
            ObservableListModel<String> model = ObservableListModel.of(source);
            List<EventRecord> events = recordEvents(model);

            source.add(0, "a");
            source.set(1, "B");
            source.move(2, 0);
            source.remove(1);
            source.clear();

            assertThat(model.getSize()).isZero();
            assertThat(events).containsExactly(
                    new EventRecord(ListDataEvent.INTERVAL_ADDED, 0, 0),
                    new EventRecord(ListDataEvent.CONTENTS_CHANGED, 1, 1),
                    new EventRecord(ListDataEvent.CONTENTS_CHANGED, 0, 2),
                    new EventRecord(ListDataEvent.INTERVAL_REMOVED, 1, 1),
                    new EventRecord(ListDataEvent.INTERVAL_REMOVED, 0, 1)
            );
        });
    }

    @Test
    void preservesBatchedChangeOrderAsSwingEvents() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.empty();
            ObservableListModel<String> model = ObservableListModel.of(source);
            List<EventRecord> events = recordEvents(model);

            source.batch(list -> {
                list.add("a");
                list.add("b");
                list.set(1, "B");
            });

            assertThat(events).containsExactly(
                    new EventRecord(ListDataEvent.INTERVAL_ADDED, 0, 0),
                    new EventRecord(ListDataEvent.INTERVAL_ADDED, 1, 1),
                    new EventRecord(ListDataEvent.CONTENTS_CHANGED, 1, 1)
            );
        });
    }

    @Test
    void closeStopsFutureSwingEvents() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.empty();
            ObservableListModel<String> model = ObservableListModel.of(source);
            List<EventRecord> events = recordEvents(model);

            model.close();
            model.close();
            source.add("ignored");

            assertThat(model.getSize()).isOne();
            assertThat(events).isEmpty();
        });
    }

    @Test
    void constructionOffEdtFailsFast() {
        ObservableArrayList<String> source = ObservableArrayList.empty();

        assertThatThrownBy(() -> ObservableListModel.of(source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");
    }

    @Test
    void observedOffEdtSourceChangeFailsFast() {
        ObservableArrayList<String> source = ObservableArrayList.empty();
        AtomicReference<ObservableListModel<String>> model = new AtomicReference<>();
        SwingEdt.runAndWait(() -> model.set(ObservableListModel.of(source)));

        assertThatThrownBy(() -> source.add("wrong-thread"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");

        SwingEdt.runAndWait(() -> model.get().close());
    }

    private static List<EventRecord> recordEvents(ObservableListModel<?> model) {
        List<EventRecord> events = new ArrayList<>();
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent event) {
                events.add(EventRecord.from(event));
            }

            @Override
            public void intervalRemoved(ListDataEvent event) {
                events.add(EventRecord.from(event));
            }

            @Override
            public void contentsChanged(ListDataEvent event) {
                events.add(EventRecord.from(event));
            }
        });
        return events;
    }

    private record EventRecord(int type, int index0, int index1) {

        static EventRecord from(ListDataEvent event) {
            return new EventRecord(event.getType(), event.getIndex0(), event.getIndex1());
        }
    }
}
