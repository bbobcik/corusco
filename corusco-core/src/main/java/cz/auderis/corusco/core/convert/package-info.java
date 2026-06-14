/**
 * String conversion primitives for text-oriented form models.
 *
 * <p>This package defines how user-facing text becomes a semantic value and how
 * a semantic value is formatted back to text. Start with
 * {@link cz.auderis.corusco.core.convert.StringConverter}, which is the
 * conversion contract used by
 * {@link cz.auderis.corusco.core.form.TextFieldModel}. A conversion result is
 * represented by {@link cz.auderis.corusco.core.convert.ParseResult}, allowing
 * invalid user input to be reported without overwriting the last valid
 * semantic value.</p>
 *
 * <p>{@link cz.auderis.corusco.core.convert.Converters} provides standard
 * converter factories for common field types. {@link
 * cz.auderis.corusco.core.convert.EmptyTextPolicy} controls whether blank text
 * is accepted, mapped to {@code null}, or treated as a parse failure. These
 * policies are part of the form model contract rather than Swing widget
 * behavior.</p>
 *
 * <p>Converters do not own problem targets, resource lookup, validation timing,
 * or Swing component state. Text field models translate parse failures into
 * typed problems from {@code cz.auderis.corusco.core.problem}; Swing bindings
 * merely move text between components and the model. Keep parsing here so
 * generated forms, tests, and non-Swing presenters all observe the same
 * conversion semantics.</p>
 */
package cz.auderis.corusco.core.convert;
