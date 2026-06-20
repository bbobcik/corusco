import javax.annotation.processing.Processor;

/**
 * Provides the Corusco annotation processor.
 *
 * <p>The processor reads Corusco annotations through {@code javax.lang.model}
 * and generates typed metadata for forms, tables, validation constraints, and
 * actions. Form and table metadata annotations are retained in class files so
 * adapter packages can generate Swing companions for already compiled model
 * classes. Generated sources are designed to be consumed by the core and Swing
 * runtime modules: they expose stable keys, descriptors, and model helpers
 * instead of requiring reflection or string property paths at runtime.</p>
 *
 * <p>Applications normally depend on this module as an annotation processor,
 * not as a runtime library. The exported processor package exists for tests and
 * build tooling that need to invoke the processor directly.</p>
 */
module cz.auderis.corusco.processor {
    requires transitive java.compiler;
    requires cz.auderis.corusco.annotations;
    requires org.jetbrains.annotations;
    requires java.desktop;

    exports cz.auderis.corusco.processor;

    provides Processor with cz.auderis.corusco.processor.CoruscoAnnotationProcessor;
}
