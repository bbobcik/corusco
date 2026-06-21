package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandTest {

    private static final ActionKey SAVE = ActionKey.of("customer/save");
    private static final ActionKey ACTIVE = ActionKey.of("customer/active");
    private static final ResourceKey<String> SAVE_TEXT = ResourceKey.of("customer.save.text", String.class);

    @Test
    void commandExecutesOnlyWhenEnabled() {
        AtomicInteger calls = new AtomicInteger();
        MutableCommand command = CommandFactory.command(
                ActionDescriptor.action(SAVE, SAVE_TEXT),
                invoked -> calls.incrementAndGet()
        );

        command.execute();
        command.setEnabled(false);
        command.execute();
        command.setEnabled(true, StandardChangeOrigin.USER);
        command.execute();

        assertThat(calls).hasValue(2);
    }

    @Test
    void toggleCommandOwnsSelectedState() {
        MutableCommand command = CommandFactory.toggle(
                ActionDescriptor.toggle(ACTIVE, SAVE_TEXT),
                false,
                invoked -> {
                }
        );

        command.setSelected(true, StandardChangeOrigin.USER);

        assertThat(command.selectable()).isTrue();
        assertThat(command.isSelected()).isTrue();
    }

    @Test
    void nonToggleCommandRejectsSelectedMutation() {
        MutableCommand command = CommandFactory.command(
                ActionDescriptor.action(SAVE, SAVE_TEXT),
                invoked -> {
                }
        );

        assertThatThrownBy(() -> command.setSelected(true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Command is not selectable: ActionKey[customer/save]");
    }

    @Test
    void commandSetRejectsDuplicateKeys() {
        MutableCommand first = CommandFactory.command(ActionDescriptor.action(SAVE, SAVE_TEXT), invoked -> {
        });
        MutableCommand second = CommandFactory.command(ActionDescriptor.action(SAVE, SAVE_TEXT), invoked -> {
        });

        assertThatThrownBy(() -> CommandSet.of(first, second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate command key: ActionKey[customer/save]");
    }

    @Test
    void commandSetRetainsInsertionOrder() {
        MutableCommand save = CommandFactory.command(ActionDescriptor.action(SAVE, SAVE_TEXT), invoked -> {
        });
        MutableCommand active = CommandFactory.toggle(ActionDescriptor.toggle(ACTIVE, SAVE_TEXT), false, invoked -> {
        });

        CommandSet set = CommandSet.of(save, active);

        assertThat(set.commands()).containsExactly(save, active);
        assertThat(set.require(ACTIVE)).isSameAs(active);
    }
}
