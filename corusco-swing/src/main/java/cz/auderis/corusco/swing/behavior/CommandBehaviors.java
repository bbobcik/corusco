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
 * Factory methods for installing Corusco commands into Swing components.
 *
 * <p>The returned behaviors adapt a core {@link Command} to a Swing
 * {@link Action}, then install that action into a button, menu item, or
 * component key map. They are intended for generated view plans and
 * handwritten views that want the same command metadata, resource lookup, and
 * enabled-state handling.</p>
 *
 * <p>Installation and returned binding cleanup must run on the Swing event
 * dispatch thread. The behaviors retain the supplied command and resources for
 * as long as the binding is installed, restore the previous Swing action or key
 * mapping on close, and close the created {@link SwingActionAdapter} during
 * cleanup.</p>
 */
public final class CommandBehaviors {

    private CommandBehaviors() {
    }

    /**
     * Creates a behavior that installs a command action on a button-like
     * component.
     *
     * <p>Installing the behavior replaces the component's current action and
     * the returned binding restores it. The command and resources must not be
     * {@code null}.</p>
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
     * <p>Installation requires the command descriptor to declare an
     * accelerator. The behavior records the previous input-map and action-map
     * entries for the same stroke/key and restores them when the returned
     * binding is closed.</p>
     *
     * @param command command with accelerator metadata
     * @param resources command resource resolver
     * @param condition Swing input-map condition
     * @param <C> component type
     * @return key binding behavior
     * @throws IllegalArgumentException when the command has no accelerator,
     *         during behavior installation
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
