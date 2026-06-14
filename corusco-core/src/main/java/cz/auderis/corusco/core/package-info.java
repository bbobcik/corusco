/**
 * Root package for Corusco's toolkit-neutral presentation model runtime.
 *
 * <p>The root package contains the module marker and points readers to the
 * focused runtime packages. Corusco core is not a widget toolkit; it is the
 * model, metadata, validation, command, resource, lifecycle, table, and task
 * foundation that Swing adapters and generated code consume.</p>
 *
 * <p>Use
 * {@link cz.auderis.corusco.core.key} for typed identifiers,
 * {@link cz.auderis.corusco.core.value} and
 * {@link cz.auderis.corusco.core.collection} for synchronous observable state,
 * {@link cz.auderis.corusco.core.form} and
 * {@link cz.auderis.corusco.core.validation} for form editing, and
 * {@link cz.auderis.corusco.core.table} for table descriptors and persisted
 * layout state. Use {@link cz.auderis.corusco.core.command} for executable
 * actions, {@link cz.auderis.corusco.core.resource} and
 * {@link cz.auderis.corusco.core.help} for user-facing metadata lookup and
 * help dispatch, and {@link cz.auderis.corusco.core.lifecycle} for deterministic
 * cleanup of subscriptions and detachable state.</p>
 *
 * <p>Core types deliberately avoid Swing dependencies. They define the data,
 * metadata, and lifecycle contracts that generated code and Swing adapters use
 * later. Thread confinement is therefore a responsibility of the adapter layer
 * or the application code that mutates a model. If the same model is connected
 * to Swing, read the corresponding package in {@code cz.auderis.corusco.swing}
 * for EDT, listener, and component-lifecycle rules.</p>
 */
package cz.auderis.corusco.core;
