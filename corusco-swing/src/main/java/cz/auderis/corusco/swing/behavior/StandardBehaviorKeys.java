package cz.auderis.corusco.swing.behavior;

/**
 * Shared keys used by built-in Swing behaviors.
 *
 * <p>Keys identify component responsibilities within a {@link BehaviorScope}.
 * Generated view plans and handwritten scopes use the same constants so primary
 * bindings, decorations, and interactions can detect conflicts consistently.
 * The key ids are stable because they may appear in diagnostics and tests, but
 * they are not a persistence format for application state.</p>
 */
public final class StandardBehaviorKeys {

    /**
     * Primary text component binding behavior.
     */
    public static final BehaviorKey TEXT_BINDING = BehaviorKey.of("binding/text");

    /**
     * Primary checkbox/toggle selected binding behavior.
     */
    public static final BehaviorKey CHECKBOX_BINDING = BehaviorKey.of("binding/checkbox");

    /**
     * Primary radio-group selected-value binding behavior.
     */
    public static final BehaviorKey RADIO_GROUP_BINDING = BehaviorKey.of("binding/radio-group");

    /**
     * Component-state binding behavior.
     */
    public static final BehaviorKey COMPONENT_STATE = BehaviorKey.of("binding/component-state");

    /**
     * Tooltip decoration behavior.
     */
    public static final BehaviorKey TOOLTIP = BehaviorKey.of("decoration/tooltip");

    /**
     * Validation tooltip decoration behavior.
     *
     * @deprecated use {@link #TOOLTIP}; validation tooltips share the same
     *         component tooltip slot as composed tooltips.
     */
    @Deprecated(since = "0.1", forRemoval = false)
    public static final BehaviorKey VALIDATION_TOOLTIP = TOOLTIP;

    /**
     * Validation border decoration behavior.
     */
    public static final BehaviorKey VALIDATION_BORDER = BehaviorKey.of("decoration/validation-border");

    /**
     * Select-all-on-focus interaction behavior.
     */
    public static final BehaviorKey SELECT_ALL_ON_FOCUS = BehaviorKey.of("interaction/select-all-on-focus");

    /**
     * Commit-on-enter interaction behavior.
     */
    public static final BehaviorKey COMMIT_ON_ENTER = BehaviorKey.of("interaction/commit-on-enter");

    /**
     * F1 help dispatch behavior.
     */
    public static final BehaviorKey HELP_ON_F1 = BehaviorKey.of("interaction/help-on-f1");

    /**
     * Focus-scoped status-bar text behavior.
     */
    public static final BehaviorKey STATUS_TEXT = BehaviorKey.of("interaction/status-text");

    /**
     * Accessible name/description decoration behavior.
     */
    public static final BehaviorKey ACCESSIBLE_TEXT = BehaviorKey.of("decoration/accessible-text");

    /**
     * Busy overlay decoration behavior for {@code JLayer}-wrapped views.
     */
    public static final BehaviorKey BUSY_OVERLAY = BehaviorKey.of("decoration/busy-overlay");

    private StandardBehaviorKeys() {
    }
}
