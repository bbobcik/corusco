package cz.auderis.corusco.core.data;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Key-based row selection that can represent query-wide "select all".
 *
 * <p>This record is the presenter-layer counterpart to Swing's view-row
 * selection. It stores row keys, not current indexes. That makes it suitable
 * for sorted, filtered, and windowed data sources where the same row may move
 * between view positions or may not be present in the currently loaded page.
 * Selection state can therefore survive refreshes and page changes as long as
 * the row key remains stable.</p>
 *
 * <p>{@link CoruscoRowSelectionMode#EXPLICIT_KEYS} stores the selected keys
 * directly and is appropriate for ordinary single or multi-row selection.
 * {@link CoruscoRowSelectionMode#ALL_MATCHING_QUERY} stores the query that
 * defines the selected universe plus excluded keys. That shape supports
 * large-result "select all" workflows without materializing every matching
 * key. {@link CoruscoRowSelectionMode#NONE} is an empty normalized state.</p>
 *
 * <p>The optional lead key represents keyboard focus or anchor selection in a
 * view. It is not a separate selected-key set. For explicit selections the
 * lead key must be one of the selected keys. For all-matching selections the
 * lead key may point at a currently focused matching row and may be cleared
 * when the visible page changes.</p>
 *
 * @param mode selection mode
 * @param query query that defines all-matching selection, or {@code null}
 * @param includedKeys explicitly selected keys
 * @param excludedKeys explicitly excluded keys for all-matching selection
 * @param leadKey lead/focus key, or {@code null}
 * @param <K> key type
 */
public record CoruscoRowSelection<K extends @NonNull Object>(
        CoruscoRowSelectionMode mode,
        @Nullable CoruscoDataQuery query,
        Set<K> includedKeys,
        Set<K> excludedKeys,
        @Nullable K leadKey
) {

    /**
     * Creates a selection.
     *
     * <p>The constructor normalizes key sets with {@link Set#copyOf(java.util.Collection)} and
     * validates that the supplied fields match the mode. Empty selections must
     * not carry keys, query, or lead state. Explicit selections must not carry
     * an all-matching query or excluded keys. All-matching selections must
     * carry the query that defines their selected universe.</p>
     *
     * @param mode mode
     * @param query all-matching query
     * @param includedKeys included keys
     * @param excludedKeys excluded keys
     * @param leadKey lead key
     */
    public CoruscoRowSelection {
        Objects.requireNonNull(mode, "mode");
        includedKeys = Set.copyOf(Objects.requireNonNull(includedKeys, "includedKeys"));
        excludedKeys = Set.copyOf(Objects.requireNonNull(excludedKeys, "excludedKeys"));
        if (mode == CoruscoRowSelectionMode.NONE) {
            if (!includedKeys.isEmpty() || !excludedKeys.isEmpty() || query != null || leadKey != null) {
                throw new IllegalArgumentException("none selection must not carry keys, query, or lead key");
            }
        } else if (mode == CoruscoRowSelectionMode.EXPLICIT_KEYS) {
            if (query != null || !excludedKeys.isEmpty()) {
                throw new IllegalArgumentException("explicit selection must not carry query or excluded keys");
            }
            if (leadKey != null && !includedKeys.contains(leadKey)) {
                throw new IllegalArgumentException("lead key must be selected");
            }
        } else if (query == null) {
            throw new IllegalArgumentException("all-matching selection requires a query");
        }
    }

    /**
     * Empty selection.
     *
     * <p>The returned instance contains no key sets, query, or lead key. Use
     * this instead of manually constructing an empty explicit selection when
     * the intended state is "nothing selected."</p>
     *
     * @param <K> key type
     * @return empty selection
     */
    public static <K extends @NonNull Object> CoruscoRowSelection<K> none() {
        return new CoruscoRowSelection<>(CoruscoRowSelectionMode.NONE, null, Set.of(), Set.of(), null);
    }

    /**
     * Creates explicit-key selection.
     *
     * <p>The supplied key set is copied. The optional lead key must be included
     * in {@code keys}; otherwise the constructor rejects the state because a
     * view focus key that is not selected would be ambiguous for explicit
     * selection.</p>
     *
     * @param keys selected keys
     * @param leadKey lead key
     * @param <K> key type
     * @return selection
     */
    public static <K extends @NonNull Object> CoruscoRowSelection<K> explicit(
            Set<K> keys,
            @Nullable K leadKey
    ) {
        return new CoruscoRowSelection<>(CoruscoRowSelectionMode.EXPLICIT_KEYS, null, keys, Set.of(), leadKey);
    }

    /**
     * Creates query-wide selection with excluded keys.
     *
     * <p>This is the scalable form used by "select all results" actions. The
     * query names the selected universe, and {@code excludedKeys} records rows
     * the user deselected afterward. The selected keys are intentionally not
     * materialized.</p>
     *
     * @param query query
     * @param excludedKeys excluded keys
     * @param leadKey lead key
     * @param <K> key type
     * @return selection
     */
    public static <K extends @NonNull Object> CoruscoRowSelection<K> allMatching(
            CoruscoDataQuery query,
            Set<K> excludedKeys,
            @Nullable K leadKey
    ) {
        return new CoruscoRowSelection<>(CoruscoRowSelectionMode.ALL_MATCHING_QUERY, query, Set.of(), excludedKeys, leadKey);
    }

    /**
     * Indicates whether a key is selected.
     *
     * <p>For all-matching selections this method answers according to the
     * exclusion set only. It assumes the caller is asking about a key that
     * belongs to the selection's query result; this record does not evaluate
     * the query against the row key.</p>
     *
     * @param key key
     * @return true if selected
     */
    public boolean contains(K key) {
        Objects.requireNonNull(key, "key");
        return switch (mode) {
            case NONE -> false;
            case EXPLICIT_KEYS -> includedKeys.contains(key);
            case ALL_MATCHING_QUERY -> !excludedKeys.contains(key);
        };
    }

    /**
     * Retains explicit selections that are visible in the current window.
     *
     * <p>All-matching selections are returned unchanged except that their lead
     * key is cleared if it is not visible. This preserves query-wide selection
     * semantics when only a small page is visible. Explicit selections are
     * intersected with {@code visibleKeys}; if no explicit key remains, the
     * result is {@link #none()}.</p>
     *
     * @param visibleKeys visible keys
     * @return adjusted selection
     */
    public CoruscoRowSelection<K> retainVisibleKeys(Set<K> visibleKeys) {
        Objects.requireNonNull(visibleKeys, "visibleKeys");
        if (mode == CoruscoRowSelectionMode.NONE) {
            return this;
        }
        if (mode == CoruscoRowSelectionMode.ALL_MATCHING_QUERY) {
            @Nullable K retainedLead = leadKey != null && visibleKeys.contains(leadKey) ? leadKey : null;
            if (Objects.equals(retainedLead, leadKey)) {
                return this;
            }
            return new CoruscoRowSelection<>(mode, query, includedKeys, excludedKeys, retainedLead);
        }
        LinkedHashSet<K> retained = LinkedHashSet.newLinkedHashSet(Math.min(includedKeys.size(), visibleKeys.size()));
        for (K key : includedKeys) {
            if (visibleKeys.contains(key)) {
                retained.add(key);
            }
        }
        @Nullable K retainedLead = leadKey != null && retained.contains(leadKey) ? leadKey : null;
        if (retained.isEmpty()) {
            return none();
        }
        return explicit(retained, retainedLead);
    }
}
