/**
 * Typed problem model for parsing, validation, and UI feedback.
 *
 * <p>This package is the shared diagnostic vocabulary for Corusco forms,
 * tables, converters, validators, and Swing decorations. Start with
 * {@link cz.auderis.corusco.core.problem.Problem}, which combines a stable
 * {@link cz.auderis.corusco.core.problem.ProblemCode}, severity, source,
 * target, and message. {@link cz.auderis.corusco.core.problem.ProblemSet}
 * carries immutable ordered collections of problems, and
 * {@link cz.auderis.corusco.core.problem.ProblemFilter} selects problems for a
 * field, row, cell, component, source, or severity threshold.</p>
 *
 * <p>{@link cz.auderis.corusco.core.problem.ProblemTarget} keeps routing
 * explicit. A problem can target the whole form, a typed field key, a row, a
 * table cell, or a typed component key. This avoids public JavaBeans property
 * paths and lets generated forms and tables preserve type information when
 * reporting validation feedback.</p>
 *
 * <p>The package is Swing-free. It does not decide whether a message becomes a
 * tooltip, border, summary row, status text, or dialog focus change. Swing
 * bindings in {@code cz.auderis.corusco.swing.binding},
 * {@code cz.auderis.corusco.swing.dialog}, and
 * {@code cz.auderis.corusco.swing.table} consume problem sets and apply those
 * presentation policies on the Event Dispatch Thread.</p>
 */
package cz.auderis.corusco.core.problem;
