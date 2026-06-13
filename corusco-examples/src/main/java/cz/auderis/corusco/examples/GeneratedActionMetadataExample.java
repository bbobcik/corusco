package cz.auderis.corusco.examples;

import cz.auderis.corusco.annotations.UiAction;
import java.util.List;

/**
 * Demonstrates generated action metadata from annotated methods.
 */
public final class GeneratedActionMetadataExample {

    /**
     * Reads generated action descriptor constants.
     *
     * @return action metadata details
     */
    public static List<String> runScenario() {
        // Generated action metadata deliberately stops at descriptors here.
        // Later stages can bind these descriptors to command instances without
        // needing runtime annotation scanning.
        return List.of(
                GeneratedActionMetadataExampleActions.SAVE_KEY.id(),
                GeneratedActionMetadataExampleActions.SAVE_TEXT.id(),
                GeneratedActionMetadataExampleActions.SAVE.tooltipKey().id(),
                Integer.toString(GeneratedActionMetadataExampleActions.SAVE.mnemonicKeyCode()),
                Boolean.toString(GeneratedActionMetadataExampleActions.TOGGLE_ACTIVE.selectable())
        );
    }

    @UiAction(
            id = "generated-customer/save",
            tooltip = "generated-customer/save/tooltip",
            mnemonic = 83
    )
    void save() {
    }

    @UiAction(id = "generated-customer/toggle-active", selectable = true)
    void toggleActive() {
    }
}
