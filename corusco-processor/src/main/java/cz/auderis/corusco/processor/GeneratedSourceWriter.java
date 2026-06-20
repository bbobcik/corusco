package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * <p>This writer is the central generated-code contract for {@code @CoruscoForm}
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
        writePresentationModelClass(formType, form);
        new FormOptionsSourceWriter(elements, filer, messager).writeOptionsClass(formType, form);
        new FormDependenciesSourceWriter(elements, filer, messager).writeDependenciesClass(formType, form);
    }

    void writeFormSwingSources(TypeElement formType, FormSpec form, String targetPackageName) {
        String sourcePackageName = elements.getPackageOf(formType).getQualifiedName().toString();
        writeViewClass(formType, form, targetPackageName);
        writeBehaviorPlanClass(formType, form, targetPackageName, sourcePackageName);
        writeBindingsClass(formType, form, targetPackageName, sourcePackageName);
    }

    private void writeResultImplementationClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String generatedType = form.resultImplementationType;
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/result-class.javafragment", packageName,
                form.sourceType, generatedType);
        for (FieldSpec field : form.fields) {
            source.addFragment("""
                    private final %s %s;

                    """.formatted(field.accessorType, field.componentName));
        }
        source.addFragment(resultConstructorSource(generatedType, form.fields));
        for (FieldSpec field : form.fields) {
            source.addFragment(resultAccessorSource(field));
        }
        source.addFragment(resultEqualsSource(form));
        source.addFragment(resultHashCodeSource(form.fields));
        source.addFragment(resultToStringSource(generatedType, form.fields));
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated form result implementation");
    }

    private void writeFieldsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Fields";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/fields-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.addFragment(fieldSource(field));
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated field keys");
    }

    private void writeResourcesClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Resources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/resources-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.addFragment(resourceKeySource(field.labelConstant, field.keyId + "/label"));
            if (field.tooltipConstant != null) {
                source.addFragment(resourceKeySource(field.tooltipConstant, field.tooltipId));
            }
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated resource keys");
    }

    private void writeProblemsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/problems-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            for (ConstraintSpec constraint : field.constraints) {
                source.addFragment(problemCodeSource(constraint));
            }
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated problem codes");
    }

    private void writeDescriptorsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Descriptors";
        String resourcesType = ownerType + "Resources";
        String problemsType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/descriptors-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        for (FieldSpec field : form.fields) {
            source.addFragment(descriptorSource(field, resourcesType, problemsType));
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated field descriptors");
    }

    private void writeFormModelClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "FormModel";
        String fieldsType = ownerType + "Fields";
        String descriptorsType = ownerType + "Descriptors";
        String resultType = form.resultType();
        String resultConstructorType = form.hasGeneratedResultImplementation()
                ? form.resultImplementationType
                : ownerType;
        String originalDescription = form.hasGeneratedResultImplementation()
                ? "original immutable form result"
                : "original immutable record";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/model-class.javafragment", packageName,
                ownerType, generatedType, Map.of("RESULT_TYPE", resultType));
        for (FieldSpec field : form.fields) {
            source.addFragment(fieldModelDeclarationSource(field));
        }
        source.addFragment("""
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
            source.addFragment(fieldModelInitializationSource(field, fieldsType));
        }
        source.addFragment("""
                        this.rules = buildRules();
                    }

                """);
        source.addFragment(descriptorListSource(form.fields, descriptorsType));
        source.addFragment(buildRulesSource(form.fields, generatedType, fieldsType));
        source.addFragment("""
                    @Override
                    protected ProblemSet validationProblems() {
                        return rules.validateAll(this);
                    }

                    @Override
                    protected %s createResult() {
                        return new %s(
                """.formatted(resultType, resultConstructorType));
        for (int i = 0; i < form.fields.size(); i++) {
            FieldSpec field = form.fields.get(i);
            String suffix = i == form.fields.size() - 1 ? "\n" : ",\n";
            source.addFragment("                " + resultValueExpression(field) + suffix);
        }
        source.addFragment("""
                        );
                    }
                """);
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated form model");
    }

    private void writePresentationModelClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "PresentationModel";
        String formModelType = ownerType + "FormModel";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/presentation-model-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment("""
                    private final %s form;

                """.formatted(formModelType));
        for (ComponentStateSpec state : form.componentStates) {
            source.addFragment(componentStateDeclarationSource(state));
        }
        source.addFragment("""
                    /**
                     * Creates a generated presentation model.
                     *
                     * @param form generated semantic form model
                     */
                    public %s(%s form) {
                        this.form = Objects.requireNonNull(form, "form");
                """.formatted(generatedType, formModelType));
        for (ComponentStateSpec state : form.componentStates) {
            source.addFragment(componentStateInitializationSource(state));
        }
        source.addFragment("""
                    }

                    /**
                     * Returns the semantic form model.
                     *
                     * @return generated form model
                     */
                    public %s form() {
                        return form;
                    }

                """.formatted(formModelType));
        for (ComponentStateSpec state : form.componentStates) {
            source.addFragment(componentStateAccessorSource(state));
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated presentation model");
    }

    private void writeViewClass(TypeElement formType, FormSpec form, String packageName) {
        String ownerType = form.sourceType;
        String generatedType = ownerType + "View";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/view-class.javafragment", packageName,
                ownerType, generatedType);
        for (FieldSpec field : form.fields) {
            source.addFragment(viewMethodSource(field));
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated view contract");
    }

    private void writeBehaviorPlanClass(
            TypeElement formType,
            FormSpec form,
            String packageName,
            String sourcePackageName
    ) {
        String ownerType = form.sourceType;
        String generatedType = ownerType + "BehaviorPlan";
        String viewType = ownerType + "View";
        String presentationModelType = generatedPeerType(sourcePackageName, packageName, ownerType + "PresentationModel");
        String optionsType = generatedPeerType(sourcePackageName, packageName, ownerType + "Options");
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/behavior-plan-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        source.addFragment("""
                    /**
                     * Installs supported generated behaviors.
                     *
                     * @param view generated view contract
                     * @param model generated presentation model
                     * @param scope owning behavior scope
                     */
                    public static void install(%s view, %s model, BehaviorScope scope) {
                        java.util.Objects.requireNonNull(view, "view");
                        java.util.Objects.requireNonNull(model, "model");
                        java.util.Objects.requireNonNull(scope, "scope");
                """.formatted(viewType, presentationModelType));
        for (FieldSpec field : form.fields) {
            source.addFragment(behaviorInstallSource(field, optionsType));
        }
        for (ComponentStateSpec state : form.componentStates) {
            for (DependencySpec dependency : state.dependencies) {
                source.addFragment(dependencyInstallSource(dependency));
            }
        }
        source.addFragment("""
                    }

                """);
        if (hasDependencies(form)) {
            source.addFragment(dependencyBindingSupportSource());
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated behavior plan");
    }

    private void writeBindingsClass(
            TypeElement formType,
            FormSpec form,
            String packageName,
            String sourcePackageName
    ) {
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Bindings";
        String viewType = ownerType + "View";
        String formModelType = generatedPeerType(sourcePackageName, packageName, ownerType + "FormModel");
        String presentationModelType = generatedPeerType(sourcePackageName, packageName, ownerType + "PresentationModel");
        String behaviorPlanType = ownerType + "BehaviorPlan";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = formClassSource("form/bindings-class.javafragment", packageName,
                ownerType, generatedType);
        source.addFragment(privateConstructorSource(generatedType));
        source.addFragment(ProcessorSourceTemplates.fragment(GeneratedSourceWriter.class, "form/bindings-install.javafragment", Map.of(
                "VIEW_TYPE", viewType,
                "FORM_MODEL_TYPE", formModelType,
                "PRESENTATION_MODEL_TYPE", presentationModelType,
                "BEHAVIOR_PLAN_TYPE", behaviorPlanType
        )));
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated binding facade");
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
        SimpleMutableFragment source = new SimpleMutableFragment();
        source.appendFormatted("""
                    /**
                     * Creates an immutable generated form result.
                     */
                    public %s(
                """, generatedType);
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            String suffix = i == fields.size() - 1 ? "\n" : ",\n";
            source.appendFormatted("            %s %s%s", field.accessorType, field.componentName, suffix);
        }
        source.append("""
                    ) {
                """);
        for (FieldSpec field : fields) {
            source.appendFormatted("        this.%s = %s;%n", field.componentName, field.componentName);
        }
        source.append("""
                    }

                """);
        return source.asString();
    }

    private String resultAccessorSource(FieldSpec field) {
        return """
                    public %s %s() {
                        return %s;
                    }

                """.formatted(field.accessorType, field.componentName, field.componentName);
    }

    private String resultEqualsSource(FormSpec form) {
        SimpleMutableFragment source = new SimpleMutableFragment();
        String comparisonType = form.hasGeneratedResultImplementation()
                ? form.resultImplementationType
                : form.sourceType;
        source.appendFormatted("""
                    @Override
                    public boolean equals(Object obj) {
                        if (this == obj) {
                            return true;
                        }
                        if (!(obj instanceof %s other)) {
                            return false;
                        }
                        return%s""", comparisonType, " ");
        for (int i = 0; i < form.fields.size(); i++) {
            FieldSpec field = form.fields.get(i);
            String prefix = i == 0 ? "" : "\n                && ";
            source.appendFormatted("%sObjects.equals(%s(), other.%s())",
                    prefix, field.componentName, field.componentName);
        }
        source.append("""
                ;
                    }

                """);
        return source.asString();
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
        SimpleMutableFragment source = new SimpleMutableFragment();
        source.appendFormatted("""
                    @Override
                    public String toString() {
                        return "%s["
                """, generatedType);
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            String prefix = i == 0 ? " + \"" : " + \", ";
            source.appendFormatted("                %s%s=\" + %s()%n",
                    prefix, field.componentName, field.componentName);
        }
        source.append("""
                                + "]";
                    }

                """);
        return source.asString();
    }

    private String resourceKeySource(String constantName, String id) {
        return ProcessorSourceTemplates.fragment(GeneratedSourceWriter.class, "common/resource-key.javafragment", Map.of(
                "RESOURCE_ID", id,
                "CONSTANT_NAME", constantName,
                "RESOURCE_ID_LITERAL", stringLiteralOrNull(id)
        )).asString();
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
                                    %s,
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
                editorDescriptorExpression(field.kind),
                field.valueClass,
                resourcesType,
                field.labelConstant,
                tooltip,
                helpTopicExpression(field),
                constraintsExpression(field, problemsType)
        );
    }

    private String editorDescriptorExpression(String kind) {
        return switch (kind) {
            case "TEXT" -> "EditorDescriptor.text()";
            case "DATE" -> "EditorDescriptor.date()";
            case "CHECK_BOX" -> "EditorDescriptor.checkBox()";
            case "COMBO_BOX" -> "EditorDescriptor.comboBox()";
            case "RADIO_GROUP" -> "EditorDescriptor.radioGroup()";
            default -> throw new IllegalStateException("Unknown field kind: " + kind);
        };
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

    private String componentStateDeclarationSource(ComponentStateSpec state) {
        return """
                    /**
                     * Component state model for {@code %s}.
                     */
                    public final ComponentStateModel %s;

                """.formatted(state.componentName, state.componentName);
    }

    private String componentStateInitializationSource(ComponentStateSpec state) {
        return """
                    this.%s = new ComponentStateModel();
                """.formatted(state.componentName).indent(4);
    }

    private String componentStateAccessorSource(ComponentStateSpec state) {
        return """
                    /**
                     * Returns component state model for {@code %s}.
                     *
                     * @return component state model
                     */
                    public ComponentStateModel %s() {
                        return %s;
                    }

                """.formatted(state.componentName, state.componentName, state.componentName);
    }

    private String descriptorListSource(List<FieldSpec> fields, String descriptorsType) {
        SimpleMutableFragment source = new SimpleMutableFragment();
        source.append("""
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
            source.appendFormatted("                %s.%s%s", descriptorsType, fields.get(i).constantName, suffix);
        }
        source.append("""
                        );
                    }

                """);
        return source.asString();
    }

    private String buildRulesSource(List<FieldSpec> fields, String generatedType, String fieldsType) {
        SimpleMutableFragment source = new SimpleMutableFragment();
        source.appendFormatted("""
                    private static RuleSet<%s> buildRules() {
                        RuleSet.Builder<%s> rules = RuleSet.builder();
                """, generatedType, generatedType);
        for (FieldSpec field : fields) {
            for (ConstraintSpec constraint : field.constraints) {
                source.append(ruleSource(field, constraint, fieldsType));
            }
        }
        source.append("""
                        return rules.build();
                    }

                """);
        return source.asString();
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

    private String behaviorInstallSource(FieldSpec field, String optionsType) {
        String componentStateBehavior = field.componentState
                ? ",\n                            StandardBehaviors.componentState(model." + field.componentName + "State)"
                : "";
        if ("TextFieldModel".equals(field.modelType)) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.textFieldBinding(model.form().%s),
                                StandardBehaviors.validationTooltip(model.form().%s.problemSet()),
                                StandardBehaviors.validationBorder(model.form().%s.problemSet()),
                                StandardBehaviors.selectAllOnFocus()%s
                        ));
                    """.formatted(
                    field.viewMethodName,
                    field.componentName,
                    field.componentName,
                    field.componentName,
                    componentStateBehavior
            );
        } else if ("CHECK_BOX".equals(field.kind)) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.checkBoxBinding(model.form().%s)%s
                        ));
                    """.formatted(field.viewMethodName, field.componentName, componentStateBehavior);
        } else if ("RADIO_GROUP".equals(field.kind) && !field.options.isEmpty()) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.radioGroupBinding(model.form().%s, %s.%s_DESCRIPTORS)%s
                        ));
                    """.formatted(
                    field.viewMethodName,
                    field.componentName,
                    optionsType,
                    field.constantName,
                    componentStateBehavior
            );
        } else if ("RADIO_GROUP".equals(field.kind)) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.radioGroupBinding(model.form().%s, %s.class)%s
                        ));
                    """.formatted(
                    field.viewMethodName,
                    field.componentName,
                    field.valueType,
                    componentStateBehavior
            );
        } else if (field.componentState) {
            return """
                        scope.install(view.%s(), List.of(
                                StandardBehaviors.componentState(model.%sState)
                        ));
                    """.formatted(field.viewMethodName, field.componentName);
        }
        return "";
    }

    private String dependencyInstallSource(DependencySpec dependency) {
        String sourceValue = dependency.sourceTextField
                ? "form()." + dependency.sourceFieldName + ".rawText()"
                : "form()." + dependency.sourceFieldName + ".value()";
        return """
                        scope.add(dependencyBinding(
                                model.%s,
                                model.%s,
                                List.of(%s),
                                DependencyEffect.%s
                        ));
                """.formatted(
                sourceValue,
                dependency.targetStateModel,
                dependencyValuesExpression(dependency.valueExpressions),
                dependency.effect
        );
    }

    private static String dependencyValuesExpression(List<String> values) {
        return values.stream().collect(java.util.stream.Collectors.joining(", "));
    }

    private static boolean hasDependencies(FormSpec form) {
        return form.componentStates.stream().anyMatch(state -> !state.dependencies.isEmpty());
    }

    private String dependencyBindingSupportSource() {
        return """
                    private static Binding dependencyBinding(
                            ReadableValue<?> source,
                            ComponentStateModel target,
                            List<?> expectedValues,
                            DependencyEffect effect
                    ) {
                        updateDependency(source.value(), target, expectedValues, effect);
                        var subscription = source.subscribe(event ->
                                updateDependency(event.newValue(), target, expectedValues, effect));
                        return subscription::close;
                    }

                    private static void updateDependency(
                            Object value,
                            ComponentStateModel target,
                            List<?> expectedValues,
                            DependencyEffect effect
                    ) {
                        boolean active = expectedValues.contains(value);
                        switch (effect) {
                            case ENABLED -> target.enabled().setValue(active, ChangeOrigin.GENERATED);
                            case VISIBLE -> target.visible().setValue(active, ChangeOrigin.GENERATED);
                            case RELEVANT -> target.relevant().setValue(active, ChangeOrigin.GENERATED);
                        }
                    }

                """;
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

    private static String privateConstructorSource(String generatedType) {
        return ProcessorSourceTemplates.privateConstructorSource(GeneratedSourceWriter.class, generatedType);
    }

    private static BasicStructuredFragment formClassSource(
            String resourceName,
            String packageName,
            String ownerType,
            String generatedType
    ) {
        return formClassSource(resourceName, packageName, ownerType, generatedType, Map.of());
    }

    private static BasicStructuredFragment formClassSource(
            String resourceName,
            String packageName,
            String ownerType,
            String generatedType,
            Map<String, String> extraValues
    ) {
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>(extraValues);
        values.put("OWNER_TYPE", ownerType);
        values.put("GENERATED_TYPE", generatedType);
        return ProcessorSourceTemplates.structuredClass(GeneratedSourceWriter.class, resourceName, packageName, values);
    }

    private static String generatedPeerType(String sourcePackageName, String targetPackageName, String typeName) {
        if (sourcePackageName.equals(targetPackageName) || sourcePackageName.isEmpty()) {
            return typeName;
        }
        return sourcePackageName + "." + typeName;
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
