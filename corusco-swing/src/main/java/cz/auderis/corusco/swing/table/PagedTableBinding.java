package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.data.CoruscoDataCount;
import cz.auderis.corusco.core.data.CoruscoDataPhase;
import cz.auderis.corusco.core.data.CoruscoDataQuery;
import cz.auderis.corusco.core.data.CoruscoDataRange;
import cz.auderis.corusco.core.data.CoruscoDataRequest;
import cz.auderis.corusco.core.data.CoruscoDataSource;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import org.jspecify.annotations.Nullable;

/**
 * EDT-confined, read-only page binding. The source remains caller-owned and
 * must publish acceptedRequest, rows and count coherently on READY. The
 * callback runs after binding state and table contents have been reconciled;
 * construction does not invoke it.
 * Local Swing sorting is detached: ordering belongs to the source query.
 */
public final class PagedTableBinding<R, K> implements Binding {
    private final JTable table;
    private final CoruscoDataSource<R, K> source;
    private final int pageSize;
    private final Runnable changed;
    private final ObservableArrayList<R> rows = ObservableArrayList.empty();
    private final ObservableTableModel<R> model;
    private final TableModel oldModel;
    private final RowSorter<? extends TableModel> oldSorter;
    private final boolean oldAutoCreateRowSorter;
    private final TableColumnModel oldColumns;
    private final boolean oldAutoCreateColumns;
    private final Subscription statusSubscription;
    private final ListSelectionListener selectionListener;
    private CoruscoDataRange requested;
    private CoruscoDataQuery requestedQuery;
    private @Nullable CoruscoDataRequest displayed;
    private CoruscoDataCount count = CoruscoDataCount.UNKNOWN;
    private @Nullable K selectedKey;
    private boolean updating;
    private boolean pending;
    private long pendingGeneration = -1;
    private long generationBeforeRequest;
    private boolean closed;

    public PagedTableBinding(JTable table, CoruscoDataSource<R, K> source, TableDescriptor<R> descriptor, int pageSize, Runnable changed) {
        SwingEdt.requireEdt();
        this.table = Objects.requireNonNull(table); this.source = Objects.requireNonNull(source);
        if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be positive");
        this.pageSize = pageSize; this.changed = Objects.requireNonNull(changed);
        var initialRange = source.range().value();
        requested = initialRange == null || initialRange.limit() == 0 ? new CoruscoDataRange(0, pageSize) : initialRange;
        requestedQuery = Objects.requireNonNullElse(source.query().value(), CoruscoDataQuery.EMPTY);
        oldModel = table.getModel(); oldSorter = table.getRowSorter(); oldAutoCreateRowSorter = table.getAutoCreateRowSorter();
        oldColumns = table.getColumnModel(); oldAutoCreateColumns = table.getAutoCreateColumnsFromModel();
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);
        table.setColumnModel(new DefaultTableColumnModel());
        table.setAutoCreateColumnsFromModel(true);
        model = ObservableTableModel.readOnly(rows, descriptor);
        table.setModel(model);
        selectionListener = event -> {
            if (!updating && !event.getValueIsAdjusting() && displayed != null && displayed.query().equals(requestedQuery)) {
                int view = table.getSelectedRow();
                selectedKey = view < 0 ? null : source.rowIdentity().keyOf(rows.get(table.convertRowIndexToModel(view)));
            }
        };
        table.getSelectionModel().addListSelectionListener(selectionListener);
        statusSubscription = source.status().subscribe(event -> reconcile());
        if (source.status().value() != null && source.status().value().phase() == CoruscoDataPhase.LOADING) {
            pending = true; pendingGeneration = source.status().value().refreshGeneration();
        }
        if (source.acceptedRequest() != null) accept();
    }

    /** Requests the first page; a changed query clears the old selection. */
    public void query(CoruscoDataQuery query) {
        ensureOpen(); Objects.requireNonNull(query);
        if (!query.equals(requestedQuery)) clearSelection();
        requestedQuery = query; requested = new CoruscoDataRange(0, pageSize);
        markPending();
        source.request(requested, query, StandardChangeOrigin.USER);
    }
    public CoruscoDataRange requested() { return requested; }
    public @Nullable CoruscoDataRequest displayed() { return displayed; }
    public boolean canPrevious() { return ready() && displayed != null && displayed.range().offset() > 0; }
    public boolean canNext() {
        if (!ready() || displayed == null || displayed.range().limit() <= 0 || rows.size() < displayed.range().limit()) return false;
        long offset = displayed.range().offset();
        int limit = displayed.range().limit();
        if (offset > Long.MAX_VALUE - limit) return false;
        return count.kind() != CoruscoDataCount.Kind.EXACT || offset + limit < count.value();
    }
    public void previous() {
        if (canPrevious() && displayed != null) {
            var range = displayed.range(); int limit = navigationSize(range);
            request(Math.max(0, range.offset() - limit), limit);
        }
    }
    public void next() {
        if (canNext() && displayed != null) {
            var range = displayed.range(); int limit = navigationSize(range);
            request(range.offset() + limit, limit);
        }
    }
    public void retry() { ensureOpen(); markPending(); source.request(requested, requestedQuery, StandardChangeOrigin.USER); }

    private int navigationSize(CoruscoDataRange range) { return range.limit() > 0 ? range.limit() : pageSize; }
    private void request(long offset, int limit) {
        ensureOpen(); requested = new CoruscoDataRange(offset, limit);
        markPending();
        source.request(requested, requestedQuery, StandardChangeOrigin.USER);
    }
    private void markPending() {
        pending = true; pendingGeneration = -1;
        generationBeforeRequest = source.status().value() == null ? 0 : source.status().value().refreshGeneration();
    }
    private boolean ready() { return !closed && !pending && source.status().value() != null && source.status().value().phase() == CoruscoDataPhase.READY; }
    private void reconcile() {
        SwingEdt.requireEdt();
        var status = source.status().value();
        if (status == null || closed) return;
        if (status.phase() == CoruscoDataPhase.LOADING) {
            pending = true; pendingGeneration = status.refreshGeneration();
            var query = source.query().value();
            if (query != null && !query.equals(requestedQuery)) { clearSelection(); requestedQuery = query; }
            var range = source.range().value(); if (range != null) requested = range;
        } else if (status.phase() == CoruscoDataPhase.READY) {
            if (pendingGeneration == status.refreshGeneration()
                    || (pendingGeneration == -1 && status.refreshGeneration() > generationBeforeRequest
                    && source.acceptedRequest() != null && source.acceptedRequest().query().equals(requestedQuery))) pending = false;
            accept();
        } else if (status.phase() == CoruscoDataPhase.FAILED && (pendingGeneration == status.refreshGeneration()
                || (pendingGeneration == -1 && status.refreshGeneration() > generationBeforeRequest))) pending = false;
        if (!closed) changed.run();
    }
    private void accept() {
        CoruscoDataRequest accepted = source.acceptedRequest();
        if (accepted == null) return;
        if (displayed != null && !accepted.query().equals(displayed.query())) selectedKey = null;
        displayed = accepted;
        count = Objects.requireNonNullElse(source.totalCount().value(), CoruscoDataCount.UNKNOWN);
        updating = true;
        try {
            rows.replaceContents(source.rows().snapshot(), StandardChangeOrigin.MODEL);
            if (closed) return;
            table.clearSelection();
            if (selectedKey != null && accepted.query().equals(requestedQuery)) {
                for (int i = 0; i < rows.size(); i++) if (selectedKey.equals(source.rowIdentity().keyOf(rows.get(i)))) {
                    int view = table.convertRowIndexToView(i); if (view >= 0) table.setRowSelectionInterval(view, view); break;
                }
            }
        } finally { updating = false; }
    }
    private void clearSelection() { selectedKey = null; updating = true; try { table.clearSelection(); } finally { updating = false; } }
    private void ensureOpen() { SwingEdt.requireEdt(); if (closed) throw new IllegalStateException("Binding is closed"); }
    @Override public void close() {
        SwingEdt.requireEdt(); if (closed) return; closed = true;
        statusSubscription.close(); table.getSelectionModel().removeListSelectionListener(selectionListener);
        model.close(); table.setModel(oldModel); table.setAutoCreateColumnsFromModel(oldAutoCreateColumns);
        table.setColumnModel(oldColumns); table.setAutoCreateRowSorter(oldAutoCreateRowSorter); table.setRowSorter(oldSorter);
    }
}
