/**
 * Toolkit-neutral tooltip content and ordering policy.
 *
 * <p>The package keeps tooltip inputs separate from Swing rendering.
 * {@link cz.auderis.corusco.core.tooltip.TooltipContent} carries validation
 * problems, disabled-state text, static help, and help availability as distinct
 * values. {@link cz.auderis.corusco.core.tooltip.TooltipPolicy} then composes
 * those values into ordered plain-text lines that Swing bindings can install on
 * components.</p>
 *
 * <p>This separation lets generated descriptors, validation models, command
 * enablement, and help topics contribute tooltip text without making the core
 * module depend on Swing. Swing adapters remain responsible for EDT access and
 * for restoring component tooltip state when bindings close.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.core.tooltip.TooltipPolicy} when you
 * need to decide which feedback wins for a single Swing tooltip slot. The
 * standard policy puts validation feedback before disabled reasons, descriptor
 * help, and help-availability hints, so users see actionable problems first.
 * Use {@link cz.auderis.corusco.core.tooltip.TooltipContent} when a binding or
 * presenter needs to assemble those inputs without committing to Swing.</p>
 *
 * <p>The package deliberately avoids HTML rendering, component lookup, and
 * localization. Resource lookup happens before static help text reaches this
 * package, and Swing bindings decide how composed lines are joined and written
 * to components.</p>
 */
package cz.auderis.corusco.core.tooltip;
