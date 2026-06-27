/**
 * Synchronous observable value primitives for Corusco presentation state.
 *
 * <p>This package solves the scalar state problem: a presenter needs to expose
 * a current value, let other code observe changes, sometimes let the value be
 * mutated, and close subscriptions predictably. It is the foundation for field
 * models, selection state, derived labels, dirty flags, enabled flags, busy
 * indicators, and any other single value that should not be represented as a
 * Swing component property.</p>
 *
 * <p>Use this package when state is not itself a form field but still needs
 * observation. Examples include the selected row in a table, the current
 * detail object in a master/detail view, a computed status message, a disabled
 * reason, a progress flag, or data loaded from an external source. Use
 * {@code cz.auderis.corusco.core.form} when the value is an editable form field
 * with reset, dirty, parse, and validation semantics.</p>
 *
 * <p>The first step is usually a {@link
 * cz.auderis.corusco.core.value.SimpleValue}:</p>
 *
 * <pre>{@code
 * WritableValue<CustomerRow> selected = SimpleValue.of(null);
 * Subscription subscription = selected.subscribe(event -> refreshDetails(event.newValue()));
 * selected.setValue(row, StandardChangeOrigin.USER);
 * subscription.close();
 * }</pre>
 *
 * <p>Code that only reads a value should depend on {@link
 * cz.auderis.corusco.core.value.ReadableValue}; code that owns mutation should
 * use {@link cz.auderis.corusco.core.value.WritableValue}. {@link
 * cz.auderis.corusco.core.value.ValueChangeEvent} describes each change and
 * {@link cz.auderis.corusco.core.value.ValueChangeListener} receives it
 * synchronously. {@link cz.auderis.corusco.core.value.StandardChangeOrigin}
 * covers framework-defined origins, while {@link
 * cz.auderis.corusco.core.value.CustomChangeOrigin} can be used for
 * application-specific diagnostic origins.</p>
 *
 * <p>Expose the narrowest interface that matches ownership. A presenter may
 * keep a {@code WritableValue} privately while exposing only a
 * {@code ReadableValue} to view code. That pattern makes it clear which object
 * owns mutation and prevents convenience writes from bypassing command logic,
 * validation timing, or loading state. Generated and handwritten bindings
 * should usually receive the readable or writable contract they actually need,
 * not the concrete implementation.</p>
 *
 * <p>Use {@link cz.auderis.corusco.core.value.MappedValue} when a display value
 * is a direct projection of another value, such as a selected customer name.
 * Use {@link cz.auderis.corusco.core.value.DerivedValue} when the result
 * depends on several values or needs custom recomputation. Keep derived values
 * read-only from the outside; mutate their sources instead.</p>
 *
 * <p>Derived values are best used for inexpensive, deterministic projections.
 * They are not a replacement for background loading, caching, or command
 * execution. If recomputing a value may block, fail, or require cancellation,
 * represent that workflow explicitly with a loadable value, task, or presenter
 * state machine and then publish the resulting scalar state through this
 * package.</p>
 *
 * <p>{@link cz.auderis.corusco.core.value.LoadableValue}, {@link
 * cz.auderis.corusco.core.value.DetachableValue}, and {@link
 * cz.auderis.corusco.core.value.MasterDetailValue} add lifecycle-aware patterns
 * for values backed by external data or another selected value. They integrate
 * with {@code cz.auderis.corusco.core.lifecycle} so presenters can release
 * cached state without destroying the owning object.</p>
 *
 * <p>Although the package is {@link org.jspecify.annotations.NullMarked}, the
 * scalar value payload is nullable by design: {@code value()},
 * {@code setValue(...)}, {@code refresh()}, suppliers, mappers, and loaders
 * use explicit nullable type-use annotations where they accept or produce a
 * possibly absent value. For example, a selected row value commonly uses
 * {@code null} to mean "no selection", while a required form field may use a
 * richer field model so absence, parse failure, and validation failure remain
 * distinguishable. Document null meaning at the presenter boundary instead of
 * relying on readers to infer it from implementation details.</p>
 *
 * <p>Subscriptions must be closed by the owner that registered them. Value
 * implementations use {@link cz.auderis.corusco.core.lifecycle.ListenerSet}
 * for listener storage: duplicate registration of the same listener instance is
 * ignored, listener identity is used instead of equality, and dispatch uses the
 * listener snapshot captured at the start of the event. Values are Swing-free
 * and make no implicit threading guarantees: events are delivered by the
 * mutating call. If a value is bound to Swing components, mutate it according
 * to the binding's EDT requirements or insert an explicit dispatch boundary.</p>
 *
 * <p>Testing values should cover initial state, event order, origin propagation,
 * unsubscribe behavior, and derived-value recomputation. For bindings, test the
 * Swing adapter separately: a value unit test should not require an Event
 * Dispatch Thread unless the class under test explicitly depends on Swing.
 * This separation keeps presenter-state tests fast and makes UI threading
 * assumptions visible in the Swing layer.</p>
 */
@NullMarked
package cz.auderis.corusco.core.value;

import org.jspecify.annotations.NullMarked;
