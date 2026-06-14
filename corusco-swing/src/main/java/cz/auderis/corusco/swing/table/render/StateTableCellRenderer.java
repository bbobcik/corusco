package cz.auderis.corusco.swing.table.render;

/**
 * Table-cell renderer for boolean, enum, and small finite state values.
 *
 * <p>The renderer deliberately performs only value-to-text conversion and
 * optional bitmap text caching. It does not decorate state with icons, colors,
 * tooltips, or resource lookup; those concerns remain application-specific
 * renderers or separate table bindings.</p>
 */
final class StateTableCellRenderer extends BitmapTextRenderer {

    private static final long serialVersionUID = 1L;

    private final StateRendererOptions options;

    StateTableCellRenderer(StateRendererOptions options) {
        super(false, options.cacheSize());
        this.options = options;
    }

    @Override
    String text(Object value) {
        if (value == null) {
            return options.nullText();
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? options.trueText() : options.falseText();
        }
        if (options.enumNames() && value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    @Override
    boolean usesBitmapCache() {
        return options.bitmapCache();
    }
}
