## Why

The app has no automatic or scheduled way to shrink the local database once
records are already synced to the server. The only two existing options are
"Delete local data" (wipes every table unconditionally, regardless of sync
state) and "Delete account" (full wipe + logout + full resync required).
Neither lets the app keep pending/error records while discarding data that
has already been safely sent to the server and is no longer needed on the
device, following the retention limits an administrator has already
configured server-side (Program/Data set settings).

The SDK now exposes `D2.retentionModule().purge()`, a transactional entry
point that resolves the existing `ProgramSetting`/`DataSetSetting` trimming
limits (`teiDBTrimming`, `eventsDBTrimming`, `settingDBTrimming`,
`periodDSDBTrimming`, `LimitScope`) and removes the oldest already-synced
records — by full tracker tree (TEI → Enrollment → Event) or by table for
aggregate data — that exceed those limits. Nothing in the app calls this yet.

## What Changes

- Add a periodic background trigger that invokes
  `D2.retentionModule().purge()` on a configurable schedule, following the
  existing background-job pattern already used for metadata/data sync
  (`SyncBackgroundJobAction`), plus a manual "run now" trigger.
- Surface purge status in the Sync Manager settings screen: schedule
  frequency, last run timestamp, and in-progress state — mirroring the
  existing "Last sync on" / in-progress indicators already shown for data
  and metadata sync.
- The purge *behavior* (what gets removed and how much is kept) is entirely
  driven by the `ProgramSetting`/`DataSetSetting` limits already synced from
  the server: the app only decides *when* the call runs, not *what* it
  purges — no per-data-type configuration is introduced on the Android side.
- Add a build-time capability flag so each flavor/fork opts in explicitly;
  disabled by default, with no flavor-specific code required to opt in
  (a single shared constant/config value).

## Capabilities

### New Capabilities
- `synced-data-retention-purge`: scheduling and surfacing of the SDK-driven
  retention purge (`D2.retentionModule().purge()`) from the app — trigger
  policy, status visibility, and per-fork enablement. Does not define the
  purge/retention logic itself (that behavior is specified and owned by the
  SDK's own `synced-data-retention-purge` capability).

### Modified Capabilities
(none — this is a new, additive capability; it does not change the behavior
of existing sync or settings capabilities, only adds a new triggered action
and a new status row)

## Impact

- Affected modules: `sync/` (KMP module: background job scheduling,
  repository), `app/` (Settings → Sync Manager screen, status wiring).
- All changes are additive and live in shared code (`sync/src/commonMain`,
  `sync/src/androidMain`, `app/src/main`) — none of it belongs in, or
  requires, any flavor source set (`app/src/oca/` or any other client
  flavor). This is a deliberate constraint: the capability is designed to be
  generic enough to propose upstream, gated only by a shared, fork-agnostic
  enablement flag.
- Dependency: requires the SDK version that publishes
  `D2.retentionModule().purge()` (`RetentionModule`, EyeSeeTea SDK fork).
- No database schema changes in the app; no new user-facing settings screen
  beyond a read-only status row on an existing screen.
