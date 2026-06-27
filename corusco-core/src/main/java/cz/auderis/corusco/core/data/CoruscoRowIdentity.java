package cz.auderis.corusco.core.data;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;

/**
 * Stable key extractor for rows in a windowed data model.
 *
 * <p>Row identity names the durable key used by selection, edit staging,
 * master-detail routing, and refresh continuity. It is intentionally separate
 * from {@link cz.auderis.corusco.core.table.TableDescriptor}: a table
 * descriptor explains columns, while row identity explains which row is which
 * across sorting, filtering, replacement rows, and virtual pages.</p>
 *
 * <p>The extractor must return a non-null key for every row accepted by the
 * data source or edit session. The key should be stable for the logical row,
 * not for the current Java object identity. A database id, business id, or
 * client-side temporary id can all be valid as long as the application uses
 * them consistently.</p>
 *
 * @param rowType concrete row type
 * @param keyType concrete key type
 * @param keyExtractor extractor that returns the stable key for a row
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoRowIdentity<R extends @NonNull Object, K extends @NonNull Object>(
        Class<R> rowType,
        Class<K> keyType,
        Function<? super R, ? extends K> keyExtractor
) {

    /**
     * Creates a row identity descriptor.
     *
     * <p>The type metadata is runtime documentation and validation support for
     * bridges such as {@link CoruscoTableDataDescriptor}. The key extractor is
     * kept as supplied and invoked only by {@link #keyOf(Object)}.</p>
     *
     * @param rowType concrete row type
     * @param keyType concrete key type
     * @param keyExtractor key extractor
     */
    public CoruscoRowIdentity {
        Objects.requireNonNull(rowType, "rowType");
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(keyExtractor, "keyExtractor");
    }

    /**
     * Creates a row identity descriptor.
     *
     * <p>This factory keeps call sites compact when row and key classes are
     * available as class literals.</p>
     *
     * @param rowType concrete row type
     * @param keyType concrete key type
     * @param keyExtractor key extractor
     * @param <R> row type
     * @param <K> key type
     * @return row identity descriptor
     */
    public static <R extends @NonNull Object, K extends @NonNull Object> CoruscoRowIdentity<R, K> of(
            Class<R> rowType,
            Class<K> keyType,
            Function<? super R, ? extends K> keyExtractor
    ) {
        return new CoruscoRowIdentity<>(rowType, keyType, keyExtractor);
    }

    /**
     * Extracts a row key.
     *
     * <p>The row and extracted key must both be non-null. A null key indicates
     * a broken identity contract and is rejected immediately rather than being
     * allowed into selection or edit maps.</p>
     *
     * @param row row
     * @return stable row key
     */
    public K keyOf(R row) {
        K key = keyExtractor.apply(Objects.requireNonNull(row, "row"));
        return Objects.requireNonNull(key, "keyExtractor result");
    }
}
