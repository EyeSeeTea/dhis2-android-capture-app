## 1. Implementation [SPORTS]

- [x] 1.1 [BE] Add `fun programHasAnalyticsMetadata(programUid: String): Boolean` to `Charts` interface (`dhis_android_analytics/src/main/java/dhis2/org/analytics/charts/Charts.kt`)
- [x] 1.2 [BE] Add the same method to `ChartsRepository` interface
- [x] 1.3 [BE] Implement in `ChartsRepositoryImpl` per `design.md §2` — metadata-only: visualization settings lookup + repeatable-stage × numeric-DE check
- [x] 1.4 [BE] Wire through `DhisAnalyticCharts` (delegate to repository)
- [x] 1.5 [BE] Replace the `hasCharts` line in `DashboardRepositoryImpl.programHasAnalytics` with a call to `charts.programHasAnalyticsMetadata(programUid!!)` (see `design.md §3`)
- [x] 1.6 [BE] Read `DashboardRepositoryImpl.programHasRelationships` implementation; confirmed metadata-only (queries `relationshipTypes`), no change needed.

## 2. Tests [SPORTS]

- [x] 2.1 [BE] Unit tests in `ChartsRepositoryImpl` covering:
  - program with `visualizationsSettings.program()[programUid]` configured → `true`
  - program with no settings but with a repeatable stage having a numeric data element → `true`
  - program with no settings, no repeatable stages, no numeric DEs → `false`
  - program with no settings, repeatable stages, but no numeric DEs → `false`
- [ ] 2.2 [BE] Unit test in `DashboardViewModelTest` — the deferred test from Fix A — confirming `pageConfigurator.displayAnalytics()` resolves off the UI dispatcher and in less than a configurable budget. **Still deferred**: `DashboardViewModel.buildNavigationBarItems` calls the top-level `isPortrait()` function, which uses `Resources.getSystem()` and throws NPE under plain JVM tests. The Phase 2 branch (where `displayAnalytics()` is invoked) is therefore unreachable from `DashboardViewModelTest` without either Robolectric or a refactor that injects orientation into the view model. Tracked as follow-up change `refactor-dashboard-viewmodel-orientation-injection`.
- [x] 2.3 [BE] Run `./gradlew :dhis_android_analytics:testDebugUnitTest` and `./gradlew :app:testSportsDebugUnitTest`

## 3. Manual validation [SPORTS]

- [x] 3.1 [QA] Build `./gradlew :app:assembleSportsDebug`, install on reproducer device, open the Strength & Conditioning TEI — the Analytics tab appears in Phase 2 without perceptible delay
- [x] 3.2 [QA] Confirm `logcat --pid <pid>` shows no sustained GC after dashboard open (no ~2 s cadence of "Background concurrent copying GC freed 28–30MB")
- [ ] 3.3 [QA] Smoke-test widp and psi dashboards on TEIs
- [ ] 3.4 [QA] For a program configured with analytics that has events → tab shows (expected). For a program configured with analytics that has **no** events yet → tab now shows (expected behavioral change; verify analytics screen handles empty data gracefully)
- [ ] 3.5 [QA] For a program with no analytics metadata at all → tab hidden (expected, same as before)

## 4. Upstream contribution

- [ ] 4.1 [BE] Port Fix A + Fix B together to `develop-eyeseetea`; open a PR against Oslo's `develop`
- [ ] 4.2 [BE] Include the ANR trace and the post-Fix-A GC profile in the upstream PR
