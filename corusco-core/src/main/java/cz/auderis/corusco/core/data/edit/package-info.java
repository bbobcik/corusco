/**
 * Technology-neutral CRUD edit staging for keyed data rows.
 *
 * <h2>Purpose</h2>
 *
 * <p>Edit sessions collect create, update, and delete intents before a backing
 * store is asked to persist them. The package models staged changes,
 * optimistic version tokens, conflicts, and save results, but it does not
 * depend on JDBC, REST, Kafka, Swing, transactions, repositories, or an
 * application-specific unit-of-work API.</p>
 *
 * <h2>Session model</h2>
 *
 * <p>A {@link cz.auderis.corusco.core.data.edit.CoruscoEditSession} is a
 * presenter-owned staging object. It uses a
 * {@link cz.auderis.corusco.core.data.CoruscoRowIdentity} to extract stable
 * keys and stores at most one staged change per key. Later create, update, or
 * delete operations for the same key replace the earlier staged operation while
 * preserving deterministic key order for the remaining entries.</p>
 *
 * <p>The session publishes two observable facts: dirty state and the current
 * immutable {@link cz.auderis.corusco.core.data.edit.CoruscoEditSet}. A save
 * command can observe dirty state for enablement and read the edit set as a
 * stable snapshot when submission begins. The session does not save anything
 * itself; a repository, REST client, batch command, or message publisher owns
 * that step.</p>
 *
 * <h2>Persistence results</h2>
 *
 * <p>{@link cz.auderis.corusco.core.data.edit.CoruscoSaveResult} separates
 * rows accepted by the backing store from row-level
 * {@link cz.auderis.corusco.core.data.edit.CoruscoConflict} instances and
 * general {@link cz.auderis.corusco.core.problem.ProblemSet} diagnostics.
 * This lets a backend report optimistic-lock conflicts, server validation
 * problems, and successfully saved rows without forcing every application into
 * the same transport or transaction model.</p>
 *
 * <h2>Threading model</h2>
 *
 * <p>Edit sessions follow the same single-owner convention as the core value
 * and collection types. Mutations and listener delivery are synchronous. If a
 * Swing form or table observes a session, mutate the session on the same
 * thread that owns the bound presentation model or route changes through the
 * appropriate Swing adapter.</p>
 */
@org.jspecify.annotations.NullMarked
package cz.auderis.corusco.core.data.edit;
