package cz.auderis.corusco.core.command;

import java.util.Objects;

/**
 * Factory for common command shapes.
 */
public final class CommandFactory {

    private CommandFactory() {
    }

    /**
     * Creates an enabled command with no selected state.
     *
     * @param descriptor action metadata
     * @param handler command handler
     * @return mutable command
     */
    public static MutableCommand command(ActionDescriptor descriptor, CommandHandler handler) {
        return new DefaultCommand(requireNonToggle(descriptor), true, false, handler);
    }

    /**
     * Creates a command with explicit initial enabled state.
     *
     * @param descriptor action metadata
     * @param enabled initial enabled flag
     * @param handler command handler
     * @return mutable command
     */
    public static MutableCommand command(ActionDescriptor descriptor, boolean enabled, CommandHandler handler) {
        return new DefaultCommand(requireNonToggle(descriptor), enabled, false, handler);
    }

    /**
     * Creates an enabled toggle command.
     *
     * @param descriptor toggle action metadata
     * @param selected initial selected flag
     * @param handler command handler
     * @return mutable command
     */
    public static MutableCommand toggle(ActionDescriptor descriptor, boolean selected, CommandHandler handler) {
        return new DefaultCommand(requireToggle(descriptor), true, selected, handler);
    }

    private static ActionDescriptor requireNonToggle(ActionDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        if (descriptor.selectable()) {
            throw new IllegalArgumentException("Use toggle() for selectable descriptors: " + descriptor.key());
        }
        return descriptor;
    }

    private static ActionDescriptor requireToggle(ActionDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        if (!descriptor.selectable()) {
            throw new IllegalArgumentException("Toggle commands require selectable descriptors: " + descriptor.key());
        }
        return descriptor;
    }
}
