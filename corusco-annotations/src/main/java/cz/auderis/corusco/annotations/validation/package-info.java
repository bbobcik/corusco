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
 * <p>Use these annotations for simple, local field constraints that belong in
 * generated metadata. Cross-field validation, asynchronous validation, and
 * business decisions should remain in ordinary Java validation code.</p>
 */
package cz.auderis.corusco.annotations.validation;
