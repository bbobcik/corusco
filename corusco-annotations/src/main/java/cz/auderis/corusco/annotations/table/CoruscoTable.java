package cz.auderis.corusco.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as a Corusco table row source type.
 *
 * <p>The annotation is retained in class files so adapter packages can request
 * Swing companions for already compiled table sources. Corusco processors
 * still read it through {@code javax.lang.model} during compilation. The
 * {@link #id()} value becomes the stable prefix for generated table and column
 * ids. Components annotated with {@link Column} inside the record become
 * generated column descriptors; unannotated components are ignored by table
 * metadata generation.</p>
 *
 * <p>The processor validates that the annotation is used on records and that
 * the id is non-blank. Generated artifacts use the source type name and stable
 * ids rather than runtime reflection, so changing the id is a compatibility
 * change for saved table state.</p>
 *
 * <p>Generated table companions are the normal place to obtain runtime table
 * metadata keys. A columns companion such as {@code CustomerRowColumns}
 * contains typed {@code cz.auderis.corusco.core.table.TableKey} and
 * {@code cz.auderis.corusco.core.table.ColumnKey} constants. Column header and
 * tooltip text use {@code cz.auderis.corusco.core.key.ResourceKey<String>}
 * constants in resources companions such as
 * {@code CustomerRowTableResources}.</p>
 *
 * <p>The same source also generates non-key runtime objects. A columns
 * companion such as {@code CustomerRowColumns} contains
 * {@code cz.auderis.corusco.core.table.Column} and
 * {@code cz.auderis.corusco.core.table.ColumnDescriptor} constants. A
 * descriptor companion such as {@code CustomerRowTableDescriptor} contains a
 * {@code cz.auderis.corusco.core.table.TableDescriptor} constant. When the
 * owning or adapter package is annotated with {@code @SwingCompanionPackage},
 * a bindings companion such as {@code CustomerRowTableBindings} installs table
 * models and selection bindings into Swing views.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CoruscoTable {

    /**
     * Stable table id used as the prefix for generated metadata ids.
     *
     * @return non-blank table id
     */
    String id();

}
