package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Immutable validation plan for a form or presentation model.
 *
 * <p>A rule set collects field-level and form-level validation rules while
 * preserving the typed dependency metadata needed by generated and handwritten
 * models. Callers can validate every rule, only rules with a particular
 * {@link ValidationTiming}, or only rules affected by a changed
 * {@link FieldKey}. This gives form models one place to aggregate validation
 * without scattering cross-field dependencies through Swing bindings.</p>
 *
 * <p>The distinction between parse errors and semantic validation is
 * intentional. Rules created with {@link Builder#field(FieldKey, Function,
 * FieldValidator)} operate on {@link TextFieldModel} semantic values and skip
 * the validator when the text field already reports parse errors. That keeps a
 * malformed text input from producing both parse and validation problems for
 * the same field. Rules created with {@link Builder#semanticField(FieldKey,
 * Function, FieldValidator)} are for fields that do not expose parse state.</p>
 *
 * <p>Instances are immutable and reusable. The rule validators they contain are
 * invoked synchronously by the validation methods and should return immutable
 * {@link ProblemSet} instances. This class does not catch validator exceptions,
 * does not install listeners, and has no Swing threading policy; callers decide
 * when validation runs and how resulting problems are surfaced.</p>
 *
 * @param <M> model type
 */
public final class RuleSet<M> {

    private final List<ValidationRule<M>> rules;

    private RuleSet(List<ValidationRule<M>> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Creates an empty rule set.
     *
     * @param <M> model type
     * @return empty rule set
     */
    public static <M> RuleSet<M> empty() {
        return new RuleSet<>(List.of());
    }

    /**
     * Starts building a rule set.
     *
     * @param <M> model type
     * @return rule-set builder
     */
    public static <M> Builder<M> builder() {
        return new Builder<>();
    }

    /**
     * Returns immutable rule entries.
     *
     * @return rules in registration order
     */
    public List<ValidationRule<M>> rules() {
        return rules;
    }

    /**
     * Validates all rules in registration order.
     *
     * <p>The returned problem set is the aggregation of every matching rule.
     * The supplied model is not retained after the call.</p>
     *
     * @param model model to validate
     * @return aggregated problems
     */
    public ProblemSet validateAll(M model) {
        return validateMatching(model, rule -> true);
    }

    /**
     * Validates rules with the supplied timing hint.
     *
     * <p>Timing is metadata only; this method does not schedule, debounce, or
     * defer validation. It simply filters the immutable rule list.</p>
     *
     * @param model model to validate
     * @param timing timing to match
     * @return aggregated problems
     */
    public ProblemSet validateTiming(M model, ValidationTiming timing) {
        Objects.requireNonNull(timing, "timing");
        return validateMatching(model, rule -> rule.timing() == timing);
    }

    /**
     * Validates rules affected by the supplied changed field key.
     *
     * <p>Only rules whose dependency list contains the key are executed. Use
     * {@link #validateAll(Object)} when a full form pass is needed before
     * commit.</p>
     *
     * @param model model to validate
     * @param changedField changed field key
     * @return aggregated problems
     */
    public ProblemSet validateFor(M model, FieldKey<?, ?> changedField) {
        Objects.requireNonNull(changedField, "changedField");
        return validateMatching(model, rule -> rule.dependsOn(changedField));
    }

    private ProblemSet validateMatching(M model, java.util.function.Predicate<ValidationRule<M>> predicate) {
        Objects.requireNonNull(model, "model");
        ProblemSet problems = ProblemSet.empty();
        for (ValidationRule<M> rule : rules) {
            if (predicate.test(rule)) {
                problems = problems.addAll(rule.validate(model));
            }
        }
        return problems;
    }

    /**
     * Mutable builder that produces an immutable rule set.
     *
     * <p>The builder is a construction helper, not a live validation object.
     * Add rules while assembling the form model, then call {@link #build()} and
     * keep the returned immutable {@link RuleSet}. The builder itself is not
     * synchronized.</p>
     *
     * @param <M> model type
     */
    public static final class Builder<M> {

        private final List<ValidationRule<M>> rules = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an on-change rule for a parsed text field.
         *
         * <p>The field accessor is invoked during validation. If the field
         * already has parse errors, the supplied semantic validator is skipped
         * and no problems are added by this rule.</p>
         *
         * @param dependency typed field dependency
         * @param fieldAccessor accessor for the text field model
         * @param validator field validator
         * @param <O> owner/model type
         * @param <T> field value type
         * @return this builder
         */
        public <O, T> Builder<M> field(
                FieldKey<O, T> dependency,
                Function<M, TextFieldModel<O, T>> fieldAccessor,
                FieldValidator<O, T> validator
        ) {
            return field(dependency, fieldAccessor, validator, ValidationTiming.ON_CHANGE);
        }

        /**
         * Adds a field rule for a parsed text field with timing metadata.
         *
         * <p>The field dependency controls incremental validation and problem
         * targeting. The timing value controls which filtered validation pass
         * includes the rule.</p>
         *
         * @param dependency typed field dependency
         * @param fieldAccessor accessor for the text field model
         * @param validator field validator
         * @param timing validation timing hint
         * @param <O> owner/model type
         * @param <T> field value type
         * @return this builder
         */
        public <O, T> Builder<M> field(
                FieldKey<O, T> dependency,
                Function<M, TextFieldModel<O, T>> fieldAccessor,
                FieldValidator<O, T> validator,
                ValidationTiming timing
        ) {
            Objects.requireNonNull(dependency, "dependency");
            Objects.requireNonNull(fieldAccessor, "fieldAccessor");
            Objects.requireNonNull(validator, "validator");
            Objects.requireNonNull(timing, "timing");
            return rule(List.of(dependency), timing, model -> {
                TextFieldModel<O, T> field = fieldAccessor.apply(model);
                if (field.problems().hasErrors()) {
                    return ProblemSet.empty();
                }
                return validator.validate(dependency, field.value());
            });
        }

        /**
         * Adds an on-change rule for a semantic field that has no parse state.
         *
         * @param dependency typed field dependency
         * @param fieldAccessor accessor for the field model
         * @param validator field validator
         * @param <O> owner/model type
         * @param <T> field value type
         * @return this builder
         */
        public <O, T> Builder<M> semanticField(
                FieldKey<O, T> dependency,
                Function<M, FieldModel<O, T>> fieldAccessor,
                FieldValidator<O, T> validator
        ) {
            return semanticField(dependency, fieldAccessor, validator, ValidationTiming.ON_CHANGE);
        }

        /**
         * Adds a field rule for a semantic field with timing metadata.
         *
         * <p>The validator receives the current semantic value from the field
         * model. Unlike text-field rules, there is no parse-error gate.</p>
         *
         * @param dependency typed field dependency
         * @param fieldAccessor accessor for the field model
         * @param validator field validator
         * @param timing validation timing hint
         * @param <O> owner/model type
         * @param <T> field value type
         * @return this builder
         */
        public <O, T> Builder<M> semanticField(
                FieldKey<O, T> dependency,
                Function<M, FieldModel<O, T>> fieldAccessor,
                FieldValidator<O, T> validator,
                ValidationTiming timing
        ) {
            Objects.requireNonNull(dependency, "dependency");
            Objects.requireNonNull(fieldAccessor, "fieldAccessor");
            Objects.requireNonNull(validator, "validator");
            Objects.requireNonNull(timing, "timing");
            return rule(List.of(dependency), timing, model -> {
                FieldModel<O, T> field = fieldAccessor.apply(model);
                return validator.validate(dependency, field.value().value());
            });
        }

        /**
         * Adds an on-change form or cross-field rule.
         *
         * <p>The dependency list should include every field whose change can
         * affect the validator result. Leave it empty only for rules that are
         * intentionally excluded from incremental field validation.</p>
         *
         * @param dependencies typed field dependencies
         * @param validator form validator
         * @return this builder
         */
        public Builder<M> form(List<FieldKey<?, ?>> dependencies, FormValidator<M> validator) {
            return rule(dependencies, ValidationTiming.ON_CHANGE, validator);
        }

        /**
         * Adds a rule with explicit timing metadata.
         *
         * <p>The dependency list is copied by the {@link ValidationRule}
         * constructor. The validator is retained and invoked synchronously by
         * the built rule set.</p>
         *
         * @param dependencies typed field dependencies
         * @param timing validation timing hint
         * @param validator form validator
         * @return this builder
         */
        public Builder<M> rule(
                List<FieldKey<?, ?>> dependencies,
                ValidationTiming timing,
                FormValidator<M> validator
        ) {
            rules.add(new ValidationRule<>(dependencies, timing, validator));
            return this;
        }

        /**
         * Builds an immutable rule set.
         *
         * <p>The current builder contents are copied. Further additions to the
         * builder do not mutate previously built rule sets.</p>
         *
         * @return rule set
         */
        public RuleSet<M> build() {
            return new RuleSet<>(rules);
        }
    }
}
