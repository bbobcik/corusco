package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemTarget;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * Resolves a validation problem to a Swing component that can receive focus.
 *
 * <p>The resolver keeps problem-to-component mapping explicit. Generated dialog
 * code can switch on typed field or component keys; hand-written code can pass
 * a lambda. No reflection, JavaBeans names, or property paths are required.</p>
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
     * @param problem problem to resolve
     * @return component to focus
     */
    Optional<JComponent> resolve(Problem problem);

    /**
     * Creates a resolver for component-targeted problems.
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
