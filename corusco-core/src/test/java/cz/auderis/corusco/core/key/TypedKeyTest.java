package cz.auderis.corusco.core.key;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypedKeyTest {

    @Test
    void fieldKeyEqualityIncludesIdOwnerAndValueType() {
        FieldKey<CustomerEdit, String> name = FieldKey.of("customer.name", CustomerEdit.class, String.class);
        FieldKey<CustomerEdit, String> same = FieldKey.of("customer.name", CustomerEdit.class, String.class);
        FieldKey<CustomerEdit, BigDecimal> differentValue =
                FieldKey.of("customer.name", CustomerEdit.class, BigDecimal.class);
        FieldKey<InvoiceEdit, String> differentOwner =
                FieldKey.of("customer.name", InvoiceEdit.class, String.class);

        assertThat(name).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(name).isNotEqualTo(differentValue);
        assertThat(name).isNotEqualTo(differentOwner);
        assertThat(name.toString()).isEqualTo("FieldKey[CustomerEdit#customer.name:String]");
    }

    @Test
    void textFieldKeyPreservesFieldKeyTypeRelationship() {
        TextFieldKey<CustomerEdit, String> text = TextFieldKey.of("customer.name", CustomerEdit.class, String.class);

        FieldKey<CustomerEdit, String> field = text.asFieldKey();

        assertThat(field.id()).isEqualTo(text.id());
        assertThat(field.ownerType()).isEqualTo(text.ownerType());
        assertThat(field.valueType()).isEqualTo(text.valueType());
        assertThat(text.toString()).isEqualTo("TextFieldKey[CustomerEdit#customer.name:String]");
    }

    @Test
    void resourceKeyEqualityIncludesValueType() {
        ResourceKey<String> label = ResourceKey.of("customer.name.label", String.class);
        ResourceKey<String> same = ResourceKey.of("customer.name.label", String.class);
        ResourceKey<Integer> differentType = ResourceKey.of("customer.name.label", Integer.class);

        assertThat(label).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(label).isNotEqualTo(differentType);
        assertThat(label.toString()).isEqualTo("ResourceKey[customer.name.label:String]");
    }

    @Test
    void actionHelpAndComponentKeysExposeStableDiagnostics() {
        ActionKey save = ActionKey.of("customer.save");
        HelpTopic help = HelpTopic.of("customer.name");
        ComponentKey<ViewTextField> component = ComponentKey.of("customer.nameField", ViewTextField.class);

        assertThat(save.toString()).isEqualTo("ActionKey[customer.save]");
        assertThat(help.toString()).isEqualTo("HelpTopic[customer.name]");
        assertThat(component.toString()).isEqualTo("ComponentKey[customer.nameField:ViewTextField]");
        assertThat(component.componentType()).isEqualTo(ViewTextField.class);
    }

    @Test
    void blankIdsAreRejected() {
        assertThatThrownBy(() -> FieldKey.of(" ", CustomerEdit.class, String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
        assertThatThrownBy(() -> ActionKey.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
    }

    @Test
    void generatedStyleConstantsRemainStronglyTyped() {
        FieldKey<CustomerEdit, BigDecimal> creditLimit = CustomerKeys.CREDIT_LIMIT;
        TextFieldKey<CustomerEdit, String> name = CustomerKeys.NAME;
        ResourceKey<String> nameLabel = CustomerKeys.NAME_LABEL;

        assertThat(creditLimit.valueType()).isEqualTo(BigDecimal.class);
        assertThat(name.asFieldKey().ownerType()).isEqualTo(CustomerEdit.class);
        assertThat(nameLabel.valueType()).isEqualTo(String.class);
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private record InvoiceEdit(String number) {
    }

    private static final class ViewTextField {
    }

    private static final class CustomerKeys {

        static final TextFieldKey<CustomerEdit, String> NAME =
                TextFieldKey.of("customer.name", CustomerEdit.class, String.class);
        static final FieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
                FieldKey.of("customer.creditLimit", CustomerEdit.class, BigDecimal.class);
        static final ResourceKey<String> NAME_LABEL =
                ResourceKey.of("customer.name.label", String.class);

        private CustomerKeys() {
        }
    }
}
