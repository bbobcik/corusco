package cz.auderis.corusco.core.form;

import java.util.ArrayDeque;

/** Serializes reentrant edits until the current edit and its notifications finish. */
final class FormMutations {
    private final ArrayDeque<Runnable> queue = new ArrayDeque<>();
    private final ArrayDeque<Runnable> afterDrain = new ArrayDeque<>();
    private boolean running;
    private Object permittedOwner;
    private String permittedOperation;
    boolean isRunning() { return running; }
    void run(Runnable mutation) {
        if (running) { queue.add(mutation); return; }
        running = true;
        try {
            mutation.run();
            while (!queue.isEmpty()) queue.remove().run();
        } finally {
            queue.clear();
            running = false;
            while (!afterDrain.isEmpty()) afterDrain.remove().run();
        }
    }
    void afterDrain(Runnable action) {
        if (running) afterDrain.add(action);
        else action.run();
    }
    void runOwned(Object owner, String operation, Runnable mutation) {
        if (running && permittedOwner == owner && operation.equals(permittedOperation)) {
            permittedOwner = null;
            permittedOperation = null;
            mutation.run();
        } else run(mutation);
    }
    void invokeChild(Object child, String operation, Runnable action) {
        Object previousOwner = permittedOwner;
        String previousOperation = permittedOperation;
        permittedOwner = child;
        permittedOperation = operation;
        try { action.run(); }
        finally { permittedOwner = previousOwner; permittedOperation = previousOperation; }
    }
}
