## Why

`DashboardViewModel.buildNavigationBarItems()` calls the top-level `isPortrait()` function in `org.dhis2.utils.OrientationUtils`, which reads `Resources.getSystem().configuration.orientation`. Under plain JVM unit tests `Resources.getSystem()` returns `null` and `isPortrait()` throws NPE. The NPE is caught by `fetchDashboardModel()`'s catch-all, so `loadNavigationBarItems()` silently aborts before reaching Phase 2 (where `pageConfigurator.displayAnalytics()` / `displayRelationships()` are invoked).

Effect: the Phase 2 branch is untestable from `DashboardViewModelTest`. The dispatcher/timing test deferred from `fix-tei-dashboard-anr-analytics` and `fix-program-has-analytics-metadata-only` cannot be written without either Robolectric (heavy for a single test) or injecting orientation into the view model.

## What Changes

Inject orientation as a view-model dependency so tests can drive Phase 2 without Android resources:

1. Add an `OrientationProvider` abstraction (single-method `isPortrait(): Boolean`) in `commons/` (upstream-safe — no fork-specific logic).
2. Provide a production implementation backed by `Resources.getSystem().configuration.orientation`, wired through the existing DI graph that already constructs `DashboardViewModel`.
3. Inject it into `DashboardViewModel` as a constructor parameter. Replace the top-level `isPortrait()` call with `orientationProvider.isPortrait()`.
4. Update `DashboardViewModelTest` to pass a fake `OrientationProvider` and add the dispatcher/timing test that was deferred: verify `pageConfigurator.displayAnalytics()` is invoked via the IO dispatcher and resolves within a configurable budget (e.g. 200 ms on a `StandardTestDispatcher` with a realistic fake).

No behavioral change for users. Callers of the top-level `isPortrait()` elsewhere in the codebase are unaffected and remain as-is; only `DashboardViewModel` migrates to the injected abstraction.

## Capabilities

### Modified Capabilities

- `tei-dashboard-navigation-bar`: the predicate-evaluation-off-main-thread requirement gains a normative unit test covering dispatcher placement and timing budget. No change to runtime behavior.

### New Capabilities

_(None.)_

## Impact

- **Code**:
  - New: `commons/src/main/java/org/dhis2/commons/orientation/OrientationProvider.kt` (interface + production impl)
  - DI wiring for `OrientationProvider` in the module(s) that build `DashboardViewModel`
  - `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardViewModel.kt` — add constructor param, replace top-level `isPortrait()` call
- **Tests**: `DashboardViewModelTest` gets a fake `OrientationProvider` in its setup and a new test `displayAnalytics resolves off the UI dispatcher within budget` covering Fix A's original deferred assertion.
- **Flavors**: All. Change is in shared code.
- **Upstream contribution**: Eligible — no EyeSeeTea-specific logic.
- **Risk**: Low. Pure refactor for testability; behavior identical.
- **Dependencies**: None added.

## Related

- Parent change: `fix-tei-dashboard-anr-analytics` (archived) — introduced the Phase 2 branch whose test coverage this change unlocks.
- Parent change: `fix-program-has-analytics-metadata-only` (archived) — also deferred the same dispatcher test.
