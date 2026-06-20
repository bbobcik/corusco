package cz.auderis.corusco.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Legacy name for a generated Swing table row source.
 *
 * <p>This annotation preserves source compatibility for consumers that adopted
 * Corusco 1.0. New code should use {@link CoruscoTable}. The processor treats a
 * {@code @SwingTable} record as a legacy table source and keeps generating the
 * same-package Swing companions that the old name implied.</p>
 *
 * @deprecated use {@link CoruscoTable}; this alias remains for compatible 1.x
 *         source upgrades
 */
@Deprecated(since = "1.1.0", forRemoval = false)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SwingTable {

    /**
     * Stable table id used as the prefix for generated metadata ids.
     *
     * @return non-blank table id
     */
    String id();
}
