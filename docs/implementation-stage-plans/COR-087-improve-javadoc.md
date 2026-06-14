# Documentation

You are working in an existing Java codebase for an advanced Swing GUI library. The implementation already exists. Your task is to review and improve the Javadoc documentation across all structural levels: module, package, class/interface/enum/annotation, constructors, methods, fields, nested types, and important package-private implementation types where documentation would materially improve maintainability.

Do not redesign the library. Do not make behavioral changes. Do not refactor production code unless a tiny, mechanical change is strictly necessary to make documentation accurate or to fix a Javadoc build error. The primary deliverable is improved, coherent, navigable Javadoc.

## 1. First inspect the project

Before editing documentation, inspect the repository structure and determine:

* Java modules, especially `module-info.java`.
* Public and protected API surface.
* Package organization and intended layering.
* Core abstractions, model types, binding types, actions, table/list models, validation, annotation processors, generated-code contracts, Swing integration points, and test/support utilities.
* Existing Javadoc quality and style.
* Existing Gradle tasks for `javadoc`, `test`, `check`, or documentation generation.

Use the code as the source of truth. Do not invent capabilities. When behavior is unclear, document only what can be inferred confidently from names, signatures, tests, package structure, and implementation.

## 2. Documentation philosophy

The amount of Javadoc must be proportional to the importance and conceptual weight of the documented element.

Use this scale:

### Trivial public methods and accessible fields

For obvious accessors, simple predicates, constants, enum values, and narrowly scoped utility methods, provide a concise synopsis.

Good examples:

```java
/**
 * Returns the current presentation name.
 */
String getDisplayName();
```

```java
/**
 * Identifies the default column-width policy.
 */
DEFAULT_WIDTH_POLICY
```

Avoid overexplaining trivial code.

### Non-trivial public methods

For methods with meaningful semantics, side effects, lifecycle constraints, threading assumptions, null handling, event behavior, generated-code interaction, validation behavior, or Swing-specific consequences, document:

* What the method does.
* What inputs mean.
* What it returns.
* Preconditions and postconditions.
* Whether `null` is accepted or returned.
* Whether it must be called on the Swing Event Dispatch Thread.
* Whether it fires events, mutates model state, installs listeners, or touches Swing components.
* Relevant failure modes and exceptions.
* Relationship to adjacent methods.
* Whether repeated calls are idempotent.
* Whether ownership of passed objects is retained, copied, wrapped, or observed.

Use `@param`, `@return`, `@throws`, and where useful `@apiNote`, `@implSpec`, or `@implNote`.

### Classes, interfaces, enums, and annotations

Every public or protected top-level type should have full Javadoc.

Class-level Javadoc should explain:

* What the type represents.
* The problem it solves.
* Typical use cases.
* How it fits into the library architecture.
* Important collaborators.
* Whether instances are mutable, reusable, thread-safe, EDT-confined, or one-shot.
* Ownership/lifecycle rules.
* Usage patterns.
* Things to remember.
* Things to avoid.
* Small code examples when they clarify normal usage.
* Extension points and subclassing rules where relevant.

For abstract classes and interfaces, distinguish carefully between:

* API contract for callers.
* Contract for implementors.
* Optional methods versus mandatory methods.
* Event/listener obligations.
* Invariants implementors must preserve.

For annotations, document:

* Where the annotation is intended to be used.
* What code generation or runtime behavior it influences.
* Valid combinations with related annotations.
* Invalid or meaningless combinations.
* Whether the annotation processor validates the constraint.
* Generated artifacts or naming conventions, if applicable.

For enums, document:

* Concept represented by the enum.
* Each constant’s semantic meaning.
* Default/recommended values where applicable.
* Compatibility implications if persisted or serialized.

### Packages

Every meaningful package should have `package-info.java`.

Package Javadoc should explain:

* The package’s responsibility.
* Logical groups of classes inside the package.
* Relationships between the package’s main abstractions.
* Which classes are intended as entry points.
* Which classes are support types.
* How this package relates to neighboring packages.
* Common usage flows.
* Important architectural constraints.
* Swing-specific notes, especially EDT, model/view separation, listeners, actions, and component lifecycle.

Package Javadoc should help a newcomer decide: “Is this the package I should start reading?”

### Modules

Every exported module should have meaningful documentation in `module-info.java`.

Module Javadoc should serve as a newcomer’s introduction to the library:

* What the module is.
* What use cases it addresses.
* What it does not try to solve.
* Main packages and their roles.
* The recommended first package/class to inspect.
* The conceptual model of the library.
* Runtime requirements and important dependencies.
* Swing/EDT rules that apply globally.
* Annotation-processing or generated-code expectations, if relevant.
* A small “getting started” example if practical.
* Links to key packages and types using `{@link ...}` or `{@linkplain ...}`.

The module documentation should be sufficient for a competent Java/Swing developer to understand what kind of library this is and where to begin.

## 3. Style references

Use the following documentation styles as inspiration:

* JDK documentation, especially Swing-related APIs such as `javax.swing`.
* SwingX documentation style where applicable.
* Apache Commons Lang3 documentation for utility-style APIs.

Prefer precise, contract-oriented documentation over marketing prose.

Use standard Javadoc idioms:

* First sentence must be a clear summary.
* Use `{@link Type}` for important API links.
* Use `{@linkplain Type label}` when a natural-language label reads better.
* Use `{@code ...}` for identifiers, literals, expressions, and short code fragments.
* Use `<pre>{@code ...}</pre>` or `{@snippet ...}` for examples, depending on what the project’s Javadoc toolchain supports.
* Use `@param`, `@return`, `@throws`, `@see`, `@since`, `@apiNote`, `@implSpec`, and `@implNote` where they add value.
* Do not use `@author`.
* Do not add noisy tags that merely restate the method signature.
* Do not document private implementation details as public contract.
* Do not promise stability, performance, threading behavior, or lifecycle behavior unless supported by the implementation.

## 4. Swing-specific documentation rules

Because this is a Swing GUI library, document these topics wherever relevant:

* Whether a type or method must be used on the Event Dispatch Thread.
* Whether listeners are added, removed, retained strongly, or invoked synchronously.
* Whether model updates fire Swing events.
* Relationship between domain model, presentation model, table/list/tree model, selection model, action, editor, renderer, and component.
* Whether a class is safe to reuse across multiple Swing components.
* Whether a model owns data or merely adapts external data.
* Whether mutations are immediately reflected in the UI.
* Whether column order, column widths, visibility, sorting, or selection are model state, view state, persisted state, or transient component state.
* How validation, conversion, formatting, and binding errors are surfaced.
* Whether a method may be called before a component is realized, after disposal, or during event dispatch.
* Whether subclasses are expected to call `fireXxx`, `super`, or specific lifecycle hooks.

## 5. Advanced GUI-library concepts to document carefully

Pay special attention to abstractions around:

* Presentation models.
* Binding models.
* Table models.
* List/tree models.
* Column metadata.
* Column order, width, visibility, sorting, filtering, and persistence.
* Actions and command objects.
* Validation and conversion.
* Generated metadata.
* Annotation-processor generated code.
* Type-safe property references.
* Component factories/builders.
* Dialog/form lifecycle.
* Context help, tooltips, descriptions, labels, and accessible names.
* Separation between domain data and visualization/presentation concerns.

Where the code separates “data model” from “view/presentation metadata”, explain that distinction explicitly. For example, if one class represents actual row data and another represents table column layout or persistent user preferences, document their mutual responsibilities and where they should not be conflated.

## 6. Examples

Add small examples where they would reduce ambiguity. Examples should be short, realistic, and compilable or near-compilable.

Prefer examples at class, package, or module level rather than bloating every method.

A class-level example may show:

* Creating a model.
* Binding it to a Swing component.
* Installing columns.
* Reacting to selection.
* Applying validation.
* Persisting/restoring presentation state.

Keep examples modest. Do not create large tutorial blocks inside low-level utility classes.

## 7. Avoid these problems

Do not:

* Generate verbose boilerplate for every method.
* Repeat the method name in prose without adding information.
* Say “Gets the X” for complex methods where semantics matter.
* Guess intent not supported by code.
* Add misleading examples.
* Claim thread safety unless proven.
* Claim immutability unless all relevant fields and object graph behavior support it.
* Hide important constraints in implementation comments only.
* Use internal implementation classes as the starting point of package documentation.
* Document generated code as if users should edit it.
* Mix user-facing API contracts with maintainers-only implementation notes.
* Add TODO-style Javadoc.
* Leave malformed HTML, broken `@link` targets, or invalid tags.

## 8. Processing strategy

Work in stages.

### Stage 1: API survey

Inspect the codebase and produce a short internal map:

* Modules.
* Packages.
* Principal public types.
* Secondary/support public types.
* Existing missing or weak Javadoc.
* Javadoc warnings likely to occur.

Then begin editing.

### Stage 2: Module and package documentation

Start with the highest structural levels:

1. `module-info.java`
2. `package-info.java` files

Create missing `package-info.java` files for meaningful packages.

The package/module documentation should establish vocabulary used later by class-level Javadoc.

### Stage 3: Core public types

Improve Javadoc for the most important public classes and interfaces first.

For each major public type, include:

* Summary sentence.
* Conceptual description.
* Main usage pattern.
* Lifecycle/threading notes.
* Collaborators and links.
* Example where useful.
* Extension/implementation contract where relevant.

### Stage 4: Public members

Improve constructors, methods, fields, enum constants, and nested types.

Prioritize:

* Public/protected API.
* Methods with side effects.
* Methods with generic types.
* Methods with listener/event behavior.
* Methods involving Swing models/components.
* Methods involving annotation-generated metadata.
* Methods where `null`, exceptions, threading, ordering, or ownership matter.

### Stage 5: Important non-public types

Document package-private types only when they represent important internal architecture that future maintainers must understand.

For purely local helpers, prefer minimal comments or none.

### Stage 6: Validation

Run the appropriate Gradle task, likely one or more of:

```bash
./gradlew javadoc
./gradlew check
./gradlew test
```

Use the actual project tasks if different.

Fix Javadoc warnings and broken links. If the repository treats Javadoc warnings as errors, preserve that standard.

## 9. Acceptance criteria

The task is complete when:

* Public modules have useful introductory Javadoc.
* Meaningful packages have `package-info.java`.
* Major public types are fully documented.
* Complex public methods have precise contracts.
* Trivial public members have concise summaries.
* Swing threading/lifecycle/event semantics are documented where relevant.
* Generated-code and annotation-processing contracts are documented where relevant.
* Examples exist where they clarify important usage patterns.
* Javadoc links resolve.
* Javadoc generation succeeds.
* Tests/checks still pass unless unrelated existing failures are discovered.
* No behavioral changes were introduced.

## 10. Final response format

When finished, report:

1. What documentation levels were updated: module, packages, types, members.
2. The most important packages/classes whose documentation was improved.
3. Any Javadoc warnings fixed.
4. Any assumptions made because the code did not fully reveal intent.
5. Commands run and their results.
6. Any remaining documentation gaps that should be handled by a human maintainer.
