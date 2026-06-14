package cz.auderis.corusco.swing.table.render;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Configures rendering of epoch timestamp table cells.
 *
 * <p>The timestamp renderer accepts {@code long} and {@link Long} values. It
 * first interprets the number using {@link EpochUnit}, then formats the
 * resulting instant in {@link #zoneId()} using {@link #pattern()}. Values of
 * other runtime types are rendered with {@link String#valueOf(Object)} so a
 * misconfigured table remains visible rather than failing during painting.</p>
 *
 * <p>The formatter pattern is deliberately explicit. The renderer does not
 * infer locale-sensitive formats from the table or from user preferences.
 * Fixed-width patterns such as {@code yyyy-MM-dd HH:mm:ss.SSS} work best for
 * dense business tables because they are visually stable and can benefit from
 * bitmap-prefix caching. The constructor validates the pattern through {@link
 * DateTimeFormatter#ofPattern(String, Locale)} using {@link Locale#ROOT}.</p>
 *
 * <p>When {@link #bitmapPrefixCache()} is {@code false}, the renderer still
 * centralizes epoch conversion and formatting but lets Swing paint text
 * normally. When it is {@code true}, the renderer paints text through a bounded
 * bitmap cache owned by one renderer instance. For the default fixed pattern,
 * the cache stores the date/hour/minute prefix separately from the changing
 * suffix. Other patterns render through a whole-text bitmap cache because the
 * implementation cannot safely infer reusable boundaries.</p>
 *
 * <p>{@link #tabularNumbers()} is a preference rather than a guarantee. Swing's
 * public font API does not expose a portable CSS-equivalent
 * {@code font-feature-settings: 'tnum'} switch. The renderer therefore falls
 * back to the active font when tabular OpenType features cannot be requested
 * through supported Java APIs.</p>
 *
 * @param epochUnit unit of the incoming {@code long} or {@link Long} value
 * @param zoneId zone used to turn the epoch value into local date-time fields
 * @param pattern fixed date-time pattern
 * @param tabularNumbers whether to request OpenType tabular numbers from the
 *        active font
 * @param bitmapPrefixCache whether to paint fixed timestamp text through a
 *        segmented bitmap cache
 * @param cacheSize maximum cached bitmap segments retained by one renderer
 * @param nullText text used for {@code null} cell values
 */
public record TimestampRendererOptions(
        EpochUnit epochUnit,
        ZoneId zoneId,
        String pattern,
        boolean tabularNumbers,
        boolean bitmapPrefixCache,
        int cacheSize,
        String nullText
) {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final int DEFAULT_CACHE_SIZE = 512;

    /**
     * Creates timestamp renderer options.
     *
     * @throws NullPointerException if {@code epochUnit}, {@code zoneId},
     *         {@code pattern}, or {@code nullText} is {@code null}
     * @throws IllegalArgumentException if {@code pattern} is blank, cannot be
     *         parsed as a date-time formatter pattern, or {@code cacheSize} is
     *         not greater than zero
     */
    public TimestampRendererOptions {
        Objects.requireNonNull(epochUnit, "epochUnit");
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(nullText, "nullText");
        if (pattern.isBlank()) {
            throw new IllegalArgumentException("pattern must not be blank");
        }
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be greater than zero");
        }
        DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }

    /**
     * Returns default millisecond timestamp options in the system zone.
     *
     * <p>The defaults use {@code yyyy-MM-dd HH:mm:ss.SSS}, request tabular
     * numbers when supported, disable bitmap-prefix caching, retain up to 512
     * bitmap entries if caching is later enabled through a copied options
     * value, and render {@code null} as an empty string.</p>
     *
     * @return default options
     */
    public static TimestampRendererOptions defaults() {
        return new TimestampRendererOptions(
                EpochUnit.MILLIS,
                ZoneId.systemDefault(),
                DEFAULT_PATTERN,
                true,
                false,
                DEFAULT_CACHE_SIZE,
                ""
        );
    }

    DateTimeFormatter formatter() {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT).withZone(zoneId);
    }

    int prefixLength() {
        return DEFAULT_PATTERN.equals(pattern) ? 16 : 0;
    }
}
