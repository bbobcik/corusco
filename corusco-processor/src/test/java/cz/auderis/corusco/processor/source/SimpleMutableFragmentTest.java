package cz.auderis.corusco.processor.source;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleMutableFragmentTest {

    @Test
    void replacesPlainPlaceholderAtGraphemeBoundaries() {
        SimpleMutableFragment fragment = new SimpleMutableFragment();
        fragment.append("public final class GENERATED_TYPE { String owner = \"OWNER_TYPE\"; }");

        fragment.replaceLocal("GENERATED_TYPE", "%s", "CustomerEditOptions");
        fragment.replaceLocal("OWNER_TYPE", "%s", "CustomerEdit");

        assertThat(fragment.asString())
                .isEqualTo("public final class CustomerEditOptions { String owner = \"CustomerEdit\"; }");
    }

    @Test
    void replacementTextMayContainDollarAndBackslash() {
        SimpleMutableFragment fragment = new SimpleMutableFragment();
        fragment.append("String text = VALUE;");

        fragment.replaceLocal("VALUE", "%s", "\"C:\\\\temp\\\\$cache\"");

        assertThat(fragment.asString()).isEqualTo("String text = \"C:\\\\temp\\\\$cache\";");
    }

    @Test
    void replacesLongerPlaceholderBeforeSharedPrefix() {
        SimpleMutableFragment fragment = new SimpleMutableFragment();
        fragment.append("ResourceKey.of(OPTION_KEY_LITERAL); // OPTION_KEY");

        fragment.replaceLocal(Map.of(
                "OPTION_KEY", "retail",
                "OPTION_KEY_LITERAL", "\"retail\""
        ));

        assertThat(fragment.asString()).isEqualTo("ResourceKey.of(\"retail\"); // retail");
    }
}
