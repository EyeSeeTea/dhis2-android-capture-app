# Sports — Customization Files

- Client: `sports`
- Flavor: `sports`
- Base branch: `develop-eyeseetea`
- Base commit: `83269ca92438e7574ea69749263cf8b8270be03f`
- Generated on: `2026-03-25`
- Working tree status: `dirty`

## Purpose

This file is for:
- listing where each confirmed client customization is implemented
- separating direct flavor files from shared-code implementation points
- tracking the technical status of each customization against `develop-eyeseetea`

This file is not for:
- raw full diff dumps
- temporary upgrade progress
- stable merge rules
- functional intent or business justification

## 1. Direct sports flavor surface

### 1.1 Flavor code

- `app/src/sports/java/org/dhis2/utils/CustomizableConstants.kt` — default URL (empty string, no pre-fill)
- `app/src/sports/java/org/dhis2/data/user/UserComponentFlavor.kt` — required empty interface stub
- `app/src/sports/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureRepositoryFunctions.kt` — program stage name query
- `app/src/sports/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — Dagger module for granular sync DI wiring

### 1.2 Flavor resources and branding

- `app/src/sports/res/mipmap-*/ic_launcher.webp` — launcher icons (all density buckets)
- `app/src/sports/res/values-*/strings.xml` — `app_name` string in ~30 locales
- `app/src/sports/res/values/strings.xml` — default locale app name
- `app/src/sportsDebug/res/mipmap-*/ic_launcher.webp` — debug launcher icons

## 2. Shared-code customization implementation points

### 2.1 Notifications system

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/notifications/NotificationRepository.kt`
- `app/src/main/java/org/dhis2/data/notifications/NotificationRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/notifications/Notification.kt`
- `app/src/main/java/org/dhis2/data/notifications/NotificationMapper.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/NotificationsFragment.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/NotificationsPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/NotificationsView.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/NotificationsAdapter.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/NotificationViewHolder.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` — implements `NotificationsView`, renders Markdown via markwon
- `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` — integrates notification loading
- `app/src/main/java/org/dhis2/usescases/main/MainView.kt` — notification display interface
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — notification UI wiring
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — notification sync trigger
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProvider.kt` — preferences used by notifications
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProviderImpl.kt`
- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — `NOTIFICATIONS` constant
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProvider.kt` — `updateServerURL()` method
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProviderImpl.kt`

Technical note:
- This is the largest shared-code customization. It touches multiple layers (data, presentation, sync). During upgrade, files modified by both branches will need `manual_reapply_on_theirs` resolution — start from develop-eyeseetea and reinsert notification hooks.
- The markwon dependency (`io.noties.markwon:core`) is required for Markdown rendering in notifications.

### 2.2 Change server URL dialog

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt`
- `app/src/main/res/layout/dialog_change_server_url.xml`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — dialog launch point
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProvider.kt` — `updateServerURL()` method
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProviderImpl.kt`

Technical note:
- Self-contained feature with its own Dagger component. Integration into MainActivity is the main shared-code touch point.

## 3. Shared drift still differing

### 3.1 PSI/WIDP flavor artifacts (to remove)

These files exist in `develop-sports` because the branch was forked from a state that included other client flavors. They are not sports customizations and should be removed during the upgrade:

- `app/src/psi/**` — entire PSI flavor (~50 files)
- `app/src/widp/**` — entire WIDP flavor (~40 files)
- `app/src/simprints/**` — entire Simprints flavor (~10 files)
- `app/src/psiDebug/**`, `app/src/widpDebug/**` — debug variants

Note: These will be automatically resolved by accepting `develop-eyeseetea` during the merge (the eyeseetea branch does not contain these flavors either, or has its own cleanup).

### 3.2 PSI-only dependency (to remove)

- `atv` (`com.github.bmelnychuk:atv`) — AndroidTreeView library, only used in `app/src/psi/`. Remove from `build.gradle.kts` and `libs.versions.toml`.

### 3.3 Version drift (~300 files)

Not customizations. These are older versions of shared code that will be resolved by accepting `develop-eyeseetea` during the merge:
- SDK version, Gradle version, Kotlin version, design system version
- Missing `sync` module (new in eyeseetea)
- Missing Sentry plugin, CycloneDX plugin, lottie-compose dependency

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles remains `customization-specs.md`.
- If code comments and functional titles diverge, prefer the title defined in `customization-specs.md` and update the code comment when possible.
