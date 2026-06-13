## Why

`DashboardRepositoryImpl.programHasAnalytics()` decides whether to show the **Analytics** tab in the TEI dashboard's bottom navigation bar. Its last predicate currently evaluates **every configured chart**:

```kotlin
// DashboardRepositoryImpl.kt:809-811
val hasCharts = enrollmentUid?.let {
    charts.geEnrollmentCharts(enrollmentUid).isNotEmpty()
} ?: false
```

`geEnrollmentCharts()` runs the full analytics pipeline (`ChartsRepositoryImpl.getAnalyticsForEnrollment` → `getDefaultAnalytics` → per-stage × per-indicator evaluation via `EventLineListRepositoryImpl.blockingEvaluate` and `ProgramIndicatorEngineImpl`), which parses every TEI attribute value's date via ICU's `SafeDateFormat`. On the sports "Strength & Conditioning" program this takes **~60 seconds** of sustained CPU on every dashboard open.

The previous change (`fix-tei-dashboard-anr-analytics`, already on this branch) moved the call off the main thread and split the nav bar into two phases, so the dashboard is now responsive from the first frame. But the CPU burn is still there: the Analytics tab takes ~60s to appear, and until it does the device allocates 28–30 MB of garbage every 2–3 seconds. This proposal removes that burn by making the predicate a metadata-only check.

## What Changes

Replace the `hasCharts` predicate with a metadata-only existence check. No chart values are evaluated; no dates are parsed.

Based on what `ChartsRepositoryImpl.getAnalyticsForEnrollment` actually draws from:

1. **`settingsAnalytics`** — `d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet()?.program()?.get(programUid)?.isNotEmpty()`. Pure metadata query.
2. **Default analytics fallback** — only reached when settingsAnalytics is empty. Sources: repeatable program stages × (numeric data elements ∪ program indicators). `programIndicators` existence is already covered by the existing `hasProgramIndicator` predicate, so the new check only has to confirm "program has at least one repeatable stage with at least one numeric data element".

The new `Charts` API exposes a cheap predicate:

```kotlin
// Charts.kt
fun programHasAnalyticsMetadata(programUid: String): Boolean
```

Implemented in `ChartsRepositoryImpl` with only `blockingGet()` / `blockingIsEmpty()` calls on the SDK's metadata repositories — no evaluation. `DashboardRepositoryImpl.programHasAnalytics()` calls this instead of `geEnrollmentCharts(...).isNotEmpty()`.

Secondary audit (confirmed in-scope from the Fix A design doc): `DashboardRepositoryImpl.programHasRelationships()` at line 767 calls `relationshipsForTeiType(teiTypeUid).blockingFirst().isNotEmpty()`. If that transitively touches actual relationship data rather than metadata, apply the same treatment. Scope extension decided after reading the implementation; default assumption is it's already metadata-only.

## Capabilities

### Modified Capabilities

- `tei-dashboard-navigation-bar` (from `fix-tei-dashboard-anr-analytics`): the Analytics tab predicate MUST complete in O(metadata queries), not O(events × indicators × attribute values).

### New Capabilities

_(None — this is a correctness/performance fix to an existing capability.)_

## Impact

- **Code**:
  - `dhis_android_analytics/src/main/java/dhis2/org/analytics/charts/Charts.kt` — add `programHasAnalyticsMetadata` interface method
  - `dhis_android_analytics/src/main/java/dhis2/org/analytics/charts/DhisAnalyticCharts.kt` — delegate to `ChartsRepository`
  - `dhis_android_analytics/src/main/java/dhis2/org/analytics/charts/ChartsRepository.kt` — add interface method
  - `dhis_android_analytics/src/main/java/dhis2/org/analytics/charts/ChartsRepositoryImpl.kt` — implement metadata-only check
  - `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardRepositoryImpl.kt` — call the new method
- **Tests**: Unit tests for `ChartsRepositoryImpl.programHasAnalyticsMetadata` covering: (a) program with visualization settings → true, (b) program without settings but with repeatable stages that have numeric DEs → true, (c) program without settings and without numeric DEs in repeatable stages and without program indicators → false. Plus the deferred dispatcher test from Fix A (verify `pageConfigurator.displayAnalytics()` resolves quickly and off the UI dispatcher).
- **Behavioral change**: The Analytics tab may now appear for programs that have analytics metadata configured but no data yet. This is the **correct** behavior — the current implementation hides the tab on data-less programs, which is surprising when a user knows analytics are configured but hasn't entered events yet.
- **Upstream contribution**: All touched code is 100% upstream Oslo. This fix should be contributed back alongside Fix A.
- **Risk**: Medium. The `Charts` API is used by other screens (Program Event Dashboard, Home analytics section); adding a method is backward-compatible, but we should verify no caller relies on `geEnrollmentCharts(...).isNotEmpty()` as an existence proxy elsewhere.
- **Performance target**: Analytics tab appears within the same frame as Details/Notes on the sports reproducer. No sustained GC activity after dashboard open.

## Related

- Preceding change: `fix-tei-dashboard-anr-analytics` — moved the call off the UI thread and added two-phase nav bar.
- ANR trace: dropbox `data_app_anr` entry dated `2026-04-17 12:36:12`.
- Sports reproducer CPU profile: 60 s of 107% user CPU + 28–30 MB/2–3 s GC after dashboard open (pre-fix), shown in post-Fix-A dropbox logs.
