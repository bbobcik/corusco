/**
 * Form-level annotations consumed by the Corusco annotation processor.
 *
 * <p>Start with {@link cz.auderis.corusco.annotations.form.SwingForm} on a
 * non-generic record that represents editable input. Then annotate each
 * generated component with exactly one field-kind annotation:
 * {@link cz.auderis.corusco.annotations.form.TextField},
 * {@link cz.auderis.corusco.annotations.form.DateField},
 * {@link cz.auderis.corusco.annotations.form.ComboBox}, or
 * {@link cz.auderis.corusco.annotations.form.CheckBox}. The processor turns
 * those components into typed field keys, field descriptors, generated form
 * models, and view contracts.</p>
 *
 * <p>Form annotations describe the source shape of generated UI metadata; they
 * do not create Swing components directly and they are not available at
 * runtime. Runtime code should work with generated descriptors, form models,
 * and binding helpers.</p>
 */
package cz.auderis.corusco.annotations.form;
