# COR-122 Harden data plane and showcase

## Roadmap Coverage

Version 1.3 typed large-data, fixed-schema dataset, and time-series hardening.

## Objective

Make the generated dataset/frame path the canonical end-to-end example and
prove its correctness, metadata preservation, allocation boundary, and Swing
table presentation behavior.

## Scope

- Extend generated-source negative and behavioral coverage for dataset roles,
  time axes, quality/missing policies, and aggregation metadata.
- Exercise generated frames and `DataSetFrameTableModel` through a realistic
  time-series showcase scenario.
- Update API review and guide documentation with the finalized public contracts.

## Out of Scope

JDBC, REST, Kafka, live streaming, backpressure, retention, charting, and a
general-purpose runtime data-frame or query-optimizer API.

## Acceptance Checks

- Core, processor, Swing, and examples tests pass.
- Generated sources compile without reflection and expose stable typed IDs.
- The showcase demonstrates descriptor, frame, table, filtering, sorting, and
  resource behavior.
- `verifyReleaseReadiness` passes against v1.2.0.
