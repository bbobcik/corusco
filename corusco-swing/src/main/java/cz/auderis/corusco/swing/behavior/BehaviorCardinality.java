package cz.auderis.corusco.swing.behavior;

/**
 * Cardinality rule for a {@link BehaviorKey} inside one {@link BehaviorScope}.
 *
 * <p>Behavior cardinality lets a scope detect accidental double installation
 * before two behaviors compete for the same component responsibility. Built-in
 * primary bindings and decorations generally use {@link #SINGLE}; command and
 * other independent interactions use {@link #MULTIPLE} when several instances
 * can coexist under related keys.</p>
 */
public enum BehaviorCardinality {

    /**
     * Only one behavior with the key may be installed.
     */
    SINGLE,

    /**
     * Multiple behaviors with the key may be installed.
     */
    MULTIPLE
}
