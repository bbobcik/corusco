package cz.auderis.corusco.core.dialog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DialogResultTest {

    @Test
    void acceptedResultCarriesCommittedValue() {
        DialogResult<String> result = DialogResult.accepted("saved");

        assertThat(result.isAccepted()).isTrue();
        assertThat(result.acceptedValue()).contains("saved");
    }

    @Test
    void cancelledResultCarriesNoValue() {
        DialogResult<String> result = DialogResult.cancelled();

        assertThat(result.isAccepted()).isFalse();
        assertThat(result.isReverted()).isFalse();
        assertThat(result.acceptedValue()).isEmpty();
    }

    @Test
    void revertedResultCarriesNoValue() {
        DialogResult<String> result = DialogResult.reverted();

        assertThat(result.isAccepted()).isFalse();
        assertThat(result.isReverted()).isTrue();
        assertThat(result.acceptedValue()).isEmpty();
    }

    @Test
    void acceptedResultRequiresNonNullValue() {
        assertThatNullPointerException()
                .isThrownBy(() -> DialogResult.accepted(null))
                .withMessageContaining("value");
    }
}
