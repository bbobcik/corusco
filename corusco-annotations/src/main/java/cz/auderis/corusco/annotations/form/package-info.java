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
 * <p>Generated form key instances live in generated companions.
 * {@code <Form>Fields} exposes
 * {@code cz.auderis.corusco.core.key.TextFieldKey} or
 * {@code cz.auderis.corusco.core.key.FieldKey} constants.
 * {@code <Form>Resources} exposes
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants for labels
 * and tooltips. Help topics from {@code @Help} are embedded in descriptors as
 * {@code cz.auderis.corusco.core.key.HelpTopic} values.</p>
 *
 * <p>Generated forms also expose
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor},
 * {@code cz.auderis.corusco.core.problem.ProblemCode},
 * {@code cz.auderis.corusco.core.form.FieldModel}, and
 * {@code cz.auderis.corusco.core.form.TextFieldModel} based runtime objects
 * through {@code <Form>Descriptors}, {@code <Form>Problems}, and
 * {@code <Form>FormModel}. Swing behavior installation is generated in
 * {@code <Form>BehaviorPlan} and surfaced through {@code <Form>Bindings}.</p>
 *
 * <p>Form annotations describe the source shape of generated UI metadata; they
 * do not create Swing components directly and they are not available at
 * runtime. Runtime code should work with generated descriptors, form models,
 * and binding helpers.</p>
 */
package cz.auderis.corusco.annotations.form;
