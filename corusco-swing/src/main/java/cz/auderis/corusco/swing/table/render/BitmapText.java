package cz.auderis.corusco.swing.table.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Cached text image and its measured width.
 */
record BitmapText(BufferedImage image, int baseline, int width) {

    static BitmapText render(String text, Font font, Color color, FontMetrics metrics) {
        int width = Math.max(1, metrics.stringWidth(text));
        int height = Math.max(1, metrics.getHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );
            graphics.setFont(font);
            graphics.setColor(color);
            graphics.drawString(text, 0, metrics.getAscent());
        } finally {
            graphics.dispose();
        }
        return new BitmapText(image, metrics.getAscent(), width);
    }
}
