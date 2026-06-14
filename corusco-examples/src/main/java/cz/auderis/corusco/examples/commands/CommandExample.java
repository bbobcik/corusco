package cz.auderis.corusco.examples.commands;

import cz.auderis.corusco.core.command.AcceleratorDescriptor;
import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.CommandBehaviors;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.command.CommandResources;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Demonstrates command/action binding without showing a native window.
 *
 * <p>The example adapts a core command descriptor to Swing actions and key
 * bindings, then exercises the binding lifecycle in a headless-safe way. It is
 * intended for readers who want command metadata to drive buttons, menus, and
 * accelerators consistently.</p>
 */
public final class CommandExample {

    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer.save.text", String.class);
    private static final ResourceKey<String> SAVE_TOOLTIP = ResourceKey.of("customer.save.tooltip", String.class);

    private CommandExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Binds one command to multiple Swing entry points.
     *
     * @return number of command invocations and final enabled state
     */
    public static List<String> runScenario() {
        java.util.concurrent.atomic.AtomicReference<List<String>> result = new java.util.concurrent.atomic.AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            AtomicInteger saves = new AtomicInteger();
            CommandResources resources = Map.of(
                    SAVE_TEXT, "Save",
                    SAVE_TOOLTIP, "Save customer"
            )::get;
            MutableCommand save = CommandFactory.command(
                    ActionDescriptor.action(SAVE, SAVE_TEXT)
                            .withTooltip(SAVE_TOOLTIP)
                            .withMnemonic(KeyEvent.VK_S)
                            .withAccelerator(AcceleratorDescriptor.of(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)),
                    command -> saves.incrementAndGet()
            );
            JButton button = new JButton();
            JMenuItem menuItem = new JMenuItem();
            JPanel panel = new JPanel();

            try (BehaviorScope scope = new BehaviorScope()) {
                // Generated action plans should produce resource-key metadata
                // like this. The resolver is the only place that turns keys
                // into localized Swing text.
                scope.install(button, List.of(CommandBehaviors.commandButton(save, resources)));
                scope.install(menuItem, List.of(CommandBehaviors.commandMenuItem(save, resources)));
                scope.install(panel, List.of(CommandBehaviors.commandKeyBinding(
                        save,
                        resources,
                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                )));

                // Every Swing entry point delegates to the same command object,
                // so disabling it immediately affects the button and menu item.
                button.doClick();
                menuItem.doClick();
                save.setEnabled(false);
                result.set(List.of(
                        Integer.toString(saves.get()),
                        Boolean.toString(button.isEnabled()),
                        Boolean.toString(menuItem.isEnabled())
                ));
            }
        });
        return result.get();
    }
}
