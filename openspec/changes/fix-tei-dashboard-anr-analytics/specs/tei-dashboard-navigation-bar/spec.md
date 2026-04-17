# Spec — TEI Dashboard Navigation Bar

## Purpose

The TEI dashboard's bottom navigation bar decides at load time which tabs to show (Details, Analytics, Relationships, Notes) based on the selected program's metadata and configured features. These predicate evaluations must not block the main thread.

## Requirements

### R1 — Predicate evaluation off the main thread (MUST)

The call that decides navigation bar tab visibility (currently `DashboardViewModel.loadNavigationBarItems()`) SHALL execute on a non-UI dispatcher. In particular, any call transitively reaching `DashboardRepositoryImpl.programHasAnalytics()`, `programHasRelationships()`, or other DB/analytics-touching predicates MUST run off the main thread.

**Given** a program with configured program indicators and many enrolled events,
**When** the user opens the TEI dashboard,
**Then** the main thread MUST NOT be observed executing `programHasAnalytics`, `getDefaultAnalytics`, or any `*.blockingEvaluate` from the analytics pipeline for more than 16ms cumulatively in any single frame.

### R2 — No ANR on dashboard open (MUST)

**Given** any TEI in any program on any supported flavor,
**When** the user opens the TEI dashboard,
**Then** the activity SHALL become interactive (input focus delivered, touch events dispatched) within 5 seconds, and no `ActivityManager: ANR` log entry SHALL be produced for `TeiDashboardMobileActivity`.

### R3 — Observer emission ordering preserved (MUST)

**Given** the `fetchDashboardModel` flow completes,
**When** observers on `dashboardModel`, `showFollowUpBar`, `syncNeeded`, `showStatusBar`, `state`, and `navigationBarUIState` receive their updates,
**Then** the `dashboardModel` observer SHALL receive its update before the navigation bar state update (matching current behavior). Downstream collectors SHALL continue to run on the main thread via Lifecycle/Compose dispatch.

### R4 — Tab visibility semantics preserved (MUST)

**Given** the same program/enrollment input,
**When** the navigation bar is rendered,
**Then** the set of visible tabs SHALL be identical to the pre-fix behavior (same result of `displayDetails`, `displayAnalytics`, `displayRelationships`, `displayNotes`).

## Non-Requirements

- This spec does NOT require that `programHasAnalytics()` itself be fast. Making it fast is the subject of a follow-up change (Fix B).
- This spec does NOT require caching of analytics results.
