package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for fixed child-composed form models.
 *
 * <p>The class implements deterministic child aggregation for forms made from
 * multiple independently testable child {@link FormModel} instances. It has no
 * Swing dependency and does not define dirty state; applications compose dirty
 * policies explicitly where dialog actions need them.</p>
 *
 * @param <R> committed result type
 */
public abstract class AbstractCompositeFormModel<R> implements CompositeFormModel<R> {

    private final List<FormModel<?>> children;

    /**
     * Creates a composite form from children in the given order.
     *
     * @param children child form models
     * @throws NullPointerException if the list or any child is {@code null}
     * @throws IllegalArgumentException if the same child instance is registered
     *         more than once
     */
    protected AbstractCompositeFormModel(List<? extends FormModel<?>> children) {
        Objects.requireNonNull(children, "children");
        this.children = List.copyOf(validateChildren(children));
    }

    /**
     * Creates a composite form from children in the given order.
     *
     * @param children child form models
     * @throws NullPointerException if the array or any child is {@code null}
     * @throws IllegalArgumentException if the same child instance is registered
     *         more than once
     */
    protected AbstractCompositeFormModel(FormModel<?>... children) {
        this(Arrays.asList(Objects.requireNonNull(children, "children")));
    }

    @Override
    public final List<? extends FormModel<?>> children() {
        return children;
    }

    @Override
    public ProblemSet problems() {
        ProblemSet result = ProblemSet.empty();
        for (FormModel<?> child : children) {
            result = result.addAll(child.problems());
        }
        return result.addAll(validationProblems());
    }

    @Override
    public boolean isCommittable() {
        return !problems().hasErrors();
    }

    @Override
    public void reset() {
        for (FormModel<?> child : children) {
            child.reset();
        }
        resetParentState();
    }

    @Override
    public void acceptCurrentValues() {
        for (FormModel<?> child : children) {
            child.acceptCurrentValues();
        }
        acceptParentCurrentValues();
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
     * Returns parent-level validation problems after child problems.
     *
     * @return validation problem set
     */
    protected ProblemSet validationProblems() {
        return ProblemSet.empty();
    }

    /**
     * Resets parent-owned non-child state after children reset.
     */
    protected void resetParentState() {
    }

    /**
     * Accepts parent-owned non-child state after child baselines are accepted.
     */
    protected void acceptParentCurrentValues() {
    }

    private static List<FormModel<?>> validateChildren(List<? extends FormModel<?>> children) {
        List<FormModel<?>> result = new ArrayList<>(children.size());
        Map<FormModel<?>, Boolean> seen = new IdentityHashMap<>();
        for (FormModel<?> child : children) {
            FormModel<?> checked = Objects.requireNonNull(child, "child");
            if (seen.put(checked, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("Duplicate child form model");
            }
            result.add(checked);
        }
        return result;
    }
}
