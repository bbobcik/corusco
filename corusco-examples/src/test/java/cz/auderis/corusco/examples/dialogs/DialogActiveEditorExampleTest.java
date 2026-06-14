package cz.auderis.corusco.examples.dialogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DialogActiveEditorExampleTest {

    @Test
    void dialogActiveEditorExampleCommitsTableEditorBeforeResult() {
        assertThat(DialogActiveEditorExample.runScenario()).containsExactly(
                "beforeOk=Alice",
                "accepted=Alicia",
                "editing=false"
        );
    }
}
