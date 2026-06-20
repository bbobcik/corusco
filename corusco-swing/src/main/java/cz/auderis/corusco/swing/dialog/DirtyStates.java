package cz.auderis.corusco.swing.dialog;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Explicit composition helpers for dialog dirty-state policies.
 */
public final class DirtyStates {

    private DirtyStates() {
    }

    /**
     * Creates a dirty state that is dirty when any supplied state is dirty.
     *
     * @param states dirty states to compose
     * @return aggregate dirty state
     */
    public static DirtyState any(DirtyState... states) {
        Objects.requireNonNull(states, "states");
        return any(Arrays.asList(states));
    }

    /**
     * Creates a dirty state that is dirty when any supplied state is dirty.
     *
     * @param states dirty states to compose
     * @return aggregate dirty state
     */
    public static DirtyState any(Collection<? extends DirtyState> states) {
        Objects.requireNonNull(states, "states");
        List<DirtyState> copy = states.stream()
                .map(state -> Objects.requireNonNull(state, "state"))
                .toList();
        if (copy.isEmpty()) {
            return DirtyState.CLEAN;
        }
        return () -> copy.stream().anyMatch(DirtyState::isDirty);
    }
}
