package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemTarget;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
 * or use {@link #fieldTargets(Map)}, {@link #componentTargets(Map)}, and
 * {@link #firstOf(ProblemFocusResolver...)}. A resolver can also prepare the
 * owning view, for example by selecting a tab, through {@link
 * #withPreparation(Runnable, ProblemFocusResolver)}. No reflection, JavaBeans
 * property names, localized labels, or table view indexes are required.</p>
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
     * Creates a resolver for field-targeted problems.
     *
     * <p>The supplied map is copied. Text field keys can be registered through
     * {@link cz.auderis.corusco.core.key.TextFieldKey#asFieldKey()}.</p>
     *
     * @param components components keyed by typed field key
     * @return field-target resolver
     */
    static ProblemFocusResolver fieldTargets(Map<? extends FieldKey<?, ?>, ? extends JComponent> components) {
        Objects.requireNonNull(components, "components");
        Map<FieldKey<?, ?>, JComponent> copy = copyFieldTargets(components);
        return problem -> {
            Objects.requireNonNull(problem, "problem");
            if (problem.target() instanceof ProblemTarget.Field<?, ?> fieldTarget) {
                return Optional.ofNullable(copy.get(fieldTarget.key()));
            }
            return Optional.empty();
        };
    }

    /**
     * Creates a resolver for component-targeted problems.
     *
     * <p>The supplied map is copied. Later map changes are not observed, while
     * the component instances themselves are retained and returned as-is.</p>
     *
     * @param components components keyed by typed component key
     * @return component-target resolver
     */
    static ProblemFocusResolver componentTargets(Map<? extends ComponentKey<?>, ? extends JComponent> components) {
        Objects.requireNonNull(components, "components");
        Map<ComponentKey<?>, JComponent> copy = copyComponentTargets(components);
        return problem -> {
            Objects.requireNonNull(problem, "problem");
            if (problem.target() instanceof ProblemTarget.Component<?> componentTarget) {
                return Optional.ofNullable(copy.get(componentTarget.key()));
            }
            return Optional.empty();
        };
    }

    /**
     * Creates a resolver that tries delegates in order.
     *
     * @param resolvers ordered resolvers
     * @return first-match resolver
     */
    static ProblemFocusResolver firstOf(ProblemFocusResolver... resolvers) {
        Objects.requireNonNull(resolvers, "resolvers");
        return firstOf(Arrays.asList(resolvers));
    }

    /**
     * Creates a resolver that tries delegates in order.
     *
     * @param resolvers ordered resolvers
     * @return first-match resolver
     */
    static ProblemFocusResolver firstOf(Collection<? extends ProblemFocusResolver> resolvers) {
        Objects.requireNonNull(resolvers, "resolvers");
        List<ProblemFocusResolver> copy = resolvers.stream()
                .map(resolver -> Objects.requireNonNull(resolver, "resolver"))
                .toList();
        return problem -> {
            Objects.requireNonNull(problem, "problem");
            for (ProblemFocusResolver resolver : copy) {
                Optional<JComponent> component = resolver.resolve(problem);
                if (component.isPresent()) {
                    return component;
                }
            }
            return Optional.empty();
        };
    }

    /**
     * Wraps a resolver with preparation work before returning a focus target.
     *
     * <p>The preparation runs only when the delegate resolves the problem. It
     * is intended for presentation work such as selecting the tab or expanding
     * the section that owns the resolved component before focus is requested.</p>
     *
     * @param preparation preparation work
     * @param delegate resolver to wrap
     * @return preparing resolver
     */
    static ProblemFocusResolver withPreparation(Runnable preparation, ProblemFocusResolver delegate) {
        Objects.requireNonNull(preparation, "preparation");
        Objects.requireNonNull(delegate, "delegate");
        return problem -> {
            Optional<JComponent> component = delegate.resolve(problem);
            component.ifPresent(ignored -> preparation.run());
            return component;
        };
    }

    private static Map<FieldKey<?, ?>, JComponent> copyFieldTargets(
            Map<? extends FieldKey<?, ?>, ? extends JComponent> components
    ) {
        Map<FieldKey<?, ?>, JComponent> copy = new LinkedHashMap<>();
        components.forEach((key, component) ->
                copy.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(component, "component")));
        return Map.copyOf(copy);
    }

    private static Map<ComponentKey<?>, JComponent> copyComponentTargets(
            Map<? extends ComponentKey<?>, ? extends JComponent> components
    ) {
        Map<ComponentKey<?>, JComponent> copy = new LinkedHashMap<>();
        components.forEach((key, component) ->
                copy.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(component, "component")));
        return Map.copyOf(copy);
    }
}
