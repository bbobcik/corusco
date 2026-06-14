package cz.auderis.corusco.swing.table.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Cached text image and its measured width.
 */
record BitmapText(BufferedImage image, int baseline, int width, int height) {

    static BitmapText render(
            String text,
            Font font,
            Color foreground,
            Color background,
            FontMetrics metrics,
            TextRenderingSettings settings,
            double scaleX,
            double scaleY
    ) {
        if (text.isEmpty()) {
            return empty(background, metrics, scaleX, scaleY);
        }
        TextLayout layout = new TextLayout(text, font, metrics.getFontRenderContext());
        Rectangle2D bounds = layout.getBounds();
        int width = Math.max(1, (int) Math.ceil(layout.getAdvance()));
        int baseline = Math.max(1, (int) Math.ceil(layout.getAscent()));
        int height = Math.max(1, (int) Math.ceil(layout.getAscent() + layout.getDescent() + layout.getLeading()));
        double safeScaleX = scaleX > 0.0 ? scaleX : 1.0;
        double safeScaleY = scaleY > 0.0 ? scaleY : 1.0;
        int imageWidth = Math.max(1, (int) Math.ceil(width * safeScaleX));
        int imageHeight = Math.max(1, (int) Math.ceil(height * safeScaleY));
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(background);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            graphics.scale(safeScaleX, safeScaleY);
            settings.apply(graphics);
            graphics.setFont(font);
            graphics.setColor(foreground);
            layout.draw(graphics, (float) Math.max(0.0, -bounds.getX()), baseline);
        } finally {
            graphics.dispose();
        }
        return new BitmapText(image, baseline, width, height);
    }

    private static BitmapText empty(Color background, FontMetrics metrics, double scaleX, double scaleY) {
        int width = 1;
        int height = Math.max(1, metrics.getHeight());
        double safeScaleX = scaleX > 0.0 ? scaleX : 1.0;
        double safeScaleY = scaleY > 0.0 ? scaleY : 1.0;
        int imageWidth = Math.max(1, (int) Math.ceil(width * safeScaleX));
        int imageHeight = Math.max(1, (int) Math.ceil(height * safeScaleY));
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(background);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
        } finally {
            graphics.dispose();
        }
        return new BitmapText(image, metrics.getAscent(), width, height);
    }
}
