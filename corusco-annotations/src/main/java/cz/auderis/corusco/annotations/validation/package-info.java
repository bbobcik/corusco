/**
 * Validation annotations that contribute generated field constraints.
 *
 * <p>These annotations are applied to generated form field components together
 * with field-kind annotations from
 * {@link cz.auderis.corusco.annotations.form}. The processor validates each
 * combination and emits constraint descriptors and runtime validation rules for
 * required values, string length, regular expressions, decimal ranges, and
 * integer ranges.</p>
 *
 * <p>Generated form companions expose the runtime side of these annotations.
 * {@code <Form>Problems} contains
 * {@code cz.auderis.corusco.core.problem.ProblemCode} constants. {@code
 * <Form>Descriptors} contains
 * {@code cz.auderis.corusco.core.meta.ConstraintDescriptor} metadata attached
 * to each generated field descriptor. {@code <Form>FormModel} wires equivalent
 * validator rules into the generated
 * {@code cz.auderis.corusco.core.validation.RuleSet}.</p>
 *
 * <p>Use these annotations for simple, local field constraints that belong in
 * generated metadata. Cross-field validation, asynchronous validation, and
 * business decisions should remain in ordinary Java validation code.</p>
 */
package cz.auderis.corusco.annotations.validation;
