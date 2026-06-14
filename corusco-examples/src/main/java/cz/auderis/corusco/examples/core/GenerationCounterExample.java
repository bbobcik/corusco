package cz.auderis.corusco.examples.core;

import cz.auderis.corusco.core.task.GenerationCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates stale asynchronous result suppression.
 *
 * <p>The scenario increments a generation counter as user input changes and
 * accepts only the result that belongs to the latest generation. It illustrates
 * the pattern used by asynchronous validation and loading code to avoid
 * publishing obsolete work.</p>
 */
public final class GenerationCounterExample {

    private GenerationCounterExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs an out-of-order validation result scenario.
     *
     * @return accepted result diagnostics
     */
    public static List<String> runScenario() {
        GenerationCounter generations = new GenerationCounter();
        List<String> accepted = new ArrayList<>();

        // Each edit schedules a new generation. The first validation may still
        // be running when the user types again, so its token must travel with
        // the asynchronous request.
        GenerationCounter.Generation firstEdit = generations.advance();
        GenerationCounter.Generation secondEdit = generations.advance();

        // Results can arrive out of order. The stale first result is ignored;
        // only the latest generation is allowed to update presentation state.
        generations.tryAccept(firstEdit, "name already used", accepted::add);
        generations.tryAccept(secondEdit, "ok", accepted::add);

        // Clearing the field invalidates outstanding work even if no new
        // validation is scheduled immediately.
        GenerationCounter.Generation beforeClear = generations.current();
        generations.invalidate();
        accepted.add("beforeClearStale=" + generations.isStale(beforeClear));
        return accepted;
    }
}
