# Tasks — fix-tei-dashboard-oom-large-enrollments

## 1. Bounded event-list rendering

- [x] 1.1 Add revealed-count paging to `StageSection` (replace binary `showAllEvents` with a revealed-events counter; `EVENTS_PAGE_SIZE = 25` constant) — mark with `// EyeSeeTea customization - bounded TEI event list`
- [x] 1.2 Update `TeiDataRepositoryImpl.getGroupedEvents` and `getTimelineEvents` to compute the visible window from the revealed count via a shared helper (no `take(eventList.size)` path)
- [x] 1.3 Update `ToggleStageEventsButtonHolder` to show "show more (N remaining)" / "show less" and emit page increments instead of the boolean toggle
- [x] 1.4 Verify `EventAdapter` diffing keeps revealed window stable across re-submissions (selection preserved, no flicker) — verified by inspection: items are diffed by event/stage uid, revealed events append at stable positions, toggle rebinds via `maxEventsToShow` content change
- [x] 1.5 Unit tests for the visible-window helper: caps, page increments, show-less reset, small-enrollment no-affordance case (concrete-value assertions)

## 2. Rule-engine context relief

- [x] 2.1 Refactor `RulesRepository.enrollmentEvents` to bulk-fetch programStages and organisationUnits (`byUid().in(uids)` + `associateBy`) — mark with `// EyeSeeTea customization - rule engine bulk context`
- [x] 2.2 Apply the same bulk-fetch to `RulesRepository.otherEvents`
- [x] 2.3 Remove the unconditional `refreshContext()` from the stage-filter/grouping pipeline in `TEIDataPresenter`; keep refresh at data-mutation entry points (event save/delete/schedule, sync) — refresh now happens in `init()` (screen re-entry) and `fetchEvents()` (post-mutation hook)
- [x] 2.4 Guard against concurrent context builds in `RuleEngineHelper` (single in-flight build, e.g. `Mutex`/memoized deferred)
- [x] 2.5 Unit tests: bulk-fetch produces identical RuleEvents to per-event lookups; context reused on grouping change; context rebuilt after event mutation

## 3. Validation

- [x] 3.1 Seed/replicate the 1036-event enrollment scenario on the sports-tracker instance; open dashboard, page through events, confirm no ANR and heap stays bounded (logcat GC watch) — validated 2026-06-12 on emulator: heap peaked at 29/53MB paging through the 1036-event stage (vs 576MB ceiling + ANR + OOM pre-fix); no ANR, heap trap (250MB) never fired
- [ ] 3.2 Run sports upgrade-validation-checklist dashboard flows (small enrollments unchanged)
- [x] 3.3 Module unit tests (StageSectionTest 6, RuleEngineHelperTest 3, RulesRepositoryTest 3 — all green), `:dhis2-mobile-program-rules:ktlintCheck`, `:app:assembleSportsDebug` — note: `:commons:ktlintCheck`/`:app:ktlintCheck` fail on pre-existing files unrelated to this change (BasicPreferenceProvider.kt, sports eventCaptureRepositoryFunctions.kt)
- [x] 3.4 Update `eyeseetea-docs/customizations/sports/customization-specs.md` and `customization-files.md` with the two customizations

## 4. Upstream follow-up (tracked, not implemented here)

- [ ] 4.1 Check upstream 3.4.x for dashboard list restructuring; draft DHIS2 Jira issue with evidence (heap histogram, GC timeline, layout analysis)
- [ ] 4.2 Open `develop-eyeseetea` proposal for the structural fix (remove NestedScrollView, restore recycling)
