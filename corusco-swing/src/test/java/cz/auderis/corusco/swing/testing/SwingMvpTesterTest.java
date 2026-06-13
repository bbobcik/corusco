package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.key.ComponentKey;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SwingMvpTesterTest {

    private static final ComponentKey<JTextField> NAME_FIELD =
            ComponentKey.of("customer/name-field", JTextField.class);
    private static final ComponentKey<JButton> SAVE_BUTTON =
            ComponentKey.of("customer/save-button", JButton.class);
    private static final ComponentKey<JTextField> ALIAS_FIELD =
            ComponentKey.of("customer/alias-field", JTextField.class);

    @Test
    void createsViewAndPresenterOnEdt() {
        AtomicBoolean viewOnEdt = new AtomicBoolean();
        AtomicBoolean presenterOnEdt = new AtomicBoolean();

        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                () -> {
                    viewOnEdt.set(SwingUtilities.isEventDispatchThread());
                    return new CustomerView();
                },
                view -> {
                    presenterOnEdt.set(SwingUtilities.isEventDispatchThread());
                    return new CustomerPresenter(view);
                }
        );

        assertThat(viewOnEdt).isTrue();
        assertThat(presenterOnEdt).isTrue();
        assertThat(tester.presenter()).hasValueSatisfying(presenter ->
                assertThat(presenter.view).isSameAs(tester.queryOnEdt((view, ignored) -> view)));
    }

    @Test
    void runAndQueryExecuteOnEdt() {
        SwingMvpTester<CustomerView, Void> tester = SwingMvpTester.create(CustomerView::new);

        tester.runOnEdt((view, presenter) -> {
            assertThat(SwingUtilities.isEventDispatchThread()).isTrue();
            view.nameField.setText("Alice");
        });

        String name = tester.queryOnEdt((view, presenter) -> {
            assertThat(SwingUtilities.isEventDispatchThread()).isTrue();
            return view.nameField.getText();
        });

        assertThat(name).isEqualTo("Alice");
    }

    @Test
    void findsComponentByMarkedComponentKey() {
        SwingMvpTester<CustomerView, Void> tester = SwingMvpTester.create(CustomerView::new);

        assertThat(tester.findComponent(NAME_FIELD)).containsSame(tester.queryOnEdt((view, presenter) -> view.nameField));
        assertThat(tester.requireComponent(SAVE_BUTTON)).isSameAs(tester.queryOnEdt((view, presenter) -> view.saveButton));
    }

    @Test
    void findsComponentByNameFallbackWhenTypeMatches() {
        SwingMvpTester<NameOnlyView, Void> tester = SwingMvpTester.create(NameOnlyView::new);

        assertThat(tester.requireComponent(NAME_FIELD)).isSameAs(tester.queryOnEdt((view, presenter) -> view.nameField));
    }

    @Test
    void nameFallbackDoesNotMatchWrongComponentType() {
        SwingMvpTester<WrongTypeView, Void> tester = SwingMvpTester.create(WrongTypeView::new);

        assertThat(tester.findComponent(NAME_FIELD)).isEmpty();
    }

    @Test
    void explicitComponentMarkerOverridesNameFallback() {
        SwingMvpTester<MarkedNameConflictView, Void> tester = SwingMvpTester.create(MarkedNameConflictView::new);

        assertThat(tester.findComponent(NAME_FIELD)).isEmpty();
        assertThat(tester.requireComponent(ALIAS_FIELD)).isSameAs(tester.queryOnEdt((view, presenter) -> view.field));
    }

    @Test
    void duplicateComponentKeysFailClearly() {
        SwingMvpTester<DuplicateView, Void> tester = SwingMvpTester.create(DuplicateView::new);

        assertThatIllegalStateException()
                .isThrownBy(() -> tester.requireComponent(NAME_FIELD))
                .withMessageContaining("Duplicate components")
                .withMessageContaining("customer/name-field");
    }

    @Test
    void missingComponentFailsClearly() {
        SwingMvpTester<CustomerView, Void> tester = SwingMvpTester.create(CustomerView::new);
        ComponentKey<JTextField> missing = ComponentKey.of("customer/missing-field", JTextField.class);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> tester.requireComponent(missing))
                .withMessageContaining("Missing component")
                .withMessageContaining("customer/missing-field");
    }

    @Test
    void viewAccessorRequiresEdt() {
        SwingMvpTester<CustomerView, Void> tester = SwingMvpTester.create(CustomerView::new);

        assertThatIllegalStateException()
                .isThrownBy(tester::view)
                .withMessageContaining("EDT");
    }

    @Test
    void createRejectsNullViewFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> SwingMvpTester.create(null))
                .withMessageContaining("viewFactory");
    }

    private static final class CustomerView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = SwingComponentKeys.mark(new JTextField(), NAME_FIELD);
        private final JButton saveButton = SwingComponentKeys.mark(new JButton(), SAVE_BUTTON);

        private CustomerView() {
            add(nameField);
            add(saveButton);
        }
    }

    private static final class NameOnlyView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = new JTextField();

        private NameOnlyView() {
            nameField.setName(NAME_FIELD.id());
            add(nameField);
        }
    }

    private static final class WrongTypeView extends JPanel {

        private static final long serialVersionUID = 1L;

        private WrongTypeView() {
            JTextArea area = new JTextArea();
            area.setName(NAME_FIELD.id());
            add(area);
        }
    }

    private static final class MarkedNameConflictView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField field = new JTextField();

        private MarkedNameConflictView() {
            field.setName(NAME_FIELD.id());
            SwingComponentKeys.mark(field, ALIAS_FIELD);
            add(field);
        }
    }

    private static final class DuplicateView extends JPanel {

        private static final long serialVersionUID = 1L;

        private DuplicateView() {
            add(SwingComponentKeys.mark(new JTextField(), NAME_FIELD));
            add(SwingComponentKeys.mark(new JTextField(), NAME_FIELD));
        }
    }

    private record CustomerPresenter(CustomerView view) {
    }
}
