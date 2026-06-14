package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import java.util.Objects;

/**
 * Resource-key based presentation metadata for a command.
 *
 * <p>Application and generated code should pass stable typed resource keys
 * instead of hard-coded button labels or tooltip strings. A later resource
 * service can resolve those keys for localization; the Swing adapter accepts a
 * small resolver so this core descriptor remains independent from resource
 * bundle mechanics.</p>
 *
 * <p>Generated {@code @UiAction} methods create {@code ActionDescriptor}
 * constants in an owner-specific companion, for example
 * {@code CustomerPresenterActions}. The same generated companion also
 * exposes descriptor lists for menu/toolbar assembly and command factories
 * that bind descriptors to an owner instance. Handwritten presenters may build
 * descriptors directly with {@link #action(ActionKey, ResourceKey)} or
 * {@link #toggle(ActionKey, ResourceKey)}.</p>
 *
 * @param key command/action identity
 * @param textKey optional resource key for visible text
 * @param tooltipKey optional resource key for tooltip text
 * @param accelerator optional keyboard shortcut metadata
 * @param mnemonicKeyCode optional mnemonic key code
 * @param selectable whether this command owns toggle selected state
 */
public record ActionDescriptor(
        ActionKey key,
        ResourceKey<String> textKey,
        ResourceKey<String> tooltipKey,
        AcceleratorDescriptor accelerator,
        Integer mnemonicKeyCode,
        boolean selectable
) {

    /**
     * Creates action metadata.
     *
     * @param key command/action identity
     * @param textKey optional resource key for visible text
     * @param tooltipKey optional resource key for tooltip text
     * @param accelerator optional keyboard shortcut metadata
     * @param mnemonicKeyCode optional mnemonic key code
     * @param selectable whether this command owns toggle selected state
     */
    public ActionDescriptor {
        Objects.requireNonNull(key, "key");
    }

    /**
     * Creates non-toggle action metadata.
     *
     * @param key command/action identity
     * @param textKey optional text resource key
     * @return descriptor
     */
    public static ActionDescriptor action(ActionKey key, ResourceKey<String> textKey) {
        return new ActionDescriptor(key, textKey, null, null, null, false);
    }

    /**
     * Creates toggle action metadata.
     *
     * @param key command/action identity
     * @param textKey optional text resource key
     * @return descriptor
     */
    public static ActionDescriptor toggle(ActionKey key, ResourceKey<String> textKey) {
        return new ActionDescriptor(key, textKey, null, null, null, true);
    }

    /**
     * Returns a copy with tooltip metadata.
     *
     * @param key tooltip resource key
     * @return descriptor copy
     */
    public ActionDescriptor withTooltip(ResourceKey<String> key) {
        return new ActionDescriptor(this.key, textKey, key, accelerator, mnemonicKeyCode, selectable);
    }

    /**
     * Returns a copy with accelerator metadata.
     *
     * @param accelerator accelerator descriptor
     * @return descriptor copy
     */
    public ActionDescriptor withAccelerator(AcceleratorDescriptor accelerator) {
        return new ActionDescriptor(key, textKey, tooltipKey, accelerator, mnemonicKeyCode, selectable);
    }

    /**
     * Returns a copy with mnemonic metadata.
     *
     * @param keyCode mnemonic key code
     * @return descriptor copy
     */
    public ActionDescriptor withMnemonic(int keyCode) {
        return new ActionDescriptor(key, textKey, tooltipKey, accelerator, keyCode, selectable);
    }
}
