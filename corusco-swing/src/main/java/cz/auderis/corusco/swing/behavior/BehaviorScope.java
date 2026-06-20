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
 * Installs, orders, validates, and owns view behaviors for one Swing lifecycle.
 *
 * <p>A behavior scope is the runtime counterpart of a generated view plan. Code
 * creates a scope during view or presenter construction, asks it to install the
 * {@link ViewBehavior behaviors} for each component, and closes it when the
 * view is torn down. The scope gives a newcomer one place to understand how
 * Corusco turns generated metadata and handwritten behavior factories into real
 * Swing listeners, bindings, actions, and decorations.</p>
 *
 * <p>Installation is EDT-confined. The scope sorts behaviors by
 * {@link BehaviorPhase}, detects duplicate {@link BehaviorCardinality#SINGLE}
 * keys, prevents multiple primary bindings on the same component, and stores
 * installed keys for tests through {@link #installedBehaviorKeys(JComponent)}.
 * It owns only the {@link Binding} handles returned by behaviors; it does not
 * own the target components, models, resources, or optional {@link HelpService}.
 * Closing the scope is idempotent through {@link BindingScope} and closes
 * installed bindings in reverse registration order.</p>
 *
 * <p>Use one scope for a view/dialog activation, not as a global singleton.
 * Avoid installing competing model bindings directly on the same component
 * outside this scope, because the scope can only validate behaviors it knows
 * about.</p>
 *
 * <p>Generated {@code @CoruscoForm} records create form-specific companions,
 * such as {@code CustomerEditBehaviorPlan} with direct calls to this scope and
 * {@code CustomerEditBindings} as a facade that delegates to that plan.
 * Handwritten views may call {@link #install(JComponent, List)} directly when
 * no generated form companion exists.</p>
 */
public final class BehaviorScope implements Binding {

    private final BindingScope bindings = new BindingScope();
    private final HelpService helpService;
    private final Map<JComponent, Set<BehaviorKey>> installedSingleKeys = new IdentityHashMap<>();
    private final Map<JComponent, List<BehaviorKey>> installedKeys = new IdentityHashMap<>();
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
     * <p>The input list is copied and sorted by phase before any behavior is
     * installed. If a behavior violates cardinality rules, installation stops
     * with {@link BehaviorConflictException}; previously installed behaviors
     * remain owned by this scope and will be closed when the scope closes.</p>
     *
     * @param component target component
     * @param behaviors behaviors to install
     * @param <C> component type
     * @throws IllegalStateException if called off the EDT or after an owned
     *         binding rejects installation
     * @throws BehaviorConflictException if a behavior conflicts with an
     *         already installed behavior on the same component
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
     * Installs behaviors grouped by phase.
     *
     * <p>This method has the same ownership and conflict semantics as
     * {@link #install(JComponent, List)} and additionally returns the installed
     * keys grouped by installation phase. It is useful in tests and generated
     * plans that need to inspect ordering without exposing behavior instances.</p>
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
     * Adds a lifecycle binding that is not tied to one Swing component.
     *
     * <p>Generated behavior plans use this for model-level dependencies whose
     * subscriptions update component-state models. The binding is owned and
     * closed with the same reverse-order lifecycle as component behaviors.</p>
     *
     * @param binding binding to own
     * @param <B> binding type
     * @return the same binding
     */
    public <B extends Binding> B add(B binding) {
        SwingEdt.requireEdt();
        return bindings.add(Objects.requireNonNull(binding, "binding"));
    }

    /**
     * Indicates whether this scope has been closed.
     *
     * @return closed flag
     */
    public boolean isClosed() {
        return bindings.isClosed();
    }

    /**
     * Returns installed behavior keys for a component in installation order.
     *
     * <p>The query must run on the EDT because behavior installation and
     * disposal are UI-lifecycle operations. The returned list is immutable and
     * does not expose behavior instances or disposable handles.</p>
     *
     * @param component component to inspect
     * @return installed behavior keys, or an empty list
     */
    public List<BehaviorKey> installedBehaviorKeys(JComponent component) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        return List.copyOf(installedKeys.getOrDefault(component, List.of()));
    }

    /**
     * Indicates whether a behavior key is currently installed on a component.
     *
     * @param component component to inspect
     * @param key behavior key
     * @return {@code true} if the key is installed
     */
    public boolean hasBehavior(JComponent component, BehaviorKey key) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(key, "key");
        return installedKeys.getOrDefault(component, List.of()).contains(key);
    }

    /**
     * Closes all installed behavior bindings and clears diagnostic state.
     *
     * <p>The call must run on the EDT. Repeated calls are allowed and do not
     * close bindings more than once. The scope no longer reports installed
     * behavior keys after it is closed.</p>
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        bindings.close();
        installedSingleKeys.clear();
        installedKeys.clear();
        primaryBindingComponents.clear();
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
        installedKeys.computeIfAbsent(component, ignored -> new ArrayList<>())
                .add(descriptor.key());
        if (descriptor.conflictsWithPrimaryBinding()) {
            primaryBindingComponents.add(component);
        }
        if (descriptor.cardinality() == BehaviorCardinality.SINGLE) {
            componentKeys.add(descriptor.key());
        }
    }
}
