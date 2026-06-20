package cz.auderis.corusco.examples.dialogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyRevertDialogExampleTest {

    @Test
    void applyRevertDialogExampleShowsSeparateDirtyBaselines() {
        assertThat(ApplyRevertDialogExample.runScenario()).containsExactly(
                "applyBefore=true",
                "revertBefore=true",
                "applyAfter=false",
                "revertAfter=true",
                "cancelAccepted=true",
                "cancelValue=applied",
                "reverted=true",
                "revertResult=true",
                "revertedName=original"
        );
    }
}
