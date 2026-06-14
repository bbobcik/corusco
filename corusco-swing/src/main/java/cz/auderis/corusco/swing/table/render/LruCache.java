package cz.auderis.corusco.swing.table.render;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small access-ordered cache used by one renderer instance.
 */
final class LruCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private final int maxSize;

    LruCache(int maxSize) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
