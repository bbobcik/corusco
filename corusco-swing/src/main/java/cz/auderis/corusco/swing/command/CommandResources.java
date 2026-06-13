package cz.auderis.corusco.swing.command;

import cz.auderis.corusco.core.key.ResourceKey;

/**
 * Resolves command resource keys at the Swing boundary.
 *
 * <p>This intentionally small interface lets early code and tests use maps,
 * while later resource services can supply localization and fallback policy
 * without changing command descriptors.</p>
 */
@FunctionalInterface
public interface CommandResources {

    /**
     * Resolves a string resource key.
     *
     * @param key resource key, never {@code null}
     * @return resolved text, possibly {@code null}
     */
    String resolve(ResourceKey<String> key);
}
