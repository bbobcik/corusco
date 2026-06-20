package cz.auderis.corusco.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated form, field, resource, problem, descriptor, view, behavior,
 * and action sources.
 *
 * <p>This writer is the central generated-code contract for {@code @SwingForm}
 * and {@code @UiAction}. It receives validated {@link FieldSpec} and
 * {@link ActionSpec} values and writes deterministic source files whose public
 * APIs are consumed by core form models, Swing bindings, examples, and tests.
 * It reports write failures through the annotation-processing messager instead
 * of exposing processor internals as runtime API.</p>
 *
 * <p>The class is package-private because users should depend on generated
 * sources and public annotations, not on writer implementation details.</p>
 */
final class GeneratedSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    GeneratedSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeFormSources(TypeElement formType, FormSpec form) {
        if (form.hasGeneratedResultImplementation()) {
            writeResultImplementationClass(formType, form);
        }
        writeFieldsClass(formType, form);
        writeResourcesClass(formType, form);
        writeProblemsClass(formType, form);
        writeDescriptorsClass(formType, form);
        writeFormModelClass(formType, form);
        writeViewClass(formType, form);
        writeBehaviorPlanClass(formType, form);
        writeBindingsClass(formType, form);
        writeOptionsClass(formType, form);
    }

    void writeActionsClass(TypeElement ownerType, List<ActionSpec> actions) {
        String packageName = elements.getPackageOf(ownerType).getQualifiedName().toString();
        String ownerName = ownerType.getSimpleName().toString();
        String generatedType = ownerName + "Actions";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.command.AcceleratorDescriptor;
                import cz.auderis.corusco.core.command.ActionDescriptor;
                import cz.auderis.corusco.core.command.CommandFactory;
                import cz.auderis.corusco.core.command.CommandSet;
                import cz.auderis.corusco.core.command.MutableCommand;
                import cz.auderis.corusco.core.key.ActionKey;
                import cz.auderis.corusco.core.key.ResourceKey;
                import java.util.List;

                /**
                 * Generated action metadata and command factories for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerName, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (ActionSpec action : actions) {
            source.append(actionSource(action));
        }
        source.append(actionDescriptorListSource(actions));
        source.append(commandFactorySource(ownerName, actions));
        source.append("}\n");
        writeSource(ownerType, qualifiedName, source.toString(), "Could not write generated action descriptors");
    }

    private void writeResultImplementationClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String generatedType = form.resultImplementationType;
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import java.util.Objects;

                /**
                 * Generated immutable result implementation for {@link %s}.
                 */
                public final class %s extends %s {

                """.formatted(form.sourceType, generatedType, form.sourceType));
        for (FieldSpec field : form.fields) {
            source.append("""
                    private final %s %s;

                    """.formatted(field.accessorType, field.componentName));
        }
        source.append(resultConstructorSource(generatedType, form.fields));
        for (FieldSpec field : form.fields) {
            source.append(resultAccessorSource(field));
        }
        source.append(resultEqualsSource(form));
        source.append(resultHashCodeSource(form.fields));
        source.append(resultToStringSource(generatedType, form.fields));
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated form result implementation");
    }

    private void writeFieldsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Fields";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.FieldKey;
                import cz.auderis.corusco.core.key.TextFieldKey;

                /**
                 * Generated field keys for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.append(fieldSource(field));
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated field keys");
    }

    private void writeResourcesClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Resources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.ResourceKey;

                /**
                 * Generated resource keys for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.append(resourceKeySource(field.labelConstant, field.keyId + "/label"));
            if (field.tooltipConstant != null) {
                source.append(resourceKeySource(field.tooltipConstant, field.tooltipId));
            }
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated resource keys");
    }

    private void writeProblemsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.problem.ProblemCode;

                /**
                 * Generated problem codes for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            for (ConstraintSpec constraint : field.constraints) {
                source.append(problemCodeSource(constraint));
            }
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated problem codes");
    }

    private void writeDescriptorsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Descriptors";
        String resourcesType = ownerType + "Resources";
        String problemsType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.HelpTopic;
                import cz.auderis.corusco.core.meta.ConstraintDescriptor;
                import cz.auderis.corusco.core.meta.FieldDescriptor;
                import cz.auderis.corusco.core.meta.FieldKind;
                import java.util.List;

                /**
                 * Generated field descriptors for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.append(descriptorSource(field, resourcesType, problemsType));
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated field descriptors");
    }

    private void writeFormModelClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "FormModel";
        String fieldsType = ownerType + "Fields";
        String descriptorsType = ownerType + "Descriptors";
        String resultConstructorType = form.hasGeneratedResultImplementation()
                ? form.resultImplementationType
                : ownerType;
        String originalDescription = form.hasGeneratedResultImplementation()
                ? "original immutable form result"
                : "original immutable record";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.convert.Converters;
                import cz.auderis.corusco.core.convert.EmptyTextPolicy;
                import cz.auderis.corusco.core.form.AbstractFormModel;
                import cz.auderis.corusco.core.form.FieldModel;
                import cz.auderis.corusco.core.form.TextFieldModel;
                import cz.auderis.corusco.core.meta.FieldDescriptor;
                import cz.auderis.corusco.core.problem.ProblemSet;
                import cz.auderis.corusco.core.validation.RuleSet;
                import cz.auderis.corusco.core.validation.Validators;
                import java.math.BigDecimal;
                import java.util.List;
                import java.util.regex.Pattern;

                /**
                 * Generated form model for {@link %s}.
                 */
                public final class %s extends AbstractFormModel<%s> {

                """.formatted(ownerType, generatedType, ownerType));
        for (FieldSpec field : form.fields) {
            source.append(fieldModelDeclarationSource(field));
        }
        source.append("""
                    private final RuleSet<%s> rules;

                    /**
                     * Creates a generated form model.
                     *
                     * @param original %s
                     */
                    public %s(%s original) {
                        java.util.Objects.requireNonNull(original, "original");
                """.formatted(generatedType, originalDescription, generatedType, ownerType));
        for (FieldSpec field : form.fields) {
            source.append(fieldModelInitializationSource(field, fieldsType));
        }
        source.append("""
                        this.rules = buildRules();
                    }

                """);
        source.append(descriptorListSource(form.fields, descriptorsType));
        source.append(buildRulesSource(form.fields, generatedType, fieldsType));
        source.append("""
                    @Override
                    protected ProblemSet validationProblems() {
                        return rules.validateAll(this);
                    }

                    @Override
                    protected %s createResult() {
                        return new %s(
                """.formatted(ownerType, resultConstructorType));
        for (int i = 0; i < form.fields.size(); i++) {
            FieldSpec field = form.fields.get(i);
            String suffix = i == form.fields.size() - 1 ? "\n" : ",\n";
            source.append("                ").append(resultValueExpression(field)).append(suffix);
        }
        source.append("""
                        );
                    }
                }
                """);
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated form model");
    }

    private void writeViewClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "View";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import javax.swing.JCheckBox;
                import javax.swing.JComboBox;
                import javax.swing.JTextField;

                /**
                 * Generated Swing view contract for {@link %s}.
                 */
                public interface %s {

                """.formatted(ownerType, generatedType));
        for (FieldSpec field : form.fields) {
            source.append(viewMethodSource(field));
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated view contract");
    }

    private void writeBehaviorPlanClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "BehaviorPlan";
        String viewType = ownerType + "View";
        String formModelType = ownerType + "FormModel";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.swing.behavior.BehaviorScope;
                import cz.auderis.corusco.swing.behavior.StandardBehaviors;
                import java.util.List;

                /**
                 * Generated behavior installation plan for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        source.append("""
                    /**
                     * Installs supported generated behaviors.
                     *
                     * @param view generated view contract
                     * @param model generated form model
                     * @param scope owning behavior scope
                     */
                    public static void install(%s view, %s model, BehaviorScope scope) {
                        java.util.Objects.requireNonNull(view, "view");
                        java.util.Objects.requireNonNull(model, "model");
                        java.util.Objects.requireNonNull(scope, "scope");
                """.formatted(viewType, formModelType));
        for (FieldSpec field : form.fields) {
            source.append(behaviorInstallSource(field));
        }
        source.append("""
                    }
                }
                """);
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated behavior plan");
    }

    private void writeBindingsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Bindings";
        String viewType = ownerType + "View";
        String formModelType = ownerType + "FormModel";
        String behaviorPlanType = ownerType + "BehaviorPlan";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.swing.behavior.BehaviorScope;

                /**
                 * Generated binding facade for {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        source.append("""
                    /**
                     * Installs generated bindings for the form view and model.
                     *
                     * @param view generated view contract
                     * @param model generated form model
                     * @param scope owning behavior scope
                     */
                    public static void install(%s view, %s model, BehaviorScope scope) {
                        %s.install(view, model, scope);
                    }
                }
                """.formatted(viewType, formModelType, behaviorPlanType));
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated binding facade");
    }

    private void writeOptionsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Options";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import java.util.List;

                /**
                 * Generated option metadata for enum combo-box fields in {@link %s}.
                 */
                public final class %s {

                """.formatted(ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            if (!field.enumOptionConstants.isEmpty()) {
                source.append(enumOptionSource(field));
            }
        }
        source.append("}\n");
        writeSource(formType, qualifiedName, source.toString(), "Could not write generated option metadata");
    }

    private String fieldSource(FieldSpec field) {
        String keyType = field.textField ? "TextFieldKey" : "FieldKey";
        String factory = field.textField ? "TextFieldKey" : "FieldKey";
        return """
                    /**
                     * Field key for {@code %s}.
                     */
                    public static final %s<%s, %s> %s =
                            %s.of(%s, %s.class, %s);

                """.formatted(
                field.keyId,
                keyType,
                field.ownerType,
                field.valueType,
                field.constantName,
                factory,
                stringLiteralOrNull(field.keyId),
                field.ownerType,
                field.valueClass
        );
    }

    private String resultConstructorSource(String generatedType, List<FieldSpec> fields) {
        StringBuilder source = new StringBuilder("""
                    /**
                     * Creates an immutable generated form result.
                     */
                    public %s(
                """.formatted(generatedType));
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            String suffix = i == fields.size() - 1 ? "\n" : ",\n";
            source.append("            ").append(field.accessorType).append(' ').append(field.componentName).append(suffix);
        }
        source.append("""
                    ) {
                """);
        for (FieldSpec field : fields) {
            source.append("        this.").append(field.componentName).append(" = ")
                    .append(field.componentName).append(";\n");
        }
        source.append("""
                    }

                """);
        return source.toString();
    }

    private String resultAccessorSource(FieldSpec field) {
        return """
                    @Override
                    public %s %s() {
                        return %s;
                    }

                """.formatted(field.accessorType, field.componentName, field.componentName);
    }

    private String resultEqualsSource(FormSpec form) {
        StringBuilder source = new StringBuilder("""
                    @Override
                    public boolean equals(Object obj) {
                        if (this == obj) {
                            return true;
                        }
                        if (!(obj instanceof %s other)) {
                            return false;
                        }
                        return%s""".formatted(form.sourceType, " "));
        for (int i = 0; i < form.fields.size(); i++) {
            FieldSpec field = form.fields.get(i);
            String prefix = i == 0 ? "" : "\n                && ";
            source.append(prefix).append("Objects.equals(")
                    .append(field.componentName).append("(), other.")
                    .append(field.componentName).append("())");
        }
        source.append("""
                ;
                    }

                """);
        return source.toString();
    }

    private String resultHashCodeSource(List<FieldSpec> fields) {
        List<String> values = new ArrayList<>();
        for (FieldSpec field : fields) {
            values.add(field.componentName + "()");
        }
        return """
                    @Override
                    public int hashCode() {
                        return Objects.hash(%s);
                    }

                """.formatted(String.join(", ", values));
    }

    private String resultToStringSource(String generatedType, List<FieldSpec> fields) {
        StringBuilder source = new StringBuilder("""
                    @Override
                    public String toString() {
                        return "%s["
                """.formatted(generatedType));
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            String prefix = i == 0 ? " + \"" : " + \", ";
            source.append("                ").append(prefix)
                    .append(field.componentName).append("=\" + ")
                    .append(field.componentName).append("()\n");
        }
        source.append("""
                                + "]";
                    }

                """);
        return source.toString();
    }

    private String actionSource(ActionSpec action) {
        StringBuilder source = new StringBuilder("""
                    /**
                     * Action key for {@code %s}.
                     */
                    public static final ActionKey %s_KEY =
                            ActionKey.of(%s);

                    /**
                     * Text resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s_TEXT =
                            ResourceKey.of(%s, String.class);

                """.formatted(
                action.id,
                action.constantName,
                stringLiteralOrNull(action.id),
                action.textId,
                action.constantName,
                stringLiteralOrNull(action.textId)
        ));
        if (action.tooltipId != null) {
            source.append("""
                    /**
                     * Tooltip resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s_TOOLTIP =
                            ResourceKey.of(%s, String.class);

                """.formatted(action.tooltipId, action.constantName, stringLiteralOrNull(action.tooltipId)));
        }
        source.append("""
                    /**
                     * Action descriptor for {@code %s}.
                     */
                    public static final ActionDescriptor %s =
                            %s;

                """.formatted(action.id, action.constantName, actionDescriptorExpression(action)));
        return source.toString();
    }

    private String actionDescriptorListSource(List<ActionSpec> actions) {
        StringBuilder source = new StringBuilder("""
                    /**
                     * Returns generated action descriptors in declaration order.
                     *
                     * @return descriptor list
                     */
                    public static List<ActionDescriptor> descriptors() {
                        return List.of(
                """);
        for (int i = 0; i < actions.size(); i++) {
            String suffix = i == actions.size() - 1 ? "\n" : ",\n";
            source.append("                ").append(actions.get(i).constantName).append(suffix);
        }
        source.append("""
                        );
                    }

                    /**
                     * Returns menu action descriptors in declaration order.
                     *
                     * @return menu descriptor list
                     */
                    public static List<ActionDescriptor> menuDescriptors() {
                        return descriptors();
                    }

                    /**
                     * Returns toolbar action descriptors in declaration order.
                     *
                     * @return toolbar descriptor list
                     */
                    public static List<ActionDescriptor> toolbarDescriptors() {
                        return descriptors();
                    }

                """);
        return source.toString();
    }

    private String commandFactorySource(String ownerName, List<ActionSpec> actions) {
        StringBuilder source = new StringBuilder();
        for (ActionSpec action : actions) {
            source.append(singleCommandFactorySource(ownerName, action));
        }
        source.append("""
                    /**
                     * Creates commands bound to an owner instance.
                     *
                     * @param owner action owner
                     * @return command set in declaration order
                     */
                    public static CommandSet commands(%s owner) {
                        java.util.Objects.requireNonNull(owner, "owner");
                        return CommandSet.of(
                """.formatted(ownerName));
        for (int i = 0; i < actions.size(); i++) {
            String suffix = i == actions.size() - 1 ? "\n" : ",\n";
            source.append("                ").append(factoryMethodName(actions.get(i))).append("(owner)").append(suffix);
        }
        source.append("""
                        );
                    }
                """);
        return source.toString();
    }

    private String singleCommandFactorySource(String ownerName, ActionSpec action) {
        String factory = action.selectable ? "toggle(" + action.constantName + ", false, command -> owner."
                : "command(" + action.constantName + ", command -> owner.";
        return """
                    /**
                     * Creates a command bound to {@link %s#%s()}.
                     *
                     * @param owner action owner
                     * @return mutable command
                     */
                    public static MutableCommand %s(%s owner) {
                        java.util.Objects.requireNonNull(owner, "owner");
                        return CommandFactory.%s%s());
                    }

                """.formatted(
                ownerName,
                action.methodName,
                factoryMethodName(action),
                ownerName,
                factory,
                action.methodName
        );
    }

    private String enumOptionSource(FieldSpec field) {
        List<String> options = new ArrayList<>();
        for (String enumConstant : field.enumOptionConstants) {
            options.add(field.valueType + "." + enumConstant);
        }
        return """
                    /**
                     * Enum options for {@code %s} in declaration order.
                     */
                    public static final List<%s> %s =
                            List.of(%s);

                """.formatted(field.keyId, field.valueType, field.constantName, String.join(", ", options));
    }

    private String resourceKeySource(String constantName, String id) {
        return """
                    /**
                     * Resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s =
                            ResourceKey.of(%s, String.class);

                """.formatted(id, constantName, stringLiteralOrNull(id));
    }

    private String problemCodeSource(ConstraintSpec constraint) {
        return """
                    /**
                     * Problem code for {@code %s}.
                     */
                    public static final ProblemCode %s =
                            ProblemCode.of(%s);

                """.formatted(
                constraint.problemId,
                constraint.problemConstant,
                stringLiteralOrNull(constraint.problemId)
        );
    }

    private String descriptorSource(FieldSpec field, String resourcesType, String problemsType) {
        String tooltip = field.tooltipConstant == null ? "null" : resourcesType + "." + field.tooltipConstant;
        return """
                    /**
                     * Descriptor for {@code %s}.
                     */
                    public static final FieldDescriptor<%s, %s> %s =
                            new FieldDescriptor<>(
                                    %s,
                                    %s,
                                    FieldKind.%s,
                                    %s,
                                    %s.%s,
                                    %s,
                                    %s,
                                    %s
                            );

                """.formatted(
                field.keyId,
                field.ownerType,
                field.valueType,
                field.constantName,
                stringLiteralOrNull(field.keyId),
                stringLiteralOrNull(field.componentName),
                field.kind,
                field.valueClass,
                resourcesType,
                field.labelConstant,
                tooltip,
                helpTopicExpression(field),
                constraintsExpression(field, problemsType)
        );
    }

    private String fieldModelDeclarationSource(FieldSpec field) {
        return """
                    /**
                     * Field model for {@code %s}.
                     */
                    public final %s<%s, %s> %s;

                """.formatted(
                field.keyId,
                field.modelType,
                field.ownerType,
                field.valueType,
                field.componentName
        );
    }

    private String fieldModelInitializationSource(FieldSpec field, String fieldsType) {
        if ("TextFieldModel".equals(field.modelType)) {
            return """
                        this.%s = register(new TextFieldModel<>(
                                %s.%s,
                                original.%s(),
                                %s
                        ));
                    """.formatted(
                    field.componentName,
                    fieldsType,
                    field.constantName,
                    field.componentName,
                    field.converterExpression
            ).indent(4);
        }
        return """
                    this.%s = register(new FieldModel<>(
                            %s.%s,
                            original.%s()
                    ));
                """.formatted(field.componentName, fieldsType, field.constantName, field.componentName)
                .indent(4);
    }

    private String descriptorListSource(List<FieldSpec> fields, String descriptorsType) {
        StringBuilder source = new StringBuilder("""
                    /**
                     * Returns generated field descriptors in source declaration order.
                     *
                     * @return descriptor list
                     */
                    public List<FieldDescriptor<?, ?>> descriptors() {
                        return List.of(
                """);
        for (int i = 0; i < fields.size(); i++) {
            String suffix = i == fields.size() - 1 ? "\n" : ",\n";
            source.append("                ").append(descriptorsType).append(".")
                    .append(fields.get(i).constantName).append(suffix);
        }
        source.append("""
                        );
                    }

                """);
        return source.toString();
    }

    private String buildRulesSource(List<FieldSpec> fields, String generatedType, String fieldsType) {
        StringBuilder source = new StringBuilder("""
                    private static RuleSet<%s> buildRules() {
                        RuleSet.Builder<%s> rules = RuleSet.builder();
                """.formatted(generatedType, generatedType));
        for (FieldSpec field : fields) {
            for (ConstraintSpec constraint : field.constraints) {
                source.append(ruleSource(field, constraint, fieldsType));
            }
        }
        source.append("""
                        return rules.build();
                    }

                """);
        return source.toString();
    }

    private String ruleSource(FieldSpec field, ConstraintSpec constraint, String fieldsType) {
        String keyExpression = "TextFieldModel".equals(field.modelType)
                ? fieldsType + "." + field.constantName + ".asFieldKey()"
                : fieldsType + "." + field.constantName;
        String accessor = "model -> model." + field.componentName;
        String validator = validatorExpression(constraint);
        if ("TextFieldModel".equals(field.modelType)) {
            return "        rules.field(" + keyExpression + ", " + accessor + ", " + validator + ");\n";
        }
        return "        rules.semanticField(" + keyExpression + ", " + accessor + ", " + validator + ");\n";
    }

    private String behaviorInstallSource(FieldSpec field) {
        if ("TextFieldModel".equals(field.modelType)) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.textFieldBinding(model.%s),
                                StandardBehaviors.validationTooltip(model.%s.problemSet()),
                                StandardBehaviors.validationBorder(model.%s.problemSet()),
                                StandardBehaviors.selectAllOnFocus()
                        ));
                    """.formatted(
                    field.viewMethodName,
                    field.componentName,
                    field.componentName,
                    field.componentName
            );
        } else if ("CHECK_BOX".equals(field.kind)) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.checkBoxBinding(model.%s)
                        ));
                    """.formatted(field.viewMethodName, field.componentName);
        }
        return "";
    }

    private String viewMethodSource(FieldSpec field) {
        return """
                    /**
                     * Returns the Swing component for {@code %s}.
                     *
                     * @return component
                     */
                    %s %s();

                """.formatted(field.keyId, field.viewComponentType, field.viewMethodName);
    }

    private String actionDescriptorExpression(ActionSpec action) {
        String expression = action.selectable
                ? "ActionDescriptor.toggle(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)"
                : "ActionDescriptor.action(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)";
        if (action.tooltipId != null) {
            expression += ".withTooltip(" + action.constantName + "_TOOLTIP)";
        }
        if (action.mnemonic != 0) {
            expression += ".withMnemonic(" + action.mnemonic + ")";
        }
        if (action.acceleratorKey != 0) {
            expression += ".withAccelerator(AcceleratorDescriptor.of("
                    + action.acceleratorKey + ", " + action.acceleratorModifiers + "))";
        }
        return expression;
    }

    private String validatorExpression(ConstraintSpec constraint) {
        String message = stringLiteralOrNull(constraint.problemId);
        return switch (constraint.kind) {
            case "REQUIRED" -> "Validators.required(" + message + ")";
            case "LENGTH" -> "Validators.length(" + constraint.min + ", " + constraint.max + ", " + message + ")";
            case "DECIMAL_RANGE" -> "Validators.decimalRange("
                    + bigDecimalOrNull(constraint.min) + ", " + bigDecimalOrNull(constraint.max) + ", " + message + ")";
            case "INT_RANGE" -> "Validators.integerRange("
                    + constraint.min + ", " + constraint.max + ", " + message + ")";
            case "REGEX" -> "Validators.regex(Pattern.compile("
                    + stringLiteralOrNull(constraint.min) + "), " + message + ")";
            default -> throw new IllegalStateException("Unknown constraint kind: " + constraint.kind);
        };
    }

    private String resultValueExpression(FieldSpec field) {
        if ("TextFieldModel".equals(field.modelType)) {
            return field.componentName + ".value()";
        }
        return field.componentName + ".value().value()";
    }

    private String helpTopicExpression(FieldSpec field) {
        return field.helpTopicId == null ? "null" : "HelpTopic.of(" + stringLiteralOrNull(field.helpTopicId) + ")";
    }

    private String constraintsExpression(FieldSpec field, String problemsType) {
        if (field.constraints.isEmpty()) {
            return "List.of()";
        }
        List<String> expressions = new ArrayList<>();
        for (ConstraintSpec constraint : field.constraints) {
            String problemReference = problemsType + "." + constraint.problemConstant;
            expressions.add(switch (constraint.kind) {
                case "REQUIRED" -> "ConstraintDescriptor.required(" + problemReference + ")";
                case "LENGTH" -> "ConstraintDescriptor.length(" + problemReference + ", "
                        + constraint.min + ", " + constraint.max + ")";
                case "DECIMAL_RANGE" -> "ConstraintDescriptor.decimalRange(" + problemReference + ", "
                        + stringLiteralOrNull(constraint.min) + ", " + stringLiteralOrNull(constraint.max) + ")";
                case "INT_RANGE" -> "ConstraintDescriptor.intRange(" + problemReference + ", "
                        + constraint.min + ", " + constraint.max + ")";
                case "REGEX" -> "ConstraintDescriptor.regex(" + problemReference + ", "
                        + stringLiteralOrNull(constraint.min) + ")";
                default -> throw new IllegalStateException("Unknown constraint kind: " + constraint.kind);
            });
        }
        return "List.of(" + String.join(", ", expressions) + ")";
    }

    private static StringBuilder sourceBuilder(String packageName) {
        StringBuilder source = new StringBuilder();
        if (!packageName.isEmpty()) {
            source.append("package ").append(packageName).append(";\n\n");
        }
        return source;
    }

    private static String privateConstructorSource(String generatedType) {
        return """
                    private %s() {
                        throw new AssertionError("No instances");
                    }

                """.formatted(generatedType);
    }

    private static String factoryMethodName(ActionSpec action) {
        return action.methodName + "Command";
    }

    private void writeSource(TypeElement originatingType, String qualifiedName, String source, String errorPrefix) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, originatingType);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(source);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, errorPrefix + ": " + e.getMessage(), originatingType);
        }
    }

    private static String stringLiteralOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String bigDecimalOrNull(String value) {
        return value == null ? "null" : "new BigDecimal(" + stringLiteralOrNull(value) + ")";
    }
}
