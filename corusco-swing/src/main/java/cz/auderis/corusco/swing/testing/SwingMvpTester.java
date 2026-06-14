package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.command.Command;
import cz.auderis.corusco.core.command.CommandSet;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableState;
import cz.auderis.corusco.swing.behavior.BehaviorKey;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Test harness for exercising Swing MVP views and presenters on the EDT.
 *
 * <p>This class is part of the Corusco test-support story for generated and
 * handwritten Swing views. It creates the root view, optional presenter, and
 * optional command set on the Swing Event Dispatch Thread, then provides
 * component lookup, command execution, problem assertions, behavior assertions,
 * and table-state assertions that also run on the EDT. Tests can therefore
 * speak in generated {@link ComponentKey}, {@link ActionKey}, field-key, and
 * table-state terms instead of reaching through Swing component trees directly
 * from the test thread.</p>
 *
 * <p>The tester does not own application resources beyond the objects returned
 * by the supplied factories. If a view or presenter installs bindings,
 * behavior scopes, task services, or native windows, the test remains
 * responsible for closing or disposing them through its own lifecycle hooks.
 * Direct accessors intentionally require the caller to already be on the EDT;
 * prefer {@link #runOnEdt(BiConsumer)} and {@link #queryOnEdt(BiFunction)}
 * whenever a test interacts with Swing state.</p>
 *
 * <p>Lookup uses {@link SwingComponentKeys} markers first and component names
 * only as a compatibility fallback. Table helpers distinguish view-row and
 * model-row coordinates, and table-state helpers assert stable persisted ids
 * rather than transient {@link javax.swing.table.TableColumn} instances.</p>
 *
 * @param <V> root view type
 * @param <P> presenter type
 */
public final class SwingMvpTester<V extends JComponent, P> {

    private final V view;
    private final P presenter;
    private final CommandSet commands;

    private SwingMvpTester(V view, P presenter, CommandSet commands) {
        this.view = Objects.requireNonNull(view, "view");
        this.presenter = presenter;
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    /**
     * Creates a tester with no presenter.
     *
     * <p>The view factory is invoked synchronously on the EDT. The resulting
     * tester has an empty command set and a missing presenter value.</p>
     *
     * @param viewFactory view factory executed on the EDT
     * @param <V> root view type
     * @return tester
     */
    public static <V extends JComponent> SwingMvpTester<V, Void> create(Supplier<? extends V> viewFactory) {
        return create(viewFactory, view -> null);
    }

    /**
     * Creates a tester with a presenter factory.
     *
     * <p>Both factories run synchronously on the EDT. The command set is empty;
     * use the three-argument overload when command assertions or execution are
     * part of the test.</p>
     *
     * @param viewFactory view factory executed on the EDT
     * @param presenterFactory presenter factory executed on the EDT
     * @param <V> root view type
     * @param <P> presenter type
     * @return tester
     */
    public static <V extends JComponent, P> SwingMvpTester<V, P> create(
            Supplier<? extends V> viewFactory,
            Function<? super V, ? extends P> presenterFactory
    ) {
        return create(viewFactory, presenterFactory, (view, presenter) -> new CommandSet(List.of()));
    }

    /**
     * Creates a tester with a presenter and command-set factory.
     *
     * <p>The factories run synchronously on the EDT in this order: view,
     * presenter, command set. Exceptions are propagated to the caller, with
     * checked exceptions wrapped in {@link IllegalStateException} by the EDT
     * dispatch helper.</p>
     *
     * @param viewFactory view factory executed on the EDT
     * @param presenterFactory presenter factory executed on the EDT
     * @param commandSetFactory command-set factory executed on the EDT after
     *         presenter creation
     * @param <V> root view type
     * @param <P> presenter type
     * @return tester
     */
    public static <V extends JComponent, P> SwingMvpTester<V, P> create(
            Supplier<? extends V> viewFactory,
            Function<? super V, ? extends P> presenterFactory,
            BiFunction<? super V, ? super P, ? extends CommandSet> commandSetFactory
    ) {
        Objects.requireNonNull(viewFactory, "viewFactory");
        Objects.requireNonNull(presenterFactory, "presenterFactory");
        Objects.requireNonNull(commandSetFactory, "commandSetFactory");
        AtomicReference<SwingMvpTester<V, P>> tester = new AtomicReference<>();
        runAndWaitUnchecked(() -> {
            V view = Objects.requireNonNull(viewFactory.get(), "viewFactory.get()");
            P presenter = presenterFactory.apply(view);
            CommandSet commands = Objects.requireNonNull(commandSetFactory.apply(view, presenter), "commandSetFactory.apply()");
            tester.set(new SwingMvpTester<>(view, presenter, commands));
        });
        return tester.get();
    }

    /**
     * Returns the root view. Callers must already be on the EDT before using
     * the returned component.
     *
     * @return root view
     */
    public V view() {
        SwingEdt.requireEdt();
        return view;
    }

    /**
     * Returns the optional presenter.
     *
     * @return optional presenter
     */
    public Optional<P> presenter() {
        return Optional.ofNullable(presenter);
    }

    /**
     * Returns the command set supplied when the tester was created.
     *
     * <p>The set is immutable, but command state is still UI-owned. Prefer
     * tester command helpers for invocation and assertions so command state is
     * touched on the EDT.</p>
     *
     * @return command set
     */
    public CommandSet commands() {
        return commands;
    }

    /**
     * Runs Swing test work on the EDT.
     *
     * @param work work receiving the view and optional presenter value
     * @return this tester
     */
    public SwingMvpTester<V, P> runOnEdt(BiConsumer<? super V, ? super P> work) {
        Objects.requireNonNull(work, "work");
        runAndWaitUnchecked(() -> work.accept(view, presenter));
        return this;
    }

    /**
     * Reads Swing test state on the EDT.
     *
     * @param query query receiving the view and optional presenter value
     * @param <R> result type
     * @return query result
     */
    public <R> R queryOnEdt(BiFunction<? super V, ? super P, ? extends R> query) {
        Objects.requireNonNull(query, "query");
        AtomicReference<R> result = new AtomicReference<>();
        runAndWaitUnchecked(() -> result.set(query.apply(view, presenter)));
        return result.get();
    }

    /**
     * Finds a component marked with a generated component key.
     *
     * <p>The lookup itself runs on the EDT. Interact with the returned
     * component through {@link #runOnEdt(BiConsumer)} or
     * {@link #queryOnEdt(BiFunction)} unless the caller is already on the EDT.</p>
     *
     * @param key component key
     * @param <C> component type
     * @return optional component
     */
    public <C extends JComponent> Optional<C> findComponent(ComponentKey<C> key) {
        Objects.requireNonNull(key, "key");
        return queryOnEdt((view, presenter) -> findComponentOnEdt(view, key));
    }

    /**
     * Requires a component marked with a generated component key.
     *
     * <p>The lookup itself runs on the EDT. Interact with the returned
     * component through {@link #runOnEdt(BiConsumer)} or
     * {@link #queryOnEdt(BiFunction)} unless the caller is already on the EDT.</p>
     *
     * @param key component key
     * @param <C> component type
     * @return matching component
     */
    public <C extends JComponent> C requireComponent(ComponentKey<C> key) {
        return findComponent(key).orElseThrow(() -> new IllegalArgumentException("Missing component: " + key));
    }

    /**
     * Enters text into a generated text component on the EDT.
     *
     * @param key component key for a {@link JTextComponent}
     * @param text replacement text
     * @param <C> text component type
     * @return this tester
     */
    public <C extends JTextComponent> SwingMvpTester<V, P> enterText(ComponentKey<C> key, String text) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(text, "text");
        runAndWaitUnchecked(() -> requireComponentOnEdt(view, key).setText(text));
        return this;
    }

    /**
     * Sets selected state on a generated checkbox, radio button, or toggle
     * button component on the EDT.
     *
     * @param key component key for an {@link AbstractButton}
     * @param selected selected state
     * @param <C> button component type
     * @return this tester
     */
    public <C extends AbstractButton> SwingMvpTester<V, P> setSelected(ComponentKey<C> key, boolean selected) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> requireComponentOnEdt(view, key).setSelected(selected));
        return this;
    }

    /**
     * Selects an item in a generated combo box on the EDT.
     *
     * <p>The item parameter follows Swing's {@link JComboBox#setSelectedItem(Object)}
     * contract because generated component keys use erased component classes.</p>
     *
     * @param key component key for a {@link JComboBox}
     * @param item item to select
     * @param <C> combo box component type
     * @return this tester
     */
    public <C extends JComboBox<?>> SwingMvpTester<V, P> selectItem(ComponentKey<C> key, Object item) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> requireComponentOnEdt(view, key).setSelectedItem(item));
        return this;
    }

    /**
     * Selects a table view row on the EDT.
     *
     * <p>The row index is the JTable view index after sorting and filtering.
     * Use {@link #selectTableModelRow(ComponentKey, int)} when the test speaks
     * in model-row coordinates.</p>
     *
     * @param key component key for a {@link JTable}
     * @param viewRow table view-row index
     * @param <C> table type
     * @return this tester
     */
    public <C extends JTable> SwingMvpTester<V, P> selectTableViewRow(ComponentKey<C> key, int viewRow) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> selectViewRow(requireComponentOnEdt(view, key), viewRow));
        return this;
    }

    /**
     * Selects a table model row on the EDT.
     *
     * <p>The row index is converted through {@link JTable#convertRowIndexToView(int)}
     * so sorted and filtered tables can be addressed by stable model row.</p>
     *
     * @param key component key for a {@link JTable}
     * @param modelRow table model-row index
     * @param <C> table type
     * @return this tester
     */
    public <C extends JTable> SwingMvpTester<V, P> selectTableModelRow(ComponentKey<C> key, int modelRow) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> {
            JTable table = requireComponentOnEdt(view, key);
            checkModelRow(table, modelRow);
            int viewRow = table.convertRowIndexToView(modelRow);
            if (viewRow < 0) {
                throw new IllegalArgumentException("Table model row is not visible: " + modelRow);
            }
            selectViewRow(table, viewRow);
        });
        return this;
    }

    /**
     * Clears table row selection on the EDT.
     *
     * @param key component key for a {@link JTable}
     * @param <C> table type
     * @return this tester
     */
    public <C extends JTable> SwingMvpTester<V, P> clearTableSelection(ComponentKey<C> key) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> requireComponentOnEdt(view, key).clearSelection());
        return this;
    }

    /**
     * Asserts selected table view row on the EDT.
     *
     * @param key component key for a {@link JTable}
     * @param expectedViewRow expected selected view row, or {@code -1}
     * @param <C> table type
     * @return this tester
     */
    public <C extends JTable> SwingMvpTester<V, P> assertSelectedTableViewRow(
            ComponentKey<C> key,
            int expectedViewRow
    ) {
        Objects.requireNonNull(key, "key");
        int actual = queryOnEdt((view, presenter) -> requireComponentOnEdt(view, key).getSelectedRow());
        if (actual != expectedViewRow) {
            throw new AssertionError("Expected table " + key + " selected view row "
                    + expectedViewRow + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts selected table model row on the EDT.
     *
     * @param key component key for a {@link JTable}
     * @param expectedModelRow expected selected model row, or {@code -1}
     * @param <C> table type
     * @return this tester
     */
    public <C extends JTable> SwingMvpTester<V, P> assertSelectedTableModelRow(
            ComponentKey<C> key,
            int expectedModelRow
    ) {
        Objects.requireNonNull(key, "key");
        int actual = queryOnEdt((view, presenter) -> {
            JTable table = requireComponentOnEdt(view, key);
            int selectedViewRow = table.getSelectedRow();
            return selectedViewRow < 0 ? -1 : table.convertRowIndexToModel(selectedViewRow);
        });
        if (actual != expectedModelRow) {
            throw new AssertionError("Expected table " + key + " selected model row "
                    + expectedModelRow + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that an EDT-read problem set contains at least one matching
     * problem.
     *
     * <p>The source function runs on the EDT and can read a form model, field
     * model, presenter, or any other UI-owned problem source. Matching uses the
     * typed {@link ProblemFilter} API instead of string paths.</p>
     *
     * @param problemSource source queried on the EDT
     * @param filter filter describing the expected problem
     * @return this tester
     */
    public SwingMvpTester<V, P> assertProblem(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            ProblemFilter filter
    ) {
        int count = countProblems(problemSource, filter);
        if (count == 0) {
            throw new AssertionError("Expected at least one matching problem but found 0");
        }
        return this;
    }

    /**
     * Asserts that an EDT-read problem set contains no matching problem.
     *
     * @param problemSource source queried on the EDT
     * @param filter filter describing the absent problem
     * @return this tester
     */
    public SwingMvpTester<V, P> assertNoProblem(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            ProblemFilter filter
    ) {
        return assertProblemCount(problemSource, filter, 0);
    }

    /**
     * Asserts the number of problems matching a filter.
     *
     * @param problemSource source queried on the EDT
     * @param filter filter describing the counted problems
     * @param expectedCount expected matching count
     * @return this tester
     */
    public SwingMvpTester<V, P> assertProblemCount(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            ProblemFilter filter,
            int expectedCount
    ) {
        if (expectedCount < 0) {
            throw new IllegalArgumentException("expectedCount must not be negative");
        }
        int count = countProblems(problemSource, filter);
        if (count != expectedCount) {
            throw new AssertionError("Expected " + expectedCount + " matching problems but found " + count);
        }
        return this;
    }

    /**
     * Asserts that an EDT-read problem set contains a validation or parse
     * problem for a typed field key and problem code.
     *
     * @param problemSource source queried on the EDT
     * @param fieldKey typed field key
     * @param code stable problem code
     * @param <O> owner/model type
     * @param <T> field value type
     * @return this tester
     */
    public <O, T> SwingMvpTester<V, P> assertProblem(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            FieldKey<O, T> fieldKey,
            ProblemCode code
    ) {
        int count = countProblems(problemSource, fieldProblemFilter(fieldKey, code));
        if (count == 0) {
            throw new AssertionError("Expected problem " + code + " for field " + fieldKey + " but found 0");
        }
        return this;
    }

    /**
     * Asserts that an EDT-read problem set contains no problem for a typed
     * field key and problem code.
     *
     * @param problemSource source queried on the EDT
     * @param fieldKey typed field key
     * @param code stable problem code
     * @param <O> owner/model type
     * @param <T> field value type
     * @return this tester
     */
    public <O, T> SwingMvpTester<V, P> assertNoProblem(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            FieldKey<O, T> fieldKey,
            ProblemCode code
    ) {
        int count = countProblems(problemSource, fieldProblemFilter(fieldKey, code));
        if (count != 0) {
            throw new AssertionError("Expected no problem " + code + " for field " + fieldKey
                    + " but found " + count);
        }
        return this;
    }

    /**
     * Asserts that a behavior key is installed on a generated component.
     *
     * <p>The scope source and component lookup both run on the EDT. The
     * behavior scope remains owned by the view or presenter under test; the
     * tester only reads its public installed-key view.</p>
     *
     * @param componentKey generated component key
     * @param scopeSource behavior scope source queried on the EDT
     * @param behaviorKey expected behavior key
     * @param <C> component type
     * @return this tester
     */
    public <C extends JComponent> SwingMvpTester<V, P> assertBehaviorInstalled(
            ComponentKey<C> componentKey,
            BiFunction<? super V, ? super P, ? extends BehaviorScope> scopeSource,
            BehaviorKey behaviorKey
    ) {
        boolean installed = hasBehavior(componentKey, scopeSource, behaviorKey);
        if (!installed) {
            throw new AssertionError("Expected behavior " + behaviorKey + " installed on " + componentKey);
        }
        return this;
    }

    /**
     * Asserts that a behavior key is not installed on a generated component.
     *
     * @param componentKey generated component key
     * @param scopeSource behavior scope source queried on the EDT
     * @param behaviorKey absent behavior key
     * @param <C> component type
     * @return this tester
     */
    public <C extends JComponent> SwingMvpTester<V, P> assertBehaviorNotInstalled(
            ComponentKey<C> componentKey,
            BiFunction<? super V, ? super P, ? extends BehaviorScope> scopeSource,
            BehaviorKey behaviorKey
    ) {
        boolean installed = hasBehavior(componentKey, scopeSource, behaviorKey);
        if (installed) {
            throw new AssertionError("Expected behavior " + behaviorKey + " not installed on " + componentKey);
        }
        return this;
    }

    /**
     * Asserts a table state's stable table id.
     *
     * <p>The source function runs on the EDT, so it can safely call
     * {@code TableStateController.captureState()} or read presenter-owned table
     * state. Assertions use stable table and column ids, not Swing column
     * instances.</p>
     *
     * @param stateSource table state source queried on the EDT
     * @param expectedTableId expected stable table id
     * @return this tester
     */
    public SwingMvpTester<V, P> assertTableStateId(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String expectedTableId
    ) {
        Objects.requireNonNull(expectedTableId, "expectedTableId");
        TableState state = readTableState(stateSource);
        if (!state.tableId().equals(expectedTableId)) {
            throw new AssertionError("Expected table state id " + expectedTableId + " but was " + state.tableId());
        }
        return this;
    }

    /**
     * Asserts a table column's visible flag.
     *
     * @param stateSource table state source queried on the EDT
     * @param columnId stable column persistence id
     * @param expectedVisible expected visibility
     * @return this tester
     */
    public SwingMvpTester<V, P> assertTableColumnVisible(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String columnId,
            boolean expectedVisible
    ) {
        TableState state = readTableState(stateSource);
        ColumnState column = requireColumnState(state, columnId);
        if (column.visible() != expectedVisible) {
            throw new AssertionError("Expected table column " + columnId + " visible="
                    + expectedVisible + " but was " + column.visible());
        }
        return this;
    }

    /**
     * Asserts a table column's visual order.
     *
     * @param stateSource table state source queried on the EDT
     * @param columnId stable column persistence id
     * @param expectedOrder expected zero-based visual order
     * @return this tester
     */
    public SwingMvpTester<V, P> assertTableColumnOrder(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String columnId,
            int expectedOrder
    ) {
        if (expectedOrder < 0) {
            throw new IllegalArgumentException("expectedOrder must not be negative");
        }
        TableState state = readTableState(stateSource);
        ColumnState column = requireColumnState(state, columnId);
        if (column.order() != expectedOrder) {
            throw new AssertionError("Expected table column " + columnId + " order "
                    + expectedOrder + " but was " + column.order());
        }
        return this;
    }

    /**
     * Asserts a table column's persisted width.
     *
     * @param stateSource table state source queried on the EDT
     * @param columnId stable column persistence id
     * @param expectedWidth expected width in pixels
     * @return this tester
     */
    public SwingMvpTester<V, P> assertTableColumnWidth(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String columnId,
            int expectedWidth
    ) {
        if (expectedWidth <= 0) {
            throw new IllegalArgumentException("expectedWidth must be greater than zero");
        }
        TableState state = readTableState(stateSource);
        ColumnState column = requireColumnState(state, columnId);
        if (column.width() != expectedWidth) {
            throw new AssertionError("Expected table column " + columnId + " width "
                    + expectedWidth + " but was " + column.width());
        }
        return this;
    }

    /**
     * Asserts a table sort entry by stable column id.
     *
     * @param stateSource table state source queried on the EDT
     * @param columnId stable column persistence id
     * @param expectedDirection expected sort direction
     * @param expectedPriority expected zero-based sort priority
     * @return this tester
     */
    public SwingMvpTester<V, P> assertTableSort(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String columnId,
            SortDirection expectedDirection,
            int expectedPriority
    ) {
        Objects.requireNonNull(expectedDirection, "expectedDirection");
        if (expectedPriority < 0) {
            throw new IllegalArgumentException("expectedPriority must not be negative");
        }
        TableState state = readTableState(stateSource);
        SortState sort = requireSortState(state, columnId);
        if (sort.direction() != expectedDirection || sort.priority() != expectedPriority) {
            throw new AssertionError("Expected table sort " + columnId + " direction="
                    + expectedDirection + " priority=" + expectedPriority
                    + " but was direction=" + sort.direction() + " priority=" + sort.priority());
        }
        return this;
    }

    /**
     * Asserts that a table has no sort entry for a stable column id.
     *
     * @param stateSource table state source queried on the EDT
     * @param columnId stable column persistence id
     * @return this tester
     */
    public SwingMvpTester<V, P> assertNoTableSort(
            BiFunction<? super V, ? super P, ? extends TableState> stateSource,
            String columnId
    ) {
        Objects.requireNonNull(columnId, "columnId");
        TableState state = readTableState(stateSource);
        boolean present = state.sort().stream()
                .anyMatch(sort -> sort.columnId().equals(columnId));
        if (present) {
            throw new AssertionError("Expected no table sort for column " + columnId);
        }
        return this;
    }

    /**
     * Finds a command by generated action key.
     *
     * <p>The lookup runs on the EDT. Prefer tester command helpers for
     * invocation and assertions so command state is touched on the EDT.</p>
     *
     * @param key action key
     * @return optional command
     */
    public Optional<Command> findCommand(ActionKey key) {
        Objects.requireNonNull(key, "key");
        return queryOnEdt((view, presenter) -> commands.find(key));
    }

    /**
     * Requires a command by generated action key.
     *
     * <p>The lookup runs on the EDT. Prefer tester command helpers for
     * invocation and assertions so command state is touched on the EDT.</p>
     *
     * @param key action key
     * @return command
     */
    public Command requireCommand(ActionKey key) {
        return findCommand(key).orElseThrow(() -> new IllegalArgumentException("Missing command: " + key));
    }

    /**
     * Executes a command by generated action key on the EDT.
     *
     * @param key action key
     * @return this tester
     */
    public SwingMvpTester<V, P> executeCommand(ActionKey key) {
        Objects.requireNonNull(key, "key");
        runAndWaitUnchecked(() -> requireCommandOnEdt(key).execute());
        return this;
    }

    /**
     * Asserts a command's enabled state on the EDT.
     *
     * @param key action key
     * @param expected expected enabled state
     * @return this tester
     */
    public SwingMvpTester<V, P> assertCommandEnabled(ActionKey key, boolean expected) {
        Objects.requireNonNull(key, "key");
        boolean actual = queryOnEdt((view, presenter) -> requireCommandOnEdt(key).isEnabled());
        if (actual != expected) {
            throw new AssertionError("Expected command " + key + " enabled=" + expected + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts a command's selected state on the EDT.
     *
     * @param key action key
     * @param expected expected selected state
     * @return this tester
     */
    public SwingMvpTester<V, P> assertCommandSelected(ActionKey key, boolean expected) {
        Objects.requireNonNull(key, "key");
        boolean actual = queryOnEdt((view, presenter) -> requireCommandOnEdt(key).isSelected());
        if (actual != expected) {
            throw new AssertionError("Expected command " + key + " selected=" + expected + " but was " + actual);
        }
        return this;
    }

    private Command requireCommandOnEdt(ActionKey key) {
        return commands.find(key).orElseThrow(() -> new IllegalArgumentException("Missing command: " + key));
    }

    private static <C extends JComponent> C requireComponentOnEdt(JComponent root, ComponentKey<C> key) {
        return findComponentOnEdt(root, key)
                .orElseThrow(() -> new IllegalArgumentException("Missing component: " + key));
    }

    private static void selectViewRow(JTable table, int viewRow) {
        checkViewRow(table, viewRow);
        table.setRowSelectionInterval(viewRow, viewRow);
        if (table.getColumnCount() > 0) {
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        }
    }

    private static void checkViewRow(JTable table, int viewRow) {
        if (viewRow < 0 || viewRow >= table.getRowCount()) {
            throw new IndexOutOfBoundsException("Table view row out of bounds: " + viewRow);
        }
    }

    private static void checkModelRow(JTable table, int modelRow) {
        if (modelRow < 0 || modelRow >= table.getModel().getRowCount()) {
            throw new IndexOutOfBoundsException("Table model row out of bounds: " + modelRow);
        }
    }

    private int countProblems(
            BiFunction<? super V, ? super P, ? extends ProblemSet> problemSource,
            ProblemFilter filter
    ) {
        Objects.requireNonNull(problemSource, "problemSource");
        Objects.requireNonNull(filter, "filter");
        return queryOnEdt((view, presenter) ->
                Objects.requireNonNull(problemSource.apply(view, presenter), "problemSource.apply()")
                        .filter(filter)
                        .size());
    }

    private static <O, T> ProblemFilter fieldProblemFilter(FieldKey<O, T> fieldKey, ProblemCode code) {
        Objects.requireNonNull(fieldKey, "fieldKey");
        Objects.requireNonNull(code, "code");
        return ProblemFilter.field(fieldKey)
                .and(problem -> problem.code().equals(code));
    }

    private <C extends JComponent> boolean hasBehavior(
            ComponentKey<C> componentKey,
            BiFunction<? super V, ? super P, ? extends BehaviorScope> scopeSource,
            BehaviorKey behaviorKey
    ) {
        Objects.requireNonNull(componentKey, "componentKey");
        Objects.requireNonNull(scopeSource, "scopeSource");
        Objects.requireNonNull(behaviorKey, "behaviorKey");
        return queryOnEdt((view, presenter) -> {
            C component = requireComponentOnEdt(view, componentKey);
            BehaviorScope scope = Objects.requireNonNull(scopeSource.apply(view, presenter), "scopeSource.apply()");
            return scope.hasBehavior(component, behaviorKey);
        });
    }

    private TableState readTableState(BiFunction<? super V, ? super P, ? extends TableState> stateSource) {
        Objects.requireNonNull(stateSource, "stateSource");
        return queryOnEdt((view, presenter) ->
                Objects.requireNonNull(stateSource.apply(view, presenter), "stateSource.apply()"));
    }

    private static ColumnState requireColumnState(TableState state, String columnId) {
        Objects.requireNonNull(columnId, "columnId");
        return state.columns().stream()
                .filter(column -> column.id().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected table state " + state.tableId()
                        + " to contain column " + columnId + " but columns were " + columnIds(state)));
    }

    private static SortState requireSortState(TableState state, String columnId) {
        Objects.requireNonNull(columnId, "columnId");
        return state.sort().stream()
                .filter(sort -> sort.columnId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected table state " + state.tableId()
                        + " to contain sort for column " + columnId + " but sorts were " + sortColumnIds(state)));
    }

    private static List<String> columnIds(TableState state) {
        return state.columns().stream()
                .map(ColumnState::id)
                .toList();
    }

    private static List<String> sortColumnIds(TableState state) {
        return state.sort().stream()
                .map(SortState::columnId)
                .toList();
    }

    private static <C extends JComponent> Optional<C> findComponentOnEdt(JComponent root, ComponentKey<C> key) {
        List<C> matches = new ArrayList<>();
        collectMatches(root, key, matches);
        if (matches.size() > 1) {
            throw new IllegalStateException("Duplicate components for key: " + key);
        }
        return matches.stream().findFirst();
    }

    private static <C extends JComponent> void collectMatches(Component component, ComponentKey<C> key, List<C> matches) {
        if (component instanceof JComponent jComponent && SwingComponentKeys.matches(jComponent, key)) {
            matches.add(key.componentType().cast(jComponent));
        }
        if (!(component instanceof Container container)) {
            return;
        }
        for (Component child : container.getComponents()) {
            collectMatches(child, key, matches);
        }
    }

    private static void runAndWaitUnchecked(Runnable work) {
        if (SwingUtilities.isEventDispatchThread()) {
            work.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(work);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for EDT", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("EDT work failed", cause);
        }
    }
}
