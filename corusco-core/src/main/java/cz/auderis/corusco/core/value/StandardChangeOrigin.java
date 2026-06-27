package cz.auderis.corusco.core.value;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Framework-defined origins for observable value changes.
 *
 * <p>These ids are reserved for the standard Corusco value and binding paths.
 * Use {@link CustomChangeOrigin} for application-specific or generated-code
 * diagnostics that need a more precise id.</p>
 *
 * <p>Choose the origin that describes the immediate actor that accepted or
 * performed the write, not the distant business reason that eventually led to
 * it. For example, a button press that changes a field directly is
 * {@link #USER}; a presenter command that reloads the same field from a
 * service after that button press is {@link #MODEL}. This keeps listeners from
 * treating refreshes, binding echoes, and direct edits as the same kind of
 * change.</p>
 *
 * <p>The standard origins have the following intended use:</p>
 *
 * <ul>
 *     <li>{@link #USER} means the UI accepted a direct user edit or gesture,
 *     such as typing into an editable control, selecting a row, checking a box,
 *     dragging a slider, pressing a command button that writes a simple value,
 *     or clearing a field through a visible UI affordance. Do not keep using it
 *     for later writes produced by a workflow that the user originally
 *     triggered.</li>
 *     <li>{@link #MODEL} means application-owned state changed through a
 *     presenter, view model, domain adapter, repository result, or command
 *     handler. Typical examples are loading an entity into fields, resetting
 *     dirty state after save, applying calculated totals, replacing detail data
 *     after master selection, or publishing the result of an application
 *     command. Do not use it for direct UI edits, binding echoes, generated
 *     plumbing, or framework housekeeping.</li>
 *     <li>{@link #BINDING} means the write exists to keep two observable
 *     surfaces synchronized, such as copying a component value into a
 *     {@link WritableValue}, reflecting a presenter value back into a widget,
 *     or bridging two value abstractions. Do not use it for the authoritative
 *     source edit. If a user typed the original value, that source write is
 *     {@link #USER}; if a presenter loaded it, that source write is
 *     {@link #MODEL}. Use {@code BINDING} for the propagation step so listeners
 *     can detect echoes and avoid feedback loops.</li>
 *     <li>{@link #SYSTEM} means framework or infrastructure code changed value
 *     state outside user gestures, application model decisions, binding
 *     synchronization, or generated adapter logic. Examples include lifecycle
 *     cleanup, disposal guards, infrastructure-driven invalidation, scheduler
 *     bookkeeping, or task-state bookkeeping. Do not use it as a vague fallback
 *     for unknown application writes.</li>
 *     <li>{@link #GENERATED} means generated Corusco code performs the write as
 *     generated code, and that distinction is useful for diagnostics or
 *     listener policy. Examples include generated form companions initializing
 *     adapter state, generated table/form glue publishing synthesized values,
 *     or generated metadata adapters updating framework-facing values. Do not
 *     use it for every write that passes through generated code: generated
 *     binding propagation is usually {@link #BINDING}, and presenter-owned
 *     application logic invoked by generated code is usually {@link #MODEL}.</li>
 * </ul>
 *
 * <p>Use {@link CustomChangeOrigin} when a stable integration or generated path
 * needs a more precise diagnostic id than these broad categories provide.</p>
 */
public enum StandardChangeOrigin implements ChangeOrigin {

    /**
     * Direct user edit or gesture accepted by the UI layer.
     *
     * <p>Use for the immediate UI write, not for later model, binding, or
     * system updates triggered by the same gesture.</p>
     */
    USER,

    /**
     * Application-owned model, presenter, or command state change.
     *
     * <p>Use for authoritative application state writes, not for direct UI
     * edits, binding propagation, generated plumbing, or framework
     * housekeeping.</p>
     */
    MODEL,

    /**
     * Propagation performed to keep observable surfaces synchronized.
     *
     * <p>Use for binding echoes and bridges, not for the authoritative source
     * edit that caused synchronization.</p>
     */
    BINDING,

    /**
     * Framework or infrastructure state change.
     *
     * <p>Use for lifecycle, disposal, scheduler, task, or infrastructure
     * bookkeeping, not as a fallback for unclear application writes.</p>
     */
    SYSTEM,

    /**
     * Generated Corusco adapter or metadata code performed the write.
     *
     * <p>Use when generated-code identity matters; use {@link #BINDING} for
     * generated binding propagation and {@link #MODEL} for presenter-owned
     * application writes invoked by generated code.</p>
     */
    GENERATED,

    ;

    /**
     * Returns the stable reserved id for this standard origin.
     *
     * @return enum name
     */
    @Override
    public String id() {
        return name();
    }

    /**
     * Reserved ids used by the standard origins.
     *
     * <p>{@link CustomChangeOrigin} rejects these ids so custom and standard
     * origins cannot be confused in diagnostics or generated code.</p>
     */
    static final Set<String> STANDARD_CHANGE_ORIGIN_IDS = EnumSet
            .allOf(StandardChangeOrigin.class)
            .stream()
            .map(StandardChangeOrigin::id)
            .collect(Collectors.toUnmodifiableSet());

}
