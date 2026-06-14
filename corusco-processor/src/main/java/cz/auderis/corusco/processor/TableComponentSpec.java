package cz.auderis.corusco.processor;

/**
 * Processor-internal description of a generated table view component slot.
 *
 * <p>The table generator currently needs the stable component name. Keeping it
 * in a separate spec makes the generated binding/view contract explicit instead
 * of hiding that contract in string assembly inside the writer.</p>
 */
final class TableComponentSpec {

    final String name;

    TableComponentSpec(String name) {
        this.name = name;
    }
}
