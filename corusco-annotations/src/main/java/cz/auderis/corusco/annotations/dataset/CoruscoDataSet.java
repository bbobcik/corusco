package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as a fixed-schema Corusco data-set source.
 *
 * <p>The generated data-set companion is semantic metadata, not Swing table
 * presentation. Annotated record components use role annotations such as
 * {@link TimeAxis}, {@link Dimension}, {@link Measure}, {@link QualityColumn},
 * or {@link DataColumn}. The processor emits a typed data-set key, column
 * descriptors, an ordered descriptor, and generated storage companions for the
 * fixed schema.</p>
 *
 * <p>Use this annotation when the row shape is stable enough to justify
 * generated code. The first supported source form is a non-generic Java
 * record. The generated code can then avoid per-row reflection and repeated
 * schema lookup in dense table rendering or adapter code.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CoruscoDataSet {

    /**
     * Stable data-set id.
     *
     * <p>The id becomes the generated {@code DataSetKey} id and the default
     * prefix for column ids. Treat it as persistent schema identity: changing
     * it changes generated constants and may affect adapter mappings, saved
     * requests, exports, or resource lookups that reference the schema.</p>
     *
     * @return non-blank data-set id
     */
    String id();
}
