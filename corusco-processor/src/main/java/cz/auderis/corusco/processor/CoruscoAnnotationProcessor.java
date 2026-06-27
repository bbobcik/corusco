package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.SwingCompanionPackage;
import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.SwingForm;
import cz.auderis.corusco.annotations.table.CoruscoTable;
import cz.auderis.corusco.annotations.table.SwingTable;
import java.util.LinkedHashSet;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Annotation processor that generates Corusco form, table, and action metadata.
 *
 * <p>The processor consumes annotations using {@code javax.lang.model}. It
 * validates record and method contracts, reports diagnostics at the annotated
 * element, and writes strongly typed source files that expose field keys, form
 * models, table descriptors, table columns, and action descriptors. Runtime
 * code should depend on those generated artifacts rather than scanning
 * annotations reflectively.</p>
 */
@SupportedAnnotationTypes({
        "cz.auderis.corusco.annotations.form.CoruscoForm",
        "cz.auderis.corusco.annotations.form.SwingForm",
        "cz.auderis.corusco.annotations.SwingCompanionPackage",
        "cz.auderis.corusco.annotations.dataset.CoruscoDataSet",
        "cz.auderis.corusco.annotations.table.CoruscoTable",
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
        FormProcessor formProcessor = new FormProcessor(elements, types, filer, messager);
        TableProcessor tableProcessor = new TableProcessor(elements, types, filer, messager);
        DataSetProcessor dataSetProcessor = new DataSetProcessor(elements, types, filer, messager);
        GeneratedSourceWriter formSourceWriter = new GeneratedSourceWriter(elements, filer, messager);
        TableSourceWriter tableSourceWriter = new TableSourceWriter(elements, filer, messager);

        Set<Element> coruscoForms = new LinkedHashSet<>(roundEnv.getElementsAnnotatedWith(CoruscoForm.class));
        Set<Element> swingForms = new LinkedHashSet<>(roundEnv.getElementsAnnotatedWith(SwingForm.class));
        for (Element element : coruscoForms) {
            if (swingForms.contains(element)) {
                error(element, "Use either @CoruscoForm or @SwingForm, not both");
                continue;
            }
            if (element instanceof TypeElement typeElement) {
                formProcessor.process(typeElement);
            }
        }
        for (Element element : swingForms) {
            if (coruscoForms.contains(element)) {
                continue;
            }
            if (element instanceof TypeElement typeElement) {
                SwingForm annotation = typeElement.getAnnotation(SwingForm.class);
                FormSpec form = formProcessor.createSpec(typeElement, annotation.id(), "@SwingForm", false);
                if (form != null) {
                    formSourceWriter.writeFormSources(typeElement, form);
                    PackageElement packageElement = elements.getPackageOf(typeElement);
                    formSourceWriter.writeFormSwingSources(
                            typeElement,
                            form,
                            packageElement.getQualifiedName().toString()
                    );
                }
            }
        }

        Set<Element> coruscoTables = new LinkedHashSet<>(roundEnv.getElementsAnnotatedWith(CoruscoTable.class));
        Set<Element> swingTables = new LinkedHashSet<>(roundEnv.getElementsAnnotatedWith(SwingTable.class));
        for (Element element : coruscoTables) {
            if (swingTables.contains(element)) {
                error(element, "Use either @CoruscoTable or @SwingTable, not both");
                continue;
            }
            if (element instanceof TypeElement typeElement) {
                tableProcessor.process(typeElement);
            }
        }
        for (Element element : swingTables) {
            if (coruscoTables.contains(element)) {
                continue;
            }
            if (element instanceof TypeElement typeElement) {
                SwingTable annotation = typeElement.getAnnotation(SwingTable.class);
                TableSpec table = tableProcessor.createSpec(typeElement, annotation.id(), "@SwingTable");
                if (table != null) {
                    tableSourceWriter.writeTableSources(typeElement, table);
                    PackageElement packageElement = elements.getPackageOf(typeElement);
                    tableSourceWriter.writeTableSwingSources(
                            typeElement,
                            table,
                            packageElement.getQualifiedName().toString()
                    );
                }
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(CoruscoDataSet.class)) {
            if (element instanceof TypeElement typeElement) {
                dataSetProcessor.process(typeElement);
            }
        }
        new SwingCompanionPackageProcessor(elements, types, filer, messager).process(roundEnv);
        new ActionProcessor(messager, new ActionSourceWriter(elements, filer, messager)).process(roundEnv);
        return true;
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
