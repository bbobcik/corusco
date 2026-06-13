package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.help.HelpService;
import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.tooltip.TooltipPolicy;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.SwingEdt;
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
import javax.swing.KeyStroke;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * Factory methods for built-in behaviors.
 */
public final class StandardBehaviors {

    private StandardBehaviors() {
    }

    /**
     * Creates a text field binding behavior.
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
     * Creates a behavior that opens a help topic when F1 is pressed.
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
}
