package cz.auderis.corusco.swing.testing;

import cz.auderis.corusco.core.key.ComponentKey;
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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * EDT-safe test harness for Swing MVP views and presenters.
 *
 * <p>The tester creates the view and optional presenter on the EDT, then runs
 * all lookup and interaction helpers on the EDT. Direct accessors are kept
 * intentionally small; tests should prefer {@link #runOnEdt(BiConsumer)} and
 * {@link #queryOnEdt(BiFunction)} when they interact with Swing state.</p>
 *
 * @param <V> root view type
 * @param <P> presenter type
 */
public final class SwingMvpTester<V extends JComponent, P> {

    private final V view;
    private final P presenter;

    private SwingMvpTester(V view, P presenter) {
        this.view = Objects.requireNonNull(view, "view");
        this.presenter = presenter;
    }

    /**
     * Creates a tester with no presenter.
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
        Objects.requireNonNull(viewFactory, "viewFactory");
        Objects.requireNonNull(presenterFactory, "presenterFactory");
        AtomicReference<SwingMvpTester<V, P>> tester = new AtomicReference<>();
        runAndWaitUnchecked(() -> {
            V view = Objects.requireNonNull(viewFactory.get(), "viewFactory.get()");
            P presenter = presenterFactory.apply(view);
            tester.set(new SwingMvpTester<>(view, presenter));
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
