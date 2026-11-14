package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.SwingCompanionPackage;
import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.SwingForm;
import cz.auderis.corusco.annotations.table.CoruscoTable;
import cz.auderis.corusco.annotations.table.SwingTable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;

/**
 * Defers annotated elements whose member types are not resolved yet.
 *
 * <p>Types produced by other annotation processors appear as
 * {@link TypeKind#ERROR} until the round after they are generated. Elements
 * that reference such types are withheld from processing and offered again in
 * the next round. Only in the final round, when no further sources can appear,
 * are they processed as they are, so that genuine errors are still reported.
 * A {@code @SwingCompanionPackage} package is deferred together with any
 * deferred form or table it contains, so its companions are generated in the
 * same round as the model sources.</p>
 */
final class DeferredElements {

    private static final List<Class<? extends Annotation>> TYPE_ANNOTATIONS = List.of(
            CoruscoForm.class,
            SwingForm.class,
            CoruscoTable.class,
            SwingTable.class,
            CoruscoDataSet.class
    );

    private final Elements elements;
    private final Set<String> deferredTypes = new LinkedHashSet<>();
    private final Set<String> deferredPackages = new LinkedHashSet<>();

    DeferredElements(Elements elements) {
        this.elements = elements;
    }

    /**
     * Returns a view of {@code roundEnv} that includes elements deferred from
     * earlier rounds and omits elements deferred in this round.
     */
    RoundEnvironment prepareRound(RoundEnvironment roundEnv) {
        List<Element> carried = takeDeferred();
        Map<Class<? extends Annotation>, Set<Element>> annotated = new LinkedHashMap<>();
        for (Class<? extends Annotation> annotation : TYPE_ANNOTATIONS) {
            annotated.put(annotation, collect(roundEnv, annotation, carried));
        }
        annotated.put(SwingCompanionPackage.class, collect(roundEnv, SwingCompanionPackage.class, carried));

        if (!roundEnv.processingOver()) {
            Set<Element> toDefer = new HashSet<>();
            Set<PackageElement> packagesWithDeferredTypes = new HashSet<>();
            for (Class<? extends Annotation> annotation : TYPE_ANNOTATIONS) {
                for (Element element : annotated.get(annotation)) {
                    if (hasUnresolvedTypes(element)) {
                        toDefer.add(element);
                        packagesWithDeferredTypes.add(elements.getPackageOf(element));
                    }
                }
            }
            for (Element element : annotated.get(SwingCompanionPackage.class)) {
                if (packagesWithDeferredTypes.contains(element) || hasUnresolvedTypes(element)) {
                    toDefer.add(element);
                }
            }
            for (Set<Element> set : annotated.values()) {
                set.removeAll(toDefer);
            }
            for (Element element : toDefer) {
                if (element instanceof TypeElement type) {
                    deferredTypes.add(type.getQualifiedName().toString());
                } else if (element instanceof PackageElement pkg) {
                    deferredPackages.add(pkg.getQualifiedName().toString());
                }
            }
        }
        return new RoundView(roundEnv, annotated);
    }

    private List<Element> takeDeferred() {
        List<Element> carried = new java.util.ArrayList<>();
        for (String name : deferredTypes) {
            TypeElement type = elements.getTypeElement(name);
            if (type != null) {
                carried.add(type);
            }
        }
        for (String name : deferredPackages) {
            PackageElement pkg = elements.getPackageElement(name);
            if (pkg != null) {
                carried.add(pkg);
            }
        }
        deferredTypes.clear();
        deferredPackages.clear();
        return carried;
    }

    private static Set<Element> collect(
            RoundEnvironment roundEnv,
            Class<? extends Annotation> annotation,
            List<Element> carried
    ) {
        Set<Element> result = new LinkedHashSet<>(roundEnv.getElementsAnnotatedWith(annotation));
        for (Element element : carried) {
            if (element.getAnnotation(annotation) != null) {
                result.add(element);
            }
        }
        return result;
    }

    static boolean hasUnresolvedTypes(Element element) {
        if (annotationsReferenceUnresolvedTypes(element)) {
            return true;
        }
        if (element instanceof TypeElement type) {
            for (Element member : type.getEnclosedElements()) {
                if (member instanceof ExecutableElement method) {
                    if (containsError(method.getReturnType())) {
                        return true;
                    }
                    for (VariableElement parameter : method.getParameters()) {
                        if (containsError(parameter.asType())) {
                            return true;
                        }
                    }
                } else if (member instanceof VariableElement || member.getKind() == ElementKind.RECORD_COMPONENT) {
                    if (containsError(member.asType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean annotationsReferenceUnresolvedTypes(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (containsError(annotation.getAnnotationType())) {
                return true;
            }
            for (AnnotationValue value : annotation.getElementValues().values()) {
                if (valueContainsError(value.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean valueContainsError(Object value) {
        if (value instanceof TypeMirror type) {
            return containsError(type);
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof AnnotationValue nested && valueContainsError(nested.getValue())) {
                    return true;
                }
            }
            return false;
        }
        // javac reports an unresolvable class literal as the string "<error>".
        return "<error>".equals(value);
    }

    private static boolean containsError(TypeMirror type) {
        if (type.getKind() == TypeKind.ERROR) {
            return true;
        }
        if (type instanceof ArrayType array) {
            return containsError(array.getComponentType());
        }
        if (type instanceof WildcardType wildcard) {
            return (wildcard.getExtendsBound() != null && containsError(wildcard.getExtendsBound()))
                    || (wildcard.getSuperBound() != null && containsError(wildcard.getSuperBound()));
        }
        if (type instanceof DeclaredType declared) {
            for (TypeMirror argument : declared.getTypeArguments()) {
                if (containsError(argument)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record RoundView(
            RoundEnvironment delegate,
            Map<Class<? extends Annotation>, Set<Element>> annotated
    ) implements RoundEnvironment {

        @Override
        public boolean processingOver() {
            return delegate.processingOver();
        }

        @Override
        public boolean errorRaised() {
            return delegate.errorRaised();
        }

        @Override
        public Set<? extends Element> getRootElements() {
            return delegate.getRootElements();
        }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(TypeElement annotation) {
            return delegate.getElementsAnnotatedWith(annotation);
        }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> annotation) {
            Set<Element> view = annotated.get(annotation);
            return view != null ? view : delegate.getElementsAnnotatedWith(annotation);
        }
    }
}
