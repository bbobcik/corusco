package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.collection.SortedList;
import cz.auderis.corusco.core.collection.FilteredList;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

/** Reproducible synthetic cleared-log snapshot benchmark; timings are observations, not gates. */
public final class DiagnosticsBenchmark {
    private DiagnosticsBenchmark() { }

    public static void main(String[] args) {
        System.out.println("java=" + System.getProperty("java.version") + " os=" + System.getProperty("os.name"));
        System.out.println("headless offscreen paint proxy; warmup=2 samples=5; times=ms allocations=bytes on EDT");
        for (int sample = -1; sample < 3; sample++) {
            boolean first = (sample & 1) == 0;
            shellPaint(first, sample);
            shellPaint(!first, sample);
        }
        for (int size : new int[] {10_000, 100_000}) {
            for (int sample = -2; sample < 5; sample++) {
                boolean first = (sample & 1) == 0;
                run(size, first, sample);
                run(size, !first, sample);
                coreEdit(size, sample);
            }
        }
    }

    private static void shellPaint(boolean synchronousLoad, int sample) {
        long start = System.nanoTime();
        SwingEdt.runAndWait(() -> {
            var runtime = new ShowcaseRuntime();
            try {
                if (synchronousLoad) {
                    runtime.loadPreparedFrame(TimeseriesDatabasePanel.prepareFrame(H2TimeseriesRepository.loadObservations()));
                }
                var shell = runtime.layer();
                shell.setSize(1220, 780);
                layoutTree(shell);
                var image = new BufferedImage(1220, 780, BufferedImage.TYPE_INT_ARGB);
                var graphics = image.createGraphics();
                try { shell.paint(graphics); } finally { graphics.dispose(); }
                if (sample >= 0) System.out.printf(java.util.Locale.ROOT,
                        "shell sample=%d variant=%s startupPaintProxy=%.3f%n", sample,
                        synchronousLoad ? "synchronous-h2" : "empty-shell", ms(System.nanoTime()-start));
            } finally { runtime.close(); }
        });
    }

    private static void layoutTree(java.awt.Container container) {
        container.doLayout();
        for (var child : container.getComponents()) {
            if (child instanceof java.awt.Container nested) layoutTree(nested);
        }
    }

    private static void coreEdit(int size, int sample) {
        var values = java.util.stream.IntStream.range(0, size).boxed().toList();
        var filteredSource = ObservableArrayList.of(values);
        var filtered = FilteredList.of(filteredSource, value -> value % 2 == 0);
        var sortedSource = ObservableArrayList.of(values);
        var sorted = SortedList.of(sortedSource, java.util.Comparator.<Integer>reverseOrder());
        var sortedFiltered = FilteredList.of(sorted, value -> value % 2 == 0);
        long start = System.nanoTime();
        filteredSource.set(size / 2, size + 2);
        long control = System.nanoTime();
        sortedSource.set(size / 2, size + 2);
        long end = System.nanoTime();
        if (sample >= 0) System.out.printf(java.util.Locale.ROOT,
                "n=%d coreEdit sample=%d filterOnly=%.3f sortedFiltered=%.3f visible=%d/%d%n",
                size, sample, ms(control-start), ms(end-control), filtered.size(), sortedFiltered.size());
        filtered.close();
        sortedFiltered.close();
        sorted.close();
    }

    private static void run(int size, boolean bulk, int sample) {
        long start = System.nanoTime();
        List<DiagnosticLogRow> snapshot = new ArrayList<>(size);
        String[] levels = {"DEBUG", "INFO", "WARN", "ERROR"};
        for (int i = 0; i < size; i++) {
            snapshot.add(new DiagnosticLogRow(1_766_000_000_000L + i, levels[i % levels.length], "log " + i));
        }
        long prepared = System.nanoTime();
        long queued = System.nanoTime();
        SwingEdt.runAndWait(() -> {
            long entered = System.nanoTime();
            var bean = ManagementFactory.getThreadMXBean();
            com.sun.management.ThreadMXBean allocation = bean instanceof com.sun.management.ThreadMXBean b
                    && b.isThreadAllocatedMemorySupported() ? b : null;
            if (allocation != null && !allocation.isThreadAllocatedMemoryEnabled()) allocation.setThreadAllocatedMemoryEnabled(true);
            long before = allocation == null ? -1 : allocation.getThreadAllocatedBytes(Thread.currentThread().threadId());
            var rows = ObservableArrayList.of(List.of(snapshot.getFirst()));
            var model = ObservableTableModel.of(rows, DiagnosticLogRowTableDescriptor.DESCRIPTOR);
            var table = new JTable(model);
            var sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            sorter.toggleSortOrder(0);
            long updateStart = System.nanoTime();
            if (bulk) rows.replaceContents(snapshot, StandardChangeOrigin.SYSTEM);
            else rows.batch(StandardChangeOrigin.SYSTEM, list -> {
                list.clear();
                snapshot.forEach(list::add);
            });
            long updated = System.nanoTime();
            sorter.setRowFilter(RowFilter.regexFilter("WARN|ERROR", 1));
            long filtered = System.nanoTime();
            table.setSize(800, 600);
            var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            var graphics = image.createGraphics();
            try { table.paint(graphics); } finally { graphics.dispose(); }
            long painted = System.nanoTime();
            long bytes = allocation == null ? -1 : allocation.getThreadAllocatedBytes(Thread.currentThread().threadId()) - before;
            if (sample >= 0) System.out.printf(java.util.Locale.ROOT,
                    "n=%d variant=%s sample=%d prep=%.3f edtDelay=%.3f sortedUpdate=%.3f filter=%.3f paintProxy=%.3f totalToPaint=%.3f allocated=%d visible=%d%n",
                    size, bulk ? "bulk" : "batch-add", sample, ms(prepared-start), ms(entered-queued),
                    ms(updated-updateStart), ms(filtered-updated), ms(painted-filtered), ms(painted-start), bytes, table.getRowCount());
            model.close();
        });
    }

    private static double ms(long nanos) { return nanos / 1_000_000.0; }
}
