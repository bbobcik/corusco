package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.dataset.AggregationFunction;
import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
import cz.auderis.corusco.annotations.dataset.DataColumn;
import cz.auderis.corusco.annotations.dataset.Dimension;
import cz.auderis.corusco.annotations.dataset.Measure;
import cz.auderis.corusco.annotations.dataset.QualityColumn;
import cz.auderis.corusco.annotations.dataset.TimeAxis;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Reads {@link CoruscoDataSet} records and delegates descriptor generation.
 */
final class DataSetProcessor {

    private final Elements elements;
    private final Types types;
    private final Filer filer;
    private final Messager messager;

    DataSetProcessor(Elements elements, Types types, Filer filer, Messager messager) {
        this.elements = elements;
        this.types = types;
        this.filer = filer;
        this.messager = messager;
    }

    void process(TypeElement dataSetType) {
        CoruscoDataSet annotation = dataSetType.getAnnotation(CoruscoDataSet.class);
        DataSetSpec dataSet = createSpec(dataSetType, annotation.id());
        if (dataSet != null) {
            new DataSetSourceWriter(elements, filer, messager).writeDataSetSources(dataSetType, dataSet);
        }
    }

    DataSetSpec createSpec(TypeElement dataSetType, String dataSetId) {
        if (dataSetType.getKind() != ElementKind.RECORD) {
            error(dataSetType, "@CoruscoDataSet is supported only on records");
            return null;
        }
        if (dataSetId.isBlank()) {
            error(dataSetType, "@CoruscoDataSet id must not be blank");
            return null;
        }
        if (!isStableId(dataSetId)) {
            error(dataSetType, "@CoruscoDataSet id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!dataSetType.getTypeParameters().isEmpty()) {
            error(dataSetType, "@CoruscoDataSet generic records are not supported by this processor stage");
            return null;
        }

        List<DataSetColumnSpec> columns = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        int timeAxisCount = 0;
        boolean failed = false;
        for (RecordComponentElement component : dataSetType.getRecordComponents()) {
            ColumnShape shape = columnShape(component);
            if (shape == null) {
                error(component, "@CoruscoDataSet record components must declare a data-set role annotation");
                failed = true;
                continue;
            }
            if (!isSupportedValueType(component.asType())) {
                error(component, "Data-set columns require primitive or declared component types");
                failed = true;
                continue;
            }
            if (shape.role.equals("TIME_AXIS")) {
                timeAxisCount++;
            }
            String id = shape.id.isBlank()
                    ? dataSetId + "/" + kebab(component.getSimpleName().toString())
                    : shape.id;
            if (!isStableId(id)) {
                error(component, "Data-set column id must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!ids.add(id)) {
                error(component, "Duplicate data-set column id in " + dataSetType.getSimpleName() + ": " + id);
                failed = true;
                continue;
            }
            if (shape.missingPolicy.equals("NAN") && !isFloatingPoint(component.asType())) {
                error(component, "NAN missing policy requires float or double component type");
                failed = true;
                continue;
            }
            columns.add(columnSpec(dataSetType, component, shape, id));
        }
        if (columns.isEmpty() && !failed) {
            error(dataSetType, "@CoruscoDataSet record must contain at least one data-set column");
            return null;
        }
        if (timeAxisCount > 1) {
            error(dataSetType, "@CoruscoDataSet record must not contain more than one @TimeAxis component");
            failed = true;
        }
        if (failed) {
            return null;
        }
        return new DataSetSpec(dataSetId, dataSetType.getSimpleName().toString(), columns);
    }

    private ColumnShape columnShape(RecordComponentElement component) {
        int annotations = 0;
        ColumnShape shape = null;
        TimeAxis timeAxis = component.getAnnotation(TimeAxis.class);
        if (timeAxis != null) {
            annotations++;
            shape = new ColumnShape(timeAxis.id(), "TIME_AXIS", timeAxis.unit(), "NONE", "NONE", List.of());
        }
        Dimension dimension = component.getAnnotation(Dimension.class);
        if (dimension != null) {
            annotations++;
            shape = new ColumnShape(dimension.id(), "DIMENSION", "", dimension.missing().name(), "NONE", List.of());
        }
        Measure measure = component.getAnnotation(Measure.class);
        if (measure != null) {
            annotations++;
            shape = new ColumnShape(
                    measure.id(),
                    "MEASURE",
                    measure.unit(),
                    measure.missing().name(),
                    measure.quality().name(),
                    aggregations(measure.aggregations())
            );
        }
        QualityColumn qualityColumn = component.getAnnotation(QualityColumn.class);
        if (qualityColumn != null) {
            annotations++;
            shape = new ColumnShape(qualityColumn.id(), "QUALITY", "", "NONE", "FLAGS", List.of());
        }
        DataColumn dataColumn = component.getAnnotation(DataColumn.class);
        if (dataColumn != null) {
            annotations++;
            shape = new ColumnShape(
                    dataColumn.id(),
                    dataColumn.role().name(),
                    dataColumn.unit(),
                    dataColumn.missing().name(),
                    dataColumn.quality().name(),
                    aggregations(dataColumn.aggregations())
            );
        }
        if (annotations > 1) {
            error(component, "Use only one data-set role annotation on a record component");
            return null;
        }
        return shape;
    }

    private static List<String> aggregations(AggregationFunction[] aggregations) {
        List<String> result = new ArrayList<>(aggregations.length);
        for (AggregationFunction aggregation : aggregations) {
            result.add(aggregation.name());
        }
        return List.copyOf(result);
    }

    private DataSetColumnSpec columnSpec(
            TypeElement dataSetType,
            RecordComponentElement component,
            ColumnShape shape,
            String keyId
    ) {
        String componentName = component.getSimpleName().toString();
        return new DataSetColumnSpec(
                constantName(componentName),
                keyId,
                componentName,
                dataSetType.getSimpleName().toString(),
                sourceType(component.asType()),
                genericType(component.asType()),
                classLiteral(component.asType()),
                shape.role,
                storage(component.asType()),
                shape.unit,
                shape.missingPolicy,
                shape.qualityPolicy,
                aggregationExpression(shape.aggregations)
        );
    }

    private String genericType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName().toString();
        }
        return type.toString();
    }

    private String sourceType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return type.toString();
        }
        return types.erasure(type).toString();
    }

    private String classLiteral(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName() + ".class";
        }
        return types.erasure(type).toString() + ".class";
    }

    private static String storage(TypeMirror type) {
        return switch (type.getKind()) {
            case BOOLEAN -> "BOOLEAN_ARRAY";
            case BYTE -> "BYTE_ARRAY";
            case SHORT -> "SHORT_ARRAY";
            case INT -> "INT_ARRAY";
            case CHAR -> "CHAR_ARRAY";
            case LONG -> "LONG_ARRAY";
            case FLOAT -> "FLOAT_ARRAY";
            case DOUBLE -> "DOUBLE_ARRAY";
            default -> "OBJECT_ARRAY";
        };
    }

    private static String aggregationExpression(List<String> aggregations) {
        if (aggregations.isEmpty()) {
            return "java.util.Set.of()";
        }
        StringBuilder result = new StringBuilder("java.util.Set.of(");
        for (int i = 0; i < aggregations.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append("AggregationFunction.").append(aggregations.get(i));
        }
        result.append(')');
        return result.toString();
    }

    private static boolean isSupportedValueType(TypeMirror type) {
        return type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED;
    }

    private static boolean isFloatingPoint(TypeMirror type) {
        return type.getKind() == TypeKind.FLOAT || type.getKind() == TypeKind.DOUBLE
                || type.toString().equals(Float.class.getName())
                || type.toString().equals(Double.class.getName());
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private static boolean isStableId(String value) {
        return value.matches("[A-Za-z0-9][A-Za-z0-9._/-]*");
    }

    private static String constantName(String componentName) {
        return kebab(componentName).replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String kebab(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (!result.isEmpty()) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private record ColumnShape(
            String id,
            String role,
            String unit,
            String missingPolicy,
            String qualityPolicy,
            List<String> aggregations
    ) {
    }
}
