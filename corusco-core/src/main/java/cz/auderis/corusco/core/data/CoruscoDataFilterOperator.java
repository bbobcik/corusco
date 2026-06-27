package cz.auderis.corusco.core.data;

/**
 * Transport-neutral filter operator.
 *
 * <p>Operators describe user or presenter intent without choosing a backend
 * syntax. A SQL adapter, REST adapter, or in-memory adapter may support only a
 * subset and should reject unsupported combinations explicitly. Operand counts
 * are validated only where the neutral meaning is unambiguous, such as null
 * checks carrying no values.</p>
 */
public enum CoruscoDataFilterOperator {
    /**
     * Field value equals the single supplied operand.
     */
    EQUALS,
    /**
     * Field value does not equal the single supplied operand.
     */
    NOT_EQUALS,
    /**
     * Field value is strictly less than the single supplied operand.
     */
    LESS_THAN,
    /**
     * Field value is less than or equal to the single supplied operand.
     */
    LESS_THAN_OR_EQUAL,
    /**
     * Field value is strictly greater than the single supplied operand.
     */
    GREATER_THAN,
    /**
     * Field value is greater than or equal to the single supplied operand.
     */
    GREATER_THAN_OR_EQUAL,
    /**
     * Field value contains the single supplied operand according to adapter
     * semantics, typically text containment.
     */
    CONTAINS,
    /**
     * Field value starts with the single supplied operand according to adapter
     * semantics.
     */
    STARTS_WITH,
    /**
     * Field value ends with the single supplied operand according to adapter
     * semantics.
     */
    ENDS_WITH,
    /**
     * Field value is a member of the supplied operand collection.
     */
    IN,
    /**
     * Field value is null. This operator carries no operands.
     */
    IS_NULL,
    /**
     * Field value is not null. This operator carries no operands.
     */
    IS_NOT_NULL
}
