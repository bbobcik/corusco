package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.core.dialog.DialogResult;
import cz.auderis.corusco.core.form.AbstractCompositeFormModel;
import cz.auderis.corusco.core.form.ComponentStateModel;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.CancelConfirmation;
import cz.auderis.corusco.swing.dialog.DirtyState;
import cz.auderis.corusco.swing.dialog.DirtyStates;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogActionState;
import cz.auderis.corusco.swing.dialog.ProblemFocusResolver;
import cz.auderis.corusco.swing.dialog.RevertPolicy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;

/**
 * Demonstrates a handwritten parent dialog session over generated child forms.
 *
 * <p>The generated form and presentation classes are composed in core and
 * presenter code. Generated Swing view/binding companions remain optional and
 * are not required by this scenario, even though this package opts into Swing
 * companion generation.</p>
 */
public final class MultiFormDialogSessionExample {

    private MultiFormDialogSessionExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs the multi-form dialog scenario.
     *
     * @return compact scenario facts
     */
    public static List<String> runScenario() {
        ScenarioTrace[] trace = new ScenarioTrace[1];
        SwingEdt.runAndWait(() -> trace[0] = runOnEdt());
        return trace[0].facts();
    }

    private static ScenarioTrace runOnEdt() {
        AbstractCustomerProfile profileOriginal = new SourceProfile(
                "Acme",
                new BigDecimal("75000.00"),
                GeneratedCustomerType.BUSINESS,
                true
        );
        SecuritySettings securityOriginal = new SourceSecuritySettings(
                SecurityAuthenticationMode.PASSWORD,
                "secret",
                "",
                ""
        );
        DialogSession session = new DialogSession(
                new AbstractCustomerProfileFormModel(profileOriginal),
                new SecuritySettingsFormModel(securityOriginal)
        );
        DialogPresentation presentation = new DialogPresentation(session);
        presentation.security().advancedSection().enabled().setValue(false, ChangeOrigin.GENERATED);

        DirtyState currentBaselineDirty = DirtyStates.any(
                () -> session.profile().name.isDirty(),
                () -> session.profile().creditLimit.isDirty(),
                () -> session.profile().type.isDirty(),
                () -> session.profile().active.isDirty(),
                () -> session.security().authenticationMode.isDirty(),
                () -> session.security().password.isDirty(),
                () -> session.security().certificateAlias.isDirty(),
                () -> session.security().externalRealm.isDirty()
        );
        Snapshot originalSnapshot = snapshotFrom(session);
        DirtyState preDialogDirty = () -> !Objects.equals(originalSnapshot, snapshotFrom(session));
        RevertPolicy revertPolicy = new SnapshotRevertPolicy(session, originalSnapshot);
        FormDialog<DialogSession, DialogResultValue> dialog = new FormDialog<>(
                session,
                new JPanel(),
                currentBaselineDirty,
                CancelConfirmation.ALWAYS_CONFIRM,
                revertPolicy
        );
        FormDialogActionState actionState = new FormDialogActionState(dialog, currentBaselineDirty, preDialogDirty);
        presentation.setActions(actionState);

        session.profile().name.setRawText("Acme Portal", ChangeOrigin.USER);
        actionState.refresh();
        boolean applyBefore = presentation.apply().enabled().value();
        boolean revertBefore = presentation.revert().enabled().value();

        dialog.apply();
        actionState.refresh();
        boolean applyAfter = presentation.apply().enabled().value();
        boolean revertAfter = presentation.revert().enabled().value();

        session.profile().name.setRawText("Ignored Edit", ChangeOrigin.USER);
        dialog.cancel();
        DialogResult<DialogResultValue> cancelResult = dialog.result();

        DialogSession revertSession = new DialogSession(
                new AbstractCustomerProfileFormModel(profileOriginal),
                new SecuritySettingsFormModel(securityOriginal)
        );
        Snapshot revertSnapshot = snapshotFrom(revertSession);
        FormDialog<DialogSession, DialogResultValue> revertDialog = new FormDialog<>(
                revertSession,
                new JPanel(),
                DirtyState.CLEAN,
                CancelConfirmation.ALWAYS_CONFIRM,
                new SnapshotRevertPolicy(revertSession, revertSnapshot)
        );
        revertSession.profile().name.setRawText("Changed", ChangeOrigin.USER);
        boolean reverted = revertDialog.revert();

        FocusPanel profileNameField = new FocusPanel();
        FocusPanel securityPasswordField = new FocusPanel();
        Counter profileReveal = new Counter();
        Counter securityReveal = new Counter();
        ProblemFocusResolver focusResolver = ProblemFocusResolver.firstOf(
                ProblemFocusResolver.withPreparation(
                        profileReveal::increment,
                        ProblemFocusResolver.fieldTargets(Map.of(
                                AbstractCustomerProfileFields.NAME.asFieldKey(),
                                profileNameField
                        ))
                ),
                ProblemFocusResolver.withPreparation(
                        securityReveal::increment,
                        ProblemFocusResolver.fieldTargets(Map.of(
                                SecuritySettingsFields.PASSWORD.asFieldKey(),
                                securityPasswordField
                        ))
                )
        );
        Problem profileProblem = Problem.validation(
                ProblemCode.of("dialog/name-required"),
                ProblemSeverity.ERROR,
                ProblemTarget.field(AbstractCustomerProfileFields.NAME.asFieldKey()),
                "Name required"
        );
        boolean focused = focusResolver.resolve(profileProblem)
                .map(component -> {
                    component.requestFocusInWindow();
                    return true;
                })
                .orElse(false);

        return new ScenarioTrace(
                Integer.toString(session.children().size()),
                Boolean.toString(applyBefore),
                Boolean.toString(revertBefore),
                Boolean.toString(applyAfter),
                Boolean.toString(revertAfter),
                Boolean.toString(cancelResult.isAccepted()),
                cancelResult.acceptedValue().map(value -> value.profile().name()).orElse("none"),
                Boolean.toString(reverted),
                revertSession.profile().name.value(),
                Boolean.toString(presentation.security().advancedSection().enabled().value()),
                presentation.profile().form().getClass().getSimpleName(),
                Boolean.toString(focused),
                Integer.toString(profileReveal.value()),
                Integer.toString(securityReveal.value()),
                Integer.toString(profileNameField.focusRequests),
                Integer.toString(securityPasswordField.focusRequests)
        );
    }

    /**
     * Handwritten parent form model.
     */
    public static final class DialogSession extends AbstractCompositeFormModel<DialogResultValue> {

        private final AbstractCustomerProfileFormModel profile;
        private final SecuritySettingsFormModel security;

        DialogSession(AbstractCustomerProfileFormModel profile, SecuritySettingsFormModel security) {
            super(profile, security);
            this.profile = profile;
            this.security = security;
        }

        AbstractCustomerProfileFormModel profile() {
            return profile;
        }

        SecuritySettingsFormModel security() {
            return security;
        }

        @Override
        protected DialogResultValue createResult() {
            return new DialogResultValue(profile.toResult(), security.toResult());
        }

        @Override
        protected ProblemSet validationProblems() {
            if (!profile.isCommittable() || !security.isCommittable()) {
                return ProblemSet.empty();
            }
            if (!profile.active.value().value()
                    && security.authenticationMode.value().value() == SecurityAuthenticationMode.EXTERNAL) {
                return ProblemSet.of(Problem.validation(
                        ProblemCode.of("dialog/inactive-external-auth"),
                        ProblemSeverity.ERROR,
                        ProblemTarget.form(),
                        "Inactive customers cannot use external authentication"
                ));
            }
            return ProblemSet.empty();
        }
    }

    private static Snapshot snapshotFrom(DialogSession session) {
        return new Snapshot(
                session.profile().name.rawText().value(),
                session.profile().creditLimit.rawText().value(),
                session.profile().type.value().value(),
                session.profile().active.value().value(),
                session.security().authenticationMode.value().value(),
                session.security().password.rawText().value(),
                session.security().certificateAlias.rawText().value(),
                session.security().externalRealm.rawText().value()
        );
    }

    /**
     * Handwritten parent presentation model.
     */
    public static final class DialogPresentation {

        private final AbstractCustomerProfilePresentationModel profile;
        private final SecuritySettingsPresentationModel security;
        private FormDialogActionState actions;

        DialogPresentation(DialogSession session) {
            this.profile = new AbstractCustomerProfilePresentationModel(session.profile());
            this.security = new SecuritySettingsPresentationModel(session.security());
        }

        AbstractCustomerProfilePresentationModel profile() {
            return profile;
        }

        SecuritySettingsPresentationModel security() {
            return security;
        }

        ComponentStateModel apply() {
            return actions.applyAction();
        }

        ComponentStateModel revert() {
            return actions.revertAction();
        }

        void setActions(FormDialogActionState actions) {
            this.actions = Objects.requireNonNull(actions, "actions");
        }
    }

    /**
     * Combined committed dialog value.
     *
     * @param profile profile result
     * @param security security result
     */
    public record DialogResultValue(
            GeneratedAbstractCustomerProfile profile,
            GeneratedSecuritySettings security
    ) {
    }

    private record Snapshot(
            String nameText,
            String creditLimitText,
            GeneratedCustomerType type,
            boolean active,
            SecurityAuthenticationMode authenticationMode,
            String passwordText,
            String certificateAliasText,
            String externalRealmText
    ) {
    }

    private record ScenarioTrace(
            String childCount,
            String applyBefore,
            String revertBefore,
            String applyAfter,
            String revertAfter,
            String cancelAccepted,
            String appliedName,
            String reverted,
            String revertedName,
            String advancedEnabled,
            String profilePresenterForm,
            String focused,
            String profileRevealCount,
            String securityRevealCount,
            String profileFocusRequests,
            String securityFocusRequests
    ) {

        List<String> facts() {
            return List.of(
                    childCount,
                    applyBefore,
                    revertBefore,
                    applyAfter,
                    revertAfter,
                    cancelAccepted,
                    appliedName,
                    reverted,
                    revertedName,
                    advancedEnabled,
                    profilePresenterForm,
                    focused,
                    profileRevealCount,
                    securityRevealCount,
                    profileFocusRequests,
                    securityFocusRequests
            );
        }
    }

    private static final class Counter {

        private int value;

        private void increment() {
            value++;
        }

        private int value() {
            return value;
        }
    }

    private static final class FocusPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private int focusRequests;

        @Override
        public boolean requestFocusInWindow() {
            focusRequests++;
            return true;
        }
    }

    private static final class SnapshotRevertPolicy implements RevertPolicy {

        private final DialogSession session;
        private final Snapshot snapshot;

        private SnapshotRevertPolicy(DialogSession session, Snapshot snapshot) {
            this.session = session;
            this.snapshot = snapshot;
        }

        @Override
        public boolean canRevert() {
            return !Objects.equals(snapshot, snapshotFrom(session));
        }

        @Override
        public boolean revert() {
            session.profile().name.setRawText(snapshot.nameText(), ChangeOrigin.SYSTEM);
            session.profile().creditLimit.setRawText(snapshot.creditLimitText(), ChangeOrigin.SYSTEM);
            session.profile().type.setValue(snapshot.type(), ChangeOrigin.SYSTEM);
            session.profile().active.setValue(snapshot.active(), ChangeOrigin.SYSTEM);
            session.security().authenticationMode.setValue(snapshot.authenticationMode(), ChangeOrigin.SYSTEM);
            session.security().password.setRawText(snapshot.passwordText(), ChangeOrigin.SYSTEM);
            session.security().certificateAlias.setRawText(snapshot.certificateAliasText(), ChangeOrigin.SYSTEM);
            session.security().externalRealm.setRawText(snapshot.externalRealmText(), ChangeOrigin.SYSTEM);
            session.acceptCurrentValues();
            return true;
        }
    }

    private static final class SourceProfile extends AbstractCustomerProfile {

        private final String name;
        private final BigDecimal creditLimit;
        private final GeneratedCustomerType type;
        private final boolean active;

        private SourceProfile(String name, BigDecimal creditLimit, GeneratedCustomerType type, boolean active) {
            this.name = name;
            this.creditLimit = creditLimit;
            this.type = type;
            this.active = active;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public BigDecimal creditLimit() {
            return creditLimit;
        }

        @Override
        public GeneratedCustomerType type() {
            return type;
        }

        @Override
        public boolean active() {
            return active;
        }
    }

    private static final class SourceSecuritySettings extends SecuritySettings {

        private final SecurityAuthenticationMode authenticationMode;
        private final String password;
        private final String certificateAlias;
        private final String externalRealm;
        private final ComponentStateModel advancedSection = new ComponentStateModel();

        private SourceSecuritySettings(
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
