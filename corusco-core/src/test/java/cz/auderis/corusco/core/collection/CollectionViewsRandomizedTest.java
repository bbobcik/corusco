package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.SimpleValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-style tests that drive derived list views with random operation
 * sequences and compare them with a plain {@link List} model.
 *
 * <p>After every step each view must equal the model computed from the source
 * snapshot, and replaying the change sets a view has emitted onto a plain list
 * must reproduce the view, so events and state never disagree.</p>
 */
class CollectionViewsRandomizedTest {

    private static final int STEPS = 400;

    private record Row(int key, int id) { }

    @ParameterizedTest
    @ValueSource(longs = {1L, 7L, 42L, 20261114L})
    void stableDistinctKeysAndReplayAcrossMixedChanges(long seed) {
        Random random = new Random(seed);
        ObservableArrayList<Row> source = ObservableArrayList.empty();
        SortedList<Row> sorted = SortedList.of(source, Comparator.comparingInt(Row::key));
        FilteredList<Row> filtered = FilteredList.of(sorted, row -> row.id() % 2 == 0);
        List<Row> sortedReplay = new ArrayList<>();
        List<Row> filteredReplay = new ArrayList<>();
        sorted.subscribe(changes -> replay(changes, sortedReplay));
        filtered.subscribe(changes -> replay(changes, filteredReplay));
        for (int step = 0; step < STEPS; step++) {
            int id = step;
            int size = source.size();
            int choice = size == 0 ? 0 : random.nextInt(5);
            switch (choice) {
                case 0 -> source.add(random.nextInt(size + 1), new Row(random.nextInt(4), id));
                case 1 -> source.remove(random.nextInt(size));
                case 2 -> source.set(random.nextInt(size), new Row(random.nextInt(4), id));
                case 3 -> source.move(random.nextInt(size), random.nextInt(size));
                default -> source.batch(list -> {
                    list.add(random.nextInt(list.size() + 1), new Row(random.nextInt(4), id));
                    list.remove(random.nextInt(list.size()));
                });
            }
            List<Row> expected = source.snapshot().stream().sorted(Comparator.comparingInt(Row::key)).toList();
            assertThat(sorted.snapshot()).as("seed %s step %s", seed, step).isEqualTo(expected);
            assertThat(sortedReplay).isEqualTo(expected);
            List<Row> expectedFiltered = expected.stream().filter(row -> row.id() % 2 == 0).toList();
            assertThat(filtered.snapshot()).isEqualTo(expectedFiltered);
            assertThat(filteredReplay).isEqualTo(expectedFiltered);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 7L, 42L, 2026L, 20261114L, -3L, 987654321L, 5L})
    void filteredAndSortedViewsMatchPlainListModel(long seed) {
        Random random = new Random(seed);
        ObservableArrayList<Integer> source = ObservableArrayList.of(randomValues(random, random.nextInt(20)));
        SimpleValue<Predicate<? super Integer>> predicate = SimpleValue.of(value -> value % 2 == 0);
        FilteredList<Integer> filtered = FilteredList.of(source, predicate);
        SortedList<Integer> sorted = SortedList.of(source, Comparator.naturalOrder());
        SortedList<Integer> sortedFiltered = SortedList.of(filtered, Comparator.reverseOrder());
        FilteredList<Integer> filteredSorted = FilteredList.of(sorted, value -> value > 25);

        List<Integer> filteredReplay = new ArrayList<>(filtered.snapshot());
        List<Integer> sortedReplay = new ArrayList<>(sorted.snapshot());
        List<Integer> sortedFilteredReplay = new ArrayList<>(sortedFiltered.snapshot());
        List<Integer> filteredSortedReplay = new ArrayList<>(filteredSorted.snapshot());
        filtered.subscribe(changes -> replay(changes, filteredReplay));
        sorted.subscribe(changes -> replay(changes, sortedReplay));
        sortedFiltered.subscribe(changes -> replay(changes, sortedFilteredReplay));
        filteredSorted.subscribe(changes -> replay(changes, filteredSortedReplay));

        List<String> log = new ArrayList<>();
        for (int step = 0; step < STEPS; step++) {
            log.add(randomOperation(random, source, predicate));
            String context = "seed " + seed + ", operations " + log;
            List<Integer> model = source.snapshot();
            Predicate<? super Integer> currentPredicate = predicate.value();

            List<Integer> expectedFiltered = model.stream().filter(currentPredicate::test).toList();
            List<Integer> expectedSorted = model.stream().sorted().toList();
            List<Integer> expectedSortedFiltered = expectedFiltered.stream().sorted(Comparator.reverseOrder()).toList();
            List<Integer> expectedFilteredSorted = expectedSorted.stream().filter(value -> value > 25).toList();

            assertThat(filtered.snapshot()).as(context).isEqualTo(expectedFiltered);
            assertThat(sorted.snapshot()).as(context).isEqualTo(expectedSorted);
            assertThat(sortedFiltered.snapshot()).as(context).isEqualTo(expectedSortedFiltered);
            assertThat(filteredSorted.snapshot()).as(context).isEqualTo(expectedFilteredSorted);
            assertThat(filteredReplay).as(context).isEqualTo(expectedFiltered);
            assertThat(sortedReplay).as(context).isEqualTo(expectedSorted);
            assertThat(sortedFilteredReplay).as(context).isEqualTo(expectedSortedFiltered);
            assertThat(filteredSortedReplay).as(context).isEqualTo(expectedFilteredSorted);
            assertThat(filtered.size()).as(context).isEqualTo(expectedFiltered.size());
            assertThat(sorted.size()).as(context).isEqualTo(expectedSorted.size());
        }
    }

    private static String randomOperation(
            Random random,
            ObservableArrayList<Integer> source,
            SimpleValue<Predicate<? super Integer>> predicate
    ) {
        int size = source.size();
        int choice = size == 0 ? random.nextInt(2) : random.nextInt(9);
        switch (choice) {
            case 0, 1 -> {
                int index = random.nextInt(size + 1);
                int value = random.nextInt(50);
                source.add(index, value);
                return "add(" + index + "," + value + ")";
            }
            case 2 -> {
                int index = random.nextInt(size);
                source.remove(index);
                return "remove(" + index + ")";
            }
            case 3 -> {
                int index = random.nextInt(size);
                int value = random.nextInt(50);
                source.set(index, value);
                return "set(" + index + "," + value + ")";
            }
            case 4 -> {
                int from = random.nextInt(size);
                int to = random.nextInt(size);
                source.move(from, to);
                return "move(" + from + "," + to + ")";
            }
            case 5 -> {
                if (random.nextInt(10) == 0) {
                    source.clear();
                    return "clear()";
                }
                int value = random.nextInt(50);
                source.add(value);
                return "append(" + value + ")";
            }
            case 6 -> {
                int divisor = 2 + random.nextInt(3);
                int remainder = random.nextInt(divisor);
                predicate.setValue(value -> value % divisor == remainder);
                return "predicate(%" + divisor + "==" + remainder + ")";
            }
            default -> {
                List<String> inner = new ArrayList<>();
                source.batch(list -> {
                    int operations = 1 + random.nextInt(4);
                    for (int i = 0; i < operations; i++) {
                        int currentSize = list.size();
                        int kind = currentSize == 0 ? 0 : random.nextInt(4);
                        switch (kind) {
                            case 0 -> {
                                int index = random.nextInt(currentSize + 1);
                                int value = random.nextInt(50);
                                list.add(index, value);
                                inner.add("add(" + index + "," + value + ")");
                            }
                            case 1 -> {
                                int index = random.nextInt(currentSize);
                                list.remove(index);
                                inner.add("remove(" + index + ")");
                            }
                            case 2 -> {
                                int index = random.nextInt(currentSize);
                                int value = random.nextInt(50);
                                list.set(index, value);
                                inner.add("set(" + index + "," + value + ")");
                            }
                            default -> {
                                int from = random.nextInt(currentSize);
                                int to = random.nextInt(currentSize);
                                list.move(from, to);
                                inner.add("move(" + from + "," + to + ")");
                            }
                        }
                    }
                });
                return "batch" + inner;
            }
        }
    }

    private static List<Integer> randomValues(Random random, int count) {
        List<Integer> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(random.nextInt(50));
        }
        return values;
    }

    private static <E extends Object> void replay(ListChangeSet<E> changes, List<E> target) {
        for (ListChange<E> change : changes.changes()) {
            switch (change) {
                case ListChange.Inserted<E> inserted -> target.addAll(inserted.index(), inserted.elements());
                case ListChange.Removed<E> removed -> {
                    List<E> range = target.subList(removed.index(), removed.index() + removed.elements().size());
                    assertThat(range).isEqualTo(removed.elements());
                    range.clear();
                }
                case ListChange.Replaced<E> replaced -> {
                    assertThat(target.get(replaced.index())).isEqualTo(replaced.oldElement());
                    target.set(replaced.index(), replaced.newElement());
                }
                case ListChange.Moved<E> moved -> {
                    E element = target.remove(moved.fromIndex());
                    assertThat(element).isEqualTo(moved.element());
                    target.add(moved.toIndex(), element);
                }
                case ListChange.Cleared<E> cleared -> {
                    assertThat(target).isEqualTo(cleared.elements());
                    target.clear();
                }
            }
        }
    }
}
