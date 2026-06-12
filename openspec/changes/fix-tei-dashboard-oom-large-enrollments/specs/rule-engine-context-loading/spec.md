# rule-engine-context-loading

## ADDED Requirements

### Requirement: Rule-event construction uses bulk metadata lookups

`RulesRepository.enrollmentEvents` and `RulesRepository.otherEvents` SHALL resolve programStage names and organisationUnit codes through bulk collection queries (one query per metadata type per evaluation), not through per-event lookups. The total number of SDK queries per context build SHALL be independent of the number of events.

#### Scenario: Building the context for a large enrollment

- **WHEN** the rule-engine context is built for an enrollment with 1036 events across 2 program stages and 3 org units
- **THEN** programStage metadata is fetched with a single `byUid().in(...)` query and organisationUnit metadata with a single `byUid().in(...)` query, and the resulting RuleEvents are identical to those produced by per-event lookups

### Requirement: Rule-engine context is reused across pure view changes

The TEI data presenter SHALL NOT rebuild the rule-engine context when only the stage filter or grouping mode changes. The context SHALL be refreshed when event data can have changed: event created, edited, deleted, scheduled, or a sync completed.

#### Scenario: Changing grouping mode

- **WHEN** the user switches between grouped-by-stage and timeline views with no data mutation in between
- **THEN** rule effects are evaluated against the cached context and no enrollment-events reload occurs

#### Scenario: Editing an event

- **WHEN** the user saves a change to an event and returns to the dashboard
- **THEN** the context is rebuilt and rule effects reflect the new data

### Requirement: Concurrent evaluations do not multiply context builds

Concurrent emissions that require evaluation SHALL share a single in-flight context build rather than each constructing their own copy.

#### Scenario: Rapid consecutive emissions

- **WHEN** stage filter and grouping emissions arrive while a context build is already running
- **THEN** at most one context build is in flight, and later evaluations reuse its result
