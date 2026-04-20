## ADDED Requirements

### Requirement: Orientation access is injected into DashboardViewModel

`DashboardViewModel` SHALL obtain orientation through an injected `OrientationProvider` abstraction instead of the top-level `isPortrait()` function in `org.dhis2.utils.OrientationUtils`. This unblocks unit tests for the Phase 2 navigation bar branch.

#### Scenario: Unit test drives Phase 2 without Android resources
- **WHEN** a test constructs `DashboardViewModel` with a fake `OrientationProvider`
- **THEN** `buildNavigationBarItems` SHALL call the injected provider instead of `Resources.getSystem()`, and `loadNavigationBarItems` SHALL reach Phase 2 without throwing from Android framework absence

### Requirement: Dispatcher placement is covered by a unit test

A unit test in `DashboardViewModelTest` SHALL assert that `pageConfigurator.displayAnalytics()` is evaluated on the IO dispatcher (not the UI dispatcher) and resolves within a configurable time budget.

#### Scenario: displayAnalytics resolves off the UI dispatcher within budget
- **WHEN** `DashboardViewModel` is constructed with distinct `StandardTestDispatcher` instances for UI and IO, and the test advances only the UI scheduler
- **THEN** `pageConfigurator.displayAnalytics()` SHALL NOT have been invoked
- **WHEN** the IO scheduler is then advanced to idle
- **THEN** `pageConfigurator.displayAnalytics()` SHALL have been invoked exactly once, and the elapsed wall-clock time of the IO scheduler advance SHALL be under 200 milliseconds
