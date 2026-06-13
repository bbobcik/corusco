# COR-028 Add Generated Table Resources

Commit message:

```text
COR-028 Add generated table resources
```

## Roadmap Coverage

Roadmap Stage 13: Annotation Processor Phase 4, generated table resource and
help metadata slice.

## Objective

Generate a dedicated table resource-key class and carry optional column help
topics into typed column descriptors. This keeps generated table metadata
aligned with generated form metadata and prepares later table header tooltip,
help, accessibility, and resource lookup behavior.

## Dependencies

- Requires COR-026 generated table column metadata.
- Requires COR-027 generated table row updaters.

## Scope

Add generated type for `CustomerRow`:

- `CustomerRowTableResources`

Required behavior:

- generate header and optional tooltip `ResourceKey<String>` constants in the
  table resource class;
- have generated column descriptors reference the table resource class instead
  of owning resource constants directly;
- read optional `@Help(topic = "...")` on `@Column` record components and pass
  `HelpTopic` into `ColumnDescriptor`;
- allow `@Help(tooltip = "...")` as the table-column tooltip source when
  `@Column(tooltip = "...")` is blank;
- reject conflicting tooltip ids declared on both `@Column` and `@Help`;
- keep existing handwritten five-argument `ColumnDescriptor` construction
  source-compatible by adding an overload.

## Required Deliverables

- Core `ColumnDescriptor` help-topic support with Javadoc.
- Annotation Javadocs updated so `@Help` covers generated table columns.
- Processor spec/writer updates for table resources and help topics.
- Processor tests proving generated resource class output and help-topic
  descriptor wiring.
- Example updated to read generated table resource/help metadata, with
  method-body comments where they clarify generated-code conventions.
- Stage-plan index update.

## Out of Scope

- Runtime resource lookup service.
- Help behavior, F1 handling, and table header tooltip behavior.
- Table state persistence.
- Generated table binding plans.

## Implementation Steps

1. Add this stage plan and index entry.
2. Extend `ColumnDescriptor` with optional `HelpTopic` while preserving the
   current constructor shape for handwritten code.
3. Extend table spec extraction to read `@Help` tooltip/topic metadata and
   validate conflicts/ids.
4. Generate `<Row>TableResources` and update generated columns to reference it.
5. Add processor tests and update the generated table example.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Generated table resource class contains header and tooltip resource keys.
- Generated column descriptors carry optional help topics without reflection.
- Existing handwritten table examples using the old descriptor constructor keep
  compiling.
- Conflicting table tooltip annotations fail compilation with a clear
  diagnostic.
- Generated editable columns and Glazed Lists row-source compatibility continue
  to work.
