# Sports — Upgrade to v3.3.1 Notes

- Client: `sports`
- Source branch: `develop-eyeseetea` at `83269ca92438e7574ea69749263cf8b8270be03f`
- Target branch: `develop-sports`
- Feature branch: `feature-sports/upgrade-to-three-three-one`
- Date: 2026-03-25

## Merge Summary

- **Merge result**: Zero conflicts (auto-merged)
- **Total files changed**: 534 (merge) + 22 (manual reapply)
- **Merge base**: `64058e739`

## Conflict Resolution

No git conflicts occurred. The merge auto-resolved because:
- Sports customizations are entirely in `app/src/sports/` (untouched by eyeseetea)
- Shared-code diffs were all version drift (older code that eyeseetea evolved/removed)

## Manual Reapply (post-merge)

Two active customizations required reapplying integration hooks onto the eyeseetea codebase:

### Notifications system (18 files touched)

| File | Change |
|------|--------|
| `ActivityGlobalAbstract.java` | Implements `NotificationsView`, `@Inject NotificationsPresenter`, `renderNotifications()`, `showNotification()`, `getNotificationContent()` |
| `MainActivity.kt` | `notificationsPresenter.refresh()` on home, `markShowNotificationsAsPending()` on settings |
| `MainPresenter.kt` | Notification hooks in navigation |
| `MainView.kt` | `markShowNotificationsAsPending()`, `refreshNotifications()` interface methods |
| `SyncPresenterImpl.kt` | `NotificationRepository` injection, `syncNotifications()` on metadata sync complete |
| `SyncDataWorkerModule.kt` | `NotificationRepository` pass-through |
| `SyncInitWorkerModule.kt` | `NotificationRepository` pass-through |
| `SyncMetadataWorkerModule.kt` | `NotificationRepository` pass-through |
| `SyncGranularRxModule.kt` | `NotificationRepository` pass-through |
| `App.java` | `NotificationsModule` wiring, `ChangeServerURLComponent` creation |
| `AppComponent.java` | `NotificationsModule` in `@Component` modules |
| `Preference.kt` | `NOTIFICATIONS` constant |
| `PreferenceProvider.kt` | `updateServerURL()` interface method |
| `PreferenceProviderImpl.kt` | `updateServerURL()` implementation |
| `PreferenceModule.kt` | `BasicPreferenceProvider` DI binding |
| `PreferenceConstants.kt` | `BASIC_SHARE_PREFS` constant |
| `build.gradle.kts` | markwon dependency |
| `libs.versions.toml` | markwon version entry |

### Change server URL dialog (6 files touched, overlapping with above)

| File | Change |
|------|--------|
| `App.java` | `ChangeServerURLComponent` creation |
| `UserComponent.java` | `extends UserComponentFlavor`, `plus(ChangeServerURLModule)` |
| `MainActivity.kt` | `onChangeServerURL()`, `isChangeServerURLVisible` |
| `PreferenceProvider.kt` | `updateServerURL()` |
| `PreferenceProviderImpl.kt` | `updateServerURL()` impl |
| `ids.xml` (new) | `change_url` id for cross-flavor compilation |

### Restored files (from develop-sports)

- `app/src/main/java/org/dhis2/data/notifications/` (4 files)
- `app/src/main/java/org/dhis2/usescases/notifications/` (10 files)
- `app/src/main/java/org/dhis2/utils/session/ChangeServer*.kt` (4 files)
- `app/src/main/res/layout/dialog_change_server_url.xml`
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProvider.kt`
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProviderImpl.kt`

### New files created

- `app/src/dhis2/java/org/dhis2/data/user/UserComponentFlavor.kt` (empty interface stub)
- `app/src/dhis2PlayServices/java/org/dhis2/data/user/UserComponentFlavor.kt`
- `app/src/dhis2Training/java/org/dhis2/data/user/UserComponentFlavor.kt`
- `app/src/eyeseetea/java/org/dhis2/data/user/UserComponentFlavor.kt`
- `app/src/main/res/values/ids.xml` (change_url id)
- `app/src/main/res/values/strings.xml` (change_server_url strings)

## Removed

- `atv` (AndroidTreeView) dependency — PSI-only, not used by sports
- `eyeseetea-coroutinesCore` / `eyeseetea-coroutinesAndroid` — redundant catalog entries

## Build Validation

| Check | Result |
|-------|--------|
| `assembleSportsDebug` | PASS |
| `assembleDhis2Debug` | PASS |
| `testSportsDebugUnitTest` | 868 passed, 0 failed |

## Tests Added

- `GetNotificationsTest.kt` — 4 tests
- `MarkNotificationAsReadTest.kt` — 4 tests
- `NotificationsPresenterTest.kt` — 5 tests
- `ChangeServerURLPresenterTest.kt` — 7 tests

## Remaining Drift

~157 files differ in shared modules due to version drift (SDK API changes, import ordering, UI refactoring, string resource locales). These are NOT customizations and will converge on the next rebase/merge cycle.
