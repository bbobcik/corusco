# Corusco Command and Action Guide

Commands are typed presentation actions that can back buttons, menu items,
toolbar items, and key bindings from one shared state owner. Action annotations
generate metadata; presenter code creates command instances and implements the
business workflow.

See [Dialog Guide](dialogs.md) for the built-in OK, Apply, and Cancel commands
used by form-dialog controllers.

Use commands when multiple UI entry points should share identity, text,
tooltip, accelerator metadata, enabled state, selected state, and invocation:

```text
generated ActionDescriptor
    -> presenter-owned Command
        -> SwingActionAdapter
            -> JButton / JMenuItem / key binding
```

## Core Types

| Type | Role |
| --- | --- |
| `ActionKey` | Stable typed command identity. |
| `ActionDescriptor` | Swing-free presentation metadata: text key, tooltip key, mnemonic, accelerator, and selectable flag. |
| `AcceleratorDescriptor` | Swing-neutral key-code and modifier metadata. |
| `Command` | Read-only command contract with identity, descriptor, enabled/selected values, and `execute()`. |
| `MutableCommand` | Presenter-owned command state for enabled and selected changes. |
| `CommandFactory` | Factory for ordinary commands and selectable toggle commands. |
| `CommandSet` | Deterministic lookup collection keyed by `ActionKey`. |
| `SwingActionAdapter` | Disposable Swing `Action` backed by a Corusco command. |
| `CommandBehaviors` | Behavior factories for command buttons, menu items, and key bindings. |

The core command model is Swing-free. Swing integration happens at the adapter
or behavior boundary.

## Generated Action Metadata

Annotate no-argument `void` methods with `@UiAction`:

```java
final class CustomerPresenter {

    @UiAction(
            id = "customer/save",
            text = "customer/save/text",
            tooltip = "customer/save/tooltip",
            mnemonic = 83
    )
    void save() {
    }

    @UiAction(id = "customer/active", selectable = true)
    void toggleActive() {
    }
}
```

The processor generates a companion named after the enclosing type, for example
`CustomerPresenterActions`. The companion exposes:

- one `ActionKey` constant per action;
- one text `ResourceKey<String>` constant per action;
- an optional tooltip resource key when `tooltip` is set;
- one `ActionDescriptor` constant per action.

Generated action descriptors do not invoke annotated methods. This is
intentional: invocation stays in presenter code and does not require runtime
annotation scanning, reflection, or method-name strings.

## Creating Commands

Presenter or controller code turns descriptors into stateful commands:

```java
ActionDescriptor saveDescriptor = CustomerPresenterActions.SAVE.withAccelerator(
        AcceleratorDescriptor.of(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)
);
MutableCommand save = CommandFactory.command(
        saveDescriptor,
        command -> saveCustomer()
);

MutableCommand active = CommandFactory.toggle(
        CustomerPresenterActions.TOGGLE_ACTIVE,
        false,
        command -> setActive(command.isSelected())
);
```

Use `CommandFactory.command(...)` for ordinary actions and
`CommandFactory.toggle(...)` for descriptors whose `selectable` flag is true.
The factory rejects mismatches so a selectable descriptor cannot silently become
a non-toggle command.

`execute()` is guarded by enabled state. Disabled commands ignore execution
requests, so buttons, menu items, keyboard shortcuts, and tests can all invoke
the same command without duplicating enabled checks.

## Enabled and Selected State

`MutableCommand` exposes explicit state mutations:

```java
save.setEnabled(model.isCommittable());
active.setSelected(customer.active());
```

Use model-origin changes for presenter decisions. `SwingActionAdapter` uses
`ChangeOrigin.USER` when a selectable Swing button changes selected state as
part of a user action.

For non-selectable commands, `selected()` is always false and
`setSelected(...)` is rejected. This keeps accidental toggle wiring visible
during development.

## Resource Resolution

`ActionDescriptor` stores resource keys, not localized strings:

```java
CommandResources resources = Map.of(
        CustomerPresenterActions.SAVE_TEXT, "Save",
        CustomerPresenterActions.SAVE_TOOLTIP, "Save customer"
)::get;
```

`SwingActionAdapter` resolves text and tooltip keys when it creates the Swing
`Action`. The small `CommandResources` interface keeps localization policy out
of `corusco-core` and lets early examples use maps while applications use their
own resource services.

## Swing Binding

Use `CommandBehaviors` when commands are part of a behavior plan:

```java
try (BehaviorScope scope = new BehaviorScope()) {
    scope.install(saveButton, List.of(CommandBehaviors.commandButton(save, resources)));
    scope.install(saveMenuItem, List.of(CommandBehaviors.commandMenuItem(save, resources)));
    scope.install(rootPanel, List.of(CommandBehaviors.commandKeyBinding(
            save,
            resources,
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    )));
}
```

The button and menu item receive separate `SwingActionAdapter` instances, but
both adapters observe the same command. Changing command enabled or selected
state updates every installed Swing entry point.

`commandKeyBinding(...)` requires accelerator metadata in the descriptor. It
installs the key stroke into the component input map and a Swing action into
the action map, then restores previous entries on close.

Use `SwingActionAdapter` directly only when behavior scopes are not the right
ownership mechanism for a screen.

## Command Sets

`CommandSet` is a deterministic lookup collection:

```java
CommandSet commands = CommandSet.of(save, active);
Command saveCommand = commands.require(CustomerPresenterActions.SAVE_KEY);
```

Use it in presenters and tests when a screen exposes actions by stable key.
Duplicate command keys are rejected during construction.

## Testing Commands

Test command behavior at the level that owns it:

- core tests for enabled gating, selected state, toggle validation, and
  `CommandSet` duplicate detection;
- Swing adapter tests for text, tooltip, mnemonic, accelerator, enabled, and
  selected propagation;
- behavior tests for cleanup and restoration of previous Swing actions or
  input-map entries;
- presenter tests through `SwingMvpTester` command helpers instead of private
  button fields;
- processor tests that generated action descriptors use expected keys and
  resource keys.

See `CommandExample` for one command bound to a button, menu item, and key
binding, and `GeneratedActionMetadataExample` for generated action descriptor
metadata.

## Good Usage Patterns

- Keep action ids stable; tests, resources, menus, and generated metadata can
  depend on them.
- Put business decisions in presenter command handlers, not in annotations or
  Swing adapters.
- Share one command object across all UI entry points for the same action.
- Resolve resources at the Swing boundary.
- Prefer generated descriptors over duplicate handwritten metadata.
- Close adapters and behavior scopes with the view lifecycle.

## Current Limits

- `@UiAction` currently generates descriptors only; it does not generate method
  invocation glue.
- Annotated methods must be no-argument `void` methods enclosed by a type.
- Generated command metadata is source-level Java, not runtime annotation
  metadata.
- Command resources are resolved through the small Swing boundary interface;
  there is no global localization service yet.
- There is no generated menu or toolbar layout model yet.
