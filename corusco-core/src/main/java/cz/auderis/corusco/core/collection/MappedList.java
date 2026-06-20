package cz.auderis.corusco.core.collection;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Read-only mapped list view over an {@link ObservableList}.
 *
 * <p>This type preserves the list-oriented API while the mapping implementation
 * lives in {@link MappedReadableCollection}. Direct mutation methods throw
 * {@link UnsupportedOperationException}; mutate the source list instead.</p>
 *
 * <p>Use {@link MappedReadableCollection} directly when a consumer only needs
 * the common read-only collection contract. Use this type for existing APIs
 * that still require {@link ObservableList} even though the projection itself
 * is read-only.</p>
 *
 * @param <S> source element type
 * @param <T> mapped element type
 */
public final class MappedList<S, T> extends MappedReadableCollection<S, T> implements ObservableList<T> {

    /**
     * Creates a mapped list view.
     *
     * @param source source list
     * @param mapper element mapper
     */
    public MappedList(ObservableList<S> source, Function<? super S, ? extends T> mapper) {
        super(source, mapper);
    }

    /**
     * Creates a mapped list view.
     *
     * @param source source list
     * @param mapper element mapper
     * @param <S> source element type
     * @param <T> mapped element type
     * @return mapped list
     */
    public static <S, T> MappedList<S, T> of(
            ObservableList<S> source,
            Function<? super S, ? extends T> mapper
    ) {
        return new MappedList<>(source, mapper);
    }

    @Override
    public void add(T element) {
        throw readOnly();
    }

    @Override
    public void add(int index, T element) {
        throw readOnly();
    }

    @Override
    public T set(int index, T element) {
        throw readOnly();
    }

    @Override
    public T remove(int index) {
        throw readOnly();
    }

    @Override
    public void move(int fromIndex, int toIndex) {
        throw readOnly();
    }

    @Override
    public void clear() {
        throw readOnly();
    }

    @Override
    public void batch(Consumer<ObservableList<T>> work) {
        throw readOnly();
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("MappedList is a read-only view; mutate the source list");
    }
}
