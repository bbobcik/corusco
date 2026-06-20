package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.annotations.form.RadioGroup;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.core.form.ComponentStateModel;

/**
 * Annotated security settings form with option metadata and component state.
 *
 * <p>This source represents a common business form: the selected
 * authentication mode controls which editor group is relevant, while tab and
 * section state remain part of the form model but not part of the committed
 * settings value.</p>
 */
@CoruscoForm(id = "security/settings")
public abstract class SecuritySettings {

    /**
     * Returns selected authentication mode.
     *
     * @return authentication mode
     */
    @RadioGroup
    public abstract SecurityAuthenticationMode authenticationMode();

    /**
     * Returns password text.
     *
     * @return password text
     */
    @TextField
    @CoruscoForm.ComponentState
    @CoruscoForm.DependsOn(
            field = "authenticationMode",
            values = "PASSWORD",
            effect = CoruscoForm.DependencyEffect.VISIBLE)
    public abstract String password();

    /**
     * Returns client certificate alias.
     *
     * @return certificate alias
     */
    @TextField
    @CoruscoForm.ComponentState
    @CoruscoForm.DependsOn(
            field = "authenticationMode",
            values = "CERTIFICATE",
            effect = CoruscoForm.DependencyEffect.VISIBLE)
    public abstract String certificateAlias();

    /**
     * Returns external realm identifier.
     *
     * @return external realm
     */
    @TextField
    @CoruscoForm.ComponentState
    @CoruscoForm.DependsOn(
            field = "authenticationMode",
            values = "EXTERNAL",
            effect = CoruscoForm.DependencyEffect.VISIBLE)
    public abstract String externalRealm();

    /**
     * State for the advanced section that groups rare controls.
     *
     * @return advanced section state
     */
    @CoruscoForm.ComponentState
    public abstract ComponentStateModel advancedSection();
}
