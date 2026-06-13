---
name: dhis2-android-sdk
description: >
  Skill for querying and writing local DHIS2 data through the DHIS2 Android SDK
  (com.github.EyeSeeTea:dhis2-android-sdk, the `d2` object). Covers the typed
  module/collection-repository API, pushing filters into the SDK query,
  avoiding N+1 query patterns, eager loading with `with*`, and choosing between
  blocking, RxJava, and coroutine accessors.
  Trigger on: any code that touches `d2.` (eventModule, trackedEntityModule,
  programModule, enrollmentModule, organisationUnitModule, ...), SDK repository
  queries, blockingGet, tracker/event data access, or DHIS2 local database reads/writes.
---

# DHIS2 Android SDK — Typed Local Data Access

## Overview

The DHIS2 Android SDK exposes all local data through the `d2` object as typed
**modules** containing **collection repositories**. Every read should be a
chainable, filtered repository query — never raw SQL, and never "fetch all,
filter in Kotlin". Queries execute lazily: nothing hits the database until a
terminal accessor (`blockingGet()`, `get()`, `getByUid()`, etc.) is called.

## Core Principles

- Always use the typed module API: `d2.<module>().<collection>().by<Filter>()...`.
- Push every filter into the SDK chain so it runs as SQL, not in memory.
- Never query inside a loop or `.map` over a collection — bulk-fetch once and join in memory.
- `blocking*` accessors only on background threads (Rx schedulers, `Dispatchers.IO`); never on main.
- Eager-load related collections with `with*()` only when you will actually read them.
- SDK access lives in **repositories** (data layer). ViewModels, Activities, and Composables never touch `d2`.

## The Module / Collection-Repository Pattern

```kotlin
val events: List<Event> =
    d2.eventModule().events()              // collection repository
        .byEnrollmentUid().eq(enrollmentUid)  // filters → SQL WHERE
        .byDeleted().isFalse
        .withTrackedEntityDataValues()        // eager-load children
        .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
        .blockingGet()                        // terminal accessor (background thread!)
```

Common modules used in this repo:

| Module | Collections |
| --- | --- |
| `d2.eventModule()` | `events()`, `eventService()` |
| `d2.enrollmentModule()` | `enrollments()`, `enrollmentService()` |
| `d2.trackedEntityModule()` | `trackedEntityInstances()`, `trackedEntityAttributes()`, `trackedEntityAttributeValues()`, `trackedEntityDataValues()` |
| `d2.programModule()` | `programs()`, `programStages()`, `programIndicators()`, `programRules()` |
| `d2.organisationUnitModule()` | `organisationUnits()` |
| `d2.dataElementModule()` | `dataElements()` |
| `d2.optionModule()` | `options()`, `optionSets()` |
| `d2.userModule()` | `userRoles()`, `userGroups()`, `authorities()` |
| `d2.settingModule()` | `analyticsSetting()`, `appearanceSettings()` |

Single-object access: `.uid(someUid)` narrows a collection repository to one
object — `d2.programModule().programs().uid(programUid).blockingGet()`.
For frequent lookups, `commons` provides shorthands in
`org.dhis2.commons.bindings.SdkExtensions` (`d2.program(uid)`, `d2.event(uid)`,
`d2.enrollment(uid)`, ...) — prefer these over re-spelling the chain.

## Push Filtering into the Query

Every `by<Field>()` filter becomes part of the SQL `WHERE` clause. Filtering
after `blockingGet()` materializes every row (and its eager-loaded children)
just to throw most of them away.

```kotlin
// BAD — loads ALL events of the enrollment into memory, then filters in Kotlin
val active = d2.eventModule().events()
    .byEnrollmentUid().eq(enrollmentUid)
    .blockingGet()
    .filter { it.deleted() != true }
    .filter { it.status() !in listOf(EventStatus.SCHEDULE, EventStatus.SKIPPED) }
    .filter { it.eventDate()?.before(Date()) == true }
```

```kotlin
// GOOD — SDK runs one indexed SQL query (real pattern from RulesRepository.kt)
val active = d2.eventModule().events()
    .byEnrollmentUid().eq(enrollmentUid)
    .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
    .byEventDate().beforeOrEqual(Date())
    .byDeleted().isFalse
    .blockingGet()
```

Filter operators available on most fields: `eq`, `neq`, `in`, `notIn`, `like`,
`isNull`/`isNotNull`, `isTrue`/`isFalse` (booleans), and for dates
`before`, `beforeOrEqual`, `after`, `afterOrEqual`, `inDatePeriods`.
Combine with `orderBy<Field>(...)` for SQL-side sorting.

For existence/count checks, never fetch the rows:

```kotlin
// BAD                                            // GOOD
.blockingGet().isNotEmpty()                       !repo.blockingIsEmpty()
.blockingGet().size                               repo.blockingCount()
.blockingGet().map { it.uid() }                   repo.blockingGetUids()
```

## Avoid N+1 Queries — Bulk-Fetch and Join

Never call `.uid(x).blockingGet()` inside a `.map`/loop over a collection.
Each call is a separate SQLite query. **This caused real ANR/OOM incidents in
this repo**: the rule-engine context build ran 2 extra queries per event
(program stage name + org unit code) over 1000+ events — thousands of queries
per dashboard open.

```kotlin
// BAD — 2 queries per event, N+1 pattern (pre-fix RulesRepository.kt)
events.map { event ->
    RuleEvent(
        programStageName = d2.programModule().programStages()
            .uid(event.programStage()).blockingGet()!!.name()!!,
        organisationUnitCode = d2.organisationUnitModule().organisationUnits()
            .uid(event.organisationUnit()).blockingGet()?.code(),
        // ...
    )
}
```

```kotlin
// GOOD — 2 queries total, regardless of event count
val stageNames = d2.programModule().programStages()
    .byUid().`in`(events.mapNotNull { it.programStage() }.distinct())
    .blockingGet()
    .associateBy({ it.uid() }, { it.name() })
val orgUnitCodes = d2.organisationUnitModule().organisationUnits()
    .byUid().`in`(events.mapNotNull { it.organisationUnit() }.distinct())
    .blockingGet()
    .associateBy({ it.uid() }, { it.code() })

events.map { event ->
    RuleEvent(
        programStageName = stageNames.getValue(event.programStage()!!)!!,
        organisationUnitCode = orgUnitCodes[event.organisationUnit()],
        // ...
    )
}
```

The same applies to any per-row lookup: collect the distinct uids, fetch once
with `byUid().in(...)`, build a `Map` keyed by uid, then join in memory.

## Eager Loading — `with*()`

`with*()` methods attach child collections to each returned row. Each one
multiplies memory per row, so load only what the caller reads.

```kotlin
.withTrackedEntityDataValues()        // events + their data values
.withTrackedEntityAttributeValues()   // TEIs + their attribute values
.withNotes()                          // enrollments/events + notes
```

- Need data values for rule evaluation? `withTrackedEntityDataValues()` is correct — one query instead of one-per-event.
- Listing events to show name/date/status only? Do **not** add `with*` — you would carry every data value of every event for nothing.

## Choosing the Async Flavor

Every repository offers the same query with several terminal accessors:

| Accessor | Returns | Use when |
| --- | --- | --- |
| `blockingGet()` / `blockingCount()` / `blockingIsEmpty()` | value, synchronously | Already on a background thread: inside `withContext(Dispatchers.IO)`, a `suspend` repository function, or an Rx chain on `Schedulers.io()`. **Never on the main thread** — it blocks on SQLite and ANRs. |
| `get()` | `Single<List<T>>` | Extending an existing RxJava chain (legacy repositories like `DashboardRepositoryImpl` use `.get().toObservable()`/`.toFlowable()`). |
| `getUids()`, `count()`, `isEmpty()` | Rx `Single` variants | Same, for projections. |

```kotlin
// New code — coroutine repository function, blocking accessor on IO dispatcher
override suspend fun enrollmentEvents(enrollmentUid: String): List<Event> =
    withContext(dispatchers.io()) {
        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byDeleted().isFalse
            .blockingGet()
    }

// Legacy code — extending an Rx chain
override fun getEnrollment(): Observable<Enrollment> =
    d2.enrollmentModule().enrollments().uid(enrollmentUid)
        .get()
        .toObservable()
```

Per project rules: coroutines/Flow for new code, Rx only when extending
existing Rx features, and never mix both in the same feature.

## Architectural Scope

`d2` is injected into **data-layer repositories only** (e.g.
`DashboardRepositoryImpl`, `TeiDataRepositoryImpl`, `RulesRepository`).
ViewModels and Presenters call repository interfaces; Activities, Fragments,
and Composables never see `d2`. If you find yourself injecting `D2` into a
ViewModel (some legacy ones do), do not extend the pattern — route the new
access through a repository.

## Common Pitfalls

- **`blockingGet()` on the main thread** — instant jank or ANR; SQLite work must be off-main.
- **`.filter { }` after `blockingGet()`** — the filter belongs in the query chain as `by<Field>()`.
- **Per-row `.uid(x).blockingGet()` in a `.map`** — the N+1 pattern; bulk-fetch with `byUid().in(...)` + `associateBy`.
- **Unneeded `with*()`** — eager-loading children you never read multiplies memory per row.
- **`blockingGet().isEmpty()`** — use `blockingIsEmpty()`; it runs `COUNT` instead of materializing rows.
- **Raw SQL or direct DB access** — the typed repositories cover filtering, ordering, and joins; raw SQL bypasses SDK consistency and sync state.
- **`d2` outside the data layer** — keep SDK access behind repository interfaces.
