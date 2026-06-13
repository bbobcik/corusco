package cz.auderis.corusco.swing.behavior;

/**
 * Cardinality rule for a behavior key within one {@link BehaviorScope}.
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
