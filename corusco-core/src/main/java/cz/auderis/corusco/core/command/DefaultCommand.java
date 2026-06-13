package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.core.key.ActionKey;
import java.util.Objects;

final class DefaultCommand implements MutableCommand {

    private final ActionDescriptor descriptor;
    private final SimpleValue<Boolean> enabled;
    private final SimpleValue<Boolean> selected;
    private final CommandHandler handler;

    DefaultCommand(ActionDescriptor descriptor, boolean enabled, boolean selected, CommandHandler handler) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.enabled = SimpleValue.of(enabled);
        this.selected = SimpleValue.of(descriptor.selectable() && selected);
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public ActionKey key() {
        return descriptor.key();
    }

    @Override
    public ActionDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public ReadableValue<Boolean> enabled() {
        return enabled;
    }

    @Override
    public ReadableValue<Boolean> selected() {
        return selected;
    }

    @Override
    public void setEnabled(boolean enabled, ChangeOrigin origin) {
        this.enabled.setValue(enabled, origin);
    }

    @Override
    public void setSelected(boolean selected, ChangeOrigin origin) {
        if (!descriptor.selectable()) {
            throw new IllegalStateException("Command is not selectable: " + key());
        }
        this.selected.setValue(selected, origin);
    }

    @Override
    public void execute() {
        if (isEnabled()) {
            handler.execute(this);
        }
    }
}
