package cz.auderis.corusco.core.meta;

/**
 * Stable presentation family for a generated form-field editor.
 *
 * <p>The family describes the semantic editor intent, not a concrete Swing
 * component class. Bindings may realize the same family with standard Swing,
 * SwingX-style widgets, or application-specific controls.</p>
 */
public enum EditorFamily {

    /**
     * Text-editable field.
     */
    TEXT,

    /**
     * Date-editable field.
     */
    DATE,

    /**
     * Boolean or mapped checkbox field.
     */
    CHECK_BOX,

    /**
     * Combo-box selection field.
     */
    COMBO_BOX,

    /**
     * Radio-button single-selection field.
     */
    RADIO_GROUP,

    /**
     * Checkbox-group multi-selection field.
     */
    CHECK_BOX_GROUP,

    /**
     * Bounded numeric field, typically rendered by a spinner or slider.
     */
    NUMERIC_RANGE,

    /**
     * Dialog-backed picker field.
     */
    PICKER,

    /**
     * Rich value with a compact summary and application-specific editor.
     */
    RICH_VALUE
}
