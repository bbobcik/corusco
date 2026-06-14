package cz.auderis.corusco.swing.table.render;

import java.awt.Color;
import java.awt.Font;

/**
 * Cache key for a text bitmap under one renderer visual state.
 */
record TextVisualKey(
        String text,
        Font font,
        Color foreground,
        Color background,
        boolean selected,
        boolean focused,
        boolean enabled,
        int rowHeight,
        double scaleX,
        double scaleY,
        TextRenderingSettings renderingSettings
) {
}
