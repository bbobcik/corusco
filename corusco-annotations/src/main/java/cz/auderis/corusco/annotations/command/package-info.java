/**
 * Command/action annotations consumed by generated command metadata.
 *
 * <p>{@link cz.auderis.corusco.annotations.command.UiAction} marks
 * no-argument {@code void} presenter methods whose id, text resource,
 * tooltip resource, mnemonic, accelerator, and selectable state should become
 * generated action descriptors. Generated companions expose
 * {@code cz.auderis.corusco.core.key.ActionKey} constants,
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants,
 * descriptors, ordered menu/toolbar metadata, and command factories without
 * runtime annotation scanning. The generated descriptor type is
 * {@code cz.auderis.corusco.core.command.ActionDescriptor}; generated
 * factories return {@code cz.auderis.corusco.core.command.MutableCommand} and
 * {@code cz.auderis.corusco.core.command.CommandSet} objects.</p>
 */
package cz.auderis.corusco.annotations.command;
