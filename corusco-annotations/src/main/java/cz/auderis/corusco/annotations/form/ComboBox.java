package cz.auderis.corusco.annotations.form;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.validation.Required;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a form source member as a generated combo-box field.
 *
 * <p>Use this annotation on a component of a {@link CoruscoForm} record or on an
 * abstract accessor method of a {@link CoruscoForm} abstract class when the value
 * should be edited through a selection control. The processor emits typed field
 * keys, descriptor metadata, and a Swing {@code JComboBox<Value>} entry in
 * generated view plans. The component type becomes the value type used by
 * generated field models and bindings.</p>
 *
 * <p>For enum-valued components the processor can also generate ordered option
 * metadata from the enum constants. For non-enum components the annotation
 * identifies the editor family only; available options, renderer, and loading
 * policy remain view/presenter responsibilities. {@link Required} and
 * {@link Help} are meaningful combinations. Text-only constraints such as
 * {@link Length} or {@link Regex} are not meaningful for combo boxes and
 * should not be used.</p>
 *
 * <p>Generated descriptors keep the combo-box field tied to stable field and
 * resource ids from the enclosing form. Runtime code should consume that
 * generated metadata rather than scanning this annotation reflectively.</p>
 *
 * <p>The generated fields companion, for example {@code CustomerEditFields}, exposes a
 * {@code cz.auderis.corusco.core.key.FieldKey} constant for each
 * {@code @ComboBox} component. Generated labels and tooltips use
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants in
 * resources companions such as {@code CustomerEditResources}. Enum-valued combo
 * boxes may also expose option lists in companions such as
 * {@code CustomerEditOptions}; those option lists are separate from key
 * identity.</p>
 *
 * <p>The generated form model, for example {@code CustomerEditFormModel}, owns a
 * {@code cz.auderis.corusco.core.form.FieldModel} for each combo box. The
 * generated descriptor is a
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor} with combo-box field
 * kind.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface ComboBox {

    /**
     * Whether enum-valued combo boxes should expose generated option metadata.
     *
     * <p>This flag has no effect for non-enum component types. Their option
     * sources are application-owned because the processor cannot know loading,
     * localization, disabled-option, or ordering policy beyond enum declaration
     * order.</p>
     *
     * @return enum option metadata flag
     */
    boolean enumOptions() default true;
}
