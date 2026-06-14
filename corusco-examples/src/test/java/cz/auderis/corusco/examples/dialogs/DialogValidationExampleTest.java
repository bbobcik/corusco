package cz.auderis.corusco.examples.dialogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DialogValidationExampleTest {

    @Test
    void dialogValidationExampleShowsSummaryAndFocus() {
        assertThat(DialogValidationExample.runScenario()).containsExactly(
                "summary=Name required",
                "focused=true",
                "focusRequests=1",
                "restored=ready"
        );
    }
}
