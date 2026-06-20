# Corusco Behavior Authoring Guide

Behaviors are reusable Swing component extensions. They are inspired by
Wicket-style behaviors, but they operate on ordinary Swing components and return
ordinary disposable bindings.

Use a behavior when code needs to attach, decorate, or enrich a component
without becoming the component itself:

- bind a field model to a text field;
- decorate a component with validation feedback;
- install tooltip, help, status text, or accessibility metadata;
- add focus or key interaction;
- bind commands to buttons, menu items, or input maps.

Do not use a behavior for business service calls, persistence, or presenter
workflow. Those belong in presenter/application code. Behaviors should be small
UI attachments that can be installed and removed with a view lifecycle.

## Core Types

| Type | Role |
| --- | --- |
| `ViewBehavior<C>` | Installs one extension on a `JComponent` subtype and returns a `Binding` cleanup handle. |
| `BehaviorDescriptor` | Declares the behavior key, phase, cardinality, and primary-binding conflict rule. |
| `BehaviorKey` | Stable behavior identity used for diagnostics, generated plans, conflict checks, and tests. |
| `BehaviorPhase` | Installation ordering: `BINDING`, then `DECORATION`, then `INTERACTION`. |
| `BehaviorScope` | Installs ordered behaviors on the EDT and owns their cleanup. |
| `BehaviorContext<C>` | Supplies the component, owning scope, and optional services such as `HelpService`. |
| `BehaviorFactory<C>` | Creates behavior lists for generated behavior plans or application-specific composition. |

The scope sorts behavior lists by phase before installation. Installed cleanup
handles are closed through an internal `BindingScope`, and behavior tracking is
cleared when the scope closes.

## Use Built-In Behaviors First

Most screens should start with `StandardBehaviors` and `CommandBehaviors`.

```java
try (BehaviorScope scope = new BehaviorScope(helpService)) {
    scope.install(nameField, List.of(
            StandardBehaviors.textFieldBinding(model.name),
            StandardBehaviors.composedTooltip(
                    model.name.problemSet(),
                    disabledReason,
                    resources.resolve(GeneratedCustomerEditResources.NAME_TOOLTIP, ""),
                    GeneratedCustomerEditDescriptors.NAME.helpTopic() != null
            ),
            StandardBehaviors.validationBorder(model.name.problemSet()),
            StandardBehaviors.selectAllOnFocus(),
            StandardBehaviors.helpOnF1(GeneratedCustomerEditDescriptors.NAME.helpTopic())
    ));
}
```

The list can be written in the natural form-field order. `BehaviorScope` still
installs binding behaviors before decoration and interaction behaviors.

Common built-ins:

| Factory | Typical target | Purpose |
| --- | --- | --- |
| `textFieldBinding` | `JTextField` | Primary `TextFieldModel` binding. |
| `textAreaBinding` | `JTextArea` | Primary multiline text binding. |
| `checkBoxBinding` | `AbstractButton` | Primary Boolean selected-state binding. |
| `validationTooltip` | `JComponent` | Tooltip from a problem set. |
| `composedTooltip` | `JComponent` | Validation, disabled reason, static help, and help hint in one tooltip policy. |
| `validationBorder` | `JComponent` | Visual validation feedback. |
| `selectAllOnFocus` | `JTextComponent` | Select text on focus gained. |
| `commitOnEnter` | `JTextComponent` | Run a commit action from Enter. |
| `statusText` | `JComponent` | Publish status-bar text while focused. |
| `accessibleText` | `JComponent` | Set accessible name and description. |
| `busyOverlay` | `JLayer<C>` | Reflect observable busy state in a layer UI. |
| `helpOnF1` | `JComponent` | Dispatch a generated help topic through a `HelpService`. |
| `commandButton` | `AbstractButton` | Bind a shared command to a button-like component. |
| `commandMenuItem` | `AbstractButton` | Bind a shared command to a menu item. |
| `commandKeyBinding` | `JComponent` | Install a command accelerator in input/action maps. |

See [Command and Action Guide](commands.md) for command state ownership,
generated action descriptors, resource resolution, and direct Swing action
adapter behavior.

## Write a Custom Behavior

A behavior has two responsibilities:

1. return a descriptor that tells the scope how to order and validate it;
2. install Swing state on the EDT and return cleanup code that restores or
   removes that state.

```java
final class PlaceholderBehavior implements ViewBehavior<JTextField> {

    private static final BehaviorKey KEY = BehaviorKey.of("decoration/placeholder");

    private final String text;

    PlaceholderBehavior(String text) {
        this.text = Objects.requireNonNull(text, "text");
    }

    @Override
    public BehaviorDescriptor descriptor() {
        return BehaviorDescriptor.single(KEY, BehaviorPhase.DECORATION);
    }

    @Override
    public Binding install(BehaviorContext<JTextField> context) {
        SwingEdt.requireEdt();
        JTextField field = context.component();
        String previous = field.getClientProperty("placeholder") instanceof String value ? value : null;

        field.putClientProperty("placeholder", text);
        return () -> {
            SwingEdt.requireEdt();
            field.putClientProperty("placeholder", previous);
        };
    }
}
```

The cleanup handle must be idempotent enough for normal scope disposal and must
run on the EDT when it touches Swing state. Prefer restoring previous Swing
state over blindly clearing it; another owner may have installed state before
the behavior.

## Choose Descriptor Metadata

Use descriptor metadata to make behavior plans predictable:

- `BehaviorDescriptor.primaryBinding(key)` for the one behavior that owns a
  component's primary value binding.
- `BehaviorDescriptor.single(key, phase)` for one install per component, such as
  validation border or accessible text.
- `BehaviorDescriptor.multiple(key, phase)` when multiple instances with the
  same kind can coexist, such as different command key bindings.

Phases should mean:

- `BINDING`: primary model/component synchronization;
- `DECORATION`: visual state, tooltip, border, accessibility text, busy layer;
- `INTERACTION`: focus listeners, input maps, help dispatch, command wiring.

Primary binding descriptors conflict with other primary binding descriptors on
the same component. Single-cardinality keys reject duplicate installation on the
same component. These failures are intentional: they catch generated behavior
plans that would otherwise silently install competing listeners.

## Ownership and Services

Create one `BehaviorScope` for the lifecycle that owns the view or dialog.

```java
final class CustomerPresenter {

    private final BehaviorScope behaviors;

    CustomerPresenter(CustomerView view, HelpService helpService, Resources resources) {
        this.behaviors = new BehaviorScope(helpService);
        behaviors.install(view.nameField(), List.of(
                StandardBehaviors.accessibleText(GeneratedCustomerEditDescriptors.NAME, resources),
                StandardBehaviors.selectAllOnFocus()
        ));
    }

    void closeOnEdt() {
        SwingEdt.requireEdt();
        behaviors.close();
    }
}
```

`BehaviorContext.helpServiceOptional()` is available to behaviors that need
application services. The built-in F1 help behavior throws during installation
when no help service is present, so missing services fail at setup time rather
than after a user presses F1.

## Generated Behavior Plans

Generated form behavior plans should install behavior lists that are explicit
and readable:

```java
CustomerEditPresentationModel presentation =
        new CustomerEditPresentationModel(model);
CustomerEditBehaviorPlan.install(view, presentation, behaviors);
```

The generated plan should stay boring:

- direct calls to generated view methods;
- direct references to generated model fields and descriptors;
- direct calls to `StandardBehaviors`;
- no runtime annotation scanning;
- no JavaBeans property names;
- no reflective lookup of private fields.

Application-specific behavior factories can wrap built-ins when a product needs
consistent behavior policy across screens, but the generated output should still
show the final behavior choices clearly.

## Testing Behaviors

Use focused unit tests for behavior internals and `SwingMvpTester` for
presenter/view behavior assertions.

```java
tester.assertBehaviorInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                StandardBehaviorKeys.SELECT_ALL_ON_FOCUS)
        .assertBehaviorNotInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                StandardBehaviorKeys.HELP_ON_F1);
```

Prefer assertions against stable `BehaviorKey` values over checks for private
listener classes, border implementations, or field names. For custom behavior
cleanup, test both installed state and post-close state.

See [Testing Guide](testing.md) for broader Swing MVP testing patterns,
component-key lookup, command assertions, and generated-source tests.

## Authoring Checklist

- Install and dispose Swing state on the EDT.
- Return a cleanup handle for every listener, input-map entry, action-map entry,
  border, tooltip, accessible value, or client property you install.
- Pick a stable `BehaviorKey`; avoid per-instance random IDs unless duplicates
  are intentionally allowed.
- Use `primaryBinding` for component value binding and only one primary binding
  per component.
- Restore previous Swing state when possible.
- Keep business logic outside the behavior.
- Cover ordering, conflict behavior, nominal installation, and cleanup in tests.

## Current Limits

- Behavior phases are fixed to binding, decoration, and interaction.
- Conflict detection is key/cardinality based; it does not understand every
  possible Swing side effect.
- Generated behavior plans currently cover generated form field bindings and
  standard behavior installation, not arbitrary product-specific behavior
  discovery.
- There is no global behavior registry or dependency injection layer.
