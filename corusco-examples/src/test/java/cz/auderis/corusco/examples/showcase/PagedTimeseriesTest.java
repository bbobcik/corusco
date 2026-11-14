package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.data.CoruscoDataCount;
import cz.auderis.corusco.core.data.CoruscoDataFilter;
import cz.auderis.corusco.core.data.CoruscoDataFilterOperator;
import cz.auderis.corusco.core.data.CoruscoDataPage;
import cz.auderis.corusco.core.data.CoruscoDataPhase;
import cz.auderis.corusco.core.data.CoruscoDataQuery;
import cz.auderis.corusco.core.data.CoruscoDataRange;
import cz.auderis.corusco.core.data.CoruscoDataRequest;
import cz.auderis.corusco.core.data.CoruscoDataSort;
import cz.auderis.corusco.core.data.CoruscoDataSource;
import cz.auderis.corusco.core.data.CoruscoDataStatus;
import cz.auderis.corusco.core.data.CoruscoRowIdentity;
import cz.auderis.corusco.core.data.DefaultCoruscoDataSource;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagedTimeseriesTest {
    @Test void clampedAcceptedPageSizeControlsNavigation() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> {
                        long start = request.range().offset();
                        if (start == 2 && worker.fail) throw new IllegalStateException("failed");
                        var rows = java.util.stream.LongStream.range(start, Math.min(start + 2, 5)).mapToObj(PagedTimeseriesTest::row).toList();
                        return new CoruscoDataPage<>(rows, new CoruscoDataRange(start, 2), CoruscoDataCount.exact(5));
                    }, tasks);
            var table = new javax.swing.JTable();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 5, () -> {});
            binding.query(CoruscoDataQuery.EMPTY); worker.runNext();
            assertThat(table.getValueAt(0, 0)).isEqualTo(0L);
            assertThat(binding.canNext()).isTrue();
            worker.fail = true; binding.next(); worker.runNext();
            assertThat(binding.requested()).isEqualTo(new CoruscoDataRange(2, 2));
            assertThat(table.getValueAt(0, 0)).isEqualTo(0L);
            worker.fail = false; binding.retry(); worker.runNext();
            assertThat(table.getValueAt(0, 0)).isEqualTo(2L);
            assertThat(binding.canNext()).isTrue();
            binding.next(); worker.runNext();
            assertThat(table.getValueAt(0, 0)).isEqualTo(4L);
            assertThat(binding.canNext()).isFalse();
            binding.previous(); worker.runNext();
            assertThat(table.getValueAt(0, 0)).isEqualTo(2L);
            binding.query(new CoruscoDataQuery(List.of(), List.of(new CoruscoDataSort("other", CoruscoDataSort.Direction.ASCENDING, 0))));
            assertThat(binding.requested()).isEqualTo(new CoruscoDataRange(0, 5));
            worker.runNext();
            binding.close(); source.close(); tasks.close();
        });
    }
    @Test void bindingKeepsAcceptedRowsDuringFailureAndRestoresSelectionOnlyForSameQuery() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> {
                        if (request.range().offset() == 2 && worker.fail) throw new IllegalStateException("failed");
                        long start = request.range().offset();
                        var rows = java.util.stream.LongStream.range(start, Math.min(start + 2, 3)).mapToObj(PagedTimeseriesTest::row).toList();
                        return new CoruscoDataPage<>(rows, request.range(), CoruscoDataCount.UNKNOWN);
                    }, tasks);
            var table = new javax.swing.JTable(1, 2);
            table.setAutoCreateColumnsFromModel(false);
            table.setAutoCreateRowSorter(true);
            var original = table.getModel();
            var originalSorter = table.getRowSorter();
            var originalColumns = table.getColumnModel();
            var originalColumn = originalColumns.getColumn(0);
            originalColumn.setPreferredWidth(173);
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, () -> {});
            assertThat(table.getRowSorter()).isNull();
            var events = new java.util.concurrent.atomic.AtomicInteger();
            table.getModel().addTableModelListener(event -> events.incrementAndGet());
            binding.query(CoruscoDataQuery.EMPTY);
            worker.runNext();
            assertThat(table.getRowCount()).isEqualTo(2);
            assertThat(events.get()).isEqualTo(1);
            table.setRowSelectionInterval(0, 0);
            binding.retry(); worker.runNext();
            assertThat(table.getSelectedRow()).isZero();
            assertThat(events.get()).isEqualTo(1);
            assertThat(binding.canNext()).isTrue();
            worker.fail = true; binding.next(); worker.runNext();
            assertThat(table.getRowCount()).isEqualTo(2);
            assertThat(binding.requested().offset()).isEqualTo(2);
            assertThat(binding.displayed().range().offset()).isZero();
            assertThat(binding.canNext()).isFalse();
            worker.fail = false; binding.retry(); worker.runNext();
            assertThat(table.getRowCount()).isOne();
            assertThat(binding.canNext()).isFalse();
            assertThat(binding.canPrevious()).isTrue();
            source.request(new CoruscoDataRange(4, 2), cz.auderis.corusco.core.value.StandardChangeOrigin.USER);
            worker.runNext();
            assertThat(table.getRowCount()).isZero();
            assertThat(binding.canPrevious()).isTrue();
            assertThat(binding.canNext()).isFalse();
            binding.previous(); worker.runNext();
            binding.query(new CoruscoDataQuery(List.of(), List.of(new CoruscoDataSort("symbol", CoruscoDataSort.Direction.ASCENDING, 0))));
            table.setRowSelectionInterval(0, 0); // old page remains visible during the new query
            worker.runNext();
            assertThat(table.getSelectedRow()).isEqualTo(-1);
            binding.close();
            assertThat(table.getModel()).isSameAs(original);
            assertThat(table.getAutoCreateRowSorter()).isTrue();
            assertThat(table.getRowSorter()).isSameAs(originalSorter);
            assertThat(table.getColumnModel()).isSameAs(originalColumns);
            assertThat(table.getAutoCreateColumnsFromModel()).isFalse();
            assertThat(table.getColumnModel().getColumn(0)).isSameAs(originalColumn);
            assertThat(originalColumn.getPreferredWidth()).isEqualTo(173);
            source.close(); tasks.close();
        });
    }

    @Test void closingFromTableListenerDoesNotTouchRestoredTable() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> new CoruscoDataPage<>(List.of(row(1)), request.range(), CoruscoDataCount.exact(1)), tasks);
            var table = new javax.swing.JTable(1, 1); var original = table.getModel();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, () -> {});
            table.getModel().addTableModelListener(event -> binding.close());
            binding.query(CoruscoDataQuery.EMPTY); worker.runNext();
            assertThat(table.getModel()).isSameAs(original);
            assertThat(table.getSelectedRow()).isEqualTo(-1);
            source.close(); tasks.close();
        });
    }

    @Test void bindingAnAlreadyReadySourceRetainsItsRequestedWindowAndExactCount() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> new CoruscoDataPage<>(List.of(row(2), row(3)), request.range(), CoruscoDataCount.exact(4)), tasks);
            source.request(new CoruscoDataRange(2, 2), cz.auderis.corusco.core.value.StandardChangeOrigin.USER); worker.runNext();
            var table = new javax.swing.JTable();
            var calls = new java.util.concurrent.atomic.AtomicInteger();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, calls::incrementAndGet);
            assertThat(calls.get()).isZero();
            assertThat(binding.requested().offset()).isEqualTo(2);
            assertThat(binding.canNext()).isFalse();
            assertThat(binding.canPrevious()).isTrue();
            binding.retry(); worker.runNext();
            assertThat(source.acceptedRequest().range().offset()).isEqualTo(2);
            binding.close(); source.close(); tasks.close();
        });
    }

    @Test void readyListenerMayRequestAnotherPageWithoutMixingDisplayedState() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> new CoruscoDataPage<>(List.of(row(request.range().offset())), request.range(), CoruscoDataCount.UNKNOWN), tasks);
            source.status().subscribe(event -> {
                if (event.newValue().phase() == CoruscoDataPhase.READY && source.acceptedRequest().range().offset() == 0)
                    source.request(new CoruscoDataRange(2, 2), cz.auderis.corusco.core.value.StandardChangeOrigin.USER);
            });
            var table = new javax.swing.JTable();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, () -> {});
            binding.query(CoruscoDataQuery.EMPTY); worker.runNext();
            assertThat(binding.requested().offset()).isEqualTo(2);
            assertThat(binding.displayed().range().offset()).isZero();
            assertThat(table.getValueAt(0, 0)).isEqualTo(0L);
            assertThat(binding.canNext()).isFalse();
            worker.runNext();
            assertThat(binding.displayed().range().offset()).isEqualTo(2);
            assertThat(table.getValueAt(0, 0)).isEqualTo(2L);
            binding.close(); source.close(); tasks.close();
        });
    }

    @Test void modelListenerQueryResetBlocksNavigationUntilDeferredRequestStarts() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var worker = new ManualWorker();
            var tasks = cz.auderis.corusco.core.task.DefaultTaskService.of(worker, Runnable::run);
            var source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence),
                    (request, cancellation) -> new CoruscoDataPage<>(List.of(row(request.range().offset()), row(request.range().offset() + 1)), request.range(), CoruscoDataCount.UNKNOWN), tasks);
            var table = new javax.swing.JTable();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, () -> {});
            var changedQuery = new CoruscoDataQuery(List.of(), List.of(new CoruscoDataSort("new-query", CoruscoDataSort.Direction.ASCENDING, 0)));
            var invoked = new java.util.concurrent.atomic.AtomicBoolean();
            table.getModel().addTableModelListener(event -> {
                if (invoked.compareAndSet(false, true)) {
                    binding.query(changedQuery);
                    assertThat(binding.canNext()).isFalse();
                    binding.next();
                }
            });
            binding.query(CoruscoDataQuery.EMPTY); worker.runNext();
            assertThat(binding.requested().offset()).isZero();
            assertThat(source.query().value()).isEqualTo(changedQuery);
            worker.runNext();
            assertThat(source.acceptedRequest().range().offset()).isZero();
            assertThat(source.acceptedRequest().query()).isEqualTo(changedQuery);
            binding.close(); source.close(); tasks.close();
        });
    }

    @Test void sourceThatCompletesSynchronouslyWithoutLoadingEnablesNavigation() {
        cz.auderis.corusco.swing.binding.SwingEdt.runAndWait(() -> {
            var source = new DirectSource();
            var binding = new cz.auderis.corusco.swing.table.PagedTableBinding<>(new javax.swing.JTable(), source,
                    TimeseriesObservationTableDescriptor.DESCRIPTOR, 2, () -> {});
            binding.query(CoruscoDataQuery.EMPTY);
            assertThat(binding.canNext()).isTrue();
            binding.next();
            assertThat(binding.displayed().range().offset()).isEqualTo(2);
            binding.close();
        });
    }

    private static final class DirectSource implements CoruscoDataSource<TimeseriesObservation, Long> {
        final cz.auderis.corusco.core.collection.ObservableArrayList<TimeseriesObservation> rows = cz.auderis.corusco.core.collection.ObservableArrayList.empty();
        final cz.auderis.corusco.core.value.SimpleValue<CoruscoDataStatus> status = cz.auderis.corusco.core.value.SimpleValue.of(CoruscoDataStatus.INITIAL);
        final cz.auderis.corusco.core.value.SimpleValue<CoruscoDataRange> range = cz.auderis.corusco.core.value.SimpleValue.of(CoruscoDataRange.EMPTY);
        final cz.auderis.corusco.core.value.SimpleValue<CoruscoDataQuery> query = cz.auderis.corusco.core.value.SimpleValue.of(CoruscoDataQuery.EMPTY);
        final cz.auderis.corusco.core.value.SimpleValue<CoruscoDataCount> count = cz.auderis.corusco.core.value.SimpleValue.of(CoruscoDataCount.UNKNOWN);
        CoruscoDataRequest accepted;
        public CoruscoRowIdentity<TimeseriesObservation, Long> rowIdentity() { return CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence); }
        public cz.auderis.corusco.core.collection.ObservableReadableCollection<TimeseriesObservation> rows() { return rows; }
        public cz.auderis.corusco.core.value.ReadableValue<CoruscoDataStatus> status() { return status; }
        public cz.auderis.corusco.core.value.ReadableValue<CoruscoDataRange> range() { return range; }
        public cz.auderis.corusco.core.value.ReadableValue<CoruscoDataQuery> query() { return query; }
        public cz.auderis.corusco.core.value.ReadableValue<CoruscoDataCount> totalCount() { return count; }
        public CoruscoDataRequest acceptedRequest() { return accepted; }
        public void request(CoruscoDataRange range, cz.auderis.corusco.core.value.ChangeOrigin origin) { request(range, query.value(), origin); }
        public void request(CoruscoDataRange range, CoruscoDataQuery query, cz.auderis.corusco.core.value.ChangeOrigin origin) {
            long generation = status.value().refreshGeneration() + 1;
            accepted = new CoruscoDataRequest(range, query, generation);
            this.range.setValue(range, origin); this.query.setValue(query, origin);
            rows.replaceContents(List.of(row(range.offset()), row(range.offset() + 1)), origin);
            status.setValue(new CoruscoDataStatus(CoruscoDataPhase.READY, generation, null), origin);
        }
        public void refresh(cz.auderis.corusco.core.value.ChangeOrigin origin) { request(range.value(), query.value(), origin); }
        public void close() {}
    }

    private static TimeseriesObservation row(long id) {
        return new TimeseriesObservation(id, id, "A", "V", "North", "C", ObservationState.values()[0],
                java.math.BigDecimal.ONE, java.math.BigDecimal.ONE, java.math.BigDecimal.ONE, 1, java.math.BigDecimal.ONE, 1);
    }

    private static final class ManualWorker extends java.util.concurrent.AbstractExecutorService {
        final java.util.ArrayDeque<Runnable> queue = new java.util.ArrayDeque<>(); boolean fail;
        public void execute(Runnable command) { queue.add(command); }
        void runNext() { queue.remove().run(); }
        public void shutdown() {} public java.util.List<Runnable> shutdownNow() { return List.of(); }
        public boolean isShutdown() { return false; } public boolean isTerminated() { return false; }
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) { return true; }
    }
    @Test void pagesApplyGlobalLiteralFiltersAndStableOrder() throws Exception {
        var query = new CoruscoDataQuery(List.of(
                new CoruscoDataFilter(TimeseriesObservationColumns.SYMBOL.key().id(), CoruscoDataFilterOperator.CONTAINS, List.of("a")),
                new CoruscoDataFilter(TimeseriesObservationColumns.REGION.key().id(), CoruscoDataFilterOperator.CONTAINS, List.of("nor"))),
                List.of(new CoruscoDataSort(TimeseriesObservationColumns.SYMBOL.key().id(), CoruscoDataSort.Direction.ASCENDING, 0)));
        var first = H2TimeseriesRepository.loadPage(new CoruscoDataRequest(new CoruscoDataRange(0, 250), query, 1), () -> false);
        var second = H2TimeseriesRepository.loadPage(new CoruscoDataRequest(new CoruscoDataRange(250, 250), query, 2), () -> false);
        assertThat(first.rows()).hasSize(250).allMatch(r -> r.symbol().contains("A") && r.region().equals("North"));
        assertThat(second.rows()).hasSize(250);
        var rows = new java.util.ArrayList<>(first.rows()); rows.addAll(second.rows());
        assertThat(rows).isSortedAccordingTo(java.util.Comparator.comparing(TimeseriesObservation::symbol).thenComparingLong(TimeseriesObservation::sequence));
        var literal = new CoruscoDataQuery(List.of(new CoruscoDataFilter(TimeseriesObservationColumns.SYMBOL.key().id(), CoruscoDataFilterOperator.CONTAINS, List.of("%_!"))), List.of());
        assertThat(H2TimeseriesRepository.loadPage(new CoruscoDataRequest(new CoruscoDataRange(0, 250), literal, 3), () -> false).rows()).isEmpty();
        var invalid = new CoruscoDataQuery(List.of(), List.of(new CoruscoDataSort("symbol desc; drop table observations", CoruscoDataSort.Direction.ASCENDING, 0)));
        assertThatThrownBy(() -> H2TimeseriesRepository.loadPage(new CoruscoDataRequest(new CoruscoDataRange(0, 2), invalid, 4), () -> false)).isInstanceOf(IllegalArgumentException.class);
        var descending = new CoruscoDataQuery(List.of(new CoruscoDataFilter(TimeseriesObservationColumns.REGION.key().id(), CoruscoDataFilterOperator.CONTAINS, List.of("  "))),
                List.of(new CoruscoDataSort(TimeseriesObservationColumns.TIMESTAMP_MILLIS.key().id(), CoruscoDataSort.Direction.DESCENDING, 0)));
        var latest = H2TimeseriesRepository.loadPage(new CoruscoDataRequest(new CoruscoDataRange(0, 2), descending, 5), () -> false);
        assertThat(latest.totalCount().value()).isEqualTo(H2TimeseriesRepository.OBSERVATION_COUNT);
        assertThat(latest.rows()).extracting(TimeseriesObservation::sequence).containsExactly(100_000L, 99_999L);
    }
}
