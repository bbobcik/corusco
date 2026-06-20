package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.CoruscoTable;
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
 * Reads {@link CoruscoTable} records and delegates generated table output.
 */
final class TableProcessor {

    private final Elements elements;
    private final Types types;
    private final Filer filer;
    private final Messager messager;

    TableProcessor(Elements elements, Types types, Filer filer, Messager messager) {
        this.elements = elements;
        this.types = types;
        this.filer = filer;
        this.messager = messager;
    }

    void process(TypeElement tableType) {
        TableSpec table = createSpec(tableType);
        if (table != null) {
            new TableSourceWriter(elements, filer, messager)
                    .writeTableSources(tableType, table);
        }
    }

    TableSpec createSpec(TypeElement tableType) {
        CoruscoTable annotation = tableType.getAnnotation(CoruscoTable.class);
        if (tableType.getKind() != ElementKind.RECORD) {
            error(tableType, "@CoruscoTable is supported only on records");
            return null;
        }
        if (annotation.id().isBlank()) {
            error(tableType, "@CoruscoTable id must not be blank");
            return null;
        }
        if (!isStableId(annotation.id())) {
            error(tableType, "@CoruscoTable id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!tableType.getTypeParameters().isEmpty()) {
            error(tableType, "@CoruscoTable generic records are not supported by this processor stage");
            return null;
        }

        List<TableComponentSpec> components = new ArrayList<>();
        for (RecordComponentElement component : tableType.getRecordComponents()) {
            components.add(new TableComponentSpec(component.getSimpleName().toString()));
        }
        List<TableColumnSpec> columns = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        boolean failed = false;
        for (RecordComponentElement component : tableType.getRecordComponents()) {
            Column annotationColumn = component.getAnnotation(Column.class);
            if (annotationColumn == null) {
                continue;
            }
            if (!isSupportedValueType(component.asType())) {
                error(component, "@Column requires primitive or declared component types");
                failed = true;
                continue;
            }
            String id = annotationColumn.id().isBlank()
                    ? annotation.id() + "/" + kebab(component.getSimpleName().toString())
                    : annotationColumn.id();
            if (!isStableId(id)) {
                error(component, "@Column id must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!ids.add(id)) {
                error(component, "Duplicate @Column id in " + tableType.getSimpleName() + ": " + id);
                failed = true;
                continue;
            }
            if (annotationColumn.width() <= 0) {
                error(component, "@Column width must be greater than zero");
                failed = true;
                continue;
            }
            if (annotationColumn.minWidth() <= 0) {
                error(component, "@Column minWidth must be greater than zero");
                failed = true;
                continue;
            }
            if (annotationColumn.minWidth() > annotationColumn.width()) {
                error(component, "@Column requires minWidth <= width");
                failed = true;
                continue;
            }
            if (annotationColumn.maxWidth() < annotationColumn.width()) {
                error(component, "@Column requires width <= maxWidth");
                failed = true;
                continue;
            }
            if (!annotationColumn.persistenceId().isBlank() && !isStableId(annotationColumn.persistenceId())) {
                error(component, "@Column persistenceId must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!annotationColumn.header().isBlank() && !isStableId(annotationColumn.header())) {
                error(component, "@Column header must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!annotationColumn.tooltip().isBlank() && !isStableId(annotationColumn.tooltip())) {
                error(component, "@Column tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            Help help = component.getAnnotation(Help.class);
            if (help != null) {
                if (!annotationColumn.tooltip().isBlank() && !help.tooltip().isBlank()) {
                    error(component, "Table column tooltip must be declared either on @Column or @Help, not both");
                    failed = true;
                    continue;
                }
                if (!help.tooltip().isBlank() && !isStableId(help.tooltip())) {
                    error(component, "@Help tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
                    failed = true;
                    continue;
                }
                if (!help.topic().isBlank() && !isStableId(help.topic())) {
                    error(component, "@Help topic must contain only letters, digits, dots, underscores, dashes, or slashes");
                    failed = true;
                    continue;
                }
            }
            columns.add(tableColumnSpec(tableType, component, annotationColumn, id, columns.size()));
        }
        if (columns.isEmpty() && !failed) {
            error(tableType, "@CoruscoTable record must contain at least one @Column component");
            return null;
        }
        if (failed) {
            return null;
        }
        return new TableSpec(
                annotation.id(),
                tableType.getSimpleName().toString(),
                components,
                columns
        );
    }

    private TableColumnSpec tableColumnSpec(
            TypeElement tableType,
            RecordComponentElement component,
            Column column,
            String keyId,
            int componentOrder
    ) {
        String componentName = component.getSimpleName().toString();
        String constantName = constantName(componentName);
        String headerId = column.header().isBlank() ? keyId + "/header" : column.header();
        Help help = component.getAnnotation(Help.class);
        String helpTooltip = help == null ? "" : help.tooltip();
        String tooltipId = !column.tooltip().isBlank() ? column.tooltip() : helpTooltip;
        String helpTopicId = help == null || help.topic().isBlank() ? null : help.topic();
        tooltipId = tooltipId.isBlank() ? null : tooltipId;
        String persistenceId = column.persistenceId().isBlank() ? keyId : column.persistenceId();
        int order = column.order() < 0 ? componentOrder : column.order();
        return new TableColumnSpec(
                constantName,
                keyId,
                componentName,
                tableType.getSimpleName().toString(),
                genericType(component.asType()),
                classLiteral(component.asType()),
                constantName + "_HEADER",
                headerId,
                tooltipId == null ? null : constantName + "_TOOLTIP",
                tooltipId,
                helpTopicId,
                persistenceId,
                column.width(),
                column.minWidth(),
                column.maxWidth(),
                order,
                column.visible(),
                column.sortable(),
                column.filterable(),
                column.hideable(),
                column.editable()
        );
    }

    private String genericType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName().toString();
        }
        return type.toString();
    }

    private String classLiteral(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName() + ".class";
        }
        return types.erasure(type).toString() + ".class";
    }

    private static boolean isSupportedValueType(TypeMirror type) {
        return type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED;
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private static boolean isStableId(String value) {
        return value.matches("[A-Za-z0-9][A-Za-z0-9._/-]*");
    }

    private static String constantName(String componentName) {
        String kebab = kebab(componentName);
        return kebab.replace('-', '_').toUpperCase(Locale.ROOT);
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
}
