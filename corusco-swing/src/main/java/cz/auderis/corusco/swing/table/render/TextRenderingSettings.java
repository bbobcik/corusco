package cz.auderis.corusco.swing.table.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;

/**
 * Text rendering hints used when painting cached text bitmaps.
 */
record TextRenderingSettings(
        Object textAntialiasing,
        Object fractionalMetrics,
        Object lcdContrast
) {

    static TextRenderingSettings from(Graphics2D graphics) {
        Map<?, ?> desktopHints = desktopHints();
        return new TextRenderingSettings(
                hint(graphics, desktopHints, RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON),
                hint(graphics, desktopHints, RenderingHints.KEY_FRACTIONALMETRICS, null),
                hint(graphics, desktopHints, RenderingHints.KEY_TEXT_LCD_CONTRAST, null)
        );
    }

    void apply(Graphics2D graphics) {
        if (textAntialiasing != null) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAntialiasing);
        }
        if (fractionalMetrics != null) {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics);
        }
        if (lcdContrast != null) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, lcdContrast);
        }
    }

    private static Object hint(
            Graphics2D graphics,
            Map<?, ?> desktopHints,
            RenderingHints.Key key,
            Object fallback
    ) {
        Object value = graphics.getRenderingHint(key);
        if (isExplicit(value)) {
            return value;
        }
        Object desktopValue = desktopHints.get(key);
        return desktopValue != null ? desktopValue : fallback;
    }

    private static boolean isExplicit(Object value) {
        return value != null
                && value != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT
                && value != RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
    }

    private static Map<?, ?> desktopHints() {
        try {
            Object property = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
            return property instanceof Map<?, ?> hints ? hints : Map.of();
        } catch (RuntimeException e) {
            return Map.of();
        }
    }
}
