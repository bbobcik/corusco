package cz.auderis.corusco.swing.behavior;

import java.util.Objects;

/**
 * Metadata describing behavior installation ordering and conflicts.
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
     * Creates a single behavior descriptor.
     *
     * @param key stable behavior key
     * @param phase installation phase
     * @return descriptor
     */
    public static BehaviorDescriptor single(BehaviorKey key, BehaviorPhase phase) {
        return new BehaviorDescriptor(key, phase, BehaviorCardinality.SINGLE, false);
    }

    /**
     * Creates a primary binding descriptor.
     *
     * @param key stable behavior key
     * @return primary binding descriptor
     */
    public static BehaviorDescriptor primaryBinding(BehaviorKey key) {
        return new BehaviorDescriptor(key, BehaviorPhase.BINDING, BehaviorCardinality.SINGLE, true);
    }
}
