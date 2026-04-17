# Spec — `programHasAnalytics` predicate

## Purpose

Deciding whether to show the Analytics tab in the TEI dashboard must be a cheap metadata check — never a chart evaluation. This spec refines the `tei-dashboard-navigation-bar` capability from the preceding change.

## Requirements

### R1 — Predicate is metadata-only (MUST)

**Given** any program in the DHIS2 database,
**When** `DashboardRepositoryImpl.programHasAnalytics()` is invoked,
**Then** the implementation MUST NOT invoke `ChartsRepositoryImpl.getAnalyticsForEnrollment`, `getDefaultAnalytics`, `ProgramIndicatorToGraph.map`, `DataElementToGraph.map`, `EventLineListRepositoryImpl.blockingEvaluate`, or any code path that parses attribute-value dates. Only SDK metadata repository calls (`*.blockingGet()`, `*.blockingIsEmpty()`, `*.blockingGetUids()`) on metadata tables (programs, program stages, program stage data elements, data elements, program indicators, program rules, program rule actions, analytics settings) are permitted.

### R2 — No sustained GC after dashboard open (MUST)

**Given** a program with configured analytics and many enrolled events,
**When** the user opens the TEI dashboard,
**Then** the log SHALL NOT show sustained "Background concurrent copying GC" entries freeing tens of MB at 2–3 s intervals for more than 5 s after `Displayed` is emitted.

### R3 — Tab visibility parity for data-bearing programs (MUST)

**Given** a program that previously (before this change) caused the Analytics tab to appear,
**When** the user opens the same TEI in the same program,
**Then** the Analytics tab SHALL still appear.

### R4 — Tab visibility for metadata-only analytics (MUST)

**Given** a program with analytics visualization settings configured but no events,
**When** the user opens the TEI dashboard,
**Then** the Analytics tab SHALL appear. (Behavioral change from pre-fix; the Analytics screen itself is responsible for rendering "no data" states gracefully.)

### R5 — Tab hidden for programs without analytics metadata (MUST)

**Given** a program with no visualization settings, no program indicators, no display-rule actions, and no repeatable stages containing numeric data elements,
**When** the user opens the TEI dashboard,
**Then** the Analytics tab SHALL NOT appear.

## Non-Requirements

- Lazy-loading of Analytics tab content (i.e. deferring chart evaluation until the tab is selected) is out of scope for this change.
- Caching of chart evaluation results is out of scope.
