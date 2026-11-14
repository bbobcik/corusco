package cz.auderis.corusco.core.form;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** Tracks immediate composite ownership without changing mutation routing. */
final class FormOwnership {
    private record Entry(WeakReference<ObservableFormModel<?>> child, WeakReference<Object> parent) { }
    private static final List<Entry> OWNERS = new ArrayList<>();

    private static void prune() {
        OWNERS.removeIf(entry -> entry.child().get() == null || entry.parent().get() == null);
    }
    private static boolean owned(ObservableFormModel<?> child) {
        return OWNERS.stream().anyMatch(entry -> entry.child().get() == child);
    }

    static synchronized void reserve(Object parent, List<? extends ObservableFormModel<?>> children) {
        prune();
        for (var child : children) {
            if (owned(child) || disposed(child)) {
                throw new IllegalStateException("Child form is disposed or already has a composite parent");
            }
        }
        for (var child : children) OWNERS.add(new Entry(new WeakReference<>(child), new WeakReference<>(parent)));
    }
    static synchronized void release(Object parent, List<? extends ObservableFormModel<?>> children) {
        prune();
        OWNERS.removeIf(entry -> entry.parent().get() == parent
                && children.stream().anyMatch(child -> child == entry.child().get()));
    }
    static synchronized void requireUnowned(ObservableFormModel<?> form) {
        prune();
        boolean activeParent = OWNERS.stream().anyMatch(entry -> entry.child().get() == form
                && entry.parent().get() instanceof AbstractObservableCompositeFormModel<?> parent
                && !parent.isDisposed());
        if (activeParent) throw new IllegalStateException("Close the composite parent first");
    }
    static boolean disposed(ObservableFormModel<?> child) {
        return child instanceof AbstractObservableFormModel<?> form && form.isDisposed()
                || child instanceof AbstractObservableCompositeFormModel<?> composite && composite.isDisposed();
    }
    private FormOwnership() { }
}
