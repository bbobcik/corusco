package cz.auderis.corusco.swing.binding;

import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.tooltip.TooltipContent;
import cz.auderis.corusco.core.tooltip.TooltipPolicy;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import java.awt.Color;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Factory for basic Swing bindings.
 *
 * <p>All factory methods must be called on the EDT. Component listeners update
 * models with {@link ChangeOrigin#USER}; model-originated changes update
 * components with a feedback guard so the binding does not loop through Swing
 * document events.</p>
 */
public final class BindingFactory {

    private BindingFactory() {
    }

    /**
     * Binds a text field to a text field model.
     *
     * @param component Swing text field
     * @param model text model
     * @param <O> owner/model type
     * @param <T> semantic value type
     * @return binding
     */
    public static <O, T> Binding textField(JTextField component, TextFieldModel<O, T> model) {
        return bindText(component::setText, component::getText, component.getDocument()::addDocumentListener,
                component.getDocument()::removeDocumentListener, model);
    }

    /**
     * Binds a text area to a text field model.
     *
     * @param component Swing text area
     * @param model text model
     * @param <O> owner/model type
     * @param <T> semantic value type
     * @return binding
     */
    public static <O, T> Binding textArea(JTextArea component, TextFieldModel<O, T> model) {
        return bindText(component::setText, component::getText, component.getDocument()::addDocumentListener,
                component.getDocument()::removeDocumentListener, model);
    }

    /**
     * Binds selected state of an abstract button to a Boolean field model.
     *
     * @param button checkbox, radio button, or toggle button
     * @param model Boolean field model
     * @param <O> owner/model type
     * @return binding
     */
    public static <O> Binding selected(AbstractButton button, FieldModel<O, Boolean> model) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(button, "button");
        Objects.requireNonNull(model, "model");
        SubscriptionScope scope = new SubscriptionScope();
        boolean[] updating = new boolean[1];
        button.setSelected(Boolean.TRUE.equals(model.value().value()));
        java.awt.event.ItemListener itemListener = event -> {
            if (!updating[0]) {
                model.setValue(button.isSelected(), ChangeOrigin.USER);
            }
        };
        button.addItemListener(itemListener);
        scope.onClose(() -> button.removeItemListener(itemListener));
        scope.add(model.value().subscribe(event -> {
            SwingEdt.requireEdt();
            updating[0] = true;
            try {
                button.setSelected(Boolean.TRUE.equals(event.newValue()));
            } finally {
                updating[0] = false;
            }
        }));
        return () -> {
            SwingEdt.requireEdt();
            scope.close();
        };
    }

    /**
     * Binds label text to a readable string value.
     *
     * @param label label to update
     * @param value readable string value
     * @return binding
     */
    public static Binding labelText(JLabel label, ReadableValue<String> value) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(value, "value");
        label.setText(value.value());
        Subscription subscription = value.subscribe(event -> {
            SwingEdt.requireEdt();
            label.setText(event.newValue());
        });
        return () -> {
            SwingEdt.requireEdt();
            subscription.close();
        };
    }

    /**
     * Binds button enabled state to a readable Boolean value.
     *
     * @param button button to update
     * @param enabled readable enabled state
     * @return binding
     */
    public static Binding enabled(AbstractButton button, ReadableValue<Boolean> enabled) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(button, "button");
        Objects.requireNonNull(enabled, "enabled");
        button.setEnabled(Boolean.TRUE.equals(enabled.value()));
        Subscription subscription = enabled.subscribe(event -> {
            SwingEdt.requireEdt();
            button.setEnabled(Boolean.TRUE.equals(event.newValue()));
        });
        return () -> {
            SwingEdt.requireEdt();
            subscription.close();
        };
    }

    /**
     * Binds component tooltip text to the most severe problem message.
     *
     * <p>This is a validation-only compatibility wrapper over
     * {@link #composedTooltip(javax.swing.JComponent, ReadableValue, ReadableValue, String, boolean, TooltipPolicy)}.
     * It uses the same problem ordering as the core tooltip policy and restores
     * the component's previous tooltip when closed.</p>
     *
     * @param component component tooltip target
     * @param problems readable problem set
     * @return binding
     */
    public static Binding validationTooltip(javax.swing.JComponent component, ReadableValue<ProblemSet> problems) {
        return composedTooltip(
                component,
                problems,
                null,
                "",
                false,
                TooltipPolicy.withoutHelpIndicator()
        );
    }

    /**
     * Binds component tooltip text to composed dynamic/static tooltip content.
     *
     * <p>The binding owns the Swing tooltip while installed. It restores the
     * tooltip text that was present at installation time when closed.</p>
     *
     * @param component component tooltip target
     * @param problems readable problem set
     * @param disabledReason readable disabled reason, or {@code null}
     * @param staticHelp static help text
     * @param helpAvailable whether F1/context help is available
     * @return binding
     */
    public static Binding composedTooltip(
            javax.swing.JComponent component,
            ReadableValue<ProblemSet> problems,
            ReadableValue<String> disabledReason,
            String staticHelp,
            boolean helpAvailable
    ) {
        return composedTooltip(
                component,
                problems,
                disabledReason,
                staticHelp,
                helpAvailable,
                TooltipPolicy.standard()
        );
    }

    /**
     * Binds component tooltip text to composed dynamic/static tooltip content.
     *
     * @param component component tooltip target
     * @param problems readable problem set
     * @param disabledReason readable disabled reason, or {@code null}
     * @param staticHelp static help text
     * @param helpAvailable whether F1/context help is available
     * @param policy tooltip policy
     * @return binding
     */
    public static Binding composedTooltip(
            javax.swing.JComponent component,
            ReadableValue<ProblemSet> problems,
            ReadableValue<String> disabledReason,
            String staticHelp,
            boolean helpAvailable,
            TooltipPolicy policy
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(problems, "problems");
        Objects.requireNonNull(policy, "policy");
        String originalTooltip = component.getToolTipText();
        updateTooltip(component, problems.value(), disabledReasonValue(disabledReason), staticHelp, helpAvailable, policy);

        SubscriptionScope scope = new SubscriptionScope();
        scope.add(problems.subscribe(event -> {
            SwingEdt.requireEdt();
            updateTooltip(component, event.newValue(), disabledReasonValue(disabledReason), staticHelp, helpAvailable, policy);
        }));
        if (disabledReason != null) {
            scope.add(disabledReason.subscribe(event -> {
                SwingEdt.requireEdt();
                updateTooltip(component, problems.value(), event.newValue(), staticHelp, helpAvailable, policy);
            }));
        }
        return () -> {
            SwingEdt.requireEdt();
            try {
                scope.close();
            } finally {
                component.setToolTipText(originalTooltip);
            }
        };
    }

    /**
     * Binds component border to error presence.
     *
     * @param component component border target
     * @param problems readable problem set
     * @return binding
     */
    public static Binding validationBorder(javax.swing.JComponent component, ReadableValue<ProblemSet> problems) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(problems, "problems");
        Border original = component.getBorder();
        updateBorder(component, original, problems.value());
        Subscription subscription = problems.subscribe(event -> {
            SwingEdt.requireEdt();
            updateBorder(component, original, event.newValue());
        });
        return () -> {
            SwingEdt.requireEdt();
            subscription.close();
            component.setBorder(original);
        };
    }

    private static <O, T> Binding bindText(
            java.util.function.Consumer<String> setter,
            java.util.function.Supplier<String> getter,
            java.util.function.Consumer<DocumentListener> addListener,
            java.util.function.Consumer<DocumentListener> removeListener,
            TextFieldModel<O, T> model
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(model, "model");
        SubscriptionScope scope = new SubscriptionScope();
        boolean[] updating = new boolean[1];
        setter.accept(model.rawText().value());
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateModel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateModel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateModel();
            }

            private void updateModel() {
                if (!updating[0]) {
                    model.setRawText(getter.get(), ChangeOrigin.USER);
                }
            }
        };
        addListener.accept(listener);
        scope.onClose(() -> removeListener.accept(listener));
        scope.add(model.rawText().subscribe(event -> {
            SwingEdt.requireEdt();
            if (!Objects.equals(getter.get(), event.newValue())) {
                updating[0] = true;
                try {
                    setter.accept(event.newValue());
                } finally {
                    updating[0] = false;
                }
            }
        }));
        return () -> {
            SwingEdt.requireEdt();
            scope.close();
        };
    }

    private static String disabledReasonValue(ReadableValue<String> disabledReason) {
        return (disabledReason == null) ? "" : disabledReason.value();
    }

    private static void updateTooltip(
            javax.swing.JComponent component,
            ProblemSet problems,
            String disabledReason,
            String staticHelp,
            boolean helpAvailable,
            TooltipPolicy policy
    ) {
        TooltipContent content = new TooltipContent(problems, disabledReason, staticHelp, helpAvailable);
        String tooltip = policy.compose(content).orElse(null);
        component.setToolTipText(tooltip);
    }

    private static void updateBorder(javax.swing.JComponent component, Border original, ProblemSet problems) {
        component.setBorder(problems.hasErrors()
                ? BorderFactory.createLineBorder(Color.RED)
                : original);
    }
}
