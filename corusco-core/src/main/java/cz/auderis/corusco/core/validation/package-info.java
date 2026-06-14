/**
 * Synchronous and asynchronous validation rule primitives.
 *
 * <p>This package turns field values and form state into typed problems. Start
 * with {@link cz.auderis.corusco.core.validation.FieldValidator} for a single
 * field value and {@link cz.auderis.corusco.core.validation.FormValidator} for
 * rules that inspect a broader form object. {@link
 * cz.auderis.corusco.core.validation.ValidationRule} and {@link
 * cz.auderis.corusco.core.validation.RuleSet} organize validators and their
 * field dependencies so generated and handwritten forms can refresh only the
 * affected problems.</p>
 *
 * <p>{@link cz.auderis.corusco.core.validation.Validators} contains standard
 * generated-compatible validators for required values, length, numeric ranges,
 * regular expressions, and date relations. The validators return
 * {@link cz.auderis.corusco.core.problem.ProblemSet} values rather than
 * throwing for normal validation failures. Parsing failures are handled by the
 * form/convert layer before validation receives a semantic value.</p>
 *
 * <p>Asynchronous validation is represented by
 * {@link cz.auderis.corusco.core.validation.AsyncFieldValidator} and
 * {@link cz.auderis.corusco.core.validation.AsyncFieldValidation}. Use it when
 * validation depends on external services or slow work. {@link
 * cz.auderis.corusco.core.validation.ValidationTiming} describes when a rule is
 * intended to run. The package remains Swing-free; task execution, cancellation,
 * stale-result suppression, and EDT delivery are coordinated by task and Swing
 * adapter packages.</p>
 */
package cz.auderis.corusco.core.validation;
