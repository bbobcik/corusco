package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

/**
 * Installs and owns behaviors for one view lifecycle.
 *
 * <p>Behaviors are sorted by {@link BehaviorPhase} before installation.
 * Installed handles are closed in reverse installation order through
 * {@link BindingScope}. Duplicate single-cardinality keys and multiple primary
 * binding behaviors fail fast.</p>
 */
public final class BehaviorScope implements Binding {

    private final BindingScope bindings = new BindingScope();
    private final Set<BehaviorKey> installedSingleKeys = new HashSet<>();
    private boolean primaryBindingInstalled;

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
            if (primaryBindingInstalled) {
                throw new BehaviorConflictException("Primary binding behavior already installed");
            }
        }
        if (descriptor.cardinality() == BehaviorCardinality.SINGLE && installedSingleKeys.contains(descriptor.key())) {
            throw new BehaviorConflictException("Behavior already installed: " + descriptor.key());
        }
        @SuppressWarnings("unchecked")
        ViewBehavior<C> typedBehavior = (ViewBehavior<C>) behavior;
        Binding installed = typedBehavior.install(new BehaviorContext<>(component, this));
        bindings.add(installed);
        if (descriptor.conflictsWithPrimaryBinding()) {
            primaryBindingInstalled = true;
        }
        if (descriptor.cardinality() == BehaviorCardinality.SINGLE) {
            installedSingleKeys.add(descriptor.key());
        }
    }
}
