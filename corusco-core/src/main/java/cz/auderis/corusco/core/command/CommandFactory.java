package cz.auderis.corusco.core.command;

import java.util.Objects;

/**
 * Creates mutable command instances from action descriptors and handlers.
 *
 * <p>The factory is the usual bridge between generated or handwritten
 * {@link ActionDescriptor} metadata and runtime command state. It chooses the
 * correct command shape from the descriptor: normal actions use
 * {@link #command(ActionDescriptor, CommandHandler)} and selectable descriptors
 * use {@link #toggle(ActionDescriptor, boolean, CommandHandler)}. Passing a
 * selectable descriptor to a non-toggle factory, or a non-selectable descriptor
 * to the toggle factory, fails fast so generated metadata and runtime command
 * behavior cannot silently drift apart.</p>
 *
 * <p>Returned commands are Swing-free presentation objects. Swing adapters such
 * as {@code SwingActionAdapter} observe them later and apply EDT rules at the
 * UI boundary.</p>
 */
public final class CommandFactory {

    private CommandFactory() {
    }

    /**
     * Creates an enabled command with no selected state.
     *
     * <p>The descriptor must not be selectable. The handler is retained
     * strongly by the returned command and invoked synchronously when the
     * command executes.</p>
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
     * <p>The descriptor must not be selectable. The initial enabled flag is
     * stored in the returned command's observable enabled value.</p>
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
     * <p>The descriptor must be selectable. The selected flag is stored in the
     * returned command's observable selected value and can be mirrored by Swing
     * buttons or menu items through a command adapter.</p>
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
