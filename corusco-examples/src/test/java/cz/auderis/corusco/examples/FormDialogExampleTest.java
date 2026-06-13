package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormDialogExampleTest {

    @Test
    void formDialogExampleShowsApplyOkAndCancel() {
        assertThat(FormDialogExample.runScenario()).containsExactly(
                "applied=Alice",
                "closedAfterApply=false",
                "accepted=Alice",
                "closedAfterOk=true",
                "cancelledHasValue=false"
        );
    }
}
