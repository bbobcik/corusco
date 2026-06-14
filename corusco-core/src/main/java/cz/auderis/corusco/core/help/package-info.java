/**
 * Toolkit-neutral help topic dispatch APIs.
 *
 * <p>This package connects stable help metadata to an application-specific help
 * system. Start with {@link cz.auderis.corusco.core.help.HelpService}, which
 * opens a typed {@link cz.auderis.corusco.core.key.HelpTopic}. The request
 * object, {@link cz.auderis.corusco.core.help.HelpRequest}, carries the topic,
 * optional UI source object, and trigger description so an implementation can
 * decide whether to open a browser page, show an embedded help panel, delegate
 * to platform help, or record a test assertion.</p>
 *
 * <p>{@link cz.auderis.corusco.core.help.DefaultHelpService} is a small
 * adapter around {@link cz.auderis.corusco.core.help.HelpHandler}. It is useful
 * for tests and simple applications. Larger applications can implement
 * {@code HelpService} directly. {@link
 * cz.auderis.corusco.core.help.HelpServiceException} reports configuration or
 * dispatch failures after a help request has actually been made.</p>
 *
 * <p>The package intentionally does not know about Swing, browsers, modal
 * dialogs, or resource-bundle mechanics. Generated descriptors and keys
 * identify help topics; Swing integration in
 * {@code cz.auderis.corusco.swing.behavior} can bind F1 to this service at the
 * component boundary. Absence of help metadata should be handled before calling
 * the service.</p>
 *
 * <p>A typical usage flow is to create one application-level help service,
 * provide it to the Swing behavior context, and let generated field or table
 * descriptors supply the help topic. Tests can replace the service with a
 * handler that records requests instead of opening UI.</p>
 */
package cz.auderis.corusco.core.help;
