package cz.auderis.corusco.swing.table.render;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Base renderer that lets subclasses choose text while this class owns bitmap
 * painting and cache invalidation.
 *
 * <p>The class still delegates normal background, border, selection, and focus
 * handling to {@link DefaultTableCellRenderer}. When bitmap caching is enabled,
 * it temporarily clears the label text, lets the superclass paint the cell
 * chrome, and then draws cached text images inside the same text rectangle that
 * Swing would have used for label text. This keeps look-and-feel state outside
 * the bitmap while avoiding repeated glyph painting for stable cell text.</p>
 *
 * <p>Instances are ordinary Swing renderers: they are reused by JTable during
 * painting, are EDT-confined, and must not be shared between unrelated tables.
 * Cache keys intentionally include the current font, colors, selected/focused
 * state, enabled state, row height, and graphics transform scale so cached text
 * is discarded when those visual inputs change.</p>
 */
abstract class BitmapTextRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    private final boolean tabularNumbers;
    private final LruCache<TextVisualKey, BitmapText> cache;
    private String paintText = "";

    BitmapTextRenderer(boolean tabularNumbers, int cacheSize) {
        this.tabularNumbers = tabularNumbers;
        this.cache = new LruCache<>(cacheSize);
        setHorizontalAlignment(JLabel.LEADING);
    }

    @Override
    public java.awt.Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.selected = isSelected;
        this.focused = hasFocus;
        if (tabularNumbers) {
            setFont(tabularFont(getFont()));
        }
        paintText = text(value);
        setText(paintText);
        return this;
    }

    final int cachedImageCount() {
        return cache.size();
    }

    /**
     * Returns a cached bitmap for text under the current visual state.
     */
    final BitmapText cachedText(Graphics2D graphics, String text) {
        AffineTransform transform = graphics.getTransform();
        double scaleX = Math.abs(transform.getScaleX());
        double scaleY = Math.abs(transform.getScaleY());
        TextRenderingSettings renderingSettings = TextRenderingSettings.from(graphics);
        TextVisualKey key = new TextVisualKey(
                text,
                getFont(),
                getForeground(),
                getBackground(),
                selected,
                focused,
                isEnabled(),
                getHeight(),
                scaleX,
                scaleY,
                renderingSettings
        );
        BitmapText bitmap = cache.get(key);
        if (bitmap == null) {
            bitmap = BitmapText.render(
                    text,
                    getFont(),
                    getForeground(),
                    getBackground(),
                    getFontMetrics(getFont()),
                    renderingSettings,
                    scaleX,
                    scaleY
            );
            cache.put(key, bitmap);
        }
        return bitmap;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (usesBitmapCache() && graphics instanceof Graphics2D graphics2D) {
            String text = paintText;
            setText("");
            super.paintComponent(graphics);
            setText(text);
            paintBitmapText(graphics2D, text);
            return;
        }
        super.paintComponent(graphics);
    }

    /**
     * Paints text as one cached bitmap in the current label text rectangle.
     */
    void paintBitmapText(Graphics2D graphics, String text) {
        Rectangle view = textViewRectangle();
        BitmapText bitmap = cachedText(graphics, text);
        int y = view.y + Math.max(0, (view.height - bitmap.height()) / 2);
        graphics.drawImage(bitmap.image(), view.x, y, bitmap.width(), bitmap.height(), null);
    }

    /**
     * Resolves the text rectangle using Swing's compound-label layout rules.
     */
    Rectangle textViewRectangle() {
        Insets insets = getInsets();
        Rectangle view = new Rectangle(
                insets.left,
                insets.top,
                Math.max(0, getWidth() - insets.left - insets.right),
                Math.max(0, getHeight() - insets.top - insets.bottom)
        );
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        FontMetrics metrics = getFontMetrics(getFont());
        Icon icon = getIcon();
        SwingUtilities.layoutCompoundLabel(
                this,
                metrics,
                paintText,
                icon,
                getVerticalAlignment(),
                getHorizontalAlignment(),
                getVerticalTextPosition(),
                getHorizontalTextPosition(),
                view,
                iconRect,
                textRect,
                getIconTextGap()
        );
        return textRect;
    }

    /**
     * Converts the model value supplied by JTable to display text.
     */
    abstract String text(Object value);

    /**
     * Returns whether this renderer should paint text through bitmap cache.
     */
    abstract boolean usesBitmapCache();

    boolean selected;

    boolean focused;

    private static Font tabularFont(Font font) {
        // AWT has no public portable TextAttribute equivalent for CSS 'tnum'.
        return font;
    }
}
