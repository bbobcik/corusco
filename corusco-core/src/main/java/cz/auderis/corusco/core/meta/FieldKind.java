package cz.auderis.corusco.core.meta;

/**
 * Presentation family assigned to generated field metadata.
 *
 * <p>The annotation processor records this value in field descriptors so
 * runtime code can choose an editor without inspecting source annotations
 * directly. The enum describes the intended control family only; it does not
 * bind callers to a specific Swing component class. Values may appear in
 * generated metadata and should therefore be treated as stable identifiers by
 * code that persists or compares descriptor data.</p>
 */
public enum FieldKind {

    /**
     * Text-editable field.
     */
    TEXT,

    /**
     * Boolean checkbox field.
     */
    CHECK_BOX,

    /**
     * Combo-box selection field.
     */
    COMBO_BOX,

    /**
     * Date-editable field.
     */
    DATE
}
