# Design — fix-tei-dashboard-oom-large-enrollments

## Context

The TEI dashboard data tab renders the enrollment's event timeline through `EventAdapter` (a `ListAdapter<EventModel, *>`) hosted in `fragment_tei_data.xml`. The layout nests a `wrap_content` RecyclerView inside a `NestedScrollView`, which forces the RecyclerView to measure its full content height: **every submitted `EventModel` is inflated and bound immediately, recycling never happens**. Each `item_event` hosts two ComposeViews.

`TeiDataRepositoryImpl` already caps the initial render: grouped-by-stage mode shows `maxEventToShow = 3` events per stage; timeline mode shows `maxEventToShow = 5`. However, the "show more" affordance (`ToggleStageEventsButtonHolder` → `StageSection.showAllEvents`) is **binary**: one tap flips the stage (or timeline) from the capped list to the complete list. On the sports-tracker instance, one enrollment has 1036 events in a single repeatable stage; tapping "show more" submits ~1036 `EventModel`s, the eager bind allocates ~27k Compose LayoutNodes / thousands of Views (heap dump evidence, 2026-06-12), the heap hits its 576MB ceiling, blocking GC cycles starve the main thread (input-dispatch ANRs), and the process eventually OOMs.

Independently, every stage-filter/grouping emission in `TEIDataPresenter` triggers `ruleEngineHelper.refreshContext()` + `evaluate()`, which rebuilds the full rule-engine context: `RulesRepository.enrollmentEvents` loads all events **and issues 2 extra SDK queries per event** (programStage name, orgUnit code) — ~2000 queries per evaluation on this enrollment, sometimes concurrently on several Rx threads.

Constraints:
- All touched files are **shared upstream code** (no `app/src/sports/` overlay involved). Per `eyeseetea-docs/upgrade/conflict-rules.md`, surviving customizations must carry `// EyeSeeTea customization - [title]` markers and stay minimal to limit future merge conflicts.
- The full structural fix (removing the `NestedScrollView`, restoring real recycling) is deliberately out of scope — it is a large upstream-facing layout change, tracked separately for `develop-eyeseetea` + DHIS2 Jira.

## Goals / Non-Goals

**Goals:**
- The dashboard never binds an unbounded number of event cards in one pass; peak memory stays proportional to what the user has explicitly revealed.
- Opening the dashboard of a 1000+-event enrollment is ANR-free and OOM-free, including after tapping "show more".
- Rule evaluation cost is bounded: O(events) SDK queries become O(1) bulk queries; unchanged contexts are reused rather than rebuilt.
- Behavior for small enrollments (< 1 page) is visually unchanged.

**Non-Goals:**
- Restoring true RecyclerView recycling (upstream contribution, separate change).
- Changing rule-engine evaluation semantics or the dhis2-rule-engine library.
- Virtualizing the Compose content inside each event card.

## Decisions

1. **Incremental paging instead of binary `showAllEvents`** — `StageSection` gains a revealed-count notion (page size `EVENTS_PAGE_SIZE = 25`). The toggle button becomes "show more (N remaining)" and reveals the next page; a "show less" affordance returns to the capped view. Alternative considered: hard cap with no way to see older events — rejected, users legitimately need historical events occasionally. Alternative: jump straight to the structural recycling fix — rejected here for merge-conflict cost; tracked upstream.
2. **Apply the same paging to both grouped and timeline modes** — both share the `take(...)` sites in `TeiDataRepositoryImpl` (`getGroupedEvents`, `getTimelineEvents`); a single helper computes the visible window. Keeps the two modes consistent and the diff small.
3. **Bulk-fetch rule-event metadata** — `RulesRepository.enrollmentEvents` collects the distinct `programStage` and `organisationUnit` uids from the loaded events, fetches each collection once with `byUid().in(uids)`, and joins via `associateBy { it.uid() }`. Removes ~2N queries per evaluation. Same change applied to `otherEvents` (event-mode evaluation) for parity.
4. **Reuse the rule-engine context across emissions** — `TEIDataPresenter` stops calling `refreshContext()` unconditionally on every stage-filter/grouping emission; the context is refreshed only when underlying data can have changed (event created/edited/deleted, sync completed — the existing refresh entry points). Stage filtering and grouping changes are pure view concerns and reuse the cached `contextData`. Alternative: debounce/serialize evaluations — weaker, still rebuilds.
5. **Marker comments** — every surviving hunk in shared files carries `// EyeSeeTea customization - bounded TEI event list` or `// EyeSeeTea customization - rule engine bulk context` so future merges classify them per conflict-rules.md.

## Risks / Trade-offs

- [Users with large stages must tap "show more" repeatedly to reach very old events] → page size 25 keeps it tolerable; revealed events stay revealed for the session; if feedback demands it, a search/filter affordance is the follow-up, not unbounded binding.
- [Stale rule effects if a refresh entry point is missed by Decision 4] → keep `refreshContext()` at every mutation callback (event save, delete, schedule, sync); add a regression test asserting re-evaluation after event mutation.
- [Even 25 more cards on a 1036-event stage re-measures the whole defeated RecyclerView] → measured cost is linear in *revealed* cards (~100s, not 1000s); acceptable until the upstream recycling fix lands.
- [Upstream merge conflicts on `TeiDataRepositoryImpl` / `EventAdapter`] → hunks are small, contiguous, and marked; conflict-rules.md classification will be `manual_reapply_on_theirs`.

## Migration Plan

No data or API migration. Single PR into `develop-sports`; validated with the sports upgrade-validation-checklist flows plus the new large-enrollment scenario (1036-event TEI). Rollback = revert the PR.

## Open Questions

- Should `EVENTS_PAGE_SIZE` be remote-configurable (ASWA / settings)? Default: no — constant first, revisit on feedback.
- Does upstream 3.4.x already restructure the dashboard list? To check while preparing the upstream contribution; if yes, the fork mitigation stays local to 3.3.x.
