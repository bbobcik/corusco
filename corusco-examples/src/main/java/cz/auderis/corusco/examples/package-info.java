/**
 * Executable examples that demonstrate Corusco APIs by topic.
 *
 * <h2>How To Read The Examples</h2>
 *
 * <p>The examples are small scenarios, not a complete application framework.
 * Each package focuses on one area of the library and shows the intended shape
 * of code that a developer can adapt in an application.</p>
 *
 * <p>Most examples intentionally keep surrounding application architecture
 * thin. They focus on the Corusco concept being demonstrated: the model,
 * descriptor, binding, generated class, dialog behavior, or test fixture that
 * matters for the topic.</p>
 *
 * <p>Start with examples that match the problem in front of you. If you are
 * learning the basic model layer, read the core examples first. If you are
 * wiring Swing components, read the Swing and dialog examples. If you are
 * evaluating annotation generation, read the generated examples.</p>
 *
 * <h2>Core Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.core} demonstrates the toolkit-neutral
 * concepts: typed keys, resources, problems, validation, observable values,
 * loadable values, master/detail values, lifecycle cleanup, help services,
 * generation counters, and asynchronous tasks.</p>
 *
 * <p>These examples are the best place to build the initial mental model. They
 * show that Corusco state exists independently of Swing components.</p>
 *
 * <h2>Collection Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.collections} demonstrates observable
 * lists, loadable lists, filtered lists, transformed lists, Swing list
 * adapters, EDT list boundaries, and Glazed Lists interoperability.</p>
 *
 * <p>Read these examples before implementing a table or combo box backed by a
 * changing row list. They show how list changes are represented before Swing
 * displays them.</p>
 *
 * <h2>Form Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.forms} demonstrates form models and
 * generated form model usage. Use these examples to understand raw text,
 * semantic values, parse state, dirty state, touched state, validation
 * problems, reset, and result creation.</p>
 *
 * <pre>{@code
 * CustomerEditFormModel model = new CustomerEditFormModel(customer);
 * model.name.setRawText("Ada", StandardChangeOrigin.USER);
 *
 * if (model.isCommittable()) {
 *     Customer updated = model.toResult();
 * }
 * }</pre>
 *
 * <h2>Table Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.tables} demonstrates generated table
 * columns, observable table models, table selection binding, table cell
 * validation, table cell tooltips, header tooltips, and persisted table state.</p>
 *
 * <p>Read these examples when you want to understand tables built from typed
 * table descriptions. The description defines stable table meaning; Swing
 * table adapters display that meaning in a concrete {@code JTable}.</p>
 *
 * <h2>Command Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.commands} demonstrates core commands
 * and generated action metadata. Use these examples when wiring buttons, menu
 * items, toolbars, or keyboard shortcuts to one shared action contract.</p>
 *
 * <h2>Dialog Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.dialogs} demonstrates form dialogs,
 * dirty cancel confirmation, validation behavior, active editor commit,
 * keyboard handling, dialog lifecycle, and asynchronous field validation.</p>
 *
 * <p>Read these examples when the user needs OK, Cancel, Apply-like behavior,
 * validation focus, or protection against losing unsaved edits.</p>
 *
 * <h2>Swing Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.swing} demonstrates direct bindings,
 * behaviors, status text, tooltips, accessible text, busy overlays, Swing task
 * integration, and a baseline Swing screen.</p>
 *
 * <p>These examples are useful after the core model is clear. They show the
 * component boundary: installing listeners, subscribing to models, updating
 * Swing state, and closing bindings.</p>
 *
 * <h2>Generated Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.generated} contains annotated source
 * fixtures and examples that consume their generated classes. This package is
 * the best place to see the full annotation-to-generated-code workflow.</p>
 *
 * <p>Generated companion classes appear at compile time. Users normally edit
 * the annotated source and consume generated output; they do not
 * hand-maintain generated classes.</p>
 *
 * <h2>Testing Examples</h2>
 *
 * <p>{@code cz.auderis.corusco.examples.testing} demonstrates Swing presenter
 * and view testing support. Read it when a test needs to exercise generated
 * behavior plans, component bindings, problem display, or table state with
 * Swing components.</p>
 *
 * <h2>Practical Reading Order</h2>
 *
 * <ol>
 *   <li>Read a core example for the model concept.</li>
 *   <li>Read the matching Swing example for component wiring.</li>
 *   <li>Read the generated example when annotations can remove repeated
 *       boilerplate.</li>
 *   <li>Read the testing example that matches the layer being tested.</li>
 * </ol>
 *
 * <p>This order keeps the mental model stable: first learn the state, then the
 * Swing adapter, then the generated shortcut.</p>
 *
 * <p>When an example uses generated classes, read the annotated source next to
 * the consuming code. The pair shows both sides of the workflow: the source
 * declaration a developer maintains and the generated API shape that the
 * application uses.</p>
 */
package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.value.StandardChangeOrigin;
