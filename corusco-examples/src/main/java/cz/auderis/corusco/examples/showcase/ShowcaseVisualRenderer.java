package cz.auderis.corusco.examples.showcase;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

final class ShowcaseVisualRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private static final int CACHE_LIMIT = 128;

    private final Kind kind;
    private final boolean cached;
    private final Map<VisualKey, BufferedImage> cache = new LinkedHashMap<>(CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<VisualKey, BufferedImage> eldest) {
            return size() > CACHE_LIMIT;
        }
    };
    private Object value;
    private String text = "";
    private Palette palette = Palette.neutral();
    private boolean selected;

    private ShowcaseVisualRenderer(Kind kind, boolean cached) {
        this.kind = kind;
        this.cached = cached;
    }

    static ShowcaseVisualRenderer state(boolean cached) {
        return new ShowcaseVisualRenderer(Kind.STATE, cached);
    }

    static ShowcaseVisualRenderer region(boolean cached) {
        return new ShowcaseVisualRenderer(Kind.REGION, cached);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.value = value;
        this.text = value == null ? "" : String.valueOf(value);
        this.palette = kind.palette(value);
        this.selected = isSelected;
        setText("");
        return this;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (text.isBlank() || !(graphics instanceof Graphics2D source)) {
            return;
        }
        Graphics2D g = (Graphics2D) source.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Object textAntialiasing = source.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (textAntialiasing != null) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAntialiasing);
            }
            FontMetrics metrics = g.getFontMetrics(getFont());
            int iconSize = Math.max(10, Math.min(14, getHeight() - 8));
            int height = Math.max(17, Math.min(getHeight() - 3, 20));
            int textWidth = metrics.stringWidth(text);
            int width = Math.min(getWidth() - 8, Math.max(46, textWidth + iconSize + 26));
            int x = 4;
            int y = Math.max(2, (getHeight() - height) / 2);
            if (cached) {
                double scaleX = renderScale(source.getTransform().getScaleX());
                double scaleY = renderScale(source.getTransform().getScaleY());
                BufferedImage image = cachedDecoration(width, height, iconSize, scaleX, scaleY);
                g.drawImage(image, x, y, width, height, null);
                paintText(g, x, y, height, iconSize, metrics);
            } else {
                paintVisual(g, x, y, width, height, iconSize, metrics);
            }
        } finally {
            g.dispose();
        }
    }

    private static double renderScale(double value) {
        return Double.isFinite(value) && (value > 0d) ? value : 1d;
    }

    private BufferedImage cachedDecoration(int width, int height, int iconSize, double scaleX, double scaleY) {
        int pixelWidth = Math.max(1, (int) Math.ceil(width * scaleX));
        int pixelHeight = Math.max(1, (int) Math.ceil(height * scaleY));
        VisualKey key = new VisualKey(kind, String.valueOf(value), selected, width, height, iconSize,
                pixelWidth, pixelHeight);
        BufferedImage image = cache.get(key);
        if (image != null) {
            return image;
        }
        image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.scale(pixelWidth / (double) width, pixelHeight / (double) height);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintDecoration(g, 0, 0, width, height, iconSize);
        } finally {
            g.dispose();
        }
        cache.put(key, image);
        return image;
    }

    private void paintVisual(Graphics2D g, int x, int y, int width, int height, int iconSize, FontMetrics metrics) {
        paintDecoration(g, x, y, width, height, iconSize);
        paintText(g, x, y, height, iconSize, metrics);
    }

    private void paintDecoration(Graphics2D g, int x, int y, int width, int height, int iconSize) {
        Color top = selected ? palette.selectedTop() : palette.top();
        Color bottom = selected ? palette.selectedBottom() : palette.bottom();
        Shape pill = new RoundRectangle2D.Double(x, y, width, height, height, height);
        Paint oldPaint = g.getPaint();
        g.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
        g.fill(pill);
        g.setComposite(AlphaComposite.SrcOver.derive(0.45f));
        g.setPaint(new GradientPaint(x, y, Color.WHITE, x + width, y + height, new Color(255, 255, 255, 0)));
        g.fill(new RoundRectangle2D.Double(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height / 2.0),
                height, height));
        g.setComposite(AlphaComposite.SrcOver);
        g.setPaint(oldPaint);
        g.setColor(palette.border());
        g.draw(pill);

        int iconX = x + 7;
        int iconY = y + (height - iconSize) / 2;
        kind.paintIcon(g, iconX, iconY, iconSize, value, palette);
    }

    private void paintText(Graphics2D g, int x, int y, int height, int iconSize, FontMetrics metrics) {
        Color foreground = selected ? palette.selectedForeground() : palette.foreground();
        int iconX = x + 7;
        g.setColor(foreground);
        int baseline = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(text, iconX + iconSize + 8, baseline);
    }

    enum Kind {
        STATE {
            @Override
            Palette palette(Object value) {
                if (value == ObservationState.OPEN) {
                    return new Palette(0xe7f6ee, 0xb7e4ca, 0x137a4d, 0x0f8a5f);
                } else if (value == ObservationState.RUNNING) {
                    return new Palette(0xe9f2ff, 0xb8d4ff, 0x1859a8, 0x1d6fd2);
                } else if (value == ObservationState.STALE) {
                    return new Palette(0xfff4de, 0xf8d28a, 0x8a4f00, 0xc77700);
                } else if (value == ObservationState.CLOSED) {
                    return new Palette(0xeff2f5, 0xd3d8df, 0x475467, 0x667085);
                }
                return Palette.neutral();
            }

            @Override
            void paintIcon(Graphics2D g, int x, int y, int size, Object value, Palette palette) {
                g.setPaint(new RadialGradientPaint(
                        x + size * 0.35f,
                        y + size * 0.30f,
                        Math.max(1f, size * 0.85f),
                        new float[] {0f, 0.72f, 1f},
                        new Color[] {palette.iconHighlight(), palette.accent(), palette.iconShadow()}
                ));
                if (value == ObservationState.RUNNING) {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(x + 2, y + 1);
                    triangle.lineTo(x + size - 1, y + size / 2.0);
                    triangle.lineTo(x + 2, y + size - 1);
                    triangle.closePath();
                    g.fill(triangle);
                } else if (value == ObservationState.STALE) {
                    Path2D diamond = new Path2D.Double();
                    diamond.moveTo(x + size / 2.0, y);
                    diamond.lineTo(x + size, y + size / 2.0);
                    diamond.lineTo(x + size / 2.0, y + size);
                    diamond.lineTo(x, y + size / 2.0);
                    diamond.closePath();
                    g.fill(diamond);
                } else if (value == ObservationState.CLOSED) {
                    g.fillRoundRect(x, y, size, size, 3, 3);
                } else {
                    g.fill(new Ellipse2D.Double(x, y, size, size));
                }
                g.setComposite(AlphaComposite.SrcOver.derive(0.38f));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x + 2, y + 2, Math.max(2, size / 2.2), Math.max(2, size / 2.2)));
                g.setComposite(AlphaComposite.SrcOver);
            }
        },
        REGION {
            @Override
            Palette palette(Object value) {
                return switch (String.valueOf(value)) {
                    case "North" -> new Palette(0xeef2ff, 0xc7d2fe, 0x3730a3, 0x4f46e5);
                    case "South" -> new Palette(0xffeceb, 0xffc7c2, 0x9f1d1d, 0xd92d20);
                    case "East" -> new Palette(0xe4f7f5, 0xa9e6df, 0x0f766e, 0x14a39b);
                    case "West" -> new Palette(0xf4ebff, 0xddc2ff, 0x6941c6, 0x7f56d9);
                    default -> Palette.neutral();
                };
            }

            @Override
            void paintIcon(Graphics2D g, int x, int y, int size, Object value, Palette palette) {
                double rotation = switch (String.valueOf(value)) {
                    case "East" -> Math.PI / 2.0;
                    case "South" -> Math.PI;
                    case "West" -> Math.PI * 1.5;
                    default -> 0.0;
                };
                Graphics2D copy = (Graphics2D) g.create();
                try {
                    copy.rotate(rotation, x + size / 2.0, y + size / 2.0);
                    copy.setPaint(new LinearGradientPaint(
                            x,
                            y,
                            x + size,
                            y + size,
                            new float[] {0f, 0.45f, 1f},
                            new Color[] {palette.iconHighlight(), palette.accent(), palette.iconShadow()},
                            MultipleGradientPaint.CycleMethod.NO_CYCLE
                    ));
                    Path2D arrow = new Path2D.Double();
                    arrow.moveTo(x + size / 2.0, y);
                    arrow.lineTo(x + size, y + size);
                    arrow.lineTo(x + size / 2.0, y + size - 3);
                    arrow.lineTo(x, y + size);
                    arrow.closePath();
                    copy.fill(arrow);
                    copy.setComposite(AlphaComposite.SrcOver.derive(0.45f));
                    copy.setColor(Color.WHITE);
                    copy.fill(new Ellipse2D.Double(x + size * 0.35, y + 2, size * 0.3, size * 0.3));
                    copy.setComposite(AlphaComposite.SrcOver);
                    copy.setColor(palette.iconShadow());
                    copy.setStroke(new BasicStroke(1.2f));
                    copy.drawLine(x + size / 2, y + 2, x + size / 2, y + size - 3);
                } finally {
                    copy.dispose();
                }
            }
        };

        abstract Palette palette(Object value);

        abstract void paintIcon(Graphics2D g, int x, int y, int size, Object value, Palette palette);
    }

    private record Palette(int backgroundRgb, int borderRgb, int foregroundRgb, int accentRgb) {

        static Palette neutral() {
            return new Palette(0xf2f4f7, 0xd0d5dd, 0x344054, 0x667085);
        }

        Color background() {
            return new Color(backgroundRgb);
        }

        Color top() {
            return tint(background(), 0.55f);
        }

        Color bottom() {
            return background();
        }

        Color border() {
            return new Color(borderRgb);
        }

        Color foreground() {
            return new Color(foregroundRgb);
        }

        Color accent() {
            return new Color(accentRgb);
        }

        Color iconHighlight() {
            return tint(accent(), 0.42f);
        }

        Color iconShadow() {
            return shade(accent(), 0.28f);
        }

        Color selectedBackground() {
            return new Color(0xffffff);
        }

        Color selectedTop() {
            return new Color(0xffffff);
        }

        Color selectedBottom() {
            return tint(background(), 0.45f);
        }

        Color selectedForeground() {
            return foreground();
        }

        private static Color tint(Color color, float amount) {
            int red = color.getRed() + Math.round((255 - color.getRed()) * amount);
            int green = color.getGreen() + Math.round((255 - color.getGreen()) * amount);
            int blue = color.getBlue() + Math.round((255 - color.getBlue()) * amount);
            return new Color(red, green, blue);
        }

        private static Color shade(Color color, float amount) {
            int red = Math.round(color.getRed() * (1f - amount));
            int green = Math.round(color.getGreen() * (1f - amount));
            int blue = Math.round(color.getBlue() * (1f - amount));
            return new Color(red, green, blue);
        }
    }

    private record VisualKey(
            Kind kind,
            String value,
            boolean selected,
            int width,
            int height,
            int iconSize,
            int pixelWidth,
            int pixelHeight
    ) {
    }
}
