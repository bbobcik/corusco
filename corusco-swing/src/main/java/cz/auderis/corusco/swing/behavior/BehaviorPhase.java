package cz.auderis.corusco.swing.behavior;

/**
 * Installation phase for behaviors.
 *
 * <p>Phases are installed in declaration order. This lets binding behaviors set
 * primary component state before decoration and interaction behaviors add
 * secondary UX affordances. Behavior descriptors use the phase to make a view
 * plan deterministic even when the plan is assembled from generated metadata
 * and handwritten behavior additions.</p>
 */
public enum BehaviorPhase {

    /**
     * Primary value/component bindings.
     */
    BINDING,

    /**
     * Visual or tooltip decoration.
     */
    DECORATION,

    /**
     * User interaction enhancements such as focus/key behaviors.
     */
    INTERACTION
}
