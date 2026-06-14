package cz.auderis.corusco.swing.table.render;

import java.util.Objects;

/**
 * Configures cached rendering of finite state table cells.
 *
 * <p>The state renderer is intended for small value domains: booleans, enum
 * constants, and application state objects with stable {@code toString()}
 * output. It converts each value to text and, when {@link #bitmapCache()} is
 * enabled, paints that text through a bounded bitmap cache owned by one
 * renderer instance. The cache is keyed by both value text and relevant visual
 * state so selected, focused, disabled, or differently styled cells do not
 * reuse incompatible images.</p>
 *
 * <p>Boolean rendering is configured explicitly through {@link #trueText()} and
 * {@link #falseText()}. Enum rendering uses {@link Enum#name()} by default so
 * technical state columns stay stable even if an enum overrides
 * {@link Object#toString()}. Set {@link #enumNames()} to {@code false} when an
 * enum's {@code toString()} is the intended display text. The renderer does
 * not perform resource lookup or localization.</p>
 *
 * <p>These options describe rendering only. They do not affect sorting,
 * filtering, editing, table state persistence, or the semantic value returned
 * by {@link cz.auderis.corusco.swing.table.ObservableTableModel}.</p>
 *
 * @param trueText text used for {@link Boolean#TRUE}
 * @param falseText text used for {@link Boolean#FALSE}
 * @param nullText text used for {@code null} cell values
 * @param enumNames whether enum constants render through {@link Enum#name()}
 *        instead of {@link Object#toString()}
 * @param bitmapCache whether to paint state text through a bounded bitmap cache
 * @param cacheSize maximum cached bitmap entries retained by one renderer
 */
public record StateRendererOptions(
        String trueText,
        String falseText,
        String nullText,
        boolean enumNames,
        boolean bitmapCache,
        int cacheSize
) {

    private static final int DEFAULT_CACHE_SIZE = 128;

    /**
     * Creates state renderer options.
     *
     * @throws NullPointerException if {@code trueText}, {@code falseText}, or
     *         {@code nullText} is {@code null}
     * @throws IllegalArgumentException if {@code cacheSize} is not greater than
     *         zero
     */
    public StateRendererOptions {
        Objects.requireNonNull(trueText, "trueText");
        Objects.requireNonNull(falseText, "falseText");
        Objects.requireNonNull(nullText, "nullText");
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be greater than zero");
        }
    }

    /**
     * Returns default state renderer options.
     *
     * <p>The defaults render booleans as {@code true} and {@code false}, render
     * {@code null} as an empty string, use enum constant names, enable bitmap
     * caching, and retain up to 128 cached entries per renderer.</p>
     *
     * @return default options
     */
    public static StateRendererOptions defaults() {
        return new StateRendererOptions("true", "false", "", true, true, DEFAULT_CACHE_SIZE);
    }
}
