package cz.auderis.corusco.core.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One precise observable-list mutation.
 *
 * <p>{@link ObservableList} implementations publish these values so adapters
 * can update derived views, Swing list models, and table models without
 * reloading the entire list. Indices are expressed in the coordinate system of
 * the list that emits the change, not necessarily an underlying storage list.
 * For insertions the index is the first inserted position after mutation. For
 * removals the index is the position the removed elements occupied before
 * mutation. For replacements the index is the replaced position. Move indices
 * are before/after positions for the same element.</p>
 *
 * <p>Change records are immutable value objects. Element references are not
 * copied, but element lists are defensively wrapped so listeners cannot mutate
 * the delivered change description.</p>
 *
 * @param <E> element type
 */
public sealed interface ListChange<E>
        permits ListChange.Inserted, ListChange.Removed, ListChange.Replaced, ListChange.Moved, ListChange.Cleared {

    /**
     * Inserted contiguous elements.
     *
     * @param index first inserted index
     * @param elements inserted elements
     * @param <E> element type
     */
    record Inserted<E>(int index, List<E> elements) implements ListChange<E> {
        public Inserted {
            elements = immutableCopy(elements);
        }
    }

    /**
     * Removed contiguous elements.
     *
     * @param index index occupied before removal
     * @param elements removed elements
     * @param <E> element type
     */
    record Removed<E>(int index, List<E> elements) implements ListChange<E> {
        public Removed {
            elements = immutableCopy(elements);
        }
    }

    /**
     * Replaced one element.
     *
     * @param index replaced index
     * @param oldElement previous element
     * @param newElement new element
     * @param <E> element type
     */
    record Replaced<E>(int index, E oldElement, E newElement) implements ListChange<E> {
    }

    /**
     * Moved one element.
     *
     * @param fromIndex original index
     * @param toIndex new index
     * @param element moved element
     * @param <E> element type
     */
    record Moved<E>(int fromIndex, int toIndex, E element) implements ListChange<E> {
    }

    /**
     * Cleared all elements.
     *
     * @param elements elements removed by clear
     * @param <E> element type
     */
    record Cleared<E>(List<E> elements) implements ListChange<E> {
        public Cleared {
            elements = immutableCopy(elements);
        }
    }

    private static <E> List<E> immutableCopy(List<E> elements) {
        return Collections.unmodifiableList(new ArrayList<>(elements));
    }
}
