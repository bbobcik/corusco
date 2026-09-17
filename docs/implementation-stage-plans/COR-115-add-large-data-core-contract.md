# COR-115 Add large data core contract and support

## Roadmap Coverage

Post-roadmap neutral large-data and edit-contract slice.

## Outcome

Add Swing-free request, page, status, filtering, sorting, row-identity, data
source, and optimistic edit/conflict contracts without selecting a transport or
persistence provider.

## Acceptance Checks

- Request and page records validate their invariants.
- Data-source state and errors are explicit and typed.
- Edit sessions preserve row identity, version tokens, conflicts, and save
  results.
- No JDBC, REST, Kafka, or runtime reflection dependency enters core.
