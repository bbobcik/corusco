/**
 * Form-level annotations consumed by the Corusco annotation processor.
 *
 * <p>This package solves the problem of describing an editable form once, in
 * ordinary Java source, and deriving the repetitive support code from that
 * declaration. A form record or abstract class names the values being edited.
 * Field-kind annotations say how each value should be represented in the
 * generated form model and optional Swing view contract. Validation and help
 * annotations add metadata without requiring runtime annotation scanning.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.annotations.form.CoruscoForm} on a
 * non-generic record that represents editable input, or on a non-generic
 * abstract class with abstract accessor methods. Then annotate each generated
 * component or accessor with exactly one field-kind annotation:
 * {@link cz.auderis.corusco.annotations.form.TextField},
 * {@link cz.auderis.corusco.annotations.form.DateField},
 * {@link cz.auderis.corusco.annotations.form.ComboBox}, {@link
 * cz.auderis.corusco.annotations.form.RadioGroup}, or {@link
 * cz.auderis.corusco.annotations.form.CheckBox}. Record components without a
 * field-kind annotation are not part of generated form metadata unless they
 * carry misplaced state metadata. Abstract value accessors must have a
 * field-kind annotation.</p>
 *
 * <p>{@link cz.auderis.corusco.annotations.form.SwingForm} remains as a
 * deprecated Corusco 1.0 source-compatible alias for record forms that expect
 * same-package Swing companions. New code should use {@link
 * cz.auderis.corusco.annotations.form.CoruscoForm} and request Swing companions
 * with {@link cz.auderis.corusco.annotations.SwingCompanionPackage}.</p>
 *
 * <p>For example, this source record:</p>
 *
 * <pre>{@code
 * @CoruscoForm(id = "customer")
 * record CustomerEdit(
 *         @TextField @Required String name,
 *         @CheckBox boolean active
 * ) {
 * }
 * }</pre>
 *
 * <p>produces generated companions whose names start with
 * {@code CustomerEdit}, such as {@code CustomerEditFields},
 * {@code CustomerEditResources}, {@code CustomerEditDescriptors},
 * {@code CustomerEditProblems}, {@code CustomerEditFormModel},
 * {@code CustomerEditPresentationModel}. Packages annotated with
 * {@link cz.auderis.corusco.annotations.SwingCompanionPackage} also receive Swing
 * companions such as {@code CustomerEditView},
 * {@code CustomerEditBehaviorPlan}, and {@code CustomerEditBindings}. The
 * prefix comes from the source type name, so renaming the source changes
 * generated type names. Abstract class forms also produce a generated
 * immutable result implementation.</p>
 *
 * <p>The first practical runtime step is usually to construct the generated
 * form model from an original record value:</p>
 *
 * <pre>{@code
 * CustomerEdit original = new CustomerEdit("Alice", true);
 * CustomerEditFormModel form = new CustomerEditFormModel(original);
 * CustomerEditPresentationModel presentation = new CustomerEditPresentationModel(form);
 * }</pre>
 *
 * <p>Generated form key instances live in generated companions. A fields
 * companion such as {@code CustomerEditFields} exposes
 * {@code cz.auderis.corusco.core.key.TextFieldKey} or
 * {@code cz.auderis.corusco.core.key.FieldKey} constants. A resources
 * companion such as {@code CustomerEditResources} exposes
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants for labels
 * and tooltips. Help topics from {@code @Help} are embedded in descriptors as
 * {@code cz.auderis.corusco.core.key.HelpTopic} values.</p>
 *
 * <p>Selection editors can also expose generated option metadata. Enum
 * constants may be annotated with {@link
 * cz.auderis.corusco.annotations.form.CoruscoForm.Option} to assign stable option keys and
 * ordering. The generated options companion derives localized label and
 * description resource keys from the field key, keeping display text out of
 * the enum itself.</p>
 *
 * <p>{@link cz.auderis.corusco.annotations.form.CoruscoForm.ComponentState}
 * requests a generated {@code ComponentStateModel} in the presentation model
 * for a field, or declares an auxiliary state member on an abstract form
 * accessor that returns {@code ComponentStateModel}. {@link
 * cz.auderis.corusco.annotations.form.CoruscoForm.DependsOn} can then describe
 * dependency metadata from one form field to that state model. The dependency
 * field name is validated against generated form members, so misspelled
 * references fail during compilation.</p>
 *
 * <p>Generated forms also expose
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor},
 * {@code cz.auderis.corusco.core.problem.ProblemCode},
 * {@code cz.auderis.corusco.core.form.FieldModel}, and
 * {@code cz.auderis.corusco.core.form.TextFieldModel} based runtime objects
 * through companions such as {@code CustomerEditDescriptors},
 * {@code CustomerEditProblems}, and {@code CustomerEditFormModel}. Visual
 * state lives in {@code CustomerEditPresentationModel}. For packages annotated
 * with {@link cz.auderis.corusco.annotations.SwingCompanionPackage}, Swing behavior
 * installation is generated in companions such as
 * {@code CustomerEditBehaviorPlan} and surfaced through
 * {@code CustomerEditBindings}.</p>
 *
 * <p>The generated view contract is deliberately an interface. Applications
 * still own layout, component construction, resource loading, and dialog shell
 * policy. Implement {@code CustomerEditView} in a panel, dialog view, or test
 * fixture, then call the package-generated bindings facade with a
 * {@code cz.auderis.corusco.swing.behavior.BehaviorScope} to install supported
 * component behavior.</p>
 *
 * <p>Use {@link cz.auderis.corusco.annotations.form.TextField} for supported
 * scalar text editing where raw text, parse state, dirty state, and semantic
 * value must be separated. Use {@link
 * cz.auderis.corusco.annotations.form.DateField} for
 * {@link java.time.LocalDate} values edited through text conversion. Use
 * {@link cz.auderis.corusco.annotations.form.CheckBox} for booleans and
 * {@link cz.auderis.corusco.annotations.form.ComboBox} or {@link
 * cz.auderis.corusco.annotations.form.RadioGroup} for selected reference values
 * or enums.</p>
 *
 * <p>Validation annotations from
 * {@code cz.auderis.corusco.annotations.validation} are compiled into both
 * declarative descriptors and executable generated validation rules. Simple
 * local constraints belong here. Cross-field validation, asynchronous service
 * checks, and business decisions should remain in ordinary Java validation code
 * where they can depend on services and application state explicitly.</p>
 *
 * <p>Stable ids are part of the generated contract. The form id becomes the
 * prefix for generated field, resource, problem, and help metadata ids. Those
 * ids can appear in tests, diagnostics, resource maps, or persisted state, so
 * changing them should be reviewed as an application compatibility change.</p>
 *
 * <p>Advanced users should treat the generated Java source as an API artifact,
 * not as hidden framework internals. It is intentionally direct: constructors
 * register field models, descriptor lists are explicit, validators are wired by
 * ordinary Java calls, and result creation calls the record constructor or the
 * generated immutable implementation constructor. When a screen needs behavior
 * outside the generated subset, extend it with ordinary code rather than
 * trying to encode application logic in annotations.</p>
 */
package cz.auderis.corusco.annotations.form;
