## ADDED Requirements

### Requirement: Predicate evaluation off the main thread

The call that decides TEI dashboard navigation bar tab visibility (currently `DashboardViewModel.loadNavigationBarItems()`) SHALL execute on a non-UI dispatcher. Any call transitively reaching `DashboardRepositoryImpl.programHasAnalytics()`, `programHasRelationships()`, or other DB/analytics-touching predicates MUST run off the main thread.

#### Scenario: Program with many events does not block main thread
- **WHEN** the user opens the TEI dashboard for a program with configured program indicators and many enrolled events
- **THEN** the main thread MUST NOT execute `programHasAnalytics`, `getDefaultAnalytics`, or any `*.blockingEvaluate` call from the analytics pipeline for more than 16ms cumulatively in any single frame

### Requirement: No ANR on dashboard open

The TEI dashboard activity SHALL become interactive within the Android input-dispatch timeout regardless of program shape.

#### Scenario: Dashboard open stays interactive on large programs
- **WHEN** the user opens the TEI dashboard for any TEI in any program on any supported flavor
- **THEN** the activity SHALL become interactive (input focus delivered, touch events dispatched) within 5 seconds, and no `ActivityManager: ANR` log entry SHALL be produced for `TeiDashboardMobileActivity`

### Requirement: Observer emission ordering preserved

The two-phase navigation bar publish SHALL preserve the existing observer contract for downstream collectors.

#### Scenario: Dashboard model observer fires before nav bar state
- **WHEN** the `fetchDashboardModel` flow completes and observers on `dashboardModel`, `showFollowUpBar`, `syncNeeded`, `showStatusBar`, `state`, and `navigationBarUIState` receive their updates
- **THEN** the `dashboardModel` observer SHALL receive its update before the navigation bar state update (matching pre-fix behavior), and downstream collectors SHALL continue to run on the main thread via Lifecycle/Compose dispatch

### Requirement: Tab visibility semantics preserved

The two-phase publish SHALL converge to the same final tab set as the pre-fix single-phase publish.

#### Scenario: Final tab set matches pre-fix behavior
- **WHEN** the navigation bar is rendered for a given program/enrollment input
- **THEN** the set of visible tabs SHALL be identical to the pre-fix behavior (same result of `displayDetails`, `displayAnalytics`, `displayRelationships`, `displayNotes`)
