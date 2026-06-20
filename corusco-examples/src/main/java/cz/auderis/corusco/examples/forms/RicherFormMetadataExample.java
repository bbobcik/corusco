package cz.auderis.corusco.examples.forms;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.form.ComponentStateModel;
import cz.auderis.corusco.examples.generated.SecurityAuthenticationMode;
import cz.auderis.corusco.examples.generated.SecuritySettings;
import cz.auderis.corusco.examples.generated.SecuritySettingsDependencies;
import cz.auderis.corusco.examples.generated.SecuritySettingsFormModel;
import cz.auderis.corusco.examples.generated.SecuritySettingsOptions;
import cz.auderis.corusco.examples.generated.SecuritySettingsPresentationModel;
import java.util.List;

/**
 * Demonstrates richer generated form metadata for business-form editors.
 */
public final class RicherFormMetadataExample {

    private RicherFormMetadataExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Exercises generated option descriptors, component-state models, and
     * dependency metadata.
     *
     * @return compact scenario facts
     */
    public static List<String> runScenario() {
        SecuritySettings original = new InitialSettings(
                SecurityAuthenticationMode.PASSWORD,
                "secret",
                null,
                null
        );
        SecuritySettingsFormModel model = new SecuritySettingsFormModel(original);
        SecuritySettingsPresentationModel presentation = new SecuritySettingsPresentationModel(model);

        // A presenter can still control state explicitly. The generated
        // dependency metadata gives it a typed inventory of the rules that
        // should drive these states automatically in a richer binding layer.
        presentation.passwordState().visible().setValue(true, ChangeOrigin.GENERATED);
        presentation.certificateAliasState().visible().setValue(false, ChangeOrigin.GENERATED);
        presentation.externalRealmState().visible().setValue(false, ChangeOrigin.GENERATED);
        presentation.advancedSection().enabled().setValue(false, ChangeOrigin.USER);

        return List.of(
                SecuritySettingsOptions.AUTHENTICATION_MODE_DESCRIPTORS.get(0).key().id(),
                Integer.toString(SecuritySettingsDependencies.all().size()),
                Boolean.toString(presentation.passwordState().visible().value()),
                Boolean.toString(presentation.certificateAliasState().visible().value()),
                Boolean.toString(presentation.advancedSection().enabled().value())
        );
    }

    private static final class InitialSettings extends SecuritySettings {

        private final SecurityAuthenticationMode authenticationMode;
        private final String password;
        private final String certificateAlias;
        private final String externalRealm;
        private final ComponentStateModel advancedSection = new ComponentStateModel();

        private InitialSettings(
                SecurityAuthenticationMode authenticationMode,
                String password,
                String certificateAlias,
                String externalRealm
        ) {
            this.authenticationMode = authenticationMode;
            this.password = password;
            this.certificateAlias = certificateAlias;
            this.externalRealm = externalRealm;
        }

        @Override
        public SecurityAuthenticationMode authenticationMode() {
            return authenticationMode;
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public String certificateAlias() {
            return certificateAlias;
        }

        @Override
        public String externalRealm() {
            return externalRealm;
        }

        @Override
        public ComponentStateModel advancedSection() {
            return advancedSection;
        }
    }
}
