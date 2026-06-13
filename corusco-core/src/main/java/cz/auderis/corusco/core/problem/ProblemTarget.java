package cz.auderis.corusco.core.problem;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import java.util.Objects;

/**
 * Typed target of a problem.
 *
 * <p>The hierarchy is sealed so callers can pattern-match over the known target
 * kinds. Field and component targets carry typed keys instead of arbitrary
 * field-name strings.</p>
 */
public sealed interface ProblemTarget
        permits ProblemTarget.Form, ProblemTarget.Field, ProblemTarget.Row,
        ProblemTarget.Cell, ProblemTarget.Component {

    /**
     * Form-wide problem target.
     */
    record Form() implements ProblemTarget {
    }

    /**
     * Field problem target preserving owner and value typing.
     *
     * @param key typed field key
     * @param <O> owner/model type
     * @param <T> field value type
     */
    record Field<O, T>(FieldKey<O, T> key) implements ProblemTarget {

        /**
         * Creates a field target.
         *
         * @param key typed field key
         */
        public Field {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * Row problem target.
     *
     * @param row row identity or row object
     * @param <R> row identity type
     */
    record Row<R>(R row) implements ProblemTarget {

        /**
         * Creates a row target.
         *
         * @param row row identity or row object
         */
        public Row {
            Objects.requireNonNull(row, "row");
        }
    }

    /**
     * Table cell problem target.
     *
     * @param row row identity or row object
     * @param column column identity
     * @param <R> row identity type
     * @param <V> column identity type
     */
    record Cell<R, V>(R row, V column) implements ProblemTarget {

        /**
         * Creates a cell target.
         *
         * @param row row identity or row object
         * @param column column identity
         */
        public Cell {
            Objects.requireNonNull(row, "row");
            Objects.requireNonNull(column, "column");
        }
    }

    /**
     * Component problem target preserving component typing.
     *
     * @param key typed component key
     * @param <C> component type
     */
    record Component<C>(ComponentKey<C> key) implements ProblemTarget {

        /**
         * Creates a component target.
         *
         * @param key typed component key
         */
        public Component {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * Creates a form target.
     *
     * @return form target
     */
    static Form form() {
        return new Form();
    }

    /**
     * Creates a field target.
     *
     * @param key typed field key
     * @param <O> owner/model type
     * @param <T> field value type
     * @return field target
     */
    static <O, T> Field<O, T> field(FieldKey<O, T> key) {
        return new Field<>(key);
    }

    /**
     * Creates a row target.
     *
     * @param row row identity or row object
     * @param <R> row identity type
     * @return row target
     */
    static <R> Row<R> row(R row) {
        return new Row<>(row);
    }

    /**
     * Creates a cell target.
     *
     * @param row row identity or row object
     * @param column column identity
     * @param <R> row identity type
     * @param <V> column identity type
     * @return cell target
     */
    static <R, V> Cell<R, V> cell(R row, V column) {
        return new Cell<>(row, column);
    }

    /**
     * Creates a component target.
     *
     * @param key typed component key
     * @param <C> component type
     * @return component target
     */
    static <C> Component<C> component(ComponentKey<C> key) {
        return new Component<>(key);
    }
}
