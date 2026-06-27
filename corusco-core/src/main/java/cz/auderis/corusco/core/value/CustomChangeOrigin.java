package cz.auderis.corusco.core.value;

import java.io.Serial;
import java.util.Objects;

/**
 * Custom origin identity attached to observable value changes.
 *
 * <p>Use custom origins when an application, generated binding, or integration
 * path needs a stable diagnostic id that is more specific than the
 * framework-defined {@link StandardChangeOrigin} constants. Custom ids must be
 * non-blank and may not reuse a standard origin id.</p>
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
     * @param id non-blank stable id that is not reserved by
     *         {@link StandardChangeOrigin}
     */
    public CustomChangeOrigin {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        } else if (StandardChangeOrigin.STANDARD_CHANGE_ORIGIN_IDS.contains(id)) {
            throw new IllegalArgumentException("cannot create custom origin with standard id");
        }
    }

    /**
     * Creates a change origin with a stable diagnostic id.
     *
     * @param id non-blank stable id that is not reserved by
     *         {@link StandardChangeOrigin}
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
