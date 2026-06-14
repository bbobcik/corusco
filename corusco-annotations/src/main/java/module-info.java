/**
 * Defines source-retained annotations for Corusco generated UI metadata.
 *
 * <p>The annotations in {@link cz.auderis.corusco.annotations} mark records
 * and record components as form fields, table columns, actions, validation
 * constraints, and user-facing help metadata. They are intended for compile
 * time processing by {@code cz.auderis.corusco.processor}; runtime framework
 * code should consume the generated descriptors rather than scanning these
 * annotations reflectively.</p>
 *
 * <p>Annotation ids become stable keys used by generated code for resources,
 * fields, actions, tables, and persisted table state. Treat those ids as part
 * of the application model: changing them changes generated metadata names and
 * may also affect saved UI preferences that use persistence ids.</p>
 */
module cz.auderis.corusco.annotations {
    exports cz.auderis.corusco.annotations;
}
