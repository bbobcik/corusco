package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.Command;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.command.CommandResources;
import cz.auderis.corusco.swing.command.SwingActionAdapter;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Behaviors that bind commands to Swing controls and key maps.
 */
public final class CommandBehaviors {

    private CommandBehaviors() {
    }

    /**
     * Creates a behavior that installs a command action on a button-like
     * component.
     *
     * @param command command to bind
     * @param resources command resource resolver
     * @param <C> button component type
     * @return command button behavior
     */
    public static <C extends AbstractButton> ViewBehavior<C> commandButton(
            Command command,
            CommandResources resources
    ) {
        return commandAction(command, resources, "command/button/");
    }

    /**
     * Creates a behavior that installs a command action on a menu item.
     *
     * <p>Menu items are Swing buttons, but this named factory makes generated
     * behavior plans easier to read.</p>
     *
     * @param command command to bind
     * @param resources command resource resolver
     * @param <C> menu item type
     * @return command menu item behavior
     */
    public static <C extends AbstractButton> ViewBehavior<C> commandMenuItem(
            Command command,
            CommandResources resources
    ) {
        return commandAction(command, resources, "command/menu-item/");
    }

    private static <C extends AbstractButton> ViewBehavior<C> commandAction(
            Command command,
            CommandResources resources,
            String keyPrefix
    ) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(resources, "resources");
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.multiple(
                        BehaviorKey.of(keyPrefix + command.key().id()),
                        BehaviorPhase.INTERACTION
                );
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                SwingEdt.requireEdt();
                Action previousAction = context.component().getAction();
                SwingActionAdapter action = new SwingActionAdapter(command, resources);
                context.component().setAction(action);
                return () -> {
                    SwingEdt.requireEdt();
                    context.component().setAction(previousAction);
                    action.close();
                };
            }
        };
    }

    /**
     * Creates a behavior that dispatches a command through a component key
     * binding.
     *
     * @param command command with accelerator metadata
     * @param resources command resource resolver
     * @param condition Swing input-map condition
     * @param <C> component type
     * @return key binding behavior
     */
    public static <C extends JComponent> ViewBehavior<C> commandKeyBinding(
            Command command,
            CommandResources resources,
            int condition
    ) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(resources, "resources");
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.multiple(
                        BehaviorKey.of("command/key-binding/" + command.key().id()),
                        BehaviorPhase.INTERACTION
                );
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                SwingEdt.requireEdt();
                ActionDescriptor descriptor = command.descriptor();
                if (descriptor.accelerator() == null) {
                    throw new IllegalArgumentException("Command has no accelerator: " + command.key());
                }
                KeyStroke stroke = KeyStroke.getKeyStroke(
                        descriptor.accelerator().keyCode(),
                        descriptor.accelerator().modifiers()
                );
                Object actionKey = command.key();
                InputMap inputMap = context.component().getInputMap(condition);
                ActionMap actionMap = context.component().getActionMap();
                Object previousInput = inputMap.get(stroke);
                Action previousAction = actionMap.get(actionKey);
                SwingActionAdapter action = new SwingActionAdapter(command, resources);
                inputMap.put(stroke, actionKey);
                actionMap.put(actionKey, action);
                return () -> {
                    SwingEdt.requireEdt();
                    if (previousInput == null) {
                        inputMap.remove(stroke);
                    } else {
                        inputMap.put(stroke, previousInput);
                    }
                    if (previousAction == null) {
                        actionMap.remove(actionKey);
                    } else {
                        actionMap.put(actionKey, previousAction);
                    }
                    action.close();
                };
            }
        };
    }
}
