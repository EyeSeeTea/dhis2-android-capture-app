## ADDED Requirements

### Requirement: `programHasAnalytics` is metadata-only

`DashboardRepositoryImpl.programHasAnalytics()` SHALL decide Analytics tab visibility using only SDK metadata repository calls. Chart evaluation pipelines MUST NOT be invoked from this predicate.

#### Scenario: Predicate does not invoke the analytics pipeline
- **WHEN** `DashboardRepositoryImpl.programHasAnalytics()` is invoked for any program
- **THEN** the implementation MUST NOT invoke `ChartsRepositoryImpl.getAnalyticsForEnrollment`, `getDefaultAnalytics`, `ProgramIndicatorToGraph.map`, `DataElementToGraph.map`, `EventLineListRepositoryImpl.blockingEvaluate`, or any code path that parses attribute-value dates; only SDK metadata repository calls (`blockingGet`, `blockingIsEmpty`, `blockingGetUids`) on metadata tables are permitted

### Requirement: No sustained GC after dashboard open

Opening the TEI dashboard on a program with configured analytics and many events SHALL NOT trigger sustained garbage collection driven by chart evaluation.

#### Scenario: Dashboard open does not thrash the GC
- **WHEN** the user opens the TEI dashboard for a program with configured analytics and many enrolled events
- **THEN** the log SHALL NOT show sustained "Background concurrent copying GC" entries freeing tens of MB at 2–3 second intervals for more than 5 seconds after `Displayed` is emitted

### Requirement: Predicate mirrors the enrollment analytics sources

The metadata predicate SHALL be true exactly when `getAnalyticsForEnrollment` would render charts, so the Analytics tab is shown if and only if it has content. It SHALL therefore check the same two sources that method reads — the TEI analytics settings (`analyticsSetting().teis().byProgram()`, rendered by `getSettingsAnalytics`) and the default analytics (repeatable stages with numeric data elements or display-in-form program indicators, rendered by `getDefaultAnalytics`) — and SHALL NOT use `analyticsSetting().visualizationsSettings().program()`, which feeds the program/home visualizations on a different screen.

#### Scenario: Program with TEI analytics settings
- **WHEN** the user opens a TEI in a program that has TEI analytics settings configured
- **THEN** the Analytics tab SHALL appear

#### Scenario: Program with default analytics from indicators only
- **WHEN** the user opens a TEI in a program with a repeatable stage and a display-in-form program indicator but no numeric data elements
- **THEN** the Analytics tab SHALL appear

#### Scenario: Program with only program/home visualization settings
- **WHEN** the user opens a TEI in a program that has `visualizationsSettings().program()` configured but no TEI analytics settings and no default analytics
- **THEN** the Analytics tab SHALL NOT appear (it would otherwise render empty)

### Requirement: Tab visibility parity for data-bearing programs

The metadata predicate SHALL not hide the Analytics tab for programs where it appeared before this change.

#### Scenario: Data-bearing program keeps its Analytics tab
- **WHEN** the user opens a TEI in a program that previously (before this change) caused the Analytics tab to appear
- **THEN** the Analytics tab SHALL still appear

### Requirement: Tab visibility for metadata-only analytics

Programs with analytics metadata configured but no enrolled events SHALL now expose the Analytics tab. The Analytics screen is responsible for handling empty-data states gracefully.

#### Scenario: Program configured with analytics but no events
- **WHEN** the user opens the TEI dashboard for a program that has TEI analytics settings configured but no events recorded
- **THEN** the Analytics tab SHALL appear (behavioral change from pre-fix)

### Requirement: Tab hidden for programs without analytics metadata

Programs with no analytics metadata whatsoever SHALL NOT expose the Analytics tab.

#### Scenario: Program with no analytics metadata
- **WHEN** the user opens the TEI dashboard for a program with no TEI analytics settings, no program indicators, no display-rule actions, and no repeatable stages containing numeric data elements
- **THEN** the Analytics tab SHALL NOT appear
