package cz.auderis.corusco.core.meta;

import java.util.Objects;

/**
 * Structured editor metadata for a generated form field.
 *
 * <p>This descriptor supersedes the older flat {@link FieldKind} value. It
 * keeps the editor family together with the semantic value shape so option
 * sources, richer selection editors, pickers, and mapped controls can evolve
 * without adding a new field-kind enum for every Swing component.</p>
 *
 * @param family editor family
 * @param selectionShape semantic selection/value shape
 */
public record EditorDescriptor(EditorFamily family, SelectionShape selectionShape) {

    /**
     * Creates editor metadata.
     *
     * @param family editor family
     * @param selectionShape semantic selection/value shape
     */
    public EditorDescriptor {
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(selectionShape, "selectionShape");
    }

    /**
     * Creates a descriptor for an existing field kind.
     *
     * @param kind existing field kind
     * @return equivalent editor descriptor
     */
    public static EditorDescriptor fromKind(FieldKind kind) {
        Objects.requireNonNull(kind, "kind");
        return switch (kind) {
            case TEXT -> text();
            case DATE -> date();
            case CHECK_BOX -> checkBox();
            case COMBO_BOX -> comboBox();
        };
    }

    /**
     * Returns a best-effort legacy kind for callers that still consume
     * {@link FieldKind}.
     *
     * @return legacy field kind
     */
    public FieldKind legacyKind() {
        return switch (family) {
            case TEXT -> FieldKind.TEXT;
            case DATE -> FieldKind.DATE;
            case CHECK_BOX -> FieldKind.CHECK_BOX;
            case COMBO_BOX, RADIO_GROUP, CHECK_BOX_GROUP, PICKER, RICH_VALUE, NUMERIC_RANGE -> FieldKind.COMBO_BOX;
        };
    }

    /**
     * Text editor descriptor.
     *
     * @return descriptor
     */
    public static EditorDescriptor text() {
        return new EditorDescriptor(EditorFamily.TEXT, SelectionShape.SINGLE_VALUE);
    }

    /**
     * Date editor descriptor.
     *
     * @return descriptor
     */
    public static EditorDescriptor date() {
        return new EditorDescriptor(EditorFamily.DATE, SelectionShape.NULLABLE_SINGLE_VALUE);
    }

    /**
     * Checkbox editor descriptor.
     *
     * @return descriptor
     */
    public static EditorDescriptor checkBox() {
        return new EditorDescriptor(EditorFamily.CHECK_BOX, SelectionShape.BOOLEAN_TWO_STATE);
    }

    /**
     * Combo-box editor descriptor.
     *
     * @return descriptor
     */
    public static EditorDescriptor comboBox() {
        return new EditorDescriptor(EditorFamily.COMBO_BOX, SelectionShape.SINGLE_VALUE);
    }

    /**
     * Radio-group editor descriptor.
     *
     * @return descriptor
     */
    public static EditorDescriptor radioGroup() {
        return new EditorDescriptor(EditorFamily.RADIO_GROUP, SelectionShape.SINGLE_VALUE);
    }
}
