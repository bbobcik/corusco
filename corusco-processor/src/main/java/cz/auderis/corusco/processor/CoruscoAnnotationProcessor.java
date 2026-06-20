package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.form.SwingForm;
import cz.auderis.corusco.annotations.table.SwingTable;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Annotation processor that generates Corusco form, table, and action metadata.
 *
 * <p>The processor consumes source-retained annotations using
 * {@code javax.lang.model}. It validates record and method contracts, reports
 * diagnostics at the annotated element, and writes strongly typed source files
 * that expose field keys, form models, table descriptors, table columns, and
 * action descriptors. Runtime code should depend on those generated artifacts
 * rather than scanning annotations reflectively.</p>
 */
@SupportedAnnotationTypes({
        "cz.auderis.corusco.annotations.form.SwingForm",
        "cz.auderis.corusco.annotations.table.SwingTable",
        "cz.auderis.corusco.annotations.command.UiAction"
})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class CoruscoAnnotationProcessor extends AbstractProcessor {

    private Elements elements;
    private Types types;
    private Messager messager;
    private Filer filer;

    /**
     * Creates a processor instance for the Java compiler.
     */
    public CoruscoAnnotationProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SwingForm.class)) {
            if (element instanceof TypeElement typeElement) {
                new FormProcessor(elements, types, filer, messager).process(typeElement);
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(SwingTable.class)) {
            if (element instanceof TypeElement typeElement) {
                new TableProcessor(elements, types, filer, messager).process(typeElement);
            }
        }
        new ActionProcessor(messager, new ActionSourceWriter(elements, filer, messager)).process(roundEnv);
        return true;
    }
}
