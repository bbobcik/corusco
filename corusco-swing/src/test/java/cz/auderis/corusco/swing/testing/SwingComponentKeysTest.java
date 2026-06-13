package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.key.ComponentKey;

import javax.swing.JLabel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SwingComponentKeysTest {

    private static final ComponentKey<JTextField> NAME_FIELD =
            ComponentKey.of("customer/name-field", JTextField.class);
    private static final ComponentKey<JLabel> NAME_LABEL =
            ComponentKey.of("customer/name-field", JLabel.class);

    @Test
    void markStoresTypedKeyAndDiagnosticName() {
        JTextField field = new JTextField();

        JTextField returned = SwingComponentKeys.mark(field, NAME_FIELD);

        assertThat(returned).isSameAs(field);
        assertThat(SwingComponentKeys.keyOf(field)).contains(NAME_FIELD);
        assertThat(field.getName()).isEqualTo("customer/name-field");
    }

    @Test
    void markKeepsExistingComponentName() {
        JTextField field = new JTextField();
        field.setName("custom-name");

        SwingComponentKeys.mark(field, NAME_FIELD);

        assertThat(field.getName()).isEqualTo("custom-name");
    }

    @Test
    void markRejectsMismatchedComponentType() {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        ComponentKey<JTextField> wrongTypeKey = (ComponentKey) NAME_LABEL;

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SwingComponentKeys.mark(new JTextField(), wrongTypeKey))
                .withMessageContaining("ComponentKey[customer/name-field:JLabel]");
    }

    @Test
    void keyOfRequiresComponent() {
        assertThatNullPointerException()
                .isThrownBy(() -> SwingComponentKeys.keyOf(null))
                .withMessageContaining("component");
    }
}
