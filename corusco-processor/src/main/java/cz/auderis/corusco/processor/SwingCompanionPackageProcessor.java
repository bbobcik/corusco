package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.SwingCompanionPackage;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.table.CoruscoTable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Generates Swing companions requested by {@link SwingCompanionPackage}.
 */
final class SwingCompanionPackageProcessor {

    private static final String SWING_COMPANION_PACKAGE_ANNOTATION =
            SwingCompanionPackage.class.getCanonicalName();

    private final Elements elements;
    private final Types types;
    private final Filer filer;
    private final Messager messager;

    SwingCompanionPackageProcessor(Elements elements, Types types, Filer filer, Messager messager) {
        this.elements = elements;
        this.types = types;
        this.filer = filer;
        this.messager = messager;
    }

    void process(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SwingCompanionPackage.class)) {
            if (element instanceof PackageElement targetPackage) {
                processPackage(targetPackage, roundEnv);
            }
        }
    }

    private void processPackage(PackageElement targetPackage, RoundEnvironment roundEnv) {
        String targetPackageName = targetPackage.getQualifiedName().toString();
        Set<String> generatedForms = new HashSet<>();
        Set<String> generatedTables = new HashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(CoruscoForm.class)) {
            if (element instanceof TypeElement formType
                    && elements.getPackageOf(formType).equals(targetPackage)) {
                writeFormSwingSources(formType, targetPackageName, generatedForms);
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(CoruscoTable.class)) {
            if (element instanceof TypeElement tableType
                    && elements.getPackageOf(tableType).equals(targetPackage)) {
                writeTableSwingSources(tableType, targetPackageName, generatedTables);
            }
        }
        for (TypeElement formType : packageTypeValues(targetPackage, "forms")) {
            writeFormSwingSources(formType, targetPackageName, generatedForms);
        }
        for (TypeElement tableType : packageTypeValues(targetPackage, "tables")) {
            writeTableSwingSources(tableType, targetPackageName, generatedTables);
        }
    }

    private void writeFormSwingSources(TypeElement formType, String targetPackageName, Set<String> generated) {
        if (!generated.add(formType.getQualifiedName().toString())) {
            return;
        }
        if (formType.getAnnotation(CoruscoForm.class) == null) {
            error(formType, "@SwingCompanionPackage forms must reference @CoruscoForm types");
            return;
        }
        FormProcessor processor = new FormProcessor(elements, types, filer, messager);
        FormSpec form = processor.createSpec(formType);
        if (form != null) {
            new GeneratedSourceWriter(elements, filer, messager)
                    .writeFormSwingSources(formType, form, targetPackageName);
        }
    }

    private void writeTableSwingSources(TypeElement tableType, String targetPackageName, Set<String> generated) {
        if (!generated.add(tableType.getQualifiedName().toString())) {
            return;
        }
        if (tableType.getAnnotation(CoruscoTable.class) == null) {
            error(tableType, "@SwingCompanionPackage tables must reference @CoruscoTable types");
            return;
        }
        TableProcessor processor = new TableProcessor(elements, types, filer, messager);
        TableSpec table = processor.createSpec(tableType);
        if (table != null) {
            new TableSourceWriter(elements, filer, messager)
                    .writeTableSwingSources(tableType, table, targetPackageName);
        }
    }

    private List<TypeElement> packageTypeValues(PackageElement targetPackage, String memberName) {
        for (AnnotationMirror annotation : targetPackage.getAnnotationMirrors()) {
            Element annotationType = annotation.getAnnotationType().asElement();
            if (!(annotationType instanceof TypeElement typeElement)
                    || !SWING_COMPANION_PACKAGE_ANNOTATION.contentEquals(typeElement.getQualifiedName())) {
                continue;
            }
            for (Map.Entry<? extends Element, ? extends AnnotationValue> entry
                    : annotation.getElementValues().entrySet()) {
                if (memberName.contentEquals(entry.getKey().getSimpleName())) {
                    return typeValues(entry.getValue());
                }
            }
        }
        return List.of();
    }

    private List<TypeElement> typeValues(AnnotationValue value) {
        List<TypeElement> result = new ArrayList<>();
        Object rawValue = value.getValue();
        if (!(rawValue instanceof List<?> values)) {
            return result;
        }
        for (Object entry : values) {
            if (entry instanceof AnnotationValue annotationValue
                    && annotationValue.getValue() instanceof TypeMirror typeMirror
                    && types.asElement(typeMirror) instanceof TypeElement typeElement) {
                result.add(typeElement);
            }
        }
        return result;
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
