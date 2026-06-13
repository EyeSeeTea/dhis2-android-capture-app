# Fix TEI dashboard ANR/OOM with large enrollments

## Why

Opening the TEI dashboard for an enrollment with a large event history (observed: 1036 events in the sports-tracker instance) drives the app into a death spiral: the heap fills to its 576MB ceiling, the GC enters blocking 1–4s cycles that free 0 bytes, every touch event times out (`Input dispatching timed out` ANRs), and the process finally dies with `OutOfMemoryError`. This makes the app unusable for exactly the high-frequency tracking use case the sports fork exists for.

Evidence (emulator session, 2026-06-12, mid-climb heap dump at 259MB + ANR traces):

- `fragment_tei_data.xml` nests a `wrap_content` RecyclerView inside a `NestedScrollView`. The RecyclerView must measure its full content height, so **all event cards are inflated and bound eagerly — recycling is completely defeated**. Each `item_event` hosts two ComposeViews; the heap dump shows ~26,800 Compose LayoutNodes, ~4,000 MaterialTextViews, ~4,800 ImageViews and ~44,000 Paint objects — UI objects dominate the ~350MB of retained memory.
- Secondary contributor: `RulesRepository.enrollmentEvents` performs 2 extra SDK queries per event (programStage name, orgUnit code) — an N+1 pattern over 1000+ events — and `TEIDataPresenter` re-runs `refreshContext()` + `evaluate()` (full context rebuild) on every stage-filter/grouping emission, sometimes concurrently on several Rx threads.

Upstream `dhis2/dhis2-android-capture-app` `develop` has the same layout structure; it only manifests with large enrollments, so it goes unnoticed on demo data.

## What Changes

- **Bound event-list rendering**: the TEI dashboard event timeline no longer materializes every event card. Program stage groups render collapsed by default when the enrollment is large, and each stage shows a bounded page of events with an explicit "show more" affordance (extending the existing `StageViewHolder` / `ToggleStageEventsButtonHolder` machinery).
- **Rule-engine context relief**: `RulesRepository.enrollmentEvents` bulk-fetches programStage names and orgUnit codes once per evaluation instead of twice per event; the rule-engine context is reused across stage-filter/grouping emissions instead of being rebuilt from scratch each time.
- Out of scope (tracked separately for upstream contribution via `develop-eyeseetea` and DHIS2 Jira): removing the `NestedScrollView` wrapper and restoring true RecyclerView recycling in `fragment_tei_data.xml`.

## Capabilities

### New Capabilities

- `tei-dashboard-event-list-rendering`: bounded rendering of the TEI dashboard event timeline — default collapsed stages for large enrollments, per-stage event paging with "show more", and memory behavior requirements.
- `rule-engine-context-loading`: requirements for building the program-rule evaluation context — bulk metadata lookups (no per-event N+1 queries) and context reuse across consecutive evaluations of the same enrollment.

### Modified Capabilities

<!-- none — existing specs (tei-dashboard-navigation-bar, program-has-analytics-predicate) are untouched -->

## Impact

- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/` — `TEIDataPresenter`, `teievents/EventAdapter`, `teievents/StageViewHolder`, `teievents/ToggleStageEventsButtonHolder` (shared upstream code: surviving changes must carry `// EyeSeeTea customization - [title]` markers per conflict-rules.md).
- `dhis2-mobile-program-rules/src/main/java/org/dhis2/mobileProgramRules/` — `RulesRepository.enrollmentEvents`, `RuleEngineHelper` context caching.
- No SDK, API, or persistence changes. No flavor-specific code.
- Risk: behavior of the event timeline changes for large enrollments (collapsed-by-default); validated against `eyeseetea-docs/upgrade/sports/upgrade-validation-checklist.md` flows.
