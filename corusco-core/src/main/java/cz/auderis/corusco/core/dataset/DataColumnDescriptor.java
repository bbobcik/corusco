package cz.auderis.corusco.core.dataset;

import cz.auderis.corusco.annotations.dataset.AggregationFunction;
import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import cz.auderis.corusco.annotations.dataset.MissingPolicy;
import cz.auderis.corusco.annotations.dataset.QualityPolicy;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Immutable semantic descriptor for one fixed-schema data column.
 *
 * <p>A data column descriptor is the generated schema record for one component
 * of a fixed-structure row. The key gives the column a stable identity, the
 * source member name links the descriptor back to the record component used by
 * generated frame accessors, and the role tells adapters how the column
 * participates in time-series or tidy-data operations.</p>
 *
 * <p>The descriptor intentionally avoids presentation concerns. It does not
 * store Swing column width, visibility, persistence ids, editability, resource
 * keys, or renderer choices. Those belong to table descriptors and adapter
 * layers. Keeping the semantic schema separate lets the same generated
 * data-set metadata drive table rendering, exports, aggregation requests, and
 * storage generation without conflating those concerns.</p>
 *
 * <p>Aggregation functions are a capability declaration, not an implementation.
 * A measure column can list functions that request builders may use; adapters
 * still decide how, where, and whether to execute the requested operation.</p>
 *
 * @param key typed column key
 * @param sourceMemberName source record component name
 * @param role semantic column role
 * @param storage physical storage hint
 * @param unit optional unit metadata
 * @param missingPolicy missing-value policy
 * @param qualityPolicy quality policy
 * @param aggregationFunctions allowed aggregation functions
 * @param <R> source row type
 * @param <V> value type
 */
public record DataColumnDescriptor<R, V>(
        DataColumnKey<R, V> key,
        String sourceMemberName,
        DataColumnRole role,
        DataStorageType storage,
        @Nullable UnitMetadata unit,
        MissingPolicy missingPolicy,
        QualityPolicy qualityPolicy,
        Set<AggregationFunction> aggregationFunctions
) {

    /**
     * Creates a descriptor.
     *
     * @param key typed column key
     * @param sourceMemberName source record component name
     * @param role semantic role
     * @param storage storage hint
     * @param unit optional unit metadata, or {@code null} when values are
     *        unitless or the unit is intentionally unspecified
     * @param missingPolicy missing-value policy
     * @param qualityPolicy quality policy
     * @param aggregationFunctions allowed aggregation functions
     */
    public DataColumnDescriptor {
        Objects.requireNonNull(key, "key");
        sourceMemberName = requireMemberName(sourceMemberName);
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(storage, "storage");
        Objects.requireNonNull(missingPolicy, "missingPolicy");
        Objects.requireNonNull(qualityPolicy, "qualityPolicy");
        Objects.requireNonNull(aggregationFunctions, "aggregationFunctions");
        aggregationFunctions = aggregationFunctions.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(aggregationFunctions));
        if (missingPolicy == MissingPolicy.NAN
                && key.valueType() != Float.class
                && key.valueType() != Double.class) {
            throw new IllegalArgumentException("NaN missing policy requires Float or Double value type");
        }
        if (role != DataColumnRole.MEASURE && !aggregationFunctions.isEmpty()) {
            throw new IllegalArgumentException("only measure columns can declare aggregation functions");
        }
    }

    /**
     * Creates a descriptor.
     *
     * @param key typed column key
     * @param sourceMemberName source record component name
     * @param role semantic role
     * @param storage storage hint
     * @param unit optional unit metadata
     * @param missingPolicy missing-value policy
     * @param qualityPolicy quality policy
     * @param aggregationFunctions allowed aggregation functions
     * @param <R> source row type
     * @param <V> value type
     * @return descriptor
     */
    public static <R, V> DataColumnDescriptor<R, V> of(
            DataColumnKey<R, V> key,
            String sourceMemberName,
            DataColumnRole role,
            DataStorageType storage,
            @Nullable UnitMetadata unit,
            MissingPolicy missingPolicy,
            QualityPolicy qualityPolicy,
            Set<AggregationFunction> aggregationFunctions
    ) {
        return new DataColumnDescriptor<>(
                key,
                sourceMemberName,
                role,
                storage,
                unit,
                missingPolicy,
                qualityPolicy,
                aggregationFunctions
        );
    }

    private static String requireMemberName(String name) {
        Objects.requireNonNull(name, "sourceMemberName");
        if (name.isBlank()) {
            throw new IllegalArgumentException("sourceMemberName must not be blank");
        }
        return name;
    }
}
