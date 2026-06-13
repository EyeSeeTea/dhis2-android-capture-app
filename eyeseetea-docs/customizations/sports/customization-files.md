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

### 2.3 Bounded TEI dashboard event list

Status: `active`

Marker: `// EyeSeeTea customization - bounded TEI event list`

Main implementation points:
- `commons/src/main/java/org/dhis2/commons/data/StageSection.kt` — `revealedEventCount` field, `EVENTS_PAGE_SIZE`, `visibleEventCount()` helper
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TeiDataRepositoryImpl.kt` — visible-window computation in `getGroupedEvents` / `getTimelineEvents`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/teievents/ToggleStageEventsButtonHolder.kt` — paged "show more (N remaining)" / "show less"
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/teievents/StageViewHolder.kt` — `StageSection` construction
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt` — paging-aware stage selection map
- `app/src/main/res/values/strings.xml` — `show_more_events_paged`

Tests:
- `commons/src/test/kotlin/org/dhis2/commons/data/StageSectionTest.kt`

### 2.4 Rule engine bulk context

Status: `active`

Marker: `// EyeSeeTea customization - rule engine bulk context`

Main implementation points:
- `dhis2-mobile-program-rules/src/main/java/org/dhis2/mobileProgramRules/RulesRepository.kt` — `programStageNamesByUid` / `orgUnitCodesByUid` bulk lookups in `enrollmentEvents` and `otherEvents`
- `dhis2-mobile-program-rules/src/main/java/org/dhis2/mobileProgramRules/RuleEngineHelper.kt` — context-build mutex, split `refreshContext`/`refreshTarget` flags
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt` — context refresh moved to `init()` and `fetchEvents()` (mutation hook), removed from the per-emission pipeline

Tests:
- `dhis2-mobile-program-rules/src/test/java/org/dhis2/mobileProgramRules/RuleEngineHelperTest.kt` — context reuse/rebuild
- `dhis2-mobile-program-rules/src/test/java/org/dhis2/mobileProgramRules/RulesRepositoryTest.kt` — bulk lookups

## 3. Shared drift still differing

### 3.1 PSI/WIDP flavor artifacts — resolved

These files have been removed from the tracked tree during the upgrade merge. Untracked leftover directories (`app/psi/`, `app/widp/`, `app/simprints/`) may remain on disk from prior checkouts but are not part of this branch.

### 3.2 PSI-only dependency — resolved

The `atv` (`com.github.bmelnychuk:atv`) dependency has been removed from both `build.gradle.kts` and `libs.versions.toml`. No references remain in tracked code.

### 3.3 Version drift (~157 files in shared modules)

Not customizations. Remaining differences between `origin/develop-eyeseetea` and HEAD in shared modules (`app/src/main/`, `commons/`, `form/`, `tracker/`, `aggregates/`) include:
- SDK API differences (e.g., `categoryComboUid()` vs `categoryCombo()?.uid()`)
- Import ordering differences
- UI code that eyeseetea refactored after the merge base (e.g., `SyncManagerFragment`, `SettingsScreen`)
- String resource drift in multiple locales
- Minor Compose and form module differences

These are expected and will converge as the branch is rebased or merged forward. None represent intentional sports customizations.

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles remains `customization-specs.md`.
- If code comments and functional titles diverge, prefer the title defined in `customization-specs.md` and update the code comment when possible.
