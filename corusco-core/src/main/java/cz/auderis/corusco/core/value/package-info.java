/**
 * Synchronous observable value primitives for Corusco presentation state.
 *
 * <p>This package is the scalar-value foundation for field models, selection
 * state, derived labels, busy flags, and other presenter-owned state. Start
 * with {@link cz.auderis.corusco.core.value.ReadableValue} and
 * {@link cz.auderis.corusco.core.value.WritableValue}. {@link
 * cz.auderis.corusco.core.value.SimpleValue} is the standard mutable
 * implementation, while {@link cz.auderis.corusco.core.value.DerivedValue} and
 * {@link cz.auderis.corusco.core.value.MappedValue} expose computed values
 * based on other observables.</p>
 *
 * <p>{@link cz.auderis.corusco.core.value.ValueChangeEvent} describes each
 * change and {@link cz.auderis.corusco.core.value.ValueChangeListener}
 * receives it synchronously. {@link cz.auderis.corusco.core.value.ChangeOrigin}
 * lets bindings distinguish user-originated and programmatic updates where a
 * model exposes that distinction. Subscriptions must be closed by the owner
 * that registered them.</p>
 *
 * <p>{@link cz.auderis.corusco.core.value.LoadableValue},
 * {@link cz.auderis.corusco.core.value.DetachableValue}, and
 * {@link cz.auderis.corusco.core.value.MasterDetailValue} add lifecycle-aware
 * patterns for values backed by external data or another selected value. These
 * types integrate with {@code cz.auderis.corusco.core.lifecycle} so presenters
 * can release cached state without destroying the owning object.</p>
 *
 * <p>Values are Swing-free and make no implicit threading guarantees. Events
 * are delivered by the mutating call. If a value is bound to Swing components,
 * mutate it according to the binding's EDT requirements or insert an explicit
 * dispatch boundary.</p>
 */
package cz.auderis.corusco.core.value;
