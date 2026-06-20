package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.annotations.form.CoruscoForm;

/**
 * Authentication modes used by the generated security-settings example.
 */
public enum SecurityAuthenticationMode {

    /**
     * Password-based authentication.
     */
    @CoruscoForm.Option(key = "password", order = 1)
    PASSWORD,

    /**
     * Client-certificate authentication.
     */
    @CoruscoForm.Option(key = "certificate", order = 2)
    CERTIFICATE,

    /**
     * Delegated external identity provider.
     */
    @CoruscoForm.Option(key = "external", order = 3)
    EXTERNAL
}
