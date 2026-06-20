package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requests Swing companions for generated form or table sources.
 *
 * <p>Use this annotation on {@code package-info.java} when source model
 * packages are compiled with only {@code corusco-core} and annotations, while a
 * separate adapter package depends on {@code corusco-swing}. The processor
 * auto-discovers {@code @CoruscoForm} and {@code @CoruscoTable} types declared in
 * this same package. Explicit targets add cross-package source types whose
 * Swing companions should also be generated in this package.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface SwingCompanionPackage {

    /**
     * Additional form source types whose Swing companions should be generated
     * here.
     *
     * @return annotated form source classes, usually from another package
     */
    Class<?>[] forms() default {};

    /**
     * Additional table source types whose Swing companions should be generated
     * here.
     *
     * @return annotated table source classes, usually from another package
     */
    Class<?>[] tables() default {};
}
