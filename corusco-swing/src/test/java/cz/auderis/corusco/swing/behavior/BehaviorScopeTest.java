package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.help.DefaultHelpService;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.meta.FieldDescriptor;
import cz.auderis.corusco.core.meta.FieldKind;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BehaviorScopeTest {

    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final FieldKey<CustomerEdit, Boolean> ACTIVE =
            FieldKey.of("customer/active", CustomerEdit.class, Boolean.class);
    private static final ResourceKey<String> NAME_LABEL = ResourceKey.of("customer/name/label", String.class);
    private static final ResourceKey<String> NAME_TOOLTIP = ResourceKey.of("customer/name/tooltip", String.class);

    @Test
    void behaviorsInstallByPhaseAndCloseInReverseInstallationOrder() {
        SwingEdt.runAndWait(() -> {
            List<String> events = new ArrayList<>();
            BehaviorScope scope = new BehaviorScope();
            JTextField field = new JTextField();

            scope.install(field, List.of(
                    tracked("interaction", BehaviorPhase.INTERACTION, events),
                    tracked("binding", BehaviorPhase.BINDING, events),
                    tracked("decoration", BehaviorPhase.DECORATION, events)
            ));

            assertThat(scope.installedBehaviorKeys(field)).containsExactly(
                    BehaviorKey.of("test/binding"),
                    BehaviorKey.of("test/decoration"),
                    BehaviorKey.of("test/interaction")
            );
            assertThat(scope.hasBehavior(field, BehaviorKey.of("test/decoration"))).isTrue();

            scope.close();

            assertThat(events).containsExactly(
                    "install:binding",
                    "install:decoration",
                    "install:interaction",
                    "close:interaction",
                    "close:decoration",
                    "close:binding"
            );
            assertThat(scope.installedBehaviorKeys(field)).isEmpty();
            assertThat(scope.hasBehavior(field, BehaviorKey.of("test/decoration"))).isFalse();
        });
    }

    @Test
    void behaviorContextExposesScopeHelpService() {
        SwingEdt.runAndWait(() -> {
            DefaultHelpService helpService = new DefaultHelpService(request -> { });
            BehaviorScope scope = new BehaviorScope(helpService);
            List<Boolean> available = new ArrayList<>();

            scope.install(new JTextField(), List.of(new ViewBehavior<JTextField>() {
                @Override
                public BehaviorDescriptor descriptor() {
                    return BehaviorDescriptor.single(BehaviorKey.of("test/help-context"), BehaviorPhase.INTERACTION);
                }

                @Override
                public Binding install(BehaviorContext<JTextField> context) {
                    available.add(context.helpServiceOptional().orElseThrow() == helpService);
                    return () -> { };
                }
            }));

            assertThat(available).containsExactly(true);
            scope.close();
        });
    }

    @Test
    void duplicateSingleBehaviorFailsFast() {
        SwingEdt.runAndWait(() -> {
            BehaviorScope scope = new BehaviorScope();
            ViewBehavior<JTextField> first = tracked("same", BehaviorPhase.DECORATION, new ArrayList<>());
            ViewBehavior<JTextField> second = tracked("same", BehaviorPhase.DECORATION, new ArrayList<>());

            assertThatThrownBy(() -> scope.install(new JTextField(), List.of(first, second)))
                    .isInstanceOf(BehaviorConflictException.class)
                    .hasMessage("Behavior already installed: test/same");
        });
    }

    @Test
    void failedInstallDoesNotReserveSingleBehaviorKey() {
        SwingEdt.runAndWait(() -> {
            List<String> events = new ArrayList<>();
            BehaviorScope scope = new BehaviorScope();
            ViewBehavior<JTextField> failing = new ViewBehavior<>() {
                @Override
                public BehaviorDescriptor descriptor() {
                    return BehaviorDescriptor.single(BehaviorKey.of("test/flaky"), BehaviorPhase.DECORATION);
                }

                @Override
                public Binding install(BehaviorContext<JTextField> context) {
                    throw new IllegalStateException("boom");
                }
            };

            assertThatThrownBy(() -> scope.install(new JTextField(), List.of(failing)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");

            scope.install(new JTextField(), List.of(tracked("flaky", BehaviorPhase.DECORATION, events)));
            scope.close();

            assertThat(events).containsExactly("install:flaky", "close:flaky");
        });
    }

    @Test
    void multiplePrimaryBindingsFailFast() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> first =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            TextFieldModel<CustomerEdit, BigDecimal> second =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.ONE, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            BehaviorScope scope = new BehaviorScope();

            assertThatThrownBy(() -> scope.install(new JTextField(), List.of(
                    StandardBehaviors.textFieldBinding(first),
                    StandardBehaviors.textFieldBinding(second)
            )))
                    .isInstanceOf(BehaviorConflictException.class)
                    .hasMessage("Primary binding behavior already installed");
        });
    }

    @Test
    void primaryBindingsAreScopedPerComponent() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> first =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            TextFieldModel<CustomerEdit, BigDecimal> second =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.ONE, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            BehaviorScope scope = new BehaviorScope();
            JTextField firstField = new JTextField();
            JTextField secondField = new JTextField();

            scope.install(firstField, List.of(
                    StandardBehaviors.textFieldBinding(first),
                    StandardBehaviors.validationTooltip(first.problemSet())
            ));
            scope.install(secondField, List.of(
                    StandardBehaviors.textFieldBinding(second),
                    StandardBehaviors.validationTooltip(second.problemSet())
            ));

            firstField.setText("11");
            secondField.setText("22");

            assertThat(first.value()).isEqualByComparingTo("11");
            assertThat(second.value()).isEqualByComparingTo("22");
            scope.close();
        });
    }

    @Test
    void textBindingBehaviorMatchesDirectBindingBehavior() {
        SwingEdt.runAndWait(() -> {
            TextFieldModel<CustomerEdit, BigDecimal> model =
                    new TextFieldModel<>(CREDIT_LIMIT, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT));
            JTextField field = new JTextField();
            BehaviorScope scope = new BehaviorScope();

            scope.install(field, List.of(
                    StandardBehaviors.textFieldBinding(model),
                    StandardBehaviors.validationTooltip(model.problemSet()),
                    StandardBehaviors.validationBorder(model.problemSet())
            ));
            field.setText("20.00");
            assertThat(model.value()).isEqualByComparingTo("20.00");
            field.setText("bad");
            assertThat(model.problems().hasErrors()).isTrue();
            assertThat(field.getToolTipText()).isEqualTo("Expected BigDecimal");

            scope.close();
            field.setText("30.00");
            assertThat(model.rawText().value()).isEqualTo("bad");
        });
    }

    @Test
    void tooltipDecoratorsShareOneBehaviorSlot() {
        SwingEdt.runAndWait(() -> {
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.empty());
            BehaviorScope scope = new BehaviorScope();

            assertThatThrownBy(() -> scope.install(new JTextField(), List.of(
                    StandardBehaviors.validationTooltip(problems),
                    StandardBehaviors.composedTooltip(problems, null, "Static help", true)
            )))
                    .isInstanceOf(BehaviorConflictException.class)
                    .hasMessage("Behavior already installed: decoration/tooltip");
        });
    }

    @Test
    void checkboxBindingBehaviorUpdatesModelAndComponent() {
        SwingEdt.runAndWait(() -> {
            FieldModel<CustomerEdit, Boolean> model = new FieldModel<>(ACTIVE, false);
            JCheckBox checkBox = new JCheckBox();
            BehaviorScope scope = new BehaviorScope();

            scope.install(checkBox, List.of(StandardBehaviors.checkBoxBinding(model)));
            checkBox.setSelected(true);
            assertThat(model.value().value()).isTrue();
            model.setValue(false, StandardChangeOrigin.MODEL);
            assertThat(checkBox.isSelected()).isFalse();
            scope.close();
        });
    }

    @Test
    void focusAndEnterBehaviorsInstallAndDispose() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField("abc");
            AtomicInteger commits = new AtomicInteger();
            BehaviorScope scope = new BehaviorScope();
            int initialListenerCount = field.getKeyListeners().length + field.getFocusListeners().length;

            scope.install(field, List.of(
                    StandardBehaviors.selectAllOnFocus(),
                    StandardBehaviors.commitOnEnter(commits::incrementAndGet)
            ));
            field.getFocusListeners()[field.getFocusListeners().length - 1]
                    .focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
            assertThat(field.getSelectedText()).isEqualTo("abc");

            field.getKeyListeners()[field.getKeyListeners().length - 1]
                    .keyPressed(new KeyEvent(field, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_ENTER, '\n'));
            assertThat(commits).hasValue(1);

            scope.close();
            int listenerCount = field.getKeyListeners().length + field.getFocusListeners().length;
            assertThat(listenerCount).isEqualTo(initialListenerCount);
        });
    }

    @Test
    void statusTextBehaviorPublishesOnFocus() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            JLabel status = new JLabel("Ready");
            BehaviorScope scope = new BehaviorScope();

            scope.install(field, List.of(StandardBehaviors.statusText(status, "Edit customer name")));

            field.getFocusListeners()[field.getFocusListeners().length - 1]
                    .focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
            assertThat(status.getText()).isEqualTo("Edit customer name");

            scope.close();
            assertThat(status.getText()).isEqualTo("Ready");
        });
    }

    @Test
    void accessibleTextBehaviorUsesFieldDescriptorResources() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            BehaviorScope scope = new BehaviorScope();
            Resources resources = Resources.of(Map.of(
                    NAME_LABEL.id(), "Customer name",
                    NAME_TOOLTIP.id(), "Enter the customer display name"
            ));

            scope.install(field, List.of(StandardBehaviors.accessibleText(nameDescriptor(NAME_TOOLTIP), resources)));

            assertThat(field.getAccessibleContext().getAccessibleName()).isEqualTo("Customer name");
            assertThat(field.getAccessibleContext().getAccessibleDescription())
                    .isEqualTo("Enter the customer display name");

            scope.close();
            assertThat(field.getAccessibleContext().getAccessibleName()).isNull();
            assertThat(field.getAccessibleContext().getAccessibleDescription()).isNull();
        });
    }

    @Test
    void accessibleTextBehaviorAllowsMissingOptionalDescription() {
        SwingEdt.runAndWait(() -> {
            JTextField field = new JTextField();
            BehaviorScope scope = new BehaviorScope();
            Resources resources = Resources.of(Map.of(NAME_LABEL.id(), "Customer name"));

            scope.install(field, List.of(StandardBehaviors.accessibleText(nameDescriptor(NAME_TOOLTIP), resources)));

            assertThat(field.getAccessibleContext().getAccessibleName()).isEqualTo("Customer name");
            assertThat(field.getAccessibleContext().getAccessibleDescription()).isEmpty();
            scope.close();
        });
    }

    private static ViewBehavior<JTextField> tracked(String id, BehaviorPhase phase, List<String> events) {
        return new ViewBehavior<>() {
            @Override
            public BehaviorDescriptor descriptor() {
                return BehaviorDescriptor.single(BehaviorKey.of("test/" + id), phase);
            }

            @Override
            public Binding install(BehaviorContext<JTextField> context) {
                events.add("install:" + id);
                return () -> events.add("close:" + id);
            }
        };
    }

    private record CustomerEdit(BigDecimal creditLimit, boolean active) {
    }

    private static FieldDescriptor<CustomerEdit, String> nameDescriptor(ResourceKey<String> tooltipKey) {
        return new FieldDescriptor<>(
                "customer/name",
                "name",
                FieldKind.TEXT,
                String.class,
                NAME_LABEL,
                tooltipKey,
                null,
                List.of()
        );
    }
}
