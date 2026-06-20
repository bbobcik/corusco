/**
 * Defines compile-time annotations for Corusco generated UI metadata.
 *
 * <p>The annotations are grouped by the generated contract they describe:
 * form field annotations in {@link cz.auderis.corusco.annotations.form},
 * table annotations in {@link cz.auderis.corusco.annotations.table}, command
 * action annotations in {@link cz.auderis.corusco.annotations.command},
 * validation annotations in {@link cz.auderis.corusco.annotations.validation},
 * and help annotations in {@link cz.auderis.corusco.annotations.help}. They
 * are intended for compile-time processing by
 * {@code cz.auderis.corusco.processor}. Form and table metadata annotations
 * are retained in class files so an adapter module can generate Swing
 * companions for already compiled model classes; runtime framework code should
 * still consume the generated descriptors rather than scanning these
 * annotations reflectively.</p>
 *
 * <p>Annotation ids become stable keys used by generated code for resources,
 * fields, actions, tables, and persisted table state. Treat those ids as part
 * of the application model: changing them changes generated metadata names and
 * may also affect saved UI preferences that use persistence ids.</p>
 */
module cz.auderis.corusco.annotations {
    exports cz.auderis.corusco.annotations;
    exports cz.auderis.corusco.annotations.command;
    exports cz.auderis.corusco.annotations.form;
    exports cz.auderis.corusco.annotations.help;
    exports cz.auderis.corusco.annotations.table;
    exports cz.auderis.corusco.annotations.validation;
}
