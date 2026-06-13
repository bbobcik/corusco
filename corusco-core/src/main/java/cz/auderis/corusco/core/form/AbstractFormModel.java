package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for handwritten and generated form models.
 *
 * <p>Subclasses register fields in their constructor. This base aggregates
 * parse and validation problems, provides reset and baseline acceptance, and
 * blocks {@link #toResult()} when any aggregated problem has error
 * severity.</p>
 *
 * @param <R> committed result type
 */
public abstract class AbstractFormModel<R> implements FormModel<R> {

    private final List<FieldModel<?, ?>> fields = new ArrayList<>();
    private final List<TextFieldModel<?, ?>> textFields = new ArrayList<>();

    /**
     * Registers a semantic field for reset and baseline acceptance.
     *
     * @param field field to register
     * @param <F> field type
     * @return the same field
     */
    protected final <F extends FieldModel<?, ?>> F register(F field) {
        fields.add(Objects.requireNonNull(field, "field"));
        return field;
    }

    /**
     * Registers a text field for reset, baseline acceptance, and problem
     * aggregation.
     *
     * @param field field to register
     * @param <F> field type
     * @return the same field
     */
    protected final <F extends TextFieldModel<?, ?>> F register(F field) {
        textFields.add(Objects.requireNonNull(field, "field"));
        fields.add(field.semanticField());
        return field;
    }

    @Override
    public ProblemSet problems() {
        ProblemSet result = ProblemSet.empty();
        for (TextFieldModel<?, ?> field : textFields) {
            result = result.addAll(field.problems());
        }
        return result.addAll(validationProblems());
    }

    @Override
    public boolean isCommittable() {
        return !problems().hasErrors();
    }

    @Override
    public void reset() {
        for (TextFieldModel<?, ?> field : textFields) {
            field.reset();
        }
        for (FieldModel<?, ?> field : fields) {
            if (!isOwnedByTextField(field)) {
                field.reset();
            }
        }
    }

    @Override
    public void acceptCurrentValues() {
        for (FieldModel<?, ?> field : fields) {
            field.acceptCurrentValue();
        }
    }

    @Override
    public final R toResult() {
        if (!isCommittable()) {
            throw new UncommittableFormException("Form is not committable");
        }
        return createResult();
    }

    /**
     * Creates a result after committability has been checked.
     *
     * @return committed result
     */
    protected abstract R createResult();

    /**
     * Returns validation problems for this form.
     *
     * <p>Subclasses can override this hook to evaluate a
     * {@link cz.auderis.corusco.core.validation.RuleSet}. Parse problems are
     * aggregated by this base class before these validation problems are added.</p>
     *
     * @return validation problem set
     */
    protected ProblemSet validationProblems() {
        return ProblemSet.empty();
    }

    private boolean isOwnedByTextField(FieldModel<?, ?> candidate) {
        for (TextFieldModel<?, ?> textField : textFields) {
            if (textField.semanticField() == candidate) {
                return true;
            }
        }
        return false;
    }
}
