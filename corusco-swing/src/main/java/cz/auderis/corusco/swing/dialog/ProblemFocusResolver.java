package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemTarget;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Resolves a validation problem to the Swing component that should receive
 * focus.
 *
 * <p>This interface is the bridge between toolkit-neutral problem targets and
 * concrete dialog components. A {@link FormDialogValidationBinding} can ask the
 * resolver for a component after validation fails and then request focus on
 * that component. The resolver does not perform validation and does not decide
 * which problem is most important; it only maps one problem to an optional
 * focus target.</p>
 *
 * <p>The mapping is intentionally explicit. Generated dialog code can map typed
 * field or component keys to accessors, and handwritten code can pass a lambda
 * or use {@link #componentTargets(Map)}. No reflection, JavaBeans property
 * names, localized labels, or table view indexes are required.</p>
 *
 * <p>Resolvers are normally called on the Event Dispatch Thread as part of
 * Swing validation presentation. They may retain component references for as
 * long as the owning validation binding is installed.</p>
 */
@FunctionalInterface
public interface ProblemFocusResolver {

    /**
     * Resolver that never finds a focus target.
     */
    ProblemFocusResolver NONE = problem -> Optional.empty();

    /**
     * Finds the component that should receive focus for a problem.
     *
     * @param problem problem to resolve, not {@code null}
     * @return component to focus, or an empty optional when this resolver does
     *         not know the target
     */
    Optional<JComponent> resolve(Problem problem);

    /**
     * Creates a resolver for component-targeted problems.
     *
     * <p>The supplied map is copied. Later map changes are not observed, while
     * the component instances themselves are retained and returned as-is.</p>
     *
     * @param components components keyed by typed component key
     * @return component-target resolver
     */
    static ProblemFocusResolver componentTargets(Map<ComponentKey<?>, ? extends JComponent> components) {
        Objects.requireNonNull(components, "components");
        Map<ComponentKey<?>, ? extends JComponent> copy = Map.copyOf(components);
        return problem -> {
            Objects.requireNonNull(problem, "problem");
            if (problem.target() instanceof ProblemTarget.Component<?> componentTarget) {
                return Optional.ofNullable(copy.get(componentTarget.key()));
            }
            return Optional.empty();
        };
    }
}
