package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.help.HelpService;
import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.meta.FieldDescriptor;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.tooltip.TooltipPolicy;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.task.BusyOverlayBinding;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.KeyStroke;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * Factory methods for the built-in Swing behaviors used by Corusco views.
 *
 * <p>These behaviors cover the common responsibilities around generated and
 * handwritten forms: primary value bindings, validation feedback, composed
 * tooltips, focus interactions, help dispatch, accessibility metadata, and
 * busy overlays. The factories return behavior objects; the behavior does not
 * touch Swing components until a {@link BehaviorScope} installs it with a
 * {@link BehaviorContext}.</p>
 *
 * <p>Bindings and decorations retain the supplied models, observables, labels,
 * resources, and help topics until the returned binding is closed. Installation
 * happens against Swing components and should be performed on the event
 * dispatch thread. Factories validate required collaborators eagerly where
 * possible; failures that depend on a component or context occur during
 * behavior installation.</p>
 */
public final class StandardBehaviors {

    private StandardBehaviors() {
    }

    /**
     * Creates a text field binding behavior.
     *
     * <p>The behavior installs a primary binding through
     * {@link BindingFactory#textField(JTextField, TextFieldModel)} and closes
     * that binding when removed from the scope.</p>
     *
     * @param model text field model
     * @param <O> owner/model type
     * @param <T> semantic value type
     * @return text field binding behavior
     */
    public static <O, T> BindingBehavior<JTextField> textFieldBinding(TextFieldModel<O, T> model) {
        Objects.requireNonNull(model, "model");
        return new BindingBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.primaryBinding(StandardBehaviorKeys.TEXT_BINDING);
            }

            @Override
            public Binding install(BehaviorContext<JTextField> context) {
                return BindingFactory.textField(context.component(), model);
            }
        };
    }

    /**
     * Creates a text area binding behavior.
     *
     * <p>The behavior installs a primary binding through
     * {@link BindingFactory#textArea(JTextArea, TextFieldModel)} and closes
     * that binding when removed from the scope.</p>
     *
     * @param model text field model
     * @param <O> owner/model type
     * @param <T> semantic value type
     * @return text area binding behavior
     */
    public static <O, T> BindingBehavior<JTextArea> textAreaBinding(TextFieldModel<O, T> model) {
        Objects.requireNonNull(model, "model");
        return new BindingBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.primaryBinding(StandardBehaviorKeys.TEXT_BINDING);
            }

            @Override
            public Binding install(BehaviorContext<JTextArea> context) {
                return BindingFactory.textArea(context.component(), model);
            }
        };
    }

    /**
     * Creates a checkbox selected-state binding behavior.
     *
     * <p>The behavior installs a primary selected-state binding and removes its
     * listeners when the returned binding is closed.</p>
     *
     * @param model Boolean field model
     * @param <O> owner/model type
     * @return checkbox binding behavior
     */
    public static <O> BindingBehavior<AbstractButton> checkBoxBinding(FieldModel<O, Boolean> model) {
        Objects.requireNonNull(model, "model");
        return new BindingBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.primaryBinding(StandardBehaviorKeys.CHECKBOX_BINDING);
            }

            @Override
            public Binding install(BehaviorContext<AbstractButton> context) {
                return BindingFactory.selected(context.component(), model);
            }
        };
    }

    /**
     * Creates a validation tooltip decoration behavior.
     *
     * <p>The installed binding observes the problem value and writes the
     * component tooltip text. It shares the same behavior key as composed
     * tooltips because Swing components have one tooltip slot.</p>
     *
     * @param problems observable problems
     * @param <C> component type
     * @return tooltip behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> validationTooltip(ReadableValue<ProblemSet> problems) {
        Objects.requireNonNull(problems, "problems");
        return new DecorationBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.TOOLTIP, BehaviorPhase.DECORATION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.validationTooltip(context.component(), problems);
            }
        };
    }

    /**
     * Creates a composed tooltip decoration behavior.
     *
     * <p>The behavior uses the core {@link TooltipPolicy#standard()} ordering:
     * validation feedback first, then disabled reason, static descriptor help,
     * and finally the F1/help indicator.</p>
     *
     * @param problems observable problems
     * @param disabledReason observable disabled reason, or {@code null}
     * @param staticHelp static descriptor/resource help
     * @param helpAvailable whether F1/context help is available
     * @param <C> component type
     * @return tooltip behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> composedTooltip(
            ReadableValue<ProblemSet> problems,
            ReadableValue<String> disabledReason,
            String staticHelp,
            boolean helpAvailable
    ) {
        return composedTooltip(problems, disabledReason, staticHelp, helpAvailable, TooltipPolicy.standard());
    }

    /**
     * Creates a composed tooltip decoration behavior.
     *
     * <p>The installed binding observes problem and disabled-reason values,
     * combines them with static help according to the supplied policy, and
     * writes the component tooltip. {@code disabledReason} may be {@code null}
     * when disabled-state text is not part of the tooltip.</p>
     *
     * @param problems observable problems
     * @param disabledReason observable disabled reason, or {@code null}
     * @param staticHelp static descriptor/resource help
     * @param helpAvailable whether F1/context help is available
     * @param policy tooltip policy
     * @param <C> component type
     * @return tooltip behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> composedTooltip(
            ReadableValue<ProblemSet> problems,
            ReadableValue<String> disabledReason,
            String staticHelp,
            boolean helpAvailable,
            TooltipPolicy policy
    ) {
        Objects.requireNonNull(problems, "problems");
        Objects.requireNonNull(policy, "policy");
        return new DecorationBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.TOOLTIP, BehaviorPhase.DECORATION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.composedTooltip(
                        context.component(),
                        problems,
                        disabledReason,
                        staticHelp,
                        helpAvailable,
                        policy
                );
            }
        };
    }

    /**
     * Creates a validation border decoration behavior.
     *
     * <p>The installed binding observes the problem value and updates the
     * component border to reflect validation state, restoring the original
     * border when closed.</p>
     *
     * @param problems observable problems
     * @param <C> component type
     * @return border behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> validationBorder(ReadableValue<ProblemSet> problems) {
        Objects.requireNonNull(problems, "problems");
        return new DecorationBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.VALIDATION_BORDER, BehaviorPhase.DECORATION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.validationBorder(context.component(), problems);
            }
        };
    }

    /**
     * Creates a behavior that selects all text when a text component gains focus.
     *
     * <p>Installation adds a focus listener and cleanup removes the same
     * listener. Both operations require the event dispatch thread.</p>
     *
     * @param <C> text component type
     * @return focus behavior
     */
    public static <C extends JTextComponent> ViewBehavior<C> selectAllOnFocus() {
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.SELECT_ALL_ON_FOCUS, BehaviorPhase.INTERACTION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                SwingEdt.requireEdt();
                FocusAdapter listener = new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        context.component().selectAll();
                    }
                };
                context.component().addFocusListener(listener);
                return () -> {
                    SwingEdt.requireEdt();
                    context.component().removeFocusListener(listener);
                };
            }
        };
    }

    /**
     * Creates a behavior that runs a commit action when Enter is pressed.
     *
     * <p>Installation adds a key listener and cleanup removes it. The supplied
     * action is retained and invoked synchronously from the key event on the
     * event dispatch thread.</p>
     *
     * @param commitAction commit action
     * @param <C> text component type
     * @return key behavior
     */
    public static <C extends JTextComponent> ViewBehavior<C> commitOnEnter(Runnable commitAction) {
        Objects.requireNonNull(commitAction, "commitAction");
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.COMMIT_ON_ENTER, BehaviorPhase.INTERACTION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                SwingEdt.requireEdt();
                KeyAdapter listener = new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            commitAction.run();
                        }
                    }
                };
                context.component().addKeyListener(listener);
                return () -> {
                    SwingEdt.requireEdt();
                    context.component().removeKeyListener(listener);
                };
            }
        };
    }

    /**
     * Creates a behavior that publishes static status-bar text while focused.
     *
     * <p>The installed binding observes focus on the target component and writes
     * to the shared status label while focused, restoring the previous label
     * state according to the binding contract when closed.</p>
     *
     * @param statusLabel shared status label
     * @param statusText status text to show while focused
     * @param <C> component type
     * @return status text behavior
     */
    public static <C extends JComponent> ViewBehavior<C> statusText(JLabel statusLabel, String statusText) {
        Objects.requireNonNull(statusLabel, "statusLabel");
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.STATUS_TEXT, BehaviorPhase.INTERACTION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.statusText(context.component(), statusLabel, statusText);
            }
        };
    }

    /**
     * Creates a behavior that publishes observable status-bar text while focused.
     *
     * <p>The installed binding observes both focus and the status text value.
     * The value subscription is retained until the binding is closed.</p>
     *
     * @param statusLabel shared status label
     * @param statusText observable status text
     * @param <C> component type
     * @return status text behavior
     */
    public static <C extends JComponent> ViewBehavior<C> statusText(
            JLabel statusLabel,
            ReadableValue<String> statusText
    ) {
        Objects.requireNonNull(statusLabel, "statusLabel");
        Objects.requireNonNull(statusText, "statusText");
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.STATUS_TEXT, BehaviorPhase.INTERACTION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.statusText(context.component(), statusLabel, statusText);
            }
        };
    }

    /**
     * Creates a behavior that sets accessible name and description.
     *
     * <p>The installed binding writes directly to the component's accessible
     * context and restores the previous accessible text when closed.</p>
     *
     * @param accessibleName accessible name
     * @param accessibleDescription accessible description
     * @param <C> component type
     * @return accessible text behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> accessibleText(
            String accessibleName,
            String accessibleDescription
    ) {
        return new DecorationBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.ACCESSIBLE_TEXT, BehaviorPhase.DECORATION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                return BindingFactory.accessibleText(context.component(), accessibleName, accessibleDescription);
            }
        };
    }

    /**
     * Creates a behavior that derives accessible text from field metadata.
     *
     * <p>The accessible name is resolved from {@link FieldDescriptor#labelKey()}
     * and the description is resolved from {@link FieldDescriptor#tooltipKey()}
     * when present. Missing optional descriptions become blank descriptions.</p>
     *
     * @param descriptor field descriptor
     * @param resources resource lookup
     * @param <C> component type
     * @return accessible text behavior
     */
    public static <C extends JComponent> DecorationBehavior<C> accessibleText(
            FieldDescriptor<?, ?> descriptor,
            Resources resources
    ) {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(resources, "resources");
        String accessibleName = resources.require(descriptor.labelKey());
        String accessibleDescription = resolveOptional(resources, descriptor.tooltipKey());
        return accessibleText(accessibleName, accessibleDescription);
    }

    /**
     * Creates a busy overlay decoration behavior for a {@code JLayer}-wrapped
     * view.
     *
     * <p>The target component must be the {@link JLayer}, not the wrapped view.
     * The installed binding observes the busy value and repaints the layer while
     * active.</p>
     *
     * @param busy observable busy state
     * @param <C> wrapped component type
     * @return busy overlay behavior
     */
    public static <C extends JComponent> DecorationBehavior<JLayer<C>> busyOverlay(ReadableValue<Boolean> busy) {
        Objects.requireNonNull(busy, "busy");
        return new DecorationBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.BUSY_OVERLAY, BehaviorPhase.DECORATION);
            }

            @Override
            public Binding install(BehaviorContext<JLayer<C>> context) {
                return BusyOverlayBinding.install(context.component(), busy);
            }
        };
    }

    /**
     * Creates a behavior that opens a help topic when F1 is pressed.
     *
     * <p>Installation requires a help service in the behavior context, installs
     * an F1 input/action-map entry, and restores previous entries during
     * cleanup. The help service is invoked synchronously from the Swing action
     * on the event dispatch thread.</p>
     *
     * @param topic help topic
     * @param <C> component type
     * @return help behavior
     */
    public static <C extends JComponent> ViewBehavior<C> helpOnF1(HelpTopic topic) {
        Objects.requireNonNull(topic, "topic");
        return new ViewBehavior<>() {
            private static final String ACTION_KEY_PREFIX = "corusco.helpOnF1.";

            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(StandardBehaviorKeys.HELP_ON_F1, BehaviorPhase.INTERACTION);
            }

            @Override
            public Binding install(BehaviorContext<C> context) {
                SwingEdt.requireEdt();
                HelpService helpService = context.helpServiceOptional()
                        .orElseThrow(() -> new IllegalStateException("HelpService is required for F1 help behavior"));
                C component = context.component();
                InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
                ActionMap actionMap = component.getActionMap();
                KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
                Object oldInput = inputMap.get(keyStroke);
                String actionKey = ACTION_KEY_PREFIX + topic.id();
                javax.swing.Action oldAction = actionMap.get(actionKey);

                inputMap.put(keyStroke, actionKey);
                actionMap.put(actionKey, new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent event) {
                        helpService.open(topic, component, "F1");
                    }
                });

                return () -> {
                    SwingEdt.requireEdt();
                    if (oldInput == null) {
                        inputMap.remove(keyStroke);
                    } else {
                        inputMap.put(keyStroke, oldInput);
                    }
                    if (oldAction == null) {
                        actionMap.remove(actionKey);
                    } else {
                        actionMap.put(actionKey, oldAction);
                    }
                };
            }
        };
    }

    private static String resolveOptional(Resources resources, ResourceKey<String> key) {
        return (key == null) ? "" : resources.resolve(key, "");
    }
}
