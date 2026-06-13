package cz.auderis.corusco.core.lifecycle;

import cz.auderis.corusco.core.collection.LoadableList;
import cz.auderis.corusco.core.value.LoadableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetachableScopeTest {

    @Test
    void detachesChildrenInReverseRegistrationOrder() {
        DetachableScope scope = new DetachableScope();
        List<String> detached = new ArrayList<>();
        scope.add(() -> detached.add("first"));
        scope.add(() -> detached.add("second"));
        scope.add(() -> detached.add("third"));

        scope.detach();

        assertThat(detached).containsExactly("third", "second", "first");
        assertThat(scope.isClosed()).isFalse();
    }

    @Test
    void repeatedDetachKeepsChildrenRegistered() {
        AtomicInteger calls = new AtomicInteger();
        DetachableScope scope = new DetachableScope();
        scope.add(calls::incrementAndGet);

        scope.detach();
        scope.detach();

        assertThat(calls).hasValue(2);
    }

    @Test
    void closeDetachesAndPreventsLaterDetachCalls() {
        AtomicInteger calls = new AtomicInteger();
        DetachableScope scope = new DetachableScope();
        scope.add(calls::incrementAndGet);

        scope.close();
        scope.close();
        scope.detach();

        assertThat(calls).hasValue(1);
        assertThat(scope.isClosed()).isTrue();
    }

    @Test
    void addingAfterCloseDetachesChildImmediately() {
        AtomicInteger calls = new AtomicInteger();
        DetachableScope scope = new DetachableScope();
        scope.close();

        Detachable child = scope.add(calls::incrementAndGet);

        assertThat(child).isNotNull();
        assertThat(calls).hasValue(1);
    }

    @Test
    void failingChildDoesNotPreventOtherChildrenFromDetaching() {
        DetachableScope scope = new DetachableScope();
        List<String> detached = new ArrayList<>();
        scope.add(() -> detached.add("first"));
        scope.add(() -> {
            detached.add("failing");
            throw new IllegalStateException("boom");
        });
        scope.add(() -> detached.add("third"));

        assertThatThrownBy(scope::detach)
                .isInstanceOf(DetachmentException.class)
                .satisfies(error -> {
                    assertThat(error.getSuppressed()).hasSize(1);
                    assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
                });
        assertThat(detached).containsExactly("third", "failing", "first");
    }

    @Test
    void closeMarksScopeClosedEvenWhenDetachmentFails() {
        DetachableScope scope = new DetachableScope();
        AtomicInteger lateCalls = new AtomicInteger();
        scope.add(() -> {
            throw new IllegalStateException("boom");
        });

        assertThatThrownBy(scope::close)
                .isInstanceOf(DetachmentException.class);
        scope.add(lateCalls::incrementAndGet);

        assertThat(scope.isClosed()).isTrue();
        assertThat(lateCalls).hasValue(1);
    }

    @Test
    void scopeDetachesLoadableModelsTogether() {
        LoadableValue<String> value = LoadableValue.of(() -> "loaded");
        LoadableList<String> rows = LoadableList.of(() -> List.of("alpha"));
        DetachableScope scope = new DetachableScope();
        scope.add(value);
        scope.add(rows);

        assertThat(value.value()).isEqualTo("loaded");
        assertThat(rows.snapshot()).containsExactly("alpha");

        scope.detach();

        assertThat(value.isAttached()).isFalse();
        assertThat(rows.isAttached()).isFalse();
    }
}
