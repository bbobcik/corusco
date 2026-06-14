package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.CommandSet;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Demonstrates the Swing MVP tester foundation.
 *
 * <p>The example creates a small presenter/view interaction and asserts it
 * through the test support APIs. It shows how to test Swing-facing presenter
 * logic without displaying a window.</p>
 */
public final class SwingMvpTesterExample {

    private static final ComponentKey<JTextField> NAME_FIELD =
            ComponentKey.of("customer/name-field", JTextField.class);
    private static final ComponentKey<JButton> SAVE_BUTTON =
            ComponentKey.of("customer/save-button", JButton.class);
    private static final ComponentKey<JCheckBox> ACTIVE_BOX =
            ComponentKey.of("customer/active-box", JCheckBox.class);
    @SuppressWarnings("rawtypes")
    private static final ComponentKey<JComboBox> TYPE_COMBO =
            ComponentKey.of("customer/type-combo", JComboBox.class);
    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer/save/text", String.class);

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
                CustomerPresenter::new,
                (view, presenter) -> presenter.commands()
        );

        // Generated views can mark real Swing components with ComponentKey
        // constants. Tests then locate components by typed ids instead of
        // brittle field names or reflection.
        JButton save = tester.requireComponent(SAVE_BUTTON);

        // Input helpers keep test bodies at the generated-key level while the
        // actual Swing mutations still happen on the EDT.
        tester.enterText(NAME_FIELD, "Alice")
                .setSelected(ACTIVE_BOX, true)
                .selectItem(TYPE_COMBO, CustomerType.VIP);

        JTextField name = tester.requireComponent(NAME_FIELD);
        tester.runOnEdt((view, presenter) -> {
            save.setEnabled(presenter.canSave(name.getText()));
        });

        // Commands stay presenter-owned and are invoked by generated ActionKey
        // constants, so tests exercise the same command model as buttons and
        // key bindings without depending on Swing action plumbing.
        tester.assertCommandEnabled(SAVE, true)
                .executeCommand(SAVE);

        return tester.queryOnEdt((view, presenter) -> List.of(
                name.getText(),
                Boolean.toString(view.activeBox.isSelected()),
                view.typeCombo.getSelectedItem().toString(),
                Boolean.toString(save.isEnabled()),
                Integer.toString(presenter.saveCalls()),
                Boolean.toString(tester.presenter().isPresent())
        ));
    }

    private static final class CustomerView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = SwingComponentKeys.mark(new JTextField(), NAME_FIELD);
        private final JButton saveButton = SwingComponentKeys.mark(new JButton(), SAVE_BUTTON);
        private final JCheckBox activeBox = SwingComponentKeys.mark(new JCheckBox(), ACTIVE_BOX);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private final JComboBox<CustomerType> typeCombo =
                SwingComponentKeys.mark(new JComboBox<>(CustomerType.values()), TYPE_COMBO);

        private CustomerView() {
            add(nameField);
            add(saveButton);
            add(activeBox);
            add(typeCombo);
        }
    }

    private enum CustomerType {
        RETAIL,
        VIP
    }

    private static final class CustomerPresenter {

        private final CustomerView view;
        private final AtomicInteger saveCalls = new AtomicInteger();

        private CustomerPresenter(CustomerView view) {
            this.view = view;
        }

        private boolean canSave(String name) {
            return !name.isBlank();
        }

        private CommandSet commands() {
            MutableCommand save = CommandFactory.command(
                    ActionDescriptor.action(SAVE, SAVE_TEXT),
                    command -> saveCalls.incrementAndGet()
            );
            return CommandSet.of(save);
        }

        private int saveCalls() {
            return saveCalls.get();
        }
    }
}
