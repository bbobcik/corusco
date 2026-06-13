package cz.auderis.corusco.swing.command;

import cz.auderis.corusco.core.command.AcceleratorDescriptor;
import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingActionAdapterTest {

    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer.save.text", String.class);
    private static final ResourceKey<String> SAVE_TOOLTIP = ResourceKey.of("customer.save.tooltip", String.class);
    private static final CommandResources RESOURCES = Map.of(
            SAVE_TEXT, "Save",
            SAVE_TOOLTIP, "Save customer"
    )::get;

    @Test
    void oneActionBacksButtonAndMenuItem() {
        SwingEdt.runAndWait(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableCommand command = CommandFactory.command(
                    descriptor(),
                    invoked -> calls.incrementAndGet()
            );
            SwingActionAdapter action = new SwingActionAdapter(command, RESOURCES);
            JButton button = new JButton(action);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);

            command.setEnabled(false);
            assertThat(button.isEnabled()).isFalse();
            assertThat(menuItem.isEnabled()).isFalse();

            command.setEnabled(true);
            button.doClick();
            menuItem.doClick();

            assertThat(button.getText()).isEqualTo("Save");
            assertThat(menuItem.getToolTipText()).isEqualTo("Save customer");
            assertThat(calls).hasValue(2);
            action.close();
        });
    }

    @Test
    void toggleActionCopiesButtonSelectionIntoCommand() {
        SwingEdt.runAndWait(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableCommand command = CommandFactory.toggle(
                    ActionDescriptor.toggle(SAVE, SAVE_TEXT),
                    false,
                    invoked -> calls.incrementAndGet()
            );
            SwingActionAdapter action = new SwingActionAdapter(command, RESOURCES);
            JToggleButton toggle = new JToggleButton(action);

            toggle.doClick();

            assertThat(command.isSelected()).isTrue();
            assertThat(calls).hasValue(1);
            action.close();
        });
    }

    @Test
    void closedAdapterStopsObservingCommandState() {
        SwingEdt.runAndWait(() -> {
            MutableCommand command = CommandFactory.command(descriptor(), invoked -> {
            });
            SwingActionAdapter action = new SwingActionAdapter(command, RESOURCES);

            action.close();
            command.setEnabled(false);

            assertThat(action.isEnabled()).isTrue();
        });
    }

    private static ActionDescriptor descriptor() {
        return ActionDescriptor.action(SAVE, SAVE_TEXT)
                .withTooltip(SAVE_TOOLTIP)
                .withMnemonic(KeyEvent.VK_S)
                .withAccelerator(AcceleratorDescriptor.of(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
    }
}
