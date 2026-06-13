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
     * Validation tooltip decoration behavior.
     */
    public static final BehaviorKey VALIDATION_TOOLTIP = BehaviorKey.of("decoration/validation-tooltip");

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

    private StandardBehaviorKeys() {
    }
}
