/**
 * Lifecycle ownership primitives for listener registrations and reusable
 * presentation state.
 *
 * <p>The package separates permanent cleanup from temporary detachment.
 * {@link cz.auderis.corusco.core.lifecycle.Disposable} and
 * {@link cz.auderis.corusco.core.lifecycle.Subscription} represent resources
 * that should be closed when a view, presenter, or binding is no longer used.
 * {@link cz.auderis.corusco.core.lifecycle.Detachable} represents cached state
 * that can be released while the owning object remains reusable.</p>
 *
 * <p>{@link cz.auderis.corusco.core.lifecycle.SubscriptionScope} and
 * {@link cz.auderis.corusco.core.lifecycle.DetachableScope} own groups of
 * children, process them in reverse registration order, and aggregate cleanup
 * failures as suppressed exceptions. Scopes synchronize registration and close
 * state, while child cleanup still runs on the calling thread and must follow
 * the child object's threading rules.</p>
 *
 * <p>{@link cz.auderis.corusco.core.lifecycle.ListenerSet} is the lightweight
 * listener-storage primitive for internal observable models. It keeps
 * registration as a cold, locked copy-on-write path while dispatch stays a
 * lock-free snapshot read. Listener identity, not equality, defines
 * membership.</p>
 *
 * <p>A common usage flow is to create a scope when a presenter, behavior, or
 * dialog is activated, add every listener or binding registration as it is
 * created, and close the scope from the same lifecycle path that disposes the
 * view. Use a detachable scope for reloadable caches such as loadable values or
 * lists: call {@code detach()} between activations to release cached data, then
 * call {@code close()} when the owner is permanently finished.</p>
 *
 * <p>The package does not define thread ownership by itself. Swing adapters
 * normally close these scopes on the Event Dispatch Thread because their
 * children touch components, while core-only models can use whatever
 * confinement their owner already enforces. Idempotency is part of the public
 * contract for subscriptions and scopes; callers may safely close them during
 * both normal and exceptional teardown paths.</p>
 */
@NullMarked
package cz.auderis.corusco.core.lifecycle;

import org.jspecify.annotations.NullMarked;
