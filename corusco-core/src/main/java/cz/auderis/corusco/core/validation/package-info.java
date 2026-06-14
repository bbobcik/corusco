/**
 * Synchronous and asynchronous validation rule primitives.
 *
 * <p>This package solves the semantic validation problem after input has been
 * parsed into typed values. Parsing answers "can this text become an integer?";
 * validation answers "is this integer acceptable for this form?". Validators
 * produce typed problems rather than throwing for normal user mistakes, so
 * Swing, tests, dialogs, and background workflows can present the same results
 * in different ways.</p>
 *
 * <p>Use this package for local field constraints, cross-field rules, form-wide
 * business checks, and asynchronous checks that call services. Do not use it
 * for text conversion; conversion belongs in {@code cz.auderis.corusco.core.convert}
 * and parse problems are collected by {@code cz.auderis.corusco.core.form}
 * before semantic validation runs.</p>
 *
 * <p>The first step for a single-field rule is a {@link
 * cz.auderis.corusco.core.validation.FieldValidator}. Standard validators are
 * available from {@link cz.auderis.corusco.core.validation.Validators}:</p>
 *
 * <pre>{@code
 * FieldValidator<String> requiredName = Validators.required("customer/name/required");
 * ProblemSet problems = requiredName.validate("Alice");
 * }</pre>
 *
 * <p>For a form, collect validators in a {@link
 * cz.auderis.corusco.core.validation.RuleSet}. A rule set records which fields
 * each rule depends on, so generated and handwritten forms can evaluate all
 * rules or refresh only affected problems after a field changes. Use {@link
 * cz.auderis.corusco.core.validation.FormValidator} when a rule needs the
 * broader form object rather than one field value.</p>
 *
 * <p>Generated {@code @SwingForm} records use this package in two ways. The
 * annotation processor emits declarative {@code ConstraintDescriptor} metadata
 * in a descriptor companion, for example {@code CustomerEditDescriptors}, and
 * the generated form model, for example {@code CustomerEditFormModel}, builds a
 * {@code RuleSet} with validators corresponding to {@code @Required},
 * {@code @Length}, {@code @Regex}, {@code @DecimalRange}, and {@code @IntRange}
 * annotations. Handwritten forms can use the same validators and rule-set
 * builder directly.</p>
 *
 * <p>{@link cz.auderis.corusco.core.validation.Validators} contains standard
 * generated-compatible validators for required values, length, numeric ranges,
 * regular expressions, and date relations. The validators return {@link
 * cz.auderis.corusco.core.problem.ProblemSet} values. Problem ids should be
 * stable {@code ProblemCode} ids, often generated in a problem-code companion
 * such as {@code CustomerEditProblems} or declared next to handwritten
 * validation rules.</p>
 *
 * <p>Problem identity is part of the validation contract. A user-facing message
 * can be translated, reworded, or made more specific, but the problem code
 * should stay stable so tests, accessibility summaries, dialog buttons, and
 * downstream integrations can recognize the same condition. Generated problem
 * companions provide stable constants for annotation-derived constraints;
 * handwritten rules should follow the same convention.</p>
 *
 * <p>Validation timing should match the cost and usefulness of the rule. A
 * required-field rule may run while editing so the view can clear an error as
 * soon as text appears. A cross-field consistency rule may run after either
 * dependent field changes. A remote uniqueness check may be delayed until
 * focus loss, Apply, or an explicit validation request. The timing metadata is
 * guidance for controllers; it does not make this package responsible for
 * scheduling work.</p>
 *
 * <p>Asynchronous validation is represented by {@link
 * cz.auderis.corusco.core.validation.AsyncFieldValidator} and {@link
 * cz.auderis.corusco.core.validation.AsyncFieldValidation}. Use it when
 * validation depends on external services or slow work, such as checking a VAT
 * number, unique name, or remote account state. {@link
 * cz.auderis.corusco.core.validation.ValidationTiming} describes when a rule is
 * intended to run. The package remains Swing-free; task execution,
 * cancellation, stale-result suppression, and EDT delivery are coordinated by
 * task and Swing adapter packages.</p>
 *
 * <p>Validators should be deterministic and side-effect-light. They may consult
 * immutable configuration, lookup tables, or service abstractions supplied by
 * the caller, but they should not directly mutate form state, Swing
 * components, or global application state. That discipline keeps validation
 * safe to rerun when a dependent field changes, when a form is reset, or when a
 * test evaluates the same rule with several inputs.</p>
 *
 * <p>Testing validators should focus on both positive and negative examples.
 * For each rule, assert that valid input produces no blocking problem, that
 * invalid input produces the expected problem code and severity, and that edge
 * values such as {@code null}, blank text, minimum length, maximum length, and
 * numeric boundaries behave intentionally. For rule sets, add tests that
 * changing one field refreshes only the rules whose dependency set includes
 * that field.</p>
 *
 * <p>Keep presentation decisions outside this package. A problem produced here
 * may become an inline field message, a tooltip, a summary banner, a disabled
 * OK button, a log entry, or a table-cell decoration. The validator's job is to
 * report precise, stable facts; UI packages decide how and when those facts
 * are shown to a user.</p>
 */
package cz.auderis.corusco.core.validation;
