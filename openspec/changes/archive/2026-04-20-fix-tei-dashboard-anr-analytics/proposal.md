## Why

Opening a TEI dashboard in the sports app (Strength & Conditioning program) produces an **ANR** (Application Not Responding) within ~10 seconds of entering the screen. The activity draws its first frame quickly (+292ms) but then the main thread stalls for 10+ seconds at ~107% user CPU before Android force-finishes the activity.

An earlier proposal (`fix-exercises-list-performance`, now preserved on `wip-sports/eventlist-caching-experiment`) targeted a different screen — the standalone event-program list (`ProgramEventDetailActivity`). The ANR is actually on `TeiDashboardMobileActivity`, and the root cause is unrelated to event rendering.

## Root cause

The ANR trace captured on-device (Android 12, Samsung SM‑G970F, `anr_2026‑04‑17‑12‑36‑03_856`) shows the main thread's Java stack at fault:

```
Looper.loop → DispatchedTask.run
  → DashboardViewModel$fetchDashboardModel$1$1.invokeSuspend (DashboardViewModel.kt:108)
    → loadNavigationBarItems (DashboardViewModel.kt:133)
      → TeiDashboardPageConfigurator.displayAnalytics (:11)
        → DashboardRepositoryImpl.programHasAnalytics (:810)
          → DhisAnalyticCharts.geEnrollmentCharts
            → ChartsRepositoryImpl.getAnalyticsForEnrollment / getDefaultAnalytics
              → ProgramIndicatorToGraph.map
                → EventLineListRepositoryImpl.blockingEvaluate ← runBlocking
                  → ProgramIndicatorEngineImpl.evaluateEvents
                    → TrackedEntityAttributeValueStoreImpl.queryByTrackedEntityInstance
                      → TrackedEntityAttributeValueDB.toDomain
                        → SafeDateFormat.parse (ICU foldCase, per attribute value)
```

Decision sequence:

1. `DashboardViewModel.fetchDashboardModel` computes the dashboard model on IO, then switches back to the UI dispatcher (line 95) and calls `loadNavigationBarItems()` **on the main thread**.
2. `loadNavigationBarItems` calls `pageConfigurator.displayAnalytics()` to decide whether to show the Analytics tab.
3. `displayAnalytics()` delegates to `DashboardRepositoryImpl.programHasAnalytics()`, whose final predicate (line 810) is `charts.geEnrollmentCharts(enrollmentUid).isNotEmpty()`.
4. `geEnrollmentCharts` eagerly executes the **full analytics pipeline**: for every program indicator configured on the program it wraps `EventLineListRepositoryImpl.blockingEvaluate` in `runBlocking {}`, evaluates every enrolled event, queries all TEI attribute values, and date-parses each one via `SafeDateFormat`/ICU.

On large sports enrollments this produces a sustained CPU burn on the main thread, exceeding Android's 5s input-dispatch timeout.

## What Changes

### Fix A — Unblock the main thread (this change)

Two coupled edits in `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardViewModel.kt`:

1. **Move `fetchDashboardModel` off the UI dispatcher.** Remove the `withContext(dispatcher.ui())` wrapper so the post-`await()` code runs on the outer `dispatcher.io()` coroutine. Convert `_noEnrollmentSelected.value = …` to `postValue(…)` (LiveData requires main-thread `setValue`, but `postValue` is safe from any thread). `MutableStateFlow.value` setters are thread-safe and keep working; Lifecycle/Compose collectors continue to dispatch observers onto the main thread.

2. **Two-phase `loadNavigationBarItems`.** Publishing the nav-bar state synchronously after `displayAnalytics()` returns means the navigation bar stays empty (and therefore no fragment is mounted) until the expensive predicate completes. With Fix A alone the ANR is gone but the screen is blank for as long as the analytics evaluation runs — on the sports reproducer, ~60 seconds. To avoid this blank-screen window, `loadNavigationBarItems()` now publishes twice:

   - **Phase 1** — publish the cheap tabs immediately (`Details` when portrait, always `Notes`). Select the first item so `navigateToFragment` mounts `TEIDataFragment`.
   - **Phase 2** — evaluate `pageConfigurator.displayAnalytics()` and `pageConfigurator.displayRelationships()` (still on IO), rebuild the full item list, publish again.

   Collectors on `navigationBarUIState` receive both updates via Compose's `collectAsStateWithLifecycle`; Analytics/Relationships tabs appear a moment later without affecting the initial render.

The fix does NOT change the observable semantics of either call site — same order, same values, same tab set.

### Fix B — Correct the logical bug in `programHasAnalytics()` (separate change)

`programHasAnalytics()` should be a **metadata-only** predicate (does the program configure analytics at all?), not a full analytics evaluation. The existing implementation computes every chart's data points just to check whether any exist. This burns CPU on every dashboard open, even without Fix A's main-thread problem.

Fix B is out of scope for this change and tracked separately under OpenSpec change `fix-program-has-analytics-metadata-only` (see its proposal for the metadata predicate strategy).

### Tests

A unit test that pins `pageConfigurator.displayAnalytics()` evaluation off the UI dispatcher is intentionally **deferred** to keep this change narrow. The behavioral change (ANR cleared, dashboard renders immediately, Analytics tab appears when predicate resolves) has been manually validated on the sports reproducer device; automated coverage will land with a follow-up that also covers Fix B.

## Capabilities

### New Capabilities

- `tei-dashboard-navigation-bar`: The TEI dashboard navigation bar predicate evaluation must never run on the main thread. Tab visibility decisions (`displayAnalytics`, `displayRelationships`, etc.) that transitively touch the database or the analytics engine run on the IO dispatcher; the navigation bar state is updated via thread-safe StateFlow assignment.

### Modified Capabilities

_(None — this is a new capability; no existing spec to modify.)_

## Impact

- **Code**: `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardViewModel.kt` — reshape `fetchDashboardModel`. No API changes; observers keep their current contracts.
- **Tests**: Existing `DashboardViewModel` tests continue to pass. Add a test that verifies `loadNavigationBarItems` does not invoke `pageConfigurator.displayAnalytics()` on the calling thread when the view model's IO dispatcher is a test dispatcher (or that the call happens off the UI dispatcher).
- **Flavors**: All (fix is in shared `app/src/main`). Sports is the confirmed reproducer; PSI / WIDP likely also benefited implicitly from the earlier screen-specific change but are not affected by this.
- **Upstream contribution**: `DashboardViewModel` and `DashboardRepositoryImpl` are 100% upstream Oslo code. This fix should be contributed back — the code is generic, no EyeSeeTea-specific patterns.
- **Performance target**: No ANR in TEI dashboard open, across sports, widp, and psi flavors. Dashboard open remains visually responsive (first frame + input focus within 1s).
- **Dependencies**: None added.
- **Risk**: Low. Fix A only changes where a pre-existing expensive call runs; it does not change the result of the call or observer ordering.

## Related

- Superseded proposal (preserved branch): `wip-sports/eventlist-caching-experiment` — targeted `ProgramEventDetail` caching. Not applied; preserved for reference.
- ANR trace: dropbox `data_app_anr` entry dated `2026-04-17 12:36:12`, device `SM_G970F`, Android 12, sports flavor debug build `v150 (3.3.0.1-widp-fork-1)`.
