package cz.auderis.corusco.annotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a no-argument method as a generated UI action source.
 *
 * <p>Use this annotation on presenter or controller methods that should become
 * generated command metadata. The processor validates that the annotated method
 * has no parameters, returns {@code void}, and declares stable action/resource
 * ids. Generated code contains an {@code ActionKey}, resource keys, and an
 * {@code ActionDescriptor}; runtime code can then create commands without
 * scanning annotations reflectively.</p>
 *
 * <p>The generated owner-specific actions companion, for example
 * {@code CustomerPresenterActions}, is the normal place to obtain runtime key
 * instances. It exposes a
 * {@code cz.auderis.corusco.core.key.ActionKey} constant, one or more
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants, an action
 * descriptor, and generated command factories for each annotated method.</p>
 *
 * <p>The generated action descriptor is a
 * {@code cz.auderis.corusco.core.command.ActionDescriptor}. Generated command
 * factories return {@code cz.auderis.corusco.core.command.MutableCommand}
 * instances and {@code commands(owner)} returns a
 * {@code cz.auderis.corusco.core.command.CommandSet}. Descriptor constants
 * remain usable directly when a presenter needs custom command construction.</p>
 *
 * <p>Mnemonic and accelerator values are metadata only. Swing adapters decide
 * how to install them on buttons, menu items, or input maps. A non-zero
 * accelerator modifier mask without an accelerator key is invalid. The
 * {@link #selectable()} flag marks toggle-style actions and affects which
 * command factory path should be used at runtime.</p>
 *
 * <p>The action id is a stable boundary value that may appear in generated
 * source, resource maps, diagnostics, and tests. Changing it should be treated
 * as a compatibility change.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface UiAction {

    /**
     * Stable action id.
     *
     * @return action id
     */
    String id();

    /**
     * Optional visible text resource id. Empty means {@code id + "/text"}.
     *
     * @return text resource id
     */
    String text() default "";

    /**
     * Optional tooltip resource id. Empty means no tooltip metadata.
     *
     * @return tooltip resource id
     */
    String tooltip() default "";

    /**
     * Optional mnemonic key code. Zero means no mnemonic metadata.
     *
     * @return mnemonic key code
     */
    int mnemonic() default 0;

    /**
     * Optional accelerator key code. Zero means no accelerator metadata.
     *
     * @return accelerator key code
     */
    int acceleratorKey() default 0;

    /**
     * Optional accelerator modifier mask.
     *
     * <p>A non-zero modifier mask is valid only when
     * {@link #acceleratorKey()} is also non-zero.</p>
     *
     * @return modifier mask
     */
    int acceleratorModifiers() default 0;

    /**
     * Whether the action is selectable/toggle-style.
     *
     * @return selectable flag
     */
    boolean selectable() default false;
}
