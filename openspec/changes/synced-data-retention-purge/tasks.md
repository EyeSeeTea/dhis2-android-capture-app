## 1. Capability flag

- [x] 1.1 Add a behavior-level test asserting the retention purge capability
  is disabled when the shared build-time flag is off, and verify it fails
  (red) before any flag exists.
- [x] 1.2 Add the shared, build-time capability flag (off by default, no
  flavor-specific file) and make the test from 1.1 pass.
  **Commit:** 1.1 + 1.2 are one commit (red -> green).

## 2. Background job: schedule and manual trigger

No unit test for the scheduling/trigger wiring itself: the equivalent
existing code (`launchDataSync`, `launchMetadataSync`, `observeDataJob` in
`AndroidSyncBackgroundJobAction`) has no unit test coverage either — it
depends on a real `WorkManager`, and this project has no established pattern
for testing it (`androidx.work:work-testing` was evaluated and reverted:
running `sync`'s instrumented tests standalone for the first time surfaced
several unrelated pre-existing infra gaps in that module — resource merge
conflicts, core library desugaring, a Sentry auto-init crash — out of scope
for this change). This group follows the same as-is convention as the rest
of the file it extends.

- [x] 2.1 Add `launchRetentionPurge(purgingPeriod: Long)` to
  `SyncBackgroundJobAction`, implemented in
  `AndroidSyncBackgroundJobAction` following the exact pattern of
  `launchDataSync` (periodic `WorkManager` request with the given interval,
  unique work name), and verify it compiles and the module's existing test
  suite still passes.
  **Commit:** its own commit (no preceding test, matching the existing
  convention for this class).
- [x] 2.2 Add a manual "run now" trigger following the same
  `launchDataSync`/period-`0` pattern, and verify it compiles and the
  module's existing test suite still passes.
  **Commit:** its own commit.
- [x] 2.3 Double-run guard already in place: `launchRetentionPurge`'s
  manual-trigger branch (task 2.2) uses `ExistingWorkPolicy.KEEP`, the same
  mechanism `launchMetadataSync`/`launchDataSync` already rely on — no
  additional code needed.
  **Commit:** none (covered by 2.2's commit).

## 3. Background job: invoke the SDK purge

- [x] 3.1 Add a test asserting that the `RetentionPurge` use case invokes
  the repository's retention purge and reports success/failure accordingly,
  and verify it fails before the use case exists.
- [x] 3.2 Implement `RetentionPurge` (use case) and
  `SyncRepository.purgeRetention()` (calling `D2.retentionModule().purge()`
  in the Android implementation), wire the use case into
  `RetentionPurgeWorker.doWork()` and register both in DI, and make the
  test from 3.1 pass.
  **Commit:** 3.1 + 3.2 are one commit (red -> green). Verified locally
  against the real SDK (`dhis2.useLocalSdk=true`) since
  `D2.retentionModule()` is not yet published to JitPack.
- [x] 3.3 Add tests asserting that a purge attempt persists a "last purge"
  timestamp and a success/failure status both when the SDK call succeeds
  and when it fails (`spec.md`: "completes, successfully or not" / "Last
  purge outcome" — mirrors the existing
  `LAST_DATA_SYNC`/`LAST_DATA_SYNC_STATUS` pattern used by data sync), and
  verify they fail before that persistence exists.
- [x] 3.4 Implement persistence of the last-purge timestamp and outcome in
  `AndroidSyncRepository.purgeRetention()` (both written unconditionally
  after the SDK call resolves, regardless of outcome), and make the tests
  from 3.3 pass.
  **Commit:** 3.3 + 3.4 are one commit (red -> green). Verified locally
  against the real SDK, same as 3.1/3.2.

## 4. Settings UI: schedule, manual trigger, status

Built bottom-up: state/data wiring first, UI last, each sub-step its own
commit.

- [x] 4.1a Add `RetentionPurgeSettingsViewModel` (period, last purge
  timestamp, has-errors, in-progress) and `SettingsRepository.retentionPurge()`
  reading the `LAST_RETENTION_PURGE`/`LAST_RETENTION_PURGE_STATUS`/
  `TIME_RETENTION_PURGE` preferences (mirrors `dataSync()`), with tests
  asserting the returned view model for both a successful and a failed
  last attempt, and verify they compile/pass.
  **Commit:** its own commit (mirrors an existing pattern, no red->green
  needed for a pure data-mapping addition — tests written alongside).
- [x] 4.1b Wire retention purge into `SettingsState`, `SyncStateInput`,
  `GetSettingsState`, `LaunchSync` (schedule/manual-trigger actions,
  observed job status via new `SyncBackgroundJobAction.observeRetentionPurgeJob()`/
  `cancelRetentionPurge()`) and `SyncManagerPresenter`, following the exact
  shape already used for data sync, with tests for the new
  `LaunchSync.SyncAction` branches.
  **Commit:** its own commit. Verified locally against the real SDK (same
  AGP/local-SDK setup as 3.x) since `sync`/`app` depend on
  `dhis2-android-sdk`.
- [x] 4.1c Add the `RetentionPurgeSettingItem` composable (frequency
  dropdown, "run now" button, last-run/status info items) and wire it into
  `SettingsScreen`/`SettingItem` enum/`SettingsUiAction`. Added an
  instrumented test (`SettingsTest.shouldShowRetentionPurgeOptionWithEditablePeriod`,
  `SettingsRobot.kt` pattern) but could not run/verify it in this
  environment — see `design.md` risk note (pre-existing
  `androidx.concurrent` dependency conflict in `app`'s
  `androidTestCompileClasspath`, unrelated to this change). Verified via
  `ktlintCheck` and the full `app` unit test suite instead.
  **Commit:** its own commit (UI wiring, no pre-existing behavior to test
  first against).
- [ ] 4.2 Add a test asserting the purge row is hidden/absent when the
  capability flag from group 1 is disabled, and verify it fails before the
  conditional rendering exists.
- [ ] 4.3 Implement the conditional rendering gated by the capability flag,
  and make the test from 4.2 pass.
  **Commit:** 4.2 + 4.3 are one commit (red -> green).

## 5. Documentation

- [ ] 5.1 Update `eyeseetea-docs/` customization-files inventories for each
  fork that enables the capability, to list the new shared files this
  change adds/touches (background job wiring, settings screen row,
  capability flag) so the fork's technical file-level inventory stays
  accurate.
- [ ] 5.2 Add a manual-validation entry (schedule change, manual trigger,
  status visibility, capability disabled by default) to each enabling
  fork's `upgrade-validation-checklist.md`.
  **Commit:** 5.1 + 5.2 together as one documentation commit, after
  implementation is verified.
