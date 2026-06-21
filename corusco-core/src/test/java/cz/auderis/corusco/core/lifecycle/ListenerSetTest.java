package cz.auderis.corusco.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListenerSetTest {

    @Test
    void dispatchesEventsToRegisteredListenersInRegistrationOrder() {
        ListenerSet<java.util.function.Consumer<String>, String> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        listeners.addListener(event -> observed.add("first:" + event));
        listeners.addListener(event -> observed.add("second:" + event));

        listeners.fireEvent("changed", java.util.function.Consumer::accept);

        assertThat(observed).containsExactly("first:changed", "second:changed");
    }

    @Test
    void duplicateRegistrationOfSameInstanceIsIgnored() {
        ListenerSet<Runnable, Void> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        Runnable listener = () -> observed.add("called");

        Subscription first = listeners.addListener(listener);
        Subscription duplicate = listeners.addListener(listener);
        duplicate.close();
        listeners.fireEvent(null, (registeredListener, ignored) -> registeredListener.run());
        first.close();
        listeners.fireEvent(null, (registeredListener, ignored) -> registeredListener.run());

        assertThat(observed).containsExactly("called");
    }

    @Test
    void identityDefinesMembershipInsteadOfEquality() {
        ListenerSet<Runnable, Void> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();

        listeners.addListener(new EqualRunnable(() -> observed.add("first")));
        listeners.addListener(new EqualRunnable(() -> observed.add("second")));
        listeners.fireEvent(null, (registeredListener, ignored) -> registeredListener.run());

        assertThat(observed).containsExactly("first", "second");
    }

    @Test
    void closingSubscriptionRemovesListenerFromFutureDispatches() {
        ListenerSet<java.util.function.Consumer<String>, String> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        Subscription subscription = listeners.addListener(observed::add);

        listeners.fireEvent("before", java.util.function.Consumer::accept);
        subscription.close();
        subscription.close();
        listeners.fireEvent("after", java.util.function.Consumer::accept);

        assertThat(observed).containsExactly("before");
    }

    @Test
    void explicitRemovalAndClearAffectFutureDispatches() {
        ListenerSet<Runnable, Void> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        Runnable first = () -> observed.add("first");
        Runnable second = () -> observed.add("second");
        listeners.addListener(first);
        listeners.addListener(second);

        listeners.removeListener(first);
        listeners.fireEvent(null, (registeredListener, ignored) -> registeredListener.run());
        listeners.clearListeners();
        listeners.fireEvent(null, (registeredListener, ignored) -> registeredListener.run());

        assertThat(observed).containsExactly("second");
    }

    @Test
    void removalDuringDispatchAffectsFutureEventsOnly() {
        ListenerSet<java.util.function.Consumer<String>, String> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        Subscription[] firstSubscription = new Subscription[1];
        java.util.function.Consumer<String> first = event -> {
            observed.add("first:" + event);
            firstSubscription[0].close();
        };
        firstSubscription[0] = listeners.addListener(first);
        listeners.addListener(event -> observed.add("second:" + event));

        listeners.fireEvent("one", java.util.function.Consumer::accept);
        listeners.fireEvent("two", java.util.function.Consumer::accept);

        assertThat(observed).containsExactly("first:one", "second:one", "second:two");
    }

    @Test
    void nullableEventPayloadIsDelivered() {
        ListenerSet<java.util.function.Consumer<String>, String> listeners = new ListenerSet<>();
        List<String> observed = new ArrayList<>();
        listeners.addListener(event -> observed.add(event == null ? "null" : event));

        listeners.fireEvent(null, java.util.function.Consumer::accept);

        assertThat(observed).containsExactly("null");
    }

    private static final class EqualRunnable implements Runnable {

        private final Runnable delegate;

        EqualRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            delegate.run();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EqualRunnable;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}
