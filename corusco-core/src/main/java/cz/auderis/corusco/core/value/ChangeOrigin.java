package cz.auderis.corusco.core.value;

import java.util.Objects;

/**
 * Describes where a value change originated.
 *
 * <p>Origins are diagnostic identity values carried by
 * {@link ValueChangeEvent}. They do not affect equality checks for value
 * mutation. The predefined constants cover the early framework paths; generated
 * code may create stable custom origins with {@link #of(String)} when a more
 * specific diagnostic id is useful.</p>
 *
 * @param id stable origin id for diagnostics and generated code internals
 */
public record ChangeOrigin(String id) {

    /**
     * Origin for changes initiated by direct user interaction.
     */
    public static final ChangeOrigin USER = new ChangeOrigin("user");

    /**
     * Origin for changes initiated by application or domain model code.
     */
    public static final ChangeOrigin MODEL = new ChangeOrigin("model");

    /**
     * Origin for changes initiated by bindings between presentation objects.
     */
    public static final ChangeOrigin BINDING = new ChangeOrigin("binding");

    /**
     * Origin for changes initiated by generated framework code.
     */
    public static final ChangeOrigin GENERATED = new ChangeOrigin("generated");

    /**
     * Origin for internal framework or system updates.
     */
    public static final ChangeOrigin SYSTEM = new ChangeOrigin("system");

    /**
     * Creates a change origin with a stable diagnostic id.
     *
     * @param id non-blank stable id
     */
    public ChangeOrigin {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a custom change origin.
     *
     * @param id non-blank stable id
     * @return a change origin with the supplied id
     */
    public static ChangeOrigin of(String id) {
        return new ChangeOrigin(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
