package cz.auderis.corusco.core.value;

import java.io.Serializable;

/**
 * Origin identity attached to observable value changes.
 *
 * <p>Origins are small diagnostic tokens that let listeners distinguish user
 * edits, model refreshes, generated binding writes, and other update paths.
 * They are carried by {@link ValueChangeEvent} and do not participate in the
 * value equality checks that decide whether an event is emitted.</p>
 *
 * <p>The origin hierarchy is intentionally sealed: use
 * {@link StandardChangeOrigin} for framework-defined origins and
 * {@link CustomChangeOrigin} for application or generated-code origins with a
 * stable id.</p>
 */
public sealed interface ChangeOrigin extends Serializable
        permits StandardChangeOrigin, CustomChangeOrigin {

    /**
     * Returns the stable diagnostic id for this origin.
     *
     * @return non-blank origin id
     */
    String id();

}
