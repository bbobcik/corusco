package cz.auderis.corusco.swing.dialog;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DirtyStatesTest {

    @Test
    void anyIsDirtyWhenAnyStateIsDirty() {
        MutableDirtyState first = new MutableDirtyState(false);
        MutableDirtyState second = new MutableDirtyState(false);
        DirtyState aggregate = DirtyStates.any(first, second);

        assertThat(aggregate.isDirty()).isFalse();

        second.dirty = true;

        assertThat(aggregate.isDirty()).isTrue();
    }

    @Test
    void emptyAnyIsClean() {
        assertThat(DirtyStates.any().isDirty()).isFalse();
    }

    @Test
    void anyRejectsNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> DirtyStates.any((DirtyState[]) null))
                .withMessageContaining("states");
        assertThatNullPointerException()
                .isThrownBy(() -> DirtyStates.any(Arrays.asList(DirtyState.CLEAN, null)))
                .withMessageContaining("state");
    }

    private static final class MutableDirtyState implements DirtyState {

        private boolean dirty;

        private MutableDirtyState(boolean dirty) {
            this.dirty = dirty;
        }

        @Override
        public boolean isDirty() {
            return dirty;
        }
    }
}
