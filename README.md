# Auderis Corusco

Auderis Corusco is an early-stage Swing presentation framework for business
applications. It is built around typed presentation models, generated metadata,
lifecycle-aware Swing behaviors, table helpers, and UI tests that can exercise
generated wiring without falling back to reflection or string property paths.

The current repository is still pre-preview, but the minimum viable runtime is
now represented across the modules: observable values and lists, field models,
validation and problem reporting, Swing bindings, generated form/table metadata,
table-state persistence, dialogs, async helpers, Glazed Lists interop, and the
Swing MVP test harness.

## Requirements

- JDK 25
- Gradle wrapper from this repository

## Quickstart

Start with [docs/quickstart.md](docs/quickstart.md). It walks through the
customer-edit record used by the examples, shows the handwritten annotation
source beside the generated model/table helpers, and points to the focused
example classes that compile in this repository.

For a fast local confidence check:

```bash
./gradlew test --quiet --stacktrace
```

For a full verification pass:

```bash
./gradlew build --quiet --stacktrace
```

## Modules

| Module | Purpose |
| --- | --- |
| `corusco-core` | Core runtime primitives: lifecycle, observable values/lists, keys, forms, validation, commands, resources, tables, table state, async values, and dialog models. |
| `corusco-glazedlists` | Optional Glazed Lists interop adapters for existing `EventList` row pipelines. |
| `corusco-swing` | Swing/AWT integration: bindings, EDT utilities, behaviors, table models, dialog helpers, task callbacks, and MVP test harness. |
| `corusco-annotations` | Compile-time annotation API for generated form, table, and action metadata. |
| `corusco-processor` | Annotation processor implementation and generated-source tests. |
| `corusco-test` | Shared test support, including generated-source compiler helpers. |
| `corusco-examples` | Focused examples and regression fixtures for every completed roadmap slice. |

## Documentation

- [Roadmap](docs/corusco-roadmap.md)
- [Architecture overview](docs/architecture.md)
- [Quickstart](docs/quickstart.md)
- [Annotation reference](docs/annotations.md)
- [Behavior authoring guide](docs/behaviors.md)
- [Form model guide](docs/forms.md)
- [Table guide](docs/tables.md)
- [Implementation stage plans](docs/implementation-stage-plans/README.md)
- [Architecture decision records](docs/adr/README.md)

Stage 20 is expanding the user-facing guides. Until the full guide set is
complete, the examples module is the most precise reference because it compiles
against the current APIs.
