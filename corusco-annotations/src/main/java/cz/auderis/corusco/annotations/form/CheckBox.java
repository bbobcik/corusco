package cz.auderis.corusco.annotations.form;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.command.UiAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a boolean form source member as a generated checkbox field.
 *
 * <p>Use this annotation on a component of a {@link CoruscoForm} record or on an
 * abstract accessor method of a {@link CoruscoForm} abstract class when the value
 * should be edited as a selected/not-selected state. The processor accepts
 * primitive {@code boolean} and {@link java.lang.Boolean} values, emits typed
 * field and descriptor metadata, and adds a Swing {@code JCheckBox} entry to
 * generated view plans.</p>
 *
 * <p>Checkbox fields are value fields, not command toggles. Use {@link
 * UiAction#selectable()} for generated toggle actions. Text-only constraints
 * such as {@link Length}, {@link Regex}, and numeric ranges are not meaningful
 * for checkboxes. {@link Help} can provide tooltip/help metadata.</p>
 *
 * <p>Non-boolean components are rejected by the processor so generated
 * bindings remain type-correct. Runtime code should consume the generated
 * descriptors and field models rather than scanning this annotation.</p>
 *
 * <p>The generated fields companion, for example {@code CustomerEditFields}, exposes a
 * {@code cz.auderis.corusco.core.key.FieldKey} constant for each
 * {@code @CheckBox} component. Generated labels and tooltips use
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants in
 * resources companions such as {@code CustomerEditResources}.</p>
 *
 * <p>The generated form model, for example {@code CustomerEditFormModel}, owns a
 * {@code cz.auderis.corusco.core.form.FieldModel} for each checkbox. The
 * generated descriptor is a
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor} with checkbox field
 * kind, and generated behavior plans install checkbox binding behavior.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface CheckBox {
}
