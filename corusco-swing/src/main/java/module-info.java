/**
 * Swing/AWT bindings, behaviors, table adapters, dialogs, and test helpers.
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
