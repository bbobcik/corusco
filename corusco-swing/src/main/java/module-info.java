/**
 * Adapts Corusco core presentation models to Swing components.
 *
 * <p>This module contains the Swing-specific layer for bindings, reusable
 * component behaviors, command-to-{@link javax.swing.Action} adapters,
 * observable list and table models, form-dialog controllers, task progress
 * bindings, and Swing test helpers. It depends transitively on
 * {@code cz.auderis.corusco.core}, so applications that use the Swing module
 * also see the typed keys, commands, form models, problems, and table
 * descriptors that those adapters consume.</p>
 *
 * <p>Most public types in this module are Event Dispatch Thread confined:
 * create them, mutate them, and close them on the EDT unless a specific type
 * documents a different rule. Background work should enter the UI through
 * {@link cz.auderis.corusco.swing.task} or explicit EDT dispatch rather than by
 * firing Swing events from worker threads.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.swing.binding} for component/value
 * bindings, {@link cz.auderis.corusco.swing.table} for descriptor-backed
 * tables and persisted column layout, and
 * {@link cz.auderis.corusco.swing.dialog} for reusable modal form semantics.
 * The behavior package provides a higher-level way to install repeatable
 * component capabilities without coupling generated presenters to concrete
 * Swing helper classes.</p>
 */
module cz.auderis.corusco.swing {
    requires transitive java.desktop;
    requires transitive cz.auderis.corusco.core;

    exports cz.auderis.corusco.swing;
    exports cz.auderis.corusco.swing.behavior;
    exports cz.auderis.corusco.swing.binding;
    exports cz.auderis.corusco.swing.collection;
    exports cz.auderis.corusco.swing.command;
    exports cz.auderis.corusco.swing.dialog;
    exports cz.auderis.corusco.swing.table;
    exports cz.auderis.corusco.swing.task;
    exports cz.auderis.corusco.swing.testing;
}
