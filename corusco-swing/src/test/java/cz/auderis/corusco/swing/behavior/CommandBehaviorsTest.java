package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.command.AcceleratorDescriptor;
import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.command.CommandResources;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandBehaviorsTest {

    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer.save.text", String.class);
    private static final CommandResources RESOURCES = Map.of(SAVE_TEXT, "Save")::get;

    @Test
    void commandButtonBehaviorInstallsActionAndRestoresPreviousAction() {
        SwingEdt.runAndWait(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableCommand command = CommandFactory.command(
                    ActionDescriptor.action(SAVE, SAVE_TEXT),
                    invoked -> calls.incrementAndGet()
            );
            JButton button = new JButton();
            Action previous = new AbstractAction("Previous") {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
            button.setAction(previous);
            BehaviorScope scope = new BehaviorScope();

            scope.install(button, List.of(CommandBehaviors.commandButton(command, RESOURCES)));
            button.doClick();
            scope.close();

            assertThat(calls).hasValue(1);
            assertThat(button.getAction()).isSameAs(previous);
        });
    }

    @Test
    void commandMenuItemBehaviorUsesSameCommandModel() {
        SwingEdt.runAndWait(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableCommand command = CommandFactory.command(
                    ActionDescriptor.action(SAVE, SAVE_TEXT),
                    invoked -> calls.incrementAndGet()
            );
            JMenuItem menuItem = new JMenuItem();
            BehaviorScope scope = new BehaviorScope();

            scope.install(menuItem, List.of(CommandBehaviors.commandMenuItem(command, RESOURCES)));
            command.setEnabled(false);
            assertThat(menuItem.isEnabled()).isFalse();
            command.setEnabled(true);
            menuItem.doClick();

            assertThat(calls).hasValue(1);
            scope.close();
        });
    }

    @Test
    void commandKeyBindingDispatchesAndRestoresInputMaps() {
        SwingEdt.runAndWait(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableCommand command = CommandFactory.command(
                    ActionDescriptor.action(SAVE, SAVE_TEXT)
                            .withAccelerator(AcceleratorDescriptor.of(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)),
                    invoked -> calls.incrementAndGet()
            );
            JPanel panel = new JPanel();
            BehaviorScope scope = new BehaviorScope();
            KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);

            scope.install(panel, List.of(CommandBehaviors.commandKeyBinding(
                    command,
                    RESOURCES,
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
            )));
            Object actionKey = panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(stroke);
            Action action = panel.getActionMap().get(actionKey);
            action.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "shortcut"));
            scope.close();

            assertThat(calls).hasValue(1);
            assertThat(panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(stroke)).isNull();
            assertThat(panel.getActionMap().get(actionKey)).isNull();
        });
    }
}
