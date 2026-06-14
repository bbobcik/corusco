package cz.auderis.corusco.swing.behavior;

import java.util.Objects;

/**
 * Immutable metadata used by {@link BehaviorScope} to order and validate a behavior.
 *
 * <p>Every {@link ViewBehavior} exposes one descriptor before installation.
 * The scope sorts descriptors by {@link BehaviorPhase}, uses the
 * {@link BehaviorKey} for diagnostics and duplicate checks, and applies
 * {@link BehaviorCardinality} plus the primary-binding flag to prevent
 * incompatible component responsibilities from being installed together.</p>
 *
 * <p>The descriptor does not own or install Swing state. It is safe to reuse a
 * descriptor across behavior instances as long as the key, phase, and
 * cardinality accurately describe the behavior's public lifecycle contract.
 * Stable keys are important for generated plans and tests, but descriptors are
 * not persisted user preferences.</p>
 *
 * @param key stable behavior key
 * @param phase installation phase
 * @param cardinality cardinality rule for this key
 * @param conflictsWithPrimaryBinding whether this behavior conflicts with
 *        other primary binding behaviors
 */
public record BehaviorDescriptor(
        BehaviorKey key,
        BehaviorPhase phase,
        BehaviorCardinality cardinality,
        boolean conflictsWithPrimaryBinding
) {

    /**
     * Creates a behavior descriptor.
     *
     * @param key stable behavior key
     * @param phase installation phase
     * @param cardinality cardinality rule
     * @param conflictsWithPrimaryBinding primary-binding conflict flag
     */
    public BehaviorDescriptor {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(cardinality, "cardinality");
    }

    /**
     * Creates a descriptor for a behavior that may appear at most once per component.
     *
     * @param key stable behavior key
     * @param phase installation phase
     * @return descriptor
     */
    public static BehaviorDescriptor single(BehaviorKey key, BehaviorPhase phase) {
        return new BehaviorDescriptor(key, phase, BehaviorCardinality.SINGLE, false);
    }

    /**
     * Creates a descriptor for a behavior that may appear multiple times per component.
     *
     * @param key stable behavior key
     * @param phase installation phase
     * @return descriptor
     */
    public static BehaviorDescriptor multiple(BehaviorKey key, BehaviorPhase phase) {
        return new BehaviorDescriptor(key, phase, BehaviorCardinality.MULTIPLE, false);
    }

    /**
     * Creates a descriptor for the primary model/component binding behavior.
     *
     * <p>Primary binding descriptors are installed in the binding phase, use
     * single cardinality, and conflict with any other behavior that claims the
     * primary binding slot for the same component.</p>
     *
     * @param key stable behavior key
     * @return primary binding descriptor
     */
    public static BehaviorDescriptor primaryBinding(BehaviorKey key) {
        return new BehaviorDescriptor(key, BehaviorPhase.BINDING, BehaviorCardinality.SINGLE, true);
    }
}
