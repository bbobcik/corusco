/**
 * Command/action annotations consumed by generated command metadata.
 *
 * <p>This package solves the compile-time action discovery problem. A Swing
 * presenter usually has methods such as save, reset, export, or toggle active
 * that should be visible in buttons, menus, toolbars, keyboard shortcuts, and
 * tests. The annotation lets source code declare the stable action identity and
 * presentation metadata once, while the processor emits ordinary Java
 * constants and factories.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.annotations.command.UiAction} on a
 * no-argument {@code void} method in the presenter or controller that owns the
 * workflow. The annotated method remains the business entry point; the
 * generated companion provides the metadata and optional command glue around
 * it. The processor rejects methods with parameters or non-void return types so
 * generated command factories have a simple, predictable shape.</p>
 *
 * <p>For example, this source type:</p>
 *
 * <pre>{@code
 * final class CustomerPresenter {
 *     @UiAction(id = "customer/save", text = "customer/save/text")
 *     void save() {
 *     }
 * }
 * }</pre>
 *
 * <p>produces an owner-specific generated companion named
 * {@code CustomerPresenterActions}. That generated name comes from the
 * enclosing type name, not from a placeholder convention. A different owner
 * type such as {@code InvoicePresenter} would produce
 * {@code InvoicePresenterActions}.</p>
 *
 * <p>The generated companion exposes
 * {@code cz.auderis.corusco.core.key.ActionKey} constants,
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants, and
 * {@code cz.auderis.corusco.core.command.ActionDescriptor} constants. Those
 * constants are the normal handles for command lookup, Swing action binding,
 * menu construction, toolbar construction, keyboard shortcut installation, and
 * generated-source tests.</p>
 *
 * <p>The companion also exposes ordered descriptor lists for menu and toolbar
 * assembly. The current grouping is intentionally small: it preserves
 * declaration order and does not try to model a full application menu system.
 * Applications that need nested menus, separators, or role-based filtering can
 * use the generated descriptors as input to their own menu policy.</p>
 *
 * <p>Generated command factories return
 * {@code cz.auderis.corusco.core.command.MutableCommand} objects bound to an
 * owner instance, and {@code commands(owner)} returns a
 * {@code cz.auderis.corusco.core.command.CommandSet}. This is additive:
 * descriptor-only usage remains available when a presenter needs custom
 * enabled-state initialization, selected-state synchronization, or a handler
 * that performs additional work before calling the annotated method.</p>
 *
 * <p>Use {@link cz.auderis.corusco.annotations.command.UiAction#selectable()}
 * for toggle-style actions. A selectable action produces toggle descriptor
 * metadata and the generated factory uses the toggle command path. The selected
 * state still belongs to the returned command instance, not to the annotation.
 * Presenter code should initialize or update selected state from the
 * application model.</p>
 *
 * <p>Stable ids are compatibility boundary values. The action id may appear in
 * generated source, tests, diagnostics, persisted shortcuts, analytics, or
 * resource maps. Changing it is different from renaming a Java method; treat it
 * as a visible API change for the screen or application module.</p>
 *
 * <p>Resource ids are deliberately keys, not localized strings. The generated
 * descriptor carries resource keys and Swing adapters resolve them later
 * through the resource layer. This keeps annotation source stable across
 * locales and prevents visible text from becoming command identity.</p>
 *
 * <p>Advanced users should note that the processor uses compile-time language
 * model metadata and emits direct Java calls. There is no runtime annotation
 * scanning, method lookup by string, or reflection-based invocation. This keeps
 * generated command metadata friendly to JPMS, static analysis, and code
 * review.</p>
 */
package cz.auderis.corusco.annotations.command;
