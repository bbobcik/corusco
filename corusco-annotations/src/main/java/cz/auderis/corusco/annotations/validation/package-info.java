/**
 * Validation annotations that contribute generated field constraints.
 *
 * <p>This package solves the declarative subset of form validation. Many fields
 * have local constraints that are stable metadata: required text, maximum
 * length, a regular expression, a decimal range, or an integer range. These
 * annotations let that subset live beside the field declaration while the
 * processor emits both descriptor metadata and executable validation rules.</p>
 *
 * <p>Apply validation annotations to record components that already have a
 * field-kind annotation from {@link cz.auderis.corusco.annotations.form}. The
 * processor validates each combination. For example, {@code @Length} and
 * {@code @Regex} are meaningful on string text fields, while range annotations
 * are meaningful on supported numeric text fields.</p>
 *
 * <p>For example, this source field:</p>
 *
 * <pre>{@code
 * @CoruscoForm(id = "customer")
 * record CustomerEdit(
 *         @TextField @Required @Length(max = 80) String name
 * ) {
 * }
 * }</pre>
 *
 * <p>produces problem-code constants in {@code CustomerEditProblems},
 * constraint descriptors in {@code CustomerEditDescriptors}, and executable
 * validator rules in {@code CustomerEditFormModel}.</p>
 *
 * <p>Generated form companions expose the runtime side of these annotations. A
 * problem-code companion such as {@code CustomerEditProblems} contains
 * {@code cz.auderis.corusco.core.problem.ProblemCode} constants. A descriptor
 * companion such as {@code CustomerEditDescriptors} contains
 * {@code cz.auderis.corusco.core.meta.ConstraintDescriptor} metadata attached
 * to each generated field descriptor. A generated form model such as
 * {@code CustomerEditFormModel} wires equivalent validator rules into the
 * generated {@code cz.auderis.corusco.core.validation.RuleSet}.</p>
 *
 * <p>Use these annotations for simple, local field constraints that belong in
 * generated metadata. Cross-field validation, asynchronous validation, and
 * business decisions should remain in ordinary Java validation code, usually by
 * adding handwritten rules to a form model or presenter-owned validation
 * service.</p>
 *
 * <p>Validation annotations do not replace parsing. A text field first converts
 * raw text to a semantic value through the converter selected by generated form
 * model code. If parsing fails, the form model reports parse problems before
 * semantic validation rules are evaluated.</p>
 *
 * <p>Problem ids generated from these annotations are stable metadata. They may
 * be used by resource maps, tests, summaries, or analytics. Renaming a field,
 * changing the form id, or changing an explicit problem-id policy affects those
 * generated ids and should be reviewed as a compatibility change.</p>
 *
 * <p>Advanced users should distinguish descriptor metadata from executable
 * validation. Descriptors describe the rule for UI explanation, generated
 * documentation, and inspection. The generated form model builds actual
 * validators from the same annotation data so runtime behavior and metadata
 * stay aligned.</p>
 *
 * <p>If a validation rule needs a service, database lookup, selected row, or
 * multiple fields, keep it out of these annotations. Use the core validation
 * package directly and connect it to the generated form model through ordinary
 * Java code. That keeps generated metadata simple and application policy
 * explicit.</p>
 *
 * <p>These annotations are retained in class files so adapter modules can
 * regenerate form companions from compiled form sources. Runtime code should
 * not look for them reflectively; it should consume generated problem codes,
 * descriptors, and form-model validation results.</p>
 */
package cz.auderis.corusco.annotations.validation;
