# Auderis Corusco

Auderis Corusco is an early-stage Swing presentation framework for business
applications. The project is being built around typed presentation models,
generated metadata, lifecycle-aware Swing behaviors, and testable UI wiring.

The repository currently contains the engineering baseline plus the first core
runtime primitives: lifecycle subscriptions, observable values, and typed
identity keys. The core also contains the first typed problem, conversion, and
non-Swing form model primitives. Annotation processing, Swing bindings,
validation rules, and table support will be added in later implementation
stages.

## Requirements

- JDK 25
- Gradle wrapper from this repository

## Modules

| Module | Purpose |
| --- | --- |
| `corusco-core` | Core runtime primitives such as lifecycle, values, keys, forms, and problems. |
| `corusco-swing` | Swing/AWT integration built on the core module. |
| `corusco-annotations` | Compile-time annotation API. |
| `corusco-processor` | Annotation processor implementation. |
| `corusco-test` | Shared test support for Corusco modules and examples. |
| `corusco-examples` | Small examples and regression playgrounds. |

## Build

Run the full baseline build with:

```bash
./gradlew clean build
```

The build uses the Java 25 toolchain and JUnit with AssertJ for tests.
