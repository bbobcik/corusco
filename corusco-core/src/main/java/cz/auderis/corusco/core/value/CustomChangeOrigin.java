package cz.auderis.corusco.core.value;

import java.io.Serial;
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
public record CustomChangeOrigin(

        String id

) implements ChangeOrigin {

    @Serial
    private static final long serialVersionUID = -1387848788753907265L;

    /**
     * Creates a change origin with a stable diagnostic id.
     *
     * @param id non-blank stable id
     */
    public CustomChangeOrigin {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a change origin with a stable diagnostic id.
     *
     * @param id non-blank stable id
     * @return custom change origin
     */
    public static CustomChangeOrigin of(String id) {
        return new CustomChangeOrigin(id);
    }

    @Override
    public String toString() {
        return id;
    }

}
