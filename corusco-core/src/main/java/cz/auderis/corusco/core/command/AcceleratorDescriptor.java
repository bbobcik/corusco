package cz.auderis.corusco.core.command;

/**
 * Swing-neutral keyboard shortcut metadata.
 *
 * <p>The numeric values intentionally mirror the key-code and modifier masks
 * used by desktop toolkits, but this descriptor does not reference AWT/Swing
 * classes. Swing integration converts it to a {@code KeyStroke} at the
 * boundary.</p>
 *
 * @param keyCode keyboard key code
 * @param modifiers keyboard modifier mask
 */
public record AcceleratorDescriptor(int keyCode, int modifiers) {

    /**
     * Creates an accelerator descriptor.
     *
     * @param keyCode keyboard key code
     * @param modifiers keyboard modifier mask
     * @return descriptor
     */
    public static AcceleratorDescriptor of(int keyCode, int modifiers) {
        return new AcceleratorDescriptor(keyCode, modifiers);
    }
}
