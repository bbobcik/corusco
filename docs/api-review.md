# Preview API Review

This review freezes the package-level public surface for the `0.1.0-preview`
line. Type and method-level compatibility is governed by
[Release Policy](release-policy.md); this document records the package names and
runtime policy decisions that are stable enough to guard in the build.

## Published Package Surface

### `corusco-core`

- `cz.auderis.corusco.core`
- `cz.auderis.corusco.core.collection`
- `cz.auderis.corusco.core.command`
- `cz.auderis.corusco.core.convert`
- `cz.auderis.corusco.core.dialog`
- `cz.auderis.corusco.core.form`
- `cz.auderis.corusco.core.help`
- `cz.auderis.corusco.core.key`
- `cz.auderis.corusco.core.lifecycle`
- `cz.auderis.corusco.core.meta`
- `cz.auderis.corusco.core.problem`
- `cz.auderis.corusco.core.resource`
- `cz.auderis.corusco.core.table`
- `cz.auderis.corusco.core.task`
- `cz.auderis.corusco.core.tooltip`
- `cz.auderis.corusco.core.validation`
- `cz.auderis.corusco.core.value`

### `corusco-glazedlists`

- `cz.auderis.corusco.glazedlists`

### `corusco-swing`

- `cz.auderis.corusco.swing`
- `cz.auderis.corusco.swing.behavior`
- `cz.auderis.corusco.swing.binding`
- `cz.auderis.corusco.swing.collection`
- `cz.auderis.corusco.swing.command`
- `cz.auderis.corusco.swing.dialog`
- `cz.auderis.corusco.swing.table`
- `cz.auderis.corusco.swing.task`
- `cz.auderis.corusco.swing.testing`

### Compile-Time and Test Modules

- `cz.auderis.corusco.annotations`
- `cz.auderis.corusco.processor`
- `cz.auderis.corusco.test`

`corusco-examples` is intentionally excluded because it is not a published
compatibility surface.

## Package Naming Review

The package names follow the module boundaries from the roadmap:

- core runtime concepts live below `cz.auderis.corusco.core`;
- Swing/AWT integration lives below `cz.auderis.corusco.swing`;
- Glazed Lists interop is isolated in `cz.auderis.corusco.glazedlists`;
- annotation API and processor implementation are separate packages;
- generated-source test support is isolated in `cz.auderis.corusco.test`.

No package uses JavaBeans, reflection, legacy, or implementation-detail naming
as part of the public surface.

## Runtime Reflection Policy

Runtime modules are:

- `corusco-core`
- `corusco-glazedlists`
- `corusco-swing`

They must not depend on JavaBeans introspection, reflective member access,
runtime annotation scanning, or classpath scanning. The preview build verifies
this with `verifyRuntimeReflectionPolicy`.

The annotation processor may use `Element.getAnnotation(...)` because it runs
inside javac over language-model elements. That is compile-time metadata access,
not runtime annotation scanning.

## Verification

Run:

```bash
./gradlew verifyPreviewApiAudit --quiet --stacktrace
```

This aggregates:

- `verifyPreviewPackageSurface`
- `verifyRuntimeReflectionPolicy`
