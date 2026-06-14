package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypedKeyExampleTest {

    @Test
    void exampleShowsAllBaselineKeyFamilies() {
        assertThat(TypedKeyExample.diagnostics())
                .containsExactly(
                        "TextFieldKey[CustomerEdit#customer/name:String]",
                        "FieldKey[CustomerEdit#customer/name:String]",
                        "FieldKey[CustomerEdit#customer/credit-limit:BigDecimal]",
                        "ResourceKey[customer/name/label:String]",
                        "ActionKey[customer/save]",
                        "HelpTopic[customer/name]",
                        "ComponentKey[customer/name-field:TextInput]",
                        "Field[key=FieldKey[CustomerEdit#customer/name:String]]"
                );
    }
}
