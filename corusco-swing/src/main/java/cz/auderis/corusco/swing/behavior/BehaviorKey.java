package cz.auderis.corusco.swing.behavior;

import java.util.Objects;

/**
 * Stable identity for a behavior kind within a generated or handwritten view plan.
 *
 * <p>Behavior keys let {@link BehaviorScope} detect duplicates, report
 * installed behavior state to tests, and associate generated metadata with
 * runtime bindings. The id should describe the component responsibility rather
 * than a particular behavior object instance; for example, built-in keys use
 * values such as {@code binding/text} or {@code decoration/tooltip}.</p>
 *
 * <p>Keys are immutable value objects and compare by id. They are intended for
 * diagnostics and generated-plan compatibility, not for storing user
 * preferences. Use {@link #of(String)} for inline construction and keep ids
 * stable when tests or generated code refer to them.</p>
 *
 * @param id stable non-blank behavior id
 */
public record BehaviorKey(String id) {

    /**
     * Creates a behavior key.
     *
     * @param id stable non-blank behavior id
     * @throws NullPointerException if {@code id} is {@code null}
     * @throws IllegalArgumentException if {@code id} is blank
     */
    public BehaviorKey {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a behavior key.
     *
     * @param id stable non-blank behavior id
     * @return behavior key
     */
    public static BehaviorKey of(String id) {
        return new BehaviorKey(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
