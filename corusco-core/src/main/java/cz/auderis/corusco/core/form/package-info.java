/**
 * Non-Swing field and form model primitives.
 *
 * <p>This package separates editable presentation state from committed domain
 * values. Start with {@link cz.auderis.corusco.core.form.FieldModel} for a
 * typed field value and {@link cz.auderis.corusco.core.form.TextFieldModel}
 * for text input that must preserve raw text, parse state, semantic values,
 * dirty state, and touched state separately. This separation lets a user
 * temporarily type invalid input without corrupting the last valid value.</p>
 *
 * <p>{@link cz.auderis.corusco.core.form.FormModel} is the aggregate contract
 * for validation problems, committability, reset, baseline acceptance, and
 * result creation. Handwritten forms usually extend
 * {@link cz.auderis.corusco.core.form.AbstractFormModel}; generated forms are
 * expected to follow the same contract. {@link
 * cz.auderis.corusco.core.form.UncommittableFormException} protects the result
 * contract when blocking errors are present.</p>
 *
 * <p>{@link cz.auderis.corusco.core.form.ParseState} records text conversion
 * state produced by converters from {@code cz.auderis.corusco.core.convert}.
 * Validation rules from {@code cz.auderis.corusco.core.validation} add semantic
 * problems after parsing succeeds. Problems are carried by
 * {@code cz.auderis.corusco.core.problem} so Swing, tests, and non-Swing code
 * can present them differently.</p>
 *
 * <p>This package does not know about Swing editors, focus, document events,
 * or dialog buttons. UI bindings are responsible for propagating component
 * changes into these models and for observing problems and dirty state.
 * Generated metadata from {@code cz.auderis.corusco.core.meta} can describe the
 * fields, but current field values live here.</p>
 */
package cz.auderis.corusco.core.form;
