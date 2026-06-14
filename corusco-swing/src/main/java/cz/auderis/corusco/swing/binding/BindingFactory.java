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
import cz.auderis.corusco.core.value.ValueChangeListener;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.accessibility.AccessibleContext;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Factory for basic disposable Swing bindings.
 *
 * <p>All factory methods must be called on the EDT. Component listeners update
 * models with {@link ChangeOrigin#USER}; model-originated changes update
 * components with a feedback guard so the binding does not loop through Swing
 * document events. Returned bindings own every listener or model subscription
 * they install and restore component state documented by the individual
 * factory method when closed.</p>
 *
 * <p>These helpers adapt existing model instances; they do not take ownership
 * of the model, component, or resource values passed to them. Closing a binding
 * removes listeners and subscriptions but does not dispose the underlying
 * model.</p>
 */
public final class BindingFactory {

    private BindingFactory() {
    }

    /**
     * Binds a text field to a text field model.
     *
     * <p>The binding installs a document listener, initializes the component
     * from {@link TextFieldModel#rawText()}, and propagates user document
     * changes back to the model with {@link ChangeOrigin#USER}. Closing the
     * binding removes both the document listener and the model subscription.</p>
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
     * <p>The binding has the same raw-text and listener ownership semantics as
     * {@link #textField(JTextField, TextFieldModel)}.</p>
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
     * <p>The component is initialized from the model. User item events update
     * the model with {@link ChangeOrigin#USER}; model events update the button
     * without writing back through the item listener. Closing the binding
     * removes the item listener and value subscription.</p>
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
     * <p>The label is initialized immediately and updated synchronously when
     * the value changes. Closing the binding removes only the value
     * subscription; it does not restore the previous label text.</p>
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
     * Publishes static status text while a component owns focus.
     *
     * <p>A {@code null} status text is treated as an empty string. The status
     * label text present at focus gain is restored on focus loss or close.</p>
     *
     * @param component component whose focus controls status ownership
     * @param statusLabel shared status label
     * @param statusText status text to show while focused
     * @return binding
     */
    public static Binding statusText(JComponent component, JLabel statusLabel, String statusText) {
        return statusText(component, statusLabel, constantText(statusText));
    }

    /**
     * Publishes observable status text while a component owns focus.
     *
     * <p>The binding snapshots the status label text on focus gain and restores
     * that snapshot on focus loss or close. Status text changes are reflected
     * immediately only while the component is focused.</p>
     *
     * @param component component whose focus controls status ownership
     * @param statusLabel shared status label
     * @param statusText observable status text
     * @return binding
     */
    public static Binding statusText(JComponent component, JLabel statusLabel, ReadableValue<String> statusText) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(statusLabel, "statusLabel");
        Objects.requireNonNull(statusText, "statusText");
        boolean[] active = new boolean[1];
        String[] previousText = new String[1];
        FocusAdapter focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (!active[0]) {
                    previousText[0] = statusLabel.getText();
                    active[0] = true;
                }
                statusLabel.setText(normalizeStatusText(statusText.value()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                restoreStatus(statusLabel, active, previousText);
            }
        };
        component.addFocusListener(focusListener);
        Subscription subscription = statusText.subscribe(event -> {
            SwingEdt.requireEdt();
            if (active[0]) {
                statusLabel.setText(normalizeStatusText(event.newValue()));
            }
        });
        return () -> {
            SwingEdt.requireEdt();
            try {
                subscription.close();
                component.removeFocusListener(focusListener);
            } finally {
                restoreStatus(statusLabel, active, previousText);
            }
        };
    }

    /**
     * Sets accessible name and description while preserving previous values.
     *
     * <p>{@code null} accessible strings are normalized to empty strings.
     * Closing the binding restores the accessible name and description that
     * were present at installation time.</p>
     *
     * @param component component whose accessible context is updated
     * @param accessibleName accessible name, or {@code null} for blank
     * @param accessibleDescription accessible description, or {@code null} for blank
     * @return binding
     */
    public static Binding accessibleText(
            JComponent component,
            String accessibleName,
            String accessibleDescription
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        AccessibleContext accessibleContext = component.getAccessibleContext();
        String previousName = accessibleContext.getAccessibleName();
        String previousDescription = accessibleContext.getAccessibleDescription();

        accessibleContext.setAccessibleName(normalizeAccessibleText(accessibleName));
        accessibleContext.setAccessibleDescription(normalizeAccessibleText(accessibleDescription));

        return () -> {
            SwingEdt.requireEdt();
            accessibleContext.setAccessibleName(previousName);
            accessibleContext.setAccessibleDescription(previousDescription);
        };
    }

    /**
     * Binds button enabled state to a readable Boolean value.
     *
     * <p>Only {@link Boolean#TRUE} enables the button. Closing the binding
     * removes the value subscription but does not restore the previous enabled
     * state.</p>
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
     * <p>The binding snapshots the original border at installation time,
     * replaces it with a red line border while the problem set has errors, and
     * restores the original border on close.</p>
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

    private static ReadableValue<String> constantText(String text) {
        String value = normalizeStatusText(text);
        return new ReadableValue<>() {
            @Override
            public String value() {
                return value;
            }

            @Override
            public Subscription subscribe(ValueChangeListener<String> listener) {
                Objects.requireNonNull(listener, "listener");
                return Subscription.EMPTY;
            }
        };
    }

    private static void restoreStatus(JLabel statusLabel, boolean[] active, String[] previousText) {
        if (!active[0]) {
            return;
        }
        active[0] = false;
        statusLabel.setText(previousText[0]);
        previousText[0] = null;
    }

    private static String normalizeStatusText(String text) {
        return (text == null) ? "" : text;
    }

    private static String normalizeAccessibleText(String text) {
        return (text == null) ? "" : text;
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
