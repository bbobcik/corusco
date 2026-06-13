package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.Command;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.CommandSet;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import static org.assertj.core.api.Assertions.catchThrowable;

class SwingMvpTesterTest {

    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ActionKey ARCHIVE = ActionKey.of("customer/archive");
    private static final ActionKey ACTIVE = ActionKey.of("customer/active");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer/save/text", String.class);
    private static final ResourceKey<String> ARCHIVE_TEXT = ResourceKey.of("customer/archive/text", String.class);
    private static final ResourceKey<String> ACTIVE_TEXT = ResourceKey.of("customer/active/text", String.class);
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

    @Test
    void commandFactoryRunsOnEdtAndStoresCommandSet() {
        AtomicBoolean commandFactoryOnEdt = new AtomicBoolean();

        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new,
                (view, presenter) -> {
                    commandFactoryOnEdt.set(SwingUtilities.isEventDispatchThread());
                    return presenter.commands();
                }
        );

        assertThat(commandFactoryOnEdt).isTrue();
        assertThat(tester.findCommand(SAVE)).containsSame(tester.commands().require(SAVE));
    }

    @Test
    void executeCommandRunsCommandOnEdtByActionKey() {
        AtomicBoolean handlerOnEdt = new AtomicBoolean();
        AtomicInteger calls = new AtomicInteger();
        MutableCommand save = command(SAVE, SAVE_TEXT, true, command -> {
            handlerOnEdt.set(SwingUtilities.isEventDispatchThread());
            calls.incrementAndGet();
        });
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(CommandSet.of(save));

        tester.executeCommand(SAVE);

        assertThat(handlerOnEdt).isTrue();
        assertThat(calls).hasValue(1);
    }

    @Test
    void disabledCommandDoesNotInvokeHandler() {
        AtomicInteger calls = new AtomicInteger();
        MutableCommand archive = command(ARCHIVE, ARCHIVE_TEXT, false, command -> calls.incrementAndGet());
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(CommandSet.of(archive));

        tester.executeCommand(ARCHIVE)
                .assertCommandEnabled(ARCHIVE, false);

        assertThat(calls).hasValue(0);
    }

    @Test
    void commandStateAssertionsUseActionKeys() {
        MutableCommand save = command(SAVE, SAVE_TEXT, true, command -> { });
        MutableCommand active = toggle(ACTIVE, ACTIVE_TEXT, true);
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(CommandSet.of(save, active));

        tester.assertCommandEnabled(SAVE, true)
                .assertCommandSelected(ACTIVE, true);
    }

    @Test
    void commandStateAssertionsReadStateOnEdt() {
        EdtCheckingCommand save = new EdtCheckingCommand();
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(CommandSet.of(save));

        tester.assertCommandEnabled(SAVE, true);

        assertThat(save.enabledReadOnEdt).isTrue();
    }

    @Test
    void commandStateAssertionFailuresAreReadable() {
        MutableCommand save = command(SAVE, SAVE_TEXT, true, command -> { });
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(CommandSet.of(save));

        Throwable selectedFailure = catchThrowable(() -> tester.assertCommandSelected(SAVE, true));
        assertThat(selectedFailure)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("ActionKey[customer/save]")
                .hasMessageContaining("selected=true")
                .hasMessageContaining("but was false");
        Throwable failure = catchThrowable(() -> tester.assertCommandEnabled(SAVE, false));
        assertThat(failure)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("ActionKey[customer/save]")
                .hasMessageContaining("enabled=false")
                .hasMessageContaining("but was true");
    }

    @Test
    void missingCommandFailsClearly() {
        SwingMvpTester<CustomerView, CustomerPresenter> tester = testerWithCommands(new CommandSet(java.util.List.of()));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> tester.requireCommand(SAVE))
                .withMessageContaining("Missing command")
                .withMessageContaining("customer/save");
    }

    @Test
    void duplicateCommandKeysFailDuringTesterCreation() {
        MutableCommand first = command(SAVE, SAVE_TEXT, true, command -> { });
        MutableCommand second = command(SAVE, SAVE_TEXT, true, command -> { });

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SwingMvpTester.create(
                        CustomerView::new,
                        CustomerPresenter::new,
                        (view, presenter) -> CommandSet.of(first, second)
                ))
                .withMessageContaining("Duplicate command key")
                .withMessageContaining("customer/save");
    }

    private static SwingMvpTester<CustomerView, CustomerPresenter> testerWithCommands(CommandSet commands) {
        return SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new,
                (view, presenter) -> commands
        );
    }

    private static MutableCommand command(
            ActionKey key,
            ResourceKey<String> textKey,
            boolean enabled,
            cz.auderis.corusco.core.command.CommandHandler handler
    ) {
        return CommandFactory.command(ActionDescriptor.action(key, textKey), enabled, handler);
    }

    private static MutableCommand toggle(ActionKey key, ResourceKey<String> textKey, boolean selected) {
        return CommandFactory.toggle(ActionDescriptor.toggle(key, textKey), selected, command -> { });
    }

    private static final class EdtCheckingCommand implements Command {

        private boolean enabledReadOnEdt;

        @Override
        public ActionKey key() {
            return SAVE;
        }

        @Override
        public ActionDescriptor descriptor() {
            return ActionDescriptor.action(SAVE, SAVE_TEXT);
        }

        @Override
        public ReadableValue<Boolean> enabled() {
            return SimpleValue.of(true);
        }

        @Override
        public ReadableValue<Boolean> selected() {
            return SimpleValue.of(false);
        }

        @Override
        public boolean isEnabled() {
            enabledReadOnEdt = SwingUtilities.isEventDispatchThread();
            return true;
        }

        @Override
        public void execute() {
        }
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

        private CommandSet commands() {
            return CommandSet.of(command(SAVE, SAVE_TEXT, true, command -> { }));
        }
    }
}
