/**
 * Provides the toolkit-neutral runtime model used by Corusco Swing
 * applications.
 *
 * <p>The core module contains no Swing components. It defines typed identity
 * keys, observable values and collections, command metadata, resource lookup,
 * form state, parsing and validation problems, table descriptors,
 * technology-neutral data read models, edit staging, and asynchronous task
 * contracts. Swing adapters in
 * {@code cz.auderis.corusco.swing} build on these contracts, but generated
 * presentation metadata and handwritten models can depend on this module while
 * avoiding a dependency on {@code java.desktop}.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.core.form} for form models,
 * {@link cz.auderis.corusco.core.table} for descriptor-backed table metadata,
 * {@link cz.auderis.corusco.core.data} for windowed enterprise data sources,
 * and {@link cz.auderis.corusco.core.value} or
 * {@link cz.auderis.corusco.core.collection} for observable presentation
 * state. Problems reported by parsing, validation, and UI integration use the
 * shared model in {@link cz.auderis.corusco.core.problem}.</p>
 *
 * <p>Types in this module are synchronous and do not themselves marshal work to
 * the Swing Event Dispatch Thread. Code that connects them to Swing must apply
 * the EDT rules documented by the Swing module.</p>
 */
module cz.auderis.corusco.core {
    requires java.prefs;
    requires transitive cz.auderis.corusco.annotations;
    requires static org.jspecify;
    requires static org.jetbrains.annotations;

    exports cz.auderis.corusco.core;
    exports cz.auderis.corusco.core.collection;
    exports cz.auderis.corusco.core.command;
    exports cz.auderis.corusco.core.convert;
    exports cz.auderis.corusco.core.data;
    exports cz.auderis.corusco.core.data.edit;
    exports cz.auderis.corusco.core.dataset;
    exports cz.auderis.corusco.core.dialog;
    exports cz.auderis.corusco.core.form;
    exports cz.auderis.corusco.core.help;
    exports cz.auderis.corusco.core.key;
    exports cz.auderis.corusco.core.lifecycle;
    exports cz.auderis.corusco.core.meta;
    exports cz.auderis.corusco.core.problem;
    exports cz.auderis.corusco.core.resource;
    exports cz.auderis.corusco.core.table;
    exports cz.auderis.corusco.core.task;
    exports cz.auderis.corusco.core.tooltip;
    exports cz.auderis.corusco.core.validation;
    exports cz.auderis.corusco.core.value;
}
