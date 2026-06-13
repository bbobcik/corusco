import javax.annotation.processing.Processor;

/**
 * Annotation processor that generates Corusco typed metadata and models.
 */
module cz.auderis.corusco.processor {
    requires transitive java.compiler;
    requires cz.auderis.corusco.annotations;

    exports cz.auderis.corusco.processor;

    provides Processor with cz.auderis.corusco.processor.CoruscoAnnotationProcessor;
}
