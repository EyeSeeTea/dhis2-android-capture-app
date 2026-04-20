## Context

`TeiDashboardMobileActivity` is the TEI dashboard screen shown when a user taps a tracked entity instance in a tracker program. On the sports flavor, opening a TEI in the "Strength & Conditioning" program produces a 10-second ANR. Android's ANR trace (captured via `dumpsys dropbox data_app_anr`) pinpoints the main thread mid-execution of:

```
DashboardViewModel.fetchDashboardModel$1$1.invokeSuspend (DashboardViewModel.kt:108)
 → loadNavigationBarItems (:133) → pageConfigurator.displayAnalytics()
 → DashboardRepositoryImpl.programHasAnalytics (:810)
 → charts.geEnrollmentCharts(enrollmentUid).isNotEmpty()
 → [full analytics evaluation + date-parsing every TEI attribute value]
```

The activity had already reached `Displayed +292ms` and the `Focus entered window` event had fired. The 10s stall is exactly in `fetchDashboardModel`'s `withContext(dispatcher.ui())` block, on the UI dispatcher.

## Goals / Non-Goals

**Goals (Fix A):**
- Eliminate the ANR on TEI dashboard open across all flavors.
- Keep observer emission ordering unchanged so downstream Compose/XML code does not need to change.
- Upstream-friendly: no EyeSeeTea-specific code paths.

**Non-Goals (Fix A):**
- Changing the semantics of `programHasAnalytics()`. It will still perform the same (expensive) work; it will just run off the main thread.
- Introducing caching of analytics results.
- Redesigning the navigation bar loading flow.

**Follow-up (Fix B, separate PR):**
- Change `programHasAnalytics()` to be a metadata-only predicate that does not evaluate charts.

## Decisions

### 1. Fix A — run navigation bar setup on IO, not UI

In `DashboardViewModel.fetchDashboardModel`:

```kotlin
// before
withContext(dispatcher.ui()) {
    val model = result.await()
    _dashboardModel.postValue(model)
    if (model is DashboardEnrollmentModel) {
        _showFollowUpBar.value = …
        _syncNeeded.value = …
        _showStatusBar.value = …
        _state.value = …
        _noEnrollmentSelected.value = false
        loadNavigationBarItems()            // ← blocks main thread
    } else {
        _noEnrollmentSelected.value = true
    }
}
```

After:

```kotlin
val model = result.await()

// Simple state emissions (already thread-safe on LiveData.postValue /
// MutableStateFlow.value); no need for dispatcher.ui() indirection.
_dashboardModel.postValue(model)
if (model is DashboardEnrollmentModel) {
    _showFollowUpBar.value = model.currentEnrollment.followUp() ?: false
    _syncNeeded.value = model.currentEnrollment.aggregatedSyncState() != SYNCED
    _showStatusBar.value = model.currentEnrollment.status()
    _state.value = model.currentEnrollment.aggregatedSyncState()
    _noEnrollmentSelected.postValue(false)

    // Expensive predicate evaluation stays off the main thread.
    loadNavigationBarItems()
} else {
    _noEnrollmentSelected.postValue(true)
}
```

Because `fetchDashboardModel` already runs its outer `launch(dispatcher.io())`, removing the `withContext(dispatcher.ui())` wrapper keeps the body on IO. `MutableStateFlow.value` and `MutableLiveData.postValue` are both safe from any thread; observers remain dispatched to the main thread by their collectors (`collectAsState`, `observe`).

**Alternative considered:** Wrap only the `loadNavigationBarItems()` call in `withContext(dispatcher.io())`. Rejected — functionally equivalent but leaves the remaining `withContext(dispatcher.ui())` as dead scaffolding, inviting future regressions that re-insert heavy work into the UI block.

**Alternative considered:** Make `pageConfigurator.displayAnalytics()` lazy and defer the analytics check until the user actually selects the analytics tab. Rejected for Fix A — it changes navigation bar behavior (tab appears/disappears after load) and is a larger refactor. Valid for Fix B if we decide not to do the metadata-only route.

### 2. Two-phase `loadNavigationBarItems`

Moving `loadNavigationBarItems()` off the UI thread (Decision 1) eliminates the ANR but exposes a separate problem: `loadNavigationBarItems()` synchronously awaits `pageConfigurator.displayAnalytics()` and `displayRelationships()` before publishing any items to `_navigationBarUIState`. On the sports reproducer, `displayAnalytics()` takes ~60 seconds. During that window the nav bar has no items, `selectedItem` stays `null`, the `navigateToFragment` block in `setUpNavigationBar`'s Composable never fires, and the user sees a blank `fragmentContainer`.

Mitigation: publish twice.

```kotlin
private fun loadNavigationBarItems() {
    // Phase 1 — cheap tabs only; picks a selected item so fragment mounts.
    _navigationBarUIState.value = _navigationBarUIState.value.copy(
        items = buildNavigationBarItems(
            includeAnalytics = false,
            includeRelationships = false,
        ),
    )
    if (navigationBarUIState.value.items.none { it.id == navigationBarUIState.value.selectedItem }) {
        onNavigationItemSelected(navigationBarUIState.value.items.first().id)
    }

    // Phase 2 — expensive predicates (we are on IO), republish full list.
    _navigationBarUIState.value = _navigationBarUIState.value.copy(
        items = buildNavigationBarItems(
            includeAnalytics = pageConfigurator.displayAnalytics(),
            includeRelationships = pageConfigurator.displayRelationships(),
        ),
    )
}
```

The item builder is extracted into a private helper so Phase 1 and Phase 2 cannot drift out of sync. Final tab set is identical to pre-fix behaviour.

**Alternative considered:** Always include Analytics/Relationships tabs unconditionally in Phase 1, so they never "pop in" later. Rejected — users would briefly see tabs that the program does not actually configure; selecting them before the predicate resolved would navigate to empty screens.

**Alternative considered:** Launch each predicate in its own coroutine and `collect` results into the StateFlow. Rejected — more machinery for essentially the same perceptual outcome, since Fix B will make the predicates cheap enough that the Phase 2 delay disappears.

### 3. Where else `loadNavigationBarItems()` is called

`loadNavigationBarItems` is only invoked from within `fetchDashboardModel`, so a single change at the call site covers all paths. No public API changes.

### 4. Observer ordering

`_dashboardModel.postValue(model)` still precedes `loadNavigationBarItems()` in code, and both run on a single coroutine, so the dashboard model observer fires before the navigation bar state update — identical to the current behavior. Phase 1 and Phase 2 nav-bar emissions reach Compose collectors in order.

### 5. Fix B sketch (separate change)

Redefine `DashboardRepositoryImpl.programHasAnalytics(programUid, enrollmentUid)` as:

```kotlin
override fun programHasAnalytics(): Boolean = programUid?.let {
    val hasDisplayRuleActions = !d2.programModule()
        .programRuleActions()
        .byProgramRuleUid().`in`(programRuleUidsForProgram(it))
        .byProgramRuleActionType()
        .`in`(ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
              ProgramRuleActionType.DISPLAYTEXT)
        .blockingIsEmpty()

    val hasProgramIndicator = !d2.programModule()
        .programIndicators()
        .byProgramUid().eq(it)
        .blockingIsEmpty()

    val hasVisualizations = /* metadata-only check; no evaluation */

    hasDisplayRuleActions || hasProgramIndicator || hasVisualizations
} ?: false
```

This is still a blocking call but runs in O(#rules + #indicators) metadata lookups instead of O(#events × #indicators × #attribute-values) chart evaluations. Fix B is scheduled after Fix A has been validated in the wild.

## Validation

1. Build sports debug and reproduce the ANR on the pre-fix code path — **done** (trace `anr_2026-04-17-12-36-03-856`, 10s Input dispatch timeout, main-thread stack points at `DashboardViewModel.loadNavigationBarItems` → `programHasAnalytics`).
2. Apply Fix A + two-phase nav bar.
3. Open the same TEI; confirm no ANR, no `InputDispatcher: not responding` log, focus and first user input land within ~1s — **done** on sports (Displayed `+372ms`, zero new `data_app_anr` dropbox entries, `APPLYING EFFECTS` marker appears when the events Rx chain completes).
4. Confirm `Details`/`Notes` tabs render immediately; `Analytics` / `Relationships` tabs appear later when their predicates resolve — **done** on sports.
5. Repeat on widp and psi flavors with test data — **pending**, tracked in `tasks.md §3`.

## Risks & Rollback

- **Risk:** Observer ordering regressions. Mitigation: keep the `_dashboardModel.postValue(model)` call immediately before `loadNavigationBarItems()`, identical to current order.
- **Risk:** `loadNavigationBarItems` implicitly assumed to run on main thread by a future reader. Mitigation: add a short comment explaining why the call is on IO; add a unit test.
- **Rollback:** One-commit revert.
