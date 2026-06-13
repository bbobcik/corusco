package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates generated-style typed key constants.
 */
public final class TypedKeyExample {

    private TypedKeyExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns diagnostic text for each baseline key family.
     *
     * @return key diagnostics
     */
    public static List<String> diagnostics() {
        return List.of(
                CustomerKeys.NAME.toString(),
                CustomerKeys.NAME.asFieldKey().toString(),
                CustomerKeys.CREDIT_LIMIT.toString(),
                CustomerKeys.NAME_LABEL.toString(),
                CustomerKeys.SAVE.toString(),
                CustomerKeys.NAME_HELP.toString(),
                CustomerKeys.NAME_COMPONENT.toString()
        );
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private static final class TextInput {
    }

    private static final class CustomerKeys {

        static final TextFieldKey<CustomerEdit, String> NAME =
                TextFieldKey.of("customer.name", CustomerEdit.class, String.class);
        static final FieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
                FieldKey.of("customer.creditLimit", CustomerEdit.class, BigDecimal.class);
        static final ResourceKey<String> NAME_LABEL =
                ResourceKey.of("customer.name.label", String.class);
        static final ActionKey SAVE =
                ActionKey.of("customer.save");
        static final HelpTopic NAME_HELP =
                HelpTopic.of("customer.name");
        static final ComponentKey<TextInput> NAME_COMPONENT =
                ComponentKey.of("customer.nameField", TextInput.class);

        private CustomerKeys() {
        }
    }
}
