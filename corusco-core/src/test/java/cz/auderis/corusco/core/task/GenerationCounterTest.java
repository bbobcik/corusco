package cz.auderis.corusco.core.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerationCounterTest {

    @Test
    void currentTokenStartsAtZeroAndAdvanceMakesOlderTokensStale() {
        GenerationCounter counter = new GenerationCounter();

        GenerationCounter.Generation initial = counter.current();
        GenerationCounter.Generation next = counter.advance();

        assertThat(initial.value()).isZero();
        assertThat(next.value()).isEqualTo(1L);
        assertThat(counter.isStale(initial)).isTrue();
        assertThat(counter.isCurrent(next)).isTrue();
    }

    @Test
    void invalidateAdvancesWithoutApplyingWork() {
        GenerationCounter counter = new GenerationCounter();
        GenerationCounter.Generation scheduled = counter.advance();

        GenerationCounter.Generation invalidated = counter.invalidate();

        assertThat(invalidated.value()).isEqualTo(scheduled.value() + 1);
        assertThat(counter.isStale(scheduled)).isTrue();
    }

    @Test
    void tryAcceptAppliesOnlyCurrentResults() {
        GenerationCounter counter = new GenerationCounter();
        GenerationCounter.Generation first = counter.advance();
        GenerationCounter.Generation second = counter.advance();
        List<String> accepted = new ArrayList<>();

        assertThat(counter.tryAccept(first, "old", accepted::add)).isFalse();
        assertThat(counter.tryAccept(second, "new", accepted::add)).isTrue();

        assertThat(accepted).containsExactly("new");
    }

    @Test
    void tryAcceptSupportsNullResults() {
        GenerationCounter counter = new GenerationCounter();
        GenerationCounter.Generation generation = counter.advance();
        AtomicBoolean acceptedNull = new AtomicBoolean();

        assertThat(counter.tryAccept(generation, null, value -> acceptedNull.set(value == null))).isTrue();

        assertThat(acceptedNull).isTrue();
    }

    @Test
    void generationChangesAreVisibleAcrossThreads() throws Exception {
        GenerationCounter counter = new GenerationCounter();
        GenerationCounter.Generation scheduled = counter.advance();
        CountDownLatch workerReady = new CountDownLatch(1);
        CountDownLatch invalidated = new CountDownLatch(1);
        AtomicBoolean staleOnWorker = new AtomicBoolean();
        Thread worker = Thread.ofVirtual().start(() -> {
            workerReady.countDown();
            try {
                invalidated.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            staleOnWorker.set(counter.isStale(scheduled));
        });

        assertThat(workerReady.await(2, TimeUnit.SECONDS)).isTrue();
        counter.invalidate();
        invalidated.countDown();
        worker.join(2_000L);

        assertThat(staleOnWorker).isTrue();
    }

    @Test
    void negativeGenerationTokenIsRejected() {
        assertThatThrownBy(() -> new GenerationCounter.Generation(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must not be negative");
    }
}
