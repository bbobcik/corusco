package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Demonstrates the Swing MVP tester foundation.
 */
public final class SwingMvpTesterExample {

    private static final ComponentKey<JTextField> NAME_FIELD =
            ComponentKey.of("customer/name-field", JTextField.class);
    private static final ComponentKey<JButton> SAVE_BUTTON =
            ComponentKey.of("customer/save-button", JButton.class);

    private SwingMvpTesterExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a generated-style view test scenario.
     *
     * @return diagnostics read through the tester
     */
    public static List<String> runScenario() {
        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new
        );

        // Generated views can mark real Swing components with ComponentKey
        // constants. Tests then locate components by typed ids instead of
        // brittle field names or reflection.
        JTextField name = tester.requireComponent(NAME_FIELD);
        JButton save = tester.requireComponent(SAVE_BUTTON);

        // Interactions still run on the EDT; the component references are just
        // stable handles that make the test intent concise.
        tester.runOnEdt((view, presenter) -> {
            name.setText("Alice");
            save.setEnabled(presenter.canSave(name.getText()));
        });

        return tester.queryOnEdt((view, presenter) -> List.of(
                name.getText(),
                Boolean.toString(save.isEnabled()),
                Boolean.toString(tester.presenter().isPresent())
        ));
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

    private record CustomerPresenter(CustomerView view) {

        private boolean canSave(String name) {
            return !name.isBlank();
        }
    }
}
