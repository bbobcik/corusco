# COR-116 Add support for tidy data and timeseries

## Roadmap Coverage

Post-roadmap fixed-schema dataset and time-series slice.

## Outcome

Add typed dataset annotations, descriptor generation, columnar frame generation,
missing/quality/time-axis metadata, aggregation requests, and an optional Swing
table bridge for non-generic annotated records.

## Acceptance Checks

- Valid fixed-schema sources generate typed keys, descriptors, and frames.
- Invalid roles, time axes, missing policies, and quality references fail with
  actionable diagnostics.
- Generated frames retain semantic metadata and avoid mandatory row
  materialization in the table bridge.
- Core, processor, Swing, and showcase tests pass.
