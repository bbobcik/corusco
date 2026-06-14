package cz.auderis.corusco.swing.table.render;

import java.awt.Graphics2D;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Table-cell renderer for numeric epoch timestamps.
 *
 * <p>The renderer keeps formatting policy in {@link TimestampRendererOptions}.
 * It accepts any {@link Number} at paint time because Swing may supply boxed
 * primitive values from different model implementations; non-numeric values are
 * rendered visibly with {@link String#valueOf(Object)} rather than throwing
 * during painting.</p>
 */
final class TimestampTableCellRenderer extends BitmapTextRenderer {

    private static final long serialVersionUID = 1L;

    private final TimestampRendererOptions options;
    private final DateTimeFormatter formatter;

    TimestampTableCellRenderer(TimestampRendererOptions options) {
        super(options.tabularNumbers(), options.cacheSize());
        this.options = options;
        this.formatter = options.formatter();
    }

    @Override
    String text(Object value) {
        if (value == null) {
            return options.nullText();
        }
        if (!(value instanceof Number number)) {
            return String.valueOf(value);
        }
        Instant instant = options.epochUnit().toInstant(number.longValue());
        return formatter.format(instant);
    }

    @Override
    boolean usesBitmapCache() {
        return options.bitmapPrefixCache();
    }

    @Override
    void paintBitmapText(Graphics2D graphics, String text) {
        int prefixLength = options.prefixLength();
        if (prefixLength <= 0 || text.length() <= prefixLength) {
            super.paintBitmapText(graphics, text);
            return;
        }
        java.awt.Rectangle view = textViewRectangle();
        BitmapText prefix = cachedText(graphics, text.substring(0, prefixLength));
        BitmapText suffix = cachedText(graphics, text.substring(prefixLength));
        int prefixY = view.y + Math.max(0, (view.height - prefix.image().getHeight()) / 2);
        int suffixY = view.y + Math.max(0, (view.height - suffix.image().getHeight()) / 2);
        graphics.drawImage(prefix.image(), view.x, prefixY, null);
        graphics.drawImage(suffix.image(), view.x + prefix.width(), suffixY, null);
    }
}
