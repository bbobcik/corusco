/**
 * Non-Swing field and form model primitives.
 *
 * <p>This package solves the editable-form state problem independently of
 * Swing. A user can type invalid text, clear an optional field, touch a field
 * without changing it, reset a form, accept a new baseline, or attempt to
 * commit a result. Those states are not the same as the immutable domain
 * record, and they should not be stored in Swing components. Form models keep
 * the editable state explicit and testable.</p>
 *
 * <p>Use this package when a screen edits a value transactionally: dialogs,
 * detail panes, wizard pages, search forms, import correction forms, and any
 * workflow where OK/Apply/Cancel semantics matter. Use {@link
 * cz.auderis.corusco.core.form.FieldModel} for semantic values such as booleans
 * or selected enum values. Use {@link
 * cz.auderis.corusco.core.form.TextFieldModel} for text entry that must
 * preserve raw text, parse state, semantic values, dirty state, and touched
 * state separately.</p>
 *
 * <p>The first step for a handwritten form is to extend {@link
 * cz.auderis.corusco.core.form.AbstractFormModel}, register fields in the
 * constructor, and implement result creation:</p>
 *
 * <pre>{@code
 * final class CustomerForm extends AbstractFormModel<Customer> {
 *     final TextFieldModel<Customer, String> name =
 *             register(new TextFieldModel<>(NAME, original.name(), Converters.string()));
 *
 *     @Override
 *     protected Customer createResult() {
 *         return new Customer(name.value());
 *     }
 * }
 * }</pre>
 *
 * <p>Generated {@code @CoruscoForm} records create an equivalent form-model
 * subclass, for example {@code CustomerEditFormModel}. The generated model registers
 * {@code TextFieldModel} instances for text/date fields, {@code FieldModel}
 * instances for checkboxes and combo boxes, exposes generated descriptors,
 * wires generated validation rules, and creates the immutable record result.
 * Generated presentation models, not form models, expose {@link
 * cz.auderis.corusco.core.form.ComponentStateModel} members for field controls,
 * sections, tabs, and other UI regions that need presentational state.
 * Handwritten and generated models share the same {@link
 * cz.auderis.corusco.core.form.FormModel} contract.</p>
 *
 * <p>{@link cz.auderis.corusco.core.form.FormModel} aggregates validation
 * problems, reports committability, resets fields to their baseline, accepts
 * current values as a new baseline, and creates a committed result. {@link
 * cz.auderis.corusco.core.form.UncommittableFormException} protects the result
 * contract when blocking errors are present.</p>
 *
 * <p>Use {@link cz.auderis.corusco.core.form.CompositeFormModel} and {@link
 * cz.auderis.corusco.core.form.AbstractCompositeFormModel} when a dialog or
 * workflow is made from several child form models. Composite forms register a
 * fixed ordered child list, aggregate child problems deterministically before
 * parent cross-form validation, delegate reset and baseline acceptance to every
 * child, and still expose one {@code FormModel} result boundary. The composite
 * layer is semantic only; it does not depend on Swing views, generated
 * bindings, or dialog buttons.</p>
 *
 * <p>Think of the baseline as the form's current committed starting point, not
 * necessarily as the original object loaded from storage. A dialog that
 * successfully applies changes may accept the current values as the new
 * baseline and remain open. A cancelled dialog usually resets to the baseline
 * or discards the form. This distinction lets long-lived editors support Apply,
 * Revert, and Close behavior without inventing a parallel dirty-tracking
 * scheme.</p>
 *
 * <p>Dirty and touched state are intentionally separate. Dirty state answers
 * whether the current value differs from the baseline. Touched state answers
 * whether the user interacted with the field, even if the final value is the
 * same. Validation timing, inline messages, and accessibility announcements
 * often need touched state so a new form does not start by presenting every
 * possible warning before the user has done anything.</p>
 *
 * <p>{@link cz.auderis.corusco.core.form.ParseState} records text conversion
 * state produced by converters from {@code cz.auderis.corusco.core.convert}.
 * Validation rules from {@code cz.auderis.corusco.core.validation} add semantic
 * problems after parsing succeeds. Problems are carried by
 * {@code cz.auderis.corusco.core.problem} so Swing, tests, and non-Swing code
 * can present them differently.</p>
 *
 * <p>Choose the field type according to the user's input surface. A text field
 * should normally remain a text field model even if the semantic value is a
 * date, number, or enum, because the user may temporarily type incomplete text.
 * A checkbox, radio selection, or already-typed combo-box value can use a
 * plain field model because there is no independent raw text to preserve.
 * Avoid reading Swing component text directly during commit; let the binding
 * update the field model first.</p>
 *
 * <p>Nullable domain values and optional fields should be modeled deliberately.
 * The form should distinguish an empty text field, a parse failure, an absent
 * optional semantic value, and a present semantic value. That clarity matters
 * for generated validators as well as for handwritten rules, because a
 * required constraint and a range constraint usually need different problem
 * codes and different user messages.</p>
 *
 * <p>Tests for form models should not require Swing. Construct the model,
 * mutate field values, assert parse state, assert problem sets, check dirty and
 * touched state, call reset or baseline acceptance, and verify {@code toResult()}
 * only succeeds when the model is committable. Generated form tests should use
 * the generated model and descriptors rather than duplicating field ids or
 * problem ids as string literals.</p>
 *
 * <p>This package does not know about Swing editors, focus, document events,
 * or dialog buttons. UI bindings propagate component changes into these models
 * and observe problems and dirty state. Dialog controllers decide when to
 * commit active editors, call {@code toResult()}, accept baselines, or reset
 * values. Generated metadata from {@code cz.auderis.corusco.core.meta}
 * describes fields, options, and dependencies; current editable values and
 * component state live here.</p>
 */
package cz.auderis.corusco.core.form;
