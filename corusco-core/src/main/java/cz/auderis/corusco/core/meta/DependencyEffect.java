package cz.auderis.corusco.core.meta;

/**
 * Presentation state controlled by a generated field dependency.
 */
public enum DependencyEffect {

    /**
     * Dependency controls enabled state.
     */
    ENABLED,

    /**
     * Dependency controls visible state.
     */
    VISIBLE,

    /**
     * Dependency controls relevance state.
     */
    RELEVANT
}
