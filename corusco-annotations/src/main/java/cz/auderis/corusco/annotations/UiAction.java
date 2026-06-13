package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a no-argument method as a generated UI action source.
 *
 * <p>This stage generates typed action metadata only. Later stages can connect
 * the descriptor to command instances and method invocation without runtime
 * annotation scanning.</p>
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
