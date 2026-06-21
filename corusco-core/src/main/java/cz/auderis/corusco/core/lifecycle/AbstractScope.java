package cz.auderis.corusco.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractScope<T> implements Disposable {

    private final List<T> children;
    private volatile boolean closed;

    protected AbstractScope() {
        children = new ArrayList<>(2);
    }

    public <D extends T> D add(D child) {
        Objects.requireNonNull(child, "child");
        if (NoOperation.INSTANCE == child) {
            return child;
        }
        synchronized (this) {
            if (!closed) {
                children.add(child);
                return child;
            }
        }
        closeChild(child);
        return child;
    }

    /**
     * Indicates whether this scope has been permanently closed.
     *
     * @return {@code true} after {@link #close()} has been called
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        final List<T> closing;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            if (children.isEmpty()) {
                return;
            }
            closing = List.copyOf(children.reversed());
            children.clear();
        }
        processChildren(closing, this::closeChild, closeFailureMessage());
    }

    protected abstract void closeChild(T child);

    protected String closeFailureMessage() {
        return "One or more children failed to close";
    }

    protected ScopeException failure(String message) {
        return new ScopeException(message);
    }

    protected synchronized final List<T> childrenSnapshot() {
        return List.copyOf(children);
    }

    protected final void processChildren(Iterable<T> children, Consumer<T> childAction, String failureMessage) {
        ScopeException failure = null;
        for (T child : children) {
            try {
                childAction.accept(child);
            } catch (RuntimeException | Error e) {
                if (failure == null) {
                    failure = failure(failureMessage);
                }
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

}
