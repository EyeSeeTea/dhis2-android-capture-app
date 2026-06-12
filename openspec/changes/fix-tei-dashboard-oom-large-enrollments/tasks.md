# Tasks — fix-tei-dashboard-oom-large-enrollments

## 1. Bounded event-list rendering

- [ ] 1.1 Add revealed-count paging to `StageSection` (replace binary `showAllEvents` with a revealed-events counter; `EVENTS_PAGE_SIZE = 25` constant) — mark with `// EyeSeeTea customization - bounded TEI event list`
- [ ] 1.2 Update `TeiDataRepositoryImpl.getGroupedEvents` and `getTimelineEvents` to compute the visible window from the revealed count via a shared helper (no `take(eventList.size)` path)
- [ ] 1.3 Update `ToggleStageEventsButtonHolder` to show "show more (N remaining)" / "show less" and emit page increments instead of the boolean toggle
- [ ] 1.4 Verify `EventAdapter` diffing keeps revealed window stable across re-submissions (selection preserved, no flicker)
- [ ] 1.5 Unit tests for the visible-window helper: caps, page increments, show-less reset, small-enrollment no-affordance case (concrete-value assertions)

## 2. Rule-engine context relief

- [ ] 2.1 Refactor `RulesRepository.enrollmentEvents` to bulk-fetch programStages and organisationUnits (`byUid().in(uids)` + `associateBy`) — mark with `// EyeSeeTea customization - rule engine bulk context`
- [ ] 2.2 Apply the same bulk-fetch to `RulesRepository.otherEvents`
- [ ] 2.3 Remove the unconditional `refreshContext()` from the stage-filter/grouping pipeline in `TEIDataPresenter`; keep refresh at data-mutation entry points (event save/delete/schedule, sync)
- [ ] 2.4 Guard against concurrent context builds in `RuleEngineHelper` (single in-flight build, e.g. `Mutex`/memoized deferred)
- [ ] 2.5 Unit tests: bulk-fetch produces identical RuleEvents to per-event lookups; context reused on grouping change; context rebuilt after event mutation

## 3. Validation

- [ ] 3.1 Seed/replicate the 1036-event enrollment scenario on the sports-tracker instance; open dashboard, page through events, confirm no ANR and heap stays bounded (logcat GC watch)
- [ ] 3.2 Run sports upgrade-validation-checklist dashboard flows (small enrollments unchanged)
- [ ] 3.3 `./gradlew :app:testSportsDebugUnitTest ktlintCheck` and `:app:assembleSportsDebug`
- [ ] 3.4 Update `eyeseetea-docs/customizations/sports/customization-specs.md` and `customization-files.md` with the two customizations

## 4. Upstream follow-up (tracked, not implemented here)

- [ ] 4.1 Check upstream 3.4.x for dashboard list restructuring; draft DHIS2 Jira issue with evidence (heap histogram, GC timeline, layout analysis)
- [ ] 4.2 Open `develop-eyeseetea` proposal for the structural fix (remove NestedScrollView, restore recycling)
