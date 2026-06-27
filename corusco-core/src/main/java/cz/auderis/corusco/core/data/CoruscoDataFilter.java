package cz.auderis.corusco.core.data;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One transport-neutral query filter.
 *
 * <p>A filter names a stable field or column id, an operator, and immutable
 * operand values. The record deliberately does not know whether the field id
 * will become a SQL column, REST parameter, in-memory property, or service
 * query attribute. Adapter modules own that translation and may reject ids or
 * operators they do not support.</p>
 *
 * <p>Values may contain {@code null} because transports differ in how they
 * represent null operands. The built-in null-check operators
 * {@link CoruscoDataFilterOperator#IS_NULL} and
 * {@link CoruscoDataFilterOperator#IS_NOT_NULL} are the exception: they carry
 * no operand values because the operator itself expresses the null test.</p>
 *
 * @param fieldId stable field or column id
 * @param operator filter operator
 * @param values immutable operands
 */
public record CoruscoDataFilter(String fieldId, CoruscoDataFilterOperator operator, List<@Nullable Object> values) {

    /**
     * Creates a filter.
     *
     * <p>The field id must be non-blank, the operator must be present, and the
     * value list is copied. The constructor validates only transport-neutral
     * shape. It does not check whether a specific backend supports this field,
     * this operator, or the runtime classes of the operands.</p>
     *
     * @param fieldId stable field or column id
     * @param operator operator
     * @param values operands
     */
    public CoruscoDataFilter {
        fieldId = requireId(fieldId, "fieldId");
        Objects.requireNonNull(operator, "operator");
        values = List.copyOf(Objects.requireNonNull(values, "values"));
        if ((operator == CoruscoDataFilterOperator.IS_NULL || operator == CoruscoDataFilterOperator.IS_NOT_NULL)
                && !values.isEmpty()) {
            throw new IllegalArgumentException("null-check filters must not have values");
        }
    }

    static String requireId(String id, String name) {
        Objects.requireNonNull(id, name);
        if (id.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return id;
    }
}
