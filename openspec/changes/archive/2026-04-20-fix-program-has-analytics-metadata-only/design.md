## Context

After `fix-tei-dashboard-anr-analytics` landed, the TEI dashboard on sports no longer ANRs, but `DashboardViewModel.loadNavigationBarItems()` Phase 2 still spends ~60 s of IO-thread CPU evaluating every configured chart just to decide whether to show the Analytics tab. Device logs show sustained GC (28–30 MB freed per 2–3 s interval) for the full duration.

The root cause is that the current `programHasAnalytics()` predicate uses `charts.geEnrollmentCharts(enrollmentUid).isNotEmpty()`. `geEnrollmentCharts` runs the full analytics pipeline — which walks every repeatable program stage, every numeric data element, every program indicator, then calls `programIndicatorToGraph.map(...)` or `dataElementToGraph.map(...)` for each, which in turn call `EventLineListRepositoryImpl.blockingEvaluate` via `runBlocking { ... }` and parse every attribute value's date via `SafeDateFormat`. This is O(events × indicators × attribute-values) per chart.

What we actually need: "does this program have any configured analytics source?" — a metadata-only predicate.

## Goals / Non-Goals

**Goals:**
- Replace the chart-evaluation predicate with a metadata-only existence check.
- Zero behavioral regression for programs that today show the Analytics tab.
- Upstream-ready: no EyeSeeTea-specific code paths.

**Non-Goals:**
- Redesigning the `Charts` API or the analytics pipeline itself.
- Caching chart evaluations.
- Changing `ChartsRepositoryImpl.getAnalyticsForEnrollment` behavior — only adding a new, cheap method.
- Lazy-loading the Analytics tab only when selected. (Possible future optimisation; not necessary once the predicate is cheap.)

## Decisions

### 1. Add `programHasAnalyticsMetadata(programUid: String): Boolean` to the `Charts` API

Adding to the API (rather than inlining the checks in `DashboardRepositoryImpl`) keeps the knowledge of "what makes a program analytics-bearing" inside the Charts module — the same place that defines `getAnalyticsForEnrollment`. If the analytics pipeline's sources evolve (new settings, new default-chart types), the predicate and the implementation stay in sync.

```kotlin
// Charts.kt
interface Charts {
    fun geEnrollmentCharts(enrollmentUid: String): List<Graph>
    fun programHasAnalyticsMetadata(programUid: String): Boolean   // new
    // … other existing methods
}
```

**Alternative considered:** Inline metadata checks in `DashboardRepositoryImpl.programHasAnalytics`. Rejected — duplicates the source-of-truth about what "analytics exists" means across modules.

### 2. Metadata predicate implementation

```kotlin
// ChartsRepositoryImpl
override fun programHasAnalyticsMetadata(programUid: String): Boolean {
    val hasConfiguredVisualizations = d2
        .settingModule()
        .analyticsSetting()
        .visualizationsSettings()
        .blockingGet()
        ?.program()
        ?.get(programUid)
        ?.isNotEmpty() == true
    if (hasConfiguredVisualizations) return true

    // Default-analytics fallback: at least one repeatable stage with at least one
    // numeric data element. Program-indicator existence is handled by the caller
    // (DashboardRepositoryImpl.programHasAnalytics already checks hasProgramIndicator).
    val repeatableStages = d2
        .programModule()
        .programStages()
        .byProgramUid().eq(programUid)
        .byRepeatable().eq(true)
        .blockingGet()
    if (repeatableStages.isEmpty()) return false

    return repeatableStages.any { stage ->
        val stageDataElementUids = d2
            .programModule()
            .programStageDataElements()
            .byProgramStage().eq(stage.uid())
            .blockingGet()
            .mapNotNull { it.dataElement()?.uid() }
        stageDataElementUids.any { deUid ->
            d2
                .dataElementModule()
                .dataElements()
                .uid(deUid)
                .blockingGet()
                ?.valueType()
                ?.isNumeric == true
        }
    }
}
```

All calls are metadata `blockingGet` / `blockingIsEmpty` on the SDK's DB-backed repos. No analytics evaluation. The last `any { ... any { ... } }` is bounded by metadata shape (typically tens of stages × tens of DEs per program), not by event count.

**Alternative considered:** Use `getVisualizationGroups(programUid)` instead of direct access to `visualizationsSettings`. Rejected — that method short-circuits on home-scope vs. program-scope in a way that's opaque here; a direct lookup on `.program()[programUid]` is simpler and easier to verify.

**Alternative considered:** Always return `true` (assume analytics exists). Rejected — produces empty Analytics tabs for programs without any configured metadata, confusing users.

### 3. Caller change in `DashboardRepositoryImpl`

```kotlin
// Before
val hasCharts = enrollmentUid?.let {
    charts.geEnrollmentCharts(enrollmentUid).isNotEmpty()
} ?: false

// After
val hasCharts = enrollmentUid?.let {
    charts.programHasAnalyticsMetadata(programUid!!)
} ?: false
```

`enrollmentUid?.let { … }` is preserved — the Analytics tab only matters when there is an active enrollment, and the current code's `null` branch already short-circuits to `false`. `programUid` is guaranteed non-null inside the outer `if (!programUid.isNullOrEmpty())` block; we propagate the assertion via `!!`.

### 4. Secondary audit: `programHasRelationships`

```kotlin
// DashboardRepositoryImpl.kt:767
override fun programHasRelationships(): Boolean =
    if (!programUid.isNullOrEmpty()) {
        val teiTypeUid = d2.programModule().programs().uid(programUid).blockingGet()?.trackedEntityType()?.uid()
        teiTypeUid?.let { relationshipsForTeiType(it) }!!.blockingFirst().isNotEmpty()
    } else {
        false
    }
```

`relationshipsForTeiType` returns relationship-type *metadata* via the SDK. `blockingFirst()` on a `Single<List<RelationshipType>>` is metadata-sized — likely fine. Decision: read the implementation during Task §1.6 and only include in this change if the call actually triggers evaluation. Otherwise leave the audit as a note in the tasks file.

## Validation

1. Before change: on sports reproducer, confirm ~60 s of sustained GC after dashboard open (baseline already documented in Fix A validation).
2. After change: open the same TEI on sports; verify the Analytics tab appears within the same Phase-2 republish (no visible delay) and no sustained GC.
3. Regression: smoke-test widp and psi. For at least one program on each flavor, confirm that the Analytics tab visibility matches pre-fix behavior (same visible/hidden state given the same data).
4. Unit tests for `ChartsRepositoryImpl.programHasAnalyticsMetadata` covering the three cases in `proposal.md §Impact/Tests`.

## Risks & Rollback

- **Risk:** A program configured with analytics metadata but no events/data previously hid the Analytics tab (because `geEnrollmentCharts(...)` returned an empty list after `.canBeShown()` filtering). After this change the tab shows and the user sees an empty analytics screen. This is arguably a bugfix but might surprise existing users.
  - Mitigation: verify the analytics screen handles "no data" gracefully (it should, since partial-data programs already hit it).
- **Risk:** Another caller of `Charts.geEnrollmentCharts` may use it as an existence proxy similarly.
  - Mitigation: grep for `geEnrollmentCharts` callers before finalizing; review whether each needs migration.
- **Rollback:** One-commit revert restores the previous predicate.
