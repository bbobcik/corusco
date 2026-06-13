package cz.auderis.corusco.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionScopeTest {

    @Test
    void subscriptionRunsCleanupOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        Subscription subscription = Subscription.of(calls::incrementAndGet);

        subscription.close();
        subscription.close();

        assertThat(calls).hasValue(1);
    }

    @Test
    void scopeClosesChildrenInReverseRegistrationOrder() {
        List<String> closed = new ArrayList<>();
        SubscriptionScope scope = new SubscriptionScope();
        scope.onClose(() -> closed.add("first"));
        scope.onClose(() -> closed.add("second"));
        scope.onClose(() -> closed.add("third"));

        scope.close();

        assertThat(closed).containsExactly("third", "second", "first");
        assertThat(scope.isClosed()).isTrue();
    }

    @Test
    void repeatedScopeCloseDoesNotCloseChildrenAgain() {
        AtomicInteger calls = new AtomicInteger();
        SubscriptionScope scope = new SubscriptionScope();
        scope.onClose(calls::incrementAndGet);

        scope.close();
        scope.close();

        assertThat(calls).hasValue(1);
    }

    @Test
    void addingAfterCloseClosesChildImmediately() {
        AtomicInteger calls = new AtomicInteger();
        SubscriptionScope scope = new SubscriptionScope();
        scope.close();

        Subscription subscription = scope.onClose(calls::incrementAndGet);

        assertThat(subscription).isNotNull();
        assertThat(calls).hasValue(1);
    }

    @Test
    void failingChildDoesNotPreventOtherChildrenFromClosing() {
        List<String> closed = new ArrayList<>();
        SubscriptionScope scope = new SubscriptionScope();
        scope.onClose(() -> closed.add("first"));
        scope.onClose(() -> {
            closed.add("failing");
            throw new IllegalStateException("boom");
        });
        scope.onClose(() -> closed.add("third"));

        assertThatThrownBy(scope::close)
                .isInstanceOf(SubscriptionScopeException.class)
                .satisfies(error -> {
                    assertThat(error.getSuppressed()).hasSize(1);
                    assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
                });

        assertThat(closed).containsExactly("third", "failing", "first");
    }

    @Test
    void listenerStyleCleanupRemovesRegisteredListener() {
        List<Runnable> listeners = new ArrayList<>();
        Runnable listener = () -> {
        };
        listeners.add(listener);

        Subscription subscription = Subscription.of(() -> listeners.remove(listener));

        subscription.close();

        assertThat(listeners).isEmpty();
    }
}
