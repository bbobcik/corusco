package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.collection.FilteredList;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.collection.SortedList;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.IntStream;

/** Single-thread relative edit timings, excluding view construction; not a timing test. */
public final class CollectionEditBenchmark {
    private record Row(int key, int id) { }
    private CollectionEditBenchmark() { }

    public static void main(String[] args) {
        System.out.printf("java=%s warmup=4 samples=9 cycles=10 allocation=calling-thread bytes%n", System.getProperty("java.version"));
        for (int size : new int[] {10_000, 100_000}) {
            for (int sample = -4; sample < 9; sample++) {
                for (int i = 0; i < 3; i++) {
                    int variant = (sample & 1) == 0 ? i : 2 - i;
                    measure(size, sample, variant);
                }
            }
        }
    }

    private static void measure(int size, int sample, int variant) {
        var source = ObservableArrayList.of(IntStream.range(0, size).mapToObj(i -> new Row(i * 2, i)).toList());
        var sorted = variant == 0 ? null : SortedList.of(source, Comparator.comparingInt(Row::key));
        var filtered = variant == 0 ? FilteredList.of(source, row -> row.id() % 2 == 0)
                : variant == 2 ? FilteredList.of(sorted, row -> row.id() % 2 == 0) : null;
        int index = size / 2;
        Row original = source.get(index);
        Row sameKey = new Row(original.key(), -1);
        Row relocated = new Row(size * 2 + 2, -2);
        Row inserted = new Row(original.key() - 1, -4);
        var managementBean = ManagementFactory.getThreadMXBean();
        var bean = managementBean instanceof com.sun.management.ThreadMXBean allocationBean
                && allocationBean.isThreadAllocatedMemorySupported()
                && allocationBean.isThreadAllocatedMemoryEnabled() ? allocationBean : null;
        long thread = Thread.currentThread().threadId();
        for (String operation : new String[] {"insertRemove", "sameIndex", "relocate"}) {
            long allocated = bean == null ? -1 : bean.getThreadAllocatedBytes(thread);
            long start = System.nanoTime();
            for (int i = 0; i < 10; i++) {
                switch (operation) {
                    case "insertRemove" -> { source.add(index, inserted); source.remove(index); }
                    case "sameIndex" -> { source.set(index, sameKey); source.set(index, original); }
                    case "relocate" -> { source.set(index, relocated); source.set(index, original); }
                    default -> throw new AssertionError(operation);
                }
            }
            long elapsed = System.nanoTime() - start;
            allocated = bean == null ? -1 : bean.getThreadAllocatedBytes(thread) - allocated;
            if (sample >= 0) System.out.printf(Locale.ROOT,
                    "size=%d sample=%d variant=%s op=%s ms=%.3f bytes=%d%n", size, sample,
                    new String[] {"filter", "sorted", "sortedFilter"}[variant], operation, elapsed / 1_000_000.0, allocated);
        }
        if (filtered != null) filtered.close();
        if (sorted != null) sorted.close();
    }
}
