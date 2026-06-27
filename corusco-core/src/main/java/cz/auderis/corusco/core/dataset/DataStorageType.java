package cz.auderis.corusco.core.dataset;

/**
 * Storage hint for generated fixed-schema frames.
 *
 * <p>The value tells generators and adapters which low-level representation is
 * expected for a column when data is materialized into a local frame. It is a
 * hint attached to the descriptor, not an exposed storage API. Implementations
 * may choose a different representation when they have a documented reason,
 * but generated code should generally keep primitive columns in primitive
 * arrays to avoid boxing in dense table or analytics paths.</p>
 */
public enum DataStorageType {

    /**
     * Primitive {@code boolean[]} storage.
     */
    BOOLEAN_ARRAY,

    /**
     * Primitive {@code byte[]} storage.
     */
    BYTE_ARRAY,

    /**
     * Primitive {@code short[]} storage.
     */
    SHORT_ARRAY,

    /**
     * Primitive {@code int[]} storage.
     */
    INT_ARRAY,

    /**
     * Primitive {@code char[]} storage.
     */
    CHAR_ARRAY,

    /**
     * Primitive {@code long[]} storage.
     */
    LONG_ARRAY,

    /**
     * Primitive {@code float[]} storage.
     */
    FLOAT_ARRAY,

    /**
     * Primitive {@code double[]} storage.
     */
    DOUBLE_ARRAY,

    /**
     * Reference-array storage for objects, enums, and other declared types.
     */
    OBJECT_ARRAY
}
