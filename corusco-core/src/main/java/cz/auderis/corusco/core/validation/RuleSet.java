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
 * Immutable collection of validation rules for a model.
 *
 * <p>A rule set can validate all rules, rules for a particular timing, or only
 * rules affected by a changed field key. Field rules created by the builder skip
 * validation when the text field already has parse errors, keeping parse and
 * validation sources separate.</p>
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
     * @param model model to validate
     * @return aggregated problems
     */
    public ProblemSet validateAll(M model) {
        return validateMatching(model, rule -> true);
    }

    /**
     * Validates rules with the supplied timing hint.
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
     * @param <M> model type
     */
    public static final class Builder<M> {

        private final List<ValidationRule<M>> rules = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a field rule for a text field.
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
         * Adds a field rule for a text field with timing metadata.
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
         * Adds a field rule for a semantic field that has no parse state.
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
         * Adds a form or cross-field rule.
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
         * @return rule set
         */
        public RuleSet<M> build() {
            return new RuleSet<>(rules);
        }
    }
}
