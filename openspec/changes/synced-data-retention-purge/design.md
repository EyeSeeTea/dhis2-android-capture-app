## Context

See `proposal.md` for motivation. The SDK already exposes
`D2.retentionModule().purge()` (SDK capability `synced-data-retention-purge`,
consumed here as an opaque, parameterless transactional call — see that
capability's own spec for what it purges and how limits are resolved). The
app already has a mature background-job pattern for periodic sync
(`SyncBackgroundJobAction` / `WorkManager`-based jobs for metadata and data
sync) and an existing settings screen (Sync Manager) that surfaces schedule
and status for those jobs. This design reuses both rather than introducing
new primitives.

This capability is intended to be generic enough to eventually propose
upstream, so nothing in this design or its implementation may live in a
flavor source set. Per `eyeseetea-docs/upgrade/conflict-rules.md`, this
design touches shared code with a history of upstream conflicts (the sync
settings screen and background job wiring), so the accept/reuse strategy
below follows that document's categories.

## Goals / Non-Goals

**Goals:**
- Reuse the existing periodic background-job and settings-status patterns
  exactly, rather than inventing a parallel mechanism.
- Keep the app-side surface minimal: trigger a job, observe its status,
  show it in Settings. All retention decision logic stays in the SDK.
- Keep the capability fully optional per build flavor, off by default.

**Non-Goals:**
- Defining or influencing what the SDK purge removes or how it resolves
  limits — that is entirely owned by the SDK's own
  `synced-data-retention-purge` capability.
- Any new device-side setting for retention rules (counts, scopes, per-type
  rules). None is introduced; see proposal.md.
- Designing richer user notification of what was deleted (e.g., an itemized
  list or an opt-in confirmation before deleting). Out of scope for this
  change.

## Decisions

- **Reuse the existing periodic-job pattern.** The app already schedules
  and observes metadata/data sync via `WorkManager`-backed jobs with a
  configurable interval, a manual "run now" action, and observable status.
  The retention purge trigger follows the same shape: a periodic job with a
  configurable frequency, a manual trigger, and an observable in-
  progress/last-run status — added as its own job, not folded into the
  existing sync job, so its schedule and status are independent of
  metadata/data sync. Alternative considered: chain the purge automatically
  after every successful data sync instead of giving it its own schedule.
  Rejected for this design because it removes the "manual on-demand" and
  "independent frequency" capabilities called out in the proposal; nothing
  about `D2.retentionModule().purge()` requires it to run adjacent to a
  sync.
- **The SDK call takes no app-supplied parameters.** Because retention
  limits are resolved entirely server-side (via already-synced settings),
  the app-side job has no configuration surface beyond *when* it runs — no
  per-data-type or per-scope options to plumb through the UI. This keeps
  the settings addition to a schedule control plus a status row, mirroring
  the existing sync settings UI one-for-one.
  SDK dependency: `D2.retentionModule().purge(): Unit` — no arguments, no
  return payload beyond success/failure. No other SDK surface is required.
- **Per-fork enablement via a single shared, build-time flag** (not a
  runtime setting, not a flavor override file). Conflict-rules classification:
  `accept_ours` — this is new shared code with no upstream equivalent to
  reconcile, so there is nothing to accept from upstream on merge; the flag
  itself lives in shared code precisely so a future upstream contribution
  does not need to be re-plumbed per flavor.
- **Status surfaces on the existing Sync Manager screen**, as an additional
  row alongside the existing metadata/data sync rows, not a new screen.
  Reduces UI surface and keeps the mental model ("this is a sync-adjacent
  background job") consistent with what already exists there.

## Risks / Trade-offs

- [The purge job could run concurrently with an in-progress data sync,
  reading sync-state columns mid-update] → Mitigation: the SDK's purge call
  is already transactional and reads `aggregatedSyncState`/settings as of
  call time; the app-side job does not need to serialize against sync
  itself, but should avoid enqueuing a purge run while a sync work request
  for the same unique-work name is actively running, using the same
  `WorkManager` unique-work guards already used to prevent double-enqueuing
  sync jobs.
- [A flavor forgets to explicitly enable the capability and silently gets
  no retention behavior] → Mitigation: the capability defaults to disabled
  and this is a deliberate, documented default (see proposal.md); flavors
  that want it must opt in explicitly, which is captured as an explicit
  task and validation-checklist entry per fork.
- [Diff/merge conflict surface on the Sync Manager screen and background
  job wiring, both shared files with upstream history] → Mitigation:
  additive changes only (new job, new settings row) rather than modifying
  existing sync job logic; per `conflict-rules.md`, classified
  `accept_ours` on merge since no equivalent exists upstream yet.
- [`D2.retentionModule().purge()` reports no intermediate progress, unlike
  `SyncData`'s per-task progress callbacks — the in-progress notification
  can only show an indeterminate state, not a percentage] → Accepted for
  this scope: last-run timestamp and in-progress status (both booleans/
  timestamps, not progress-dependent) are unaffected. Revisit only if time
  remains at the end of implementation — not a blocking gap for the
  requirements in spec.md.
- [Data sync also persists the failure stack trace of the last error
  (`SyncRepository.saveDataSyncError` → SDK local data store), used for
  diagnostics; retention purge does not — only the success/failure
  timestamp and outcome (see "Purge status visibility" in spec.md)] →
  Accepted for this scope: no requirement in spec.md asks for a diagnostic
  log, and `RetentionPurge`'s use case already surfaces the mapped
  `DomainError` through `Result.failure` to any caller that wants to log
  it (e.g. Sentry via the worker's own crash reporting, same as other
  workers). Revisit only if time remains at the end of implementation —
  not a blocking gap for the requirements in spec.md.
- [The instrumented UI test for the new Settings row
  (`SettingsTest.shouldShowRetentionPurgeOptionWithEditablePeriod`,
  `androidTest`) could not be run in this environment: `app`'s
  `dhis2DebugAndroidTestCompileClasspath` fails to resolve
  (`androidx.concurrent:concurrent-futures{,-ktx}` version conflict between
  a "lock file"-constrained 1.1.0 and `androidx.test:core:1.7.0`'s 1.2.0,
  via consistent resolution) — pre-existing, unrelated to this change,
  reproduces even with `--refresh-dependencies`] → Test is written and
  committed; unit tests (`SettingsRepositoryTest`, `LaunchSyncTest`,
  `SyncManagerPresenterTest`) and manual `ktlintCheck`/unit-test runs cover
  the logic. Revisit running/verifying the instrumented test at the end of
  implementation — not a blocking gap for this task, but this dependency
  conflict itself may be worth a separate fix outside this change.
