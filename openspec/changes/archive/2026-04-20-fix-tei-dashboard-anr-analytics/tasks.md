## 1. Fix A — Unblock the main thread [SPORTS]

- [x] 1.1 [BE] Create branch `fix-sports/tei-dashboard-anr-analytics` from `feature-sports/setup-claude-dev-environment`
- [x] 1.2 [BE] In `DashboardViewModel.fetchDashboardModel`, remove the `withContext(dispatcher.ui())` wrapper so the post-`await()` code runs on the outer `dispatcher.io()` coroutine
- [x] 1.3 [BE] Convert `_noEnrollmentSelected.value = …` to `postValue(…)` (LiveData requires main for `value=`)
- [x] 1.4 [BE] Confirm `MutableStateFlow.value = …` assignments for `_showFollowUpBar`, `_syncNeeded`, `_showStatusBar`, `_state` are safe from IO (StateFlow setter is thread-safe)
- [x] 1.5 [BE] Add an inline comment explaining the IO-dispatcher invariant around `loadNavigationBarItems()`

## 2. Two-phase navigation bar [SPORTS]

- [x] 2.1 [BE] Extract a `buildNavigationBarItems(includeAnalytics, includeRelationships)` helper so both phases build from the same source
- [x] 2.2 [BE] Phase 1 — publish items without Analytics/Relationships; auto-select first item to trigger fragment mount
- [x] 2.3 [BE] Phase 2 — evaluate `displayAnalytics()` / `displayRelationships()` (on IO) and republish the full list

## 3. Manual validation [SPORTS]

- [x] 3.1 [QA] Build `./gradlew :app:assembleSportsDebug`, install on the reproducer device, open a TEI in the Strength & Conditioning program, confirm no ANR (`Displayed +372ms`, focus delivered, no dropbox `data_app_anr` entry)
- [x] 3.2 [QA] Confirm `Details` and `Notes` tabs render immediately; `Analytics` tab appears when its predicate resolves
- [ ] 3.3 [QA] Smoke-test widp (`:app:assembleWidpDebug`) and psi (`:app:assemblePsiDebug`) dashboards on a TEI — the fix is in shared code so both flavors should benefit identically
- [ ] 3.4 [QA] On each flavor, confirm the final tab set matches pre-fix behaviour for a program with configured analytics AND for a program without

## 4. Follow-ups (deferred)

- [ ] 4.1 [BE] **Unit test** — pin `pageConfigurator.displayAnalytics()` evaluation off the UI dispatcher via a test dispatcher. Still deferred: `buildNavigationBarItems` calls top-level `isPortrait()` → `Resources.getSystem()` which NPEs in plain JVM tests. Follow-up change `refactor-dashboard-viewmodel-orientation-injection` tracks the refactor.
- [x] 4.2 [BE] **Fix B** — metadata-only `programHasAnalytics`. Tracked as separate OpenSpec change `fix-program-has-analytics-metadata-only`.
- [x] 4.3 [BE] **Audit `programHasRelationships()`** — at `DashboardRepositoryImpl.kt:767` it calls `relationshipsForTeiType(...).blockingFirst()`. Confirmed metadata-only (queries `relationshipTypes`), no change needed.

## 5. Upstream contribution

- [ ] 5.1 [BE] Once Fix A is merged to `develop-sports` and validated, port to `develop-eyeseetea` and open a PR against Oslo's `develop`
- [ ] 5.2 [BE] Reference the ANR trace and the two-phase nav-bar rationale in the upstream PR description
