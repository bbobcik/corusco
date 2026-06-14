package cz.auderis.corusco.swing.command;

import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.Command;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Disposable Swing {@link Action} backed by one Corusco command.
 *
 * <p>The adapter observes command enabled/selected values and updates Swing
 * action properties on the EDT. For selectable mutable commands, user-originated
 * button state is copied back into the command before the handler runs so
 * buttons, menu items, and key bindings share one selected-state owner.</p>
 *
 * <p>The adapter owns only its subscriptions to the command. Closing it is
 * idempotent through the underlying subscription scope and does not disable,
 * reset, or dispose the command itself. Swing serialization is not part of this
 * adapter's contract; use it as a live UI binding.</p>
 */
public final class SwingActionAdapter extends AbstractAction implements Binding {

    private final Command command;
    private final SubscriptionScope subscriptions = new SubscriptionScope();

    /**
     * Creates a Swing action adapter.
     *
     * <p>Action metadata is resolved once during construction. Later resource
     * changes are not observed by this adapter. Command state changes are
     * observed until {@link #close()} is called.</p>
     *
     * @param command command to adapt
     * @param resources resource resolver
     */
    public SwingActionAdapter(Command command, CommandResources resources) {
        SwingEdt.requireEdt();
        this.command = Objects.requireNonNull(command, "command");
        Objects.requireNonNull(resources, "resources");
        installMetadata(resources);
        setEnabled(command.isEnabled());
        putValue(Action.SELECTED_KEY, command.isSelected());
        subscriptions.add(command.enabled().subscribe(event -> {
            SwingEdt.requireEdt();
            setEnabled(Boolean.TRUE.equals(event.newValue()));
        }));
        subscriptions.add(command.selected().subscribe(event -> {
            SwingEdt.requireEdt();
            putValue(Action.SELECTED_KEY, Boolean.TRUE.equals(event.newValue()));
        }));
    }

    /**
     * Returns the adapted command.
     *
     * @return command
     */
    public Command command() {
        return command;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        SwingEdt.requireEdt();
        if (!command.isEnabled()) {
            return;
        }
        copySelectedStateFromEventSource(event);
        command.execute();
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        subscriptions.close();
    }

    private void installMetadata(CommandResources resources) {
        ActionDescriptor descriptor = command.descriptor();
        if (descriptor.textKey() != null) {
            putValue(Action.NAME, resources.resolve(descriptor.textKey()));
        }
        if (descriptor.tooltipKey() != null) {
            putValue(Action.SHORT_DESCRIPTION, resources.resolve(descriptor.tooltipKey()));
        }
        if (descriptor.accelerator() != null) {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    descriptor.accelerator().keyCode(),
                    descriptor.accelerator().modifiers()
            ));
        }
        if (descriptor.mnemonicKeyCode() != null) {
            putValue(Action.MNEMONIC_KEY, descriptor.mnemonicKeyCode());
        }
    }

    private void copySelectedStateFromEventSource(ActionEvent event) {
        if (!(command instanceof MutableCommand mutableCommand) || !command.selectable()) {
            return;
        }
        if (event.getSource() instanceof AbstractButton button) {
            mutableCommand.setSelected(button.isSelected(), ChangeOrigin.USER);
        }
    }
}
