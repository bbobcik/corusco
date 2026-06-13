package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.help.HelpService;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.JComponent;

/**
 * Installs and owns behaviors for one view lifecycle.
 *
 * <p>Behaviors are sorted by {@link BehaviorPhase} before installation.
 * Installed handles are closed in reverse installation order through
 * {@link BindingScope}. Duplicate single-cardinality keys and multiple primary
 * binding behaviors fail fast per component.</p>
 */
public final class BehaviorScope implements Binding {

    private final BindingScope bindings = new BindingScope();
    private final HelpService helpService;
    private final Map<JComponent, Set<BehaviorKey>> installedSingleKeys = new IdentityHashMap<>();
    private final Set<JComponent> primaryBindingComponents = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Creates a behavior scope without optional application services.
     */
    public BehaviorScope() {
        this.helpService = null;
    }

    /**
     * Creates a behavior scope with a help service available to help behaviors.
     *
     * @param helpService help service
     */
    public BehaviorScope(HelpService helpService) {
        this.helpService = Objects.requireNonNull(helpService, "helpService");
    }

    /**
     * Installs behaviors on a component.
     *
     * @param component target component
     * @param behaviors behaviors to install
     * @param <C> component type
     */
    public <C extends JComponent> void install(C component, List<? extends ViewBehavior<? super C>> behaviors) {
        SwingEdt.requireEdt();
        List<ViewBehavior<? super C>> ordered = new ArrayList<>(behaviors);
        ordered.sort(Comparator.comparing(behavior -> behavior.descriptor().phase()));
        for (ViewBehavior<? super C> behavior : ordered) {
            installOne(component, behavior);
        }
    }

    /**
     * Installs behaviors grouped by phase. Useful in tests and generated plans
     * that need to inspect ordering.
     *
     * @param component target component
     * @param behaviors behaviors to install
     * @param <C> component type
     * @return installed behavior keys by phase
     */
    public <C extends JComponent> EnumMap<BehaviorPhase, List<BehaviorKey>> installTracked(
            C component,
            List<? extends ViewBehavior<? super C>> behaviors
    ) {
        SwingEdt.requireEdt();
        EnumMap<BehaviorPhase, List<BehaviorKey>> installed = new EnumMap<>(BehaviorPhase.class);
        List<ViewBehavior<? super C>> ordered = new ArrayList<>(behaviors);
        ordered.sort(Comparator.comparing(behavior -> behavior.descriptor().phase()));
        for (ViewBehavior<? super C> behavior : ordered) {
            installOne(component, behavior);
            installed.computeIfAbsent(behavior.descriptor().phase(), phase -> new ArrayList<>())
                    .add(behavior.descriptor().key());
        }
        return installed;
    }

    /**
     * Indicates whether this scope is closed.
     *
     * @return closed flag
     */
    public boolean isClosed() {
        return bindings.isClosed();
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        bindings.close();
    }

    private <C extends JComponent> void installOne(C component, ViewBehavior<? super C> behavior) {
        BehaviorDescriptor descriptor = behavior.descriptor();
        if (descriptor.conflictsWithPrimaryBinding()) {
            if (primaryBindingComponents.contains(component)) {
                throw new BehaviorConflictException("Primary binding behavior already installed");
            }
        }
        Set<BehaviorKey> componentKeys = installedSingleKeys.computeIfAbsent(component, ignored -> new java.util.HashSet<>());
        if (descriptor.cardinality() == BehaviorCardinality.SINGLE && componentKeys.contains(descriptor.key())) {
            throw new BehaviorConflictException("Behavior already installed: " + descriptor.key());
        }
        @SuppressWarnings("unchecked")
        ViewBehavior<C> typedBehavior = (ViewBehavior<C>) behavior;
        Binding installed = typedBehavior.install(new BehaviorContext<>(component, this, helpService));
        bindings.add(installed);
        if (descriptor.conflictsWithPrimaryBinding()) {
            primaryBindingComponents.add(component);
        }
        if (descriptor.cardinality() == BehaviorCardinality.SINGLE) {
            componentKeys.add(descriptor.key());
        }
    }
}
