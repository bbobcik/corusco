package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.ResourceKey;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditorDescriptorTest {

    @Test
    void editorDescriptorsExposeLegacyKindDuringMigration() {
        assertThat(EditorDescriptor.text().legacyKind()).isEqualTo(FieldKind.TEXT);
        assertThat(EditorDescriptor.date().legacyKind()).isEqualTo(FieldKind.DATE);
        assertThat(EditorDescriptor.checkBox().legacyKind()).isEqualTo(FieldKind.CHECK_BOX);
        assertThat(EditorDescriptor.comboBox().legacyKind()).isEqualTo(FieldKind.COMBO_BOX);
        assertThat(EditorDescriptor.radioGroup().legacyKind()).isEqualTo(FieldKind.COMBO_BOX);
    }

    @Test
    void fieldDescriptorCarriesStructuredEditorMetadata() {
        FieldDescriptor<CustomerEdit, CustomerType> descriptor = new FieldDescriptor<>(
                "customer/type",
                "type",
                EditorDescriptor.radioGroup(),
                CustomerType.class,
                ResourceKey.of("customer/type/label", String.class),
                null,
                null,
                List.of()
        );

        assertThat(descriptor.editor().family()).isEqualTo(EditorFamily.RADIO_GROUP);
        assertThat(descriptor.editor().selectionShape()).isEqualTo(SelectionShape.SINGLE_VALUE);
        assertThat(descriptor.kind()).isEqualTo(FieldKind.COMBO_BOX);
    }

    @Test
    void optionResourcePrefixDerivesResourceKeysFromFieldKey() {
        FieldKey<CustomerEdit, CustomerType> key = FieldKey.of(
                "customer/security/authentication-mode",
                CustomerEdit.class,
                CustomerType.class
        );
        OptionResourcePrefix prefix = OptionResourcePrefix.of(key);
        OptionKey option = OptionKey.of("certificate");

        assertThat(prefix.label(option).id()).isEqualTo("customer.security.authentication-mode.certificate.label");
        assertThat(prefix.description(option).id())
                .isEqualTo("customer.security.authentication-mode.certificate.description");
        assertThat(prefix.help(option).id()).isEqualTo("customer.security.authentication-mode.certificate.help");
    }

    @Test
    void optionDescriptorRequiresValueKeyAndLabel() {
        OptionKey key = OptionKey.of("business");
        ResourceKey<String> label = ResourceKey.of("customer.type.business.label", String.class);

        OptionDescriptor<CustomerType> descriptor =
                OptionDescriptor.of(CustomerType.BUSINESS, key, label, null, null);

        assertThat(descriptor.value()).isEqualTo(CustomerType.BUSINESS);
        assertThat(descriptor.key()).isEqualTo(key);
        assertThat(descriptor.labelKey()).isEqualTo(label);
        assertThat(descriptor.helpKey()).isNull();
        assertThatThrownBy(() -> OptionKey.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    private enum CustomerType {
        RETAIL,
        BUSINESS
    }

    private record CustomerEdit(CustomerType type) {
    }
}
