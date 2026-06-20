package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.key.FieldKey;
import java.util.List;
import java.util.Objects;

/**
 * Metadata describing a generated dependency between a source field and a
 * target component-state model.
 *
 * @param source source field key
 * @param targetStateModel target component-state model member name
 * @param expectedValues source values that satisfy the condition
 * @param effect target state effect controlled by the dependency
 * @param <T> expected value type
 */
public record FieldDependency<T>(
        FieldKey<?, ?> source,
        String targetStateModel,
        List<T> expectedValues,
        DependencyEffect effect
) {

    /**
     * Creates dependency metadata.
     *
     * @param source source field key
     * @param targetStateModel target component-state model member name
     * @param expectedValues source values that satisfy the condition
     * @param effect target state effect controlled by the dependency
     */
    public FieldDependency {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(targetStateModel, "targetStateModel");
        if (targetStateModel.isBlank()) {
            throw new IllegalArgumentException("Target state model must not be blank");
        }
        expectedValues = List.copyOf(expectedValues);
        if (expectedValues.isEmpty()) {
            throw new IllegalArgumentException("Expected values must not be empty");
        }
        effect = Objects.requireNonNull(effect, "effect");
    }

    /**
     * Creates dependency metadata.
     *
     * @param source source field key
     * @param targetStateModel target component-state model member name
     * @param expectedValues source values that satisfy the condition
     * @param effect target state effect controlled by the dependency
     * @param <T> expected value type
     * @return dependency metadata
     */
    public static <T> FieldDependency<T> of(
            FieldKey<?, ?> source,
            String targetStateModel,
            List<T> expectedValues,
            DependencyEffect effect
    ) {
        return new FieldDependency<>(source, targetStateModel, expectedValues, effect);
    }
}
