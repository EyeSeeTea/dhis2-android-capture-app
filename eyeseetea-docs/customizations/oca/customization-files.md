# OCA customization files vs develop-eyeseetea

Technical inventory of the OCA customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `oca`
- Flavor: `oca`
- Base branch: `develop-eyeseetea`
- Base commit: `f87bec8c3`
- Generated on: `2026-09-10`
- Working tree status: `clean`

## Scope

This inventory is based on:
- direct flavor files under `app/src/oca/` and `app/src/ocaDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

This file is not a full raw diff dump. Its goal is to answer:
- which confirmed functional customizations exist for OCA
- where they are implemented
- what their current technical status is

## Validated customization count

**1 confirmed functional customization.** The `oca` flavor exists (product flavor, `applicationId`, branding, and the boilerplate DI/extension-point files every flavor must carry), and none of it diverges functionally from `develop-eyeseetea` — see §1 below for the full flavor surface and why each file is boilerplate, not a customization. The one confirmed functional customization, Synced Data Retention Purge, is entirely shared-code — see §2.1.

## 1. Direct OCA flavor surface

### 1.1 OCA flavor code

All three files were created together in commit `89a61f78b "[EyeSeeTea] Add OCA flavor"`. None carries OCA-specific business logic — verified byte-for-byte against other flavors:

- `app/src/oca/java/org/dhis2/di/PostMetadataSyncModule.kt` — identical to `eyeseetea`'s (empty `module { }`). Required boilerplate: every flavor source set must carry this file so `KoinInitialization` can register the `PostMetadataSyncAction` extension point unconditionally (see `eyeseetea-docs/customization-techniques.md` — T2). OCA registers no actions.
- `app/src/oca/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — identical to `dhis2`'s (byte-for-byte, save for the license header). Required Dagger DI boilerplate every flavor must provide.
- `app/src/oca/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` — was copied from `dhis2` (no-Play-Services pattern: `download()`/`DownloadMethod.File`) at flavor creation, but OCA is published to Google Play. Corrected in commit `57234a04a "fix: use Play Store update flow for oca flavor"` to match `dhis2PlayServices`'s pattern (`getUrl()`/`DownloadMethod.Url`) — this is baseline distribution-driven behavior, not an OCA customization. See `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` §1.1 for the criterion.

### 1.2 OCA flavor resources and branding

- `app/src/oca/res/values*/strings.xml` — `app_name` and localized strings (10 locale variants)
- `app/src/ocaDebug/res/values*/strings.xml` — debug build-type string overrides (9 locale variants)

## 2. Shared-code customization implementation points

### 2.1 Synced Data Retention Purge

Spec: `openspec/specs/synced-data-retention-purge/spec.md`. Lets the app periodically shrink its local database by purging already-synced records via the SDK's `D2.retentionModule().purge()`, independent of the data/metadata sync schedule. Entirely shared code — no `app/src/oca/` files involved. Enabled on this fork by `RetentionPurgeCapability.IS_ENABLED = true` (see `sync/.../RetentionPurgeCapability.kt`); if a future fork needs it disabled, the baseline default should move back to `false` with an explicit per-fork override point (see that change's `design.md` "Decisions").

**New shared files:**

- `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/RetentionPurgeCapability.kt` — capability flag, `IS_ENABLED = true` on this fork
- `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/domain/RetentionPurge.kt` — use case, delegates to `SyncRepository.purgeRetention()`
- `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/data/RetentionPurgeWorker.kt` — `CoroutineWorker` invoking `RetentionPurge`
- `app/src/main/java/org/dhis2/usescases/settings/models/RetentionPurgeSettingsViewModel.kt` — Settings screen view model (period, last purge date, error/progress flags)
- `app/src/main/java/org/dhis2/usescases/settings/ui/RetentionPurgeSettingItem.kt` — Settings screen composable (period dropdown, "Purge Now" button, status info)

**Inline edits in existing shared files** (each marked `// EyeSeeTea customization - Synced Data Retention Purge` at the customized block):

- `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/data/SyncBackgroundJobAction.kt` — added `launchRetentionPurge`/`observeRetentionPurgeJob`/`cancelRetentionPurge` to the interface
- `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/data/AndroidSyncBackgroundJobAction.kt` — WorkManager implementation of the above (`RETENTION_PURGE`/`RETENTION_PURGE_NOW` tags, one-time vs. periodic `WorkRequest` pattern)
- `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/data/SyncRepository.kt` — added `purgeRetention(): Result<Unit>` to the interface
- `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/data/AndroidSyncRepository.kt` — implementation: calls `d2.retentionModule().purge()`, persists last-purge timestamp and outcome to preferences
- `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/di/SyncModule.android.kt` — Koin registration (`RetentionPurge` factory, `RetentionPurgeWorker` worker)
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/providers/PreferenceConstants.kt` — `LAST_RETENTION_PURGE`, `TIME_RETENTION_PURGE`, `LAST_RETENTION_PURGE_STATUS` preference keys
- `app/src/main/java/org/dhis2/usescases/settings/SettingsRepository.kt` — `retentionPurge(): Single<RetentionPurgeSettingsViewModel>`, reads the new preference keys
- `app/src/main/java/org/dhis2/usescases/settings/domain/LaunchSync.kt` — `SyncAction.PurgeRetentionNow`/`UpdateRetentionPurgePeriod`, third `retentionPurgeWorkInfo` flow merged into `syncWorkInfo`, `hasSyncFinished` extended with a third parameter
- `app/src/main/java/org/dhis2/usescases/settings/models/SettingsState.kt` — `retentionPurgeSettingsViewModel` field, `canInitRetentionPurge()`
- `app/src/main/java/org/dhis2/usescases/settings/models/SyncStateInput.kt` — `retentionPurgeInProgress` field
- `app/src/main/java/org/dhis2/usescases/settings/domain/GetSettingsState.kt` — wires `settingsRepository.retentionPurge()` into `SettingsState`
- `app/src/main/java/org/dhis2/usescases/settings/SyncManagerPresenter.kt` — `purgeRetentionNow()`, `onRetentionPurgePeriodChanged()`, wires the third work-info flow
- `app/src/main/java/org/dhis2/usescases/settings/SettingItem.kt` — `RETENTION_PURGE` enum entry
- `app/src/main/java/org/dhis2/usescases/settings/models/SettingsUiAction.kt` — `PurgeRetentionNow`, `OnRetentionPurgePeriodChanged`
- `app/src/main/java/org/dhis2/usescases/settings/ui/SettingsScreen.kt` — renders `RetentionPurgeSettingItem` gated by `RetentionPurgeCapability.IS_ENABLED`
- `app/src/main/res/values/strings.xml` — `PURGE_RETENTION`, `last_retention_purge`, `purging_retention_data`, `retention_purge_error_text`, `settingsRetentionPurge`

**Known trade-offs (accepted, not blocking):**

- No granular progress callback during the purge itself — the SDK does not expose intermediate progress for `retentionModule().purge()`, so the UI only shows enqueued/running/finished, not a percentage.
- No stacktrace logging on purge failure — unlike `saveDataSyncError` for data sync, only a success/failure boolean is persisted (`LAST_RETENTION_PURGE_STATUS`).
- The instrumented UI test (`SettingsTest.shouldShowRetentionPurgeOptionWithEditablePeriod`) could not be run in this environment due to a preexisting `androidx.concurrent` version conflict in `androidTest` dependencies, unrelated to this customization.

## 3. Shared drift still differing

Empty — no unclassified diffs against `develop-eyeseetea` as of this writing.

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
