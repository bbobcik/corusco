package cz.auderis.corusco.swing.behavior;

/**
 * Standard behavior keys used by built-in Swing behaviors.
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

    private StandardBehaviorKeys() {
    }
}
