package cz.auderis.corusco.processor;

/**
 * Normalized generated option metadata.
 */
final class OptionSpec {

    final String constantName;
    final String enumConstantName;
    final String key;

    OptionSpec(String constantName, String enumConstantName, String key) {
        this.constantName = constantName;
        this.enumConstantName = enumConstantName;
        this.key = key;
    }
}
