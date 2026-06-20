package cz.auderis.corusco.annotations.validation;

import cz.auderis.corusco.annotations.form.CheckBox;
import cz.auderis.corusco.annotations.form.ComboBox;
import cz.auderis.corusco.annotations.form.DateField;
import cz.auderis.corusco.annotations.form.TextField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a generated field as required.
 *
 * <p>Use this annotation on a record component or abstract accessor method that
 * also declares a Corusco field kind such as {@link TextField},
 * {@link DateField}, {@link ComboBox}, or {@link CheckBox}. The processor emits
 * declarative constraint metadata and a generated validator/problem code for
 * the field. For text fields, required means non-null and non-blank; for other
 * field kinds, it means a non-null semantic value where that distinction
 * applies.</p>
 *
 * <p>The annotation does not make the Java record component non-null and does
 * not change constructor behavior. It describes generated form validation.
 * Applying it to a component that is not part of generated form metadata is
 * invalid or meaningless and should be reported by the processor where the
 * surrounding source is processed.</p>
 *
 * <p>The generated form model attaches this rule to the generated field key
 * from a fields companion such as {@code CustomerEditFields}. The generated
 * problem-code companion, for example {@code CustomerEditProblems},
 * exposes the problem code used by the corresponding descriptor and validator.
 * Field keys are instances of {@code cz.auderis.corusco.core.key.FieldKey} or
 * {@code cz.auderis.corusco.core.key.TextFieldKey}, depending on the field
 * kind annotation.</p>
 *
 * <p>Generated descriptors represent this rule with
 * {@code cz.auderis.corusco.core.meta.ConstraintDescriptor}; generated
 * validators are wired in a generated form model such as
 * {@code CustomerEditFormModel}.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface Required {
}
