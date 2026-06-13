package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservableComboBoxModelTest {

    @Test
    void routesMutableComboOperationsToSourceList() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.of(List.of("b"));
            ObservableComboBoxModel<String> model = ObservableComboBoxModel.of(source);

            model.insertElementAt("a", 0);
            model.addElement("c");
            model.removeElement("b");
            model.removeElementAt(1);

            assertThat(source.snapshot()).containsExactly("a");
            assertThat(model.getSize()).isOne();
            assertThat(model.getElementAt(0)).isEqualTo("a");
        });
    }

    @Test
    void clearsSelectionWhenSelectedElementIsRemoved() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.of(List.of("a", "b", "c"));
            ObservableComboBoxModel<String> model = ObservableComboBoxModel.of(source);
            List<Integer> eventTypes = recordEventTypes(model);

            model.setSelectedItem("b");
            source.remove(1);

            assertThat(model.getSelectedItem()).isNull();
            assertThat(eventTypes).containsExactly(
                    ListDataEvent.CONTENTS_CHANGED,
                    ListDataEvent.INTERVAL_REMOVED,
                    ListDataEvent.CONTENTS_CHANGED
            );
        });
    }

    @Test
    void retainsSelectionWhenUnrelatedElementChanges() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.of(List.of("a", "b"));
            ObservableComboBoxModel<String> model = ObservableComboBoxModel.of(source);

            model.setSelectedItem("b");
            source.set(0, "A");
            source.move(1, 0);

            assertThat(model.getSelectedItem()).isEqualTo("b");
        });
    }

    @Test
    void retainsSelectionWhenEqualElementStillExists() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<String> source = ObservableArrayList.of(List.of("b", "b"));
            ObservableComboBoxModel<String> model = ObservableComboBoxModel.of(source);

            model.setSelectedItem("b");
            source.remove(0);
            source.set(0, "b");

            assertThat(model.getSelectedItem()).isEqualTo("b");
        });
    }

    private static List<Integer> recordEventTypes(ObservableComboBoxModel<?> model) {
        List<Integer> eventTypes = new ArrayList<>();
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent event) {
                eventTypes.add(event.getType());
            }

            @Override
            public void intervalRemoved(ListDataEvent event) {
                eventTypes.add(event.getType());
            }

            @Override
            public void contentsChanged(ListDataEvent event) {
                eventTypes.add(event.getType());
            }
        });
        return eventTypes;
    }
}
