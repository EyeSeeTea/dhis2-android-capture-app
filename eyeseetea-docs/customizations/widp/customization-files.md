# WIDP customization files vs develop-eyeseetea

Technical inventory of the WIDP customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `widp`
- Flavor: `widp`
- Base branch: `develop-eyeseetea`
- Base commit: `b1e8cdb9b`
- Generated on: `2026-03-25`
- Last updated: `2026-04-17`
- Working tree status: `clean` (post-merge to 3.3.1, build OK)

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch
- this file documents the WIDP-specific implementation points that still survive in code

## Scope

This inventory is based on:
- direct flavor files under `app/src/widp/` and `app/src/widpDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- two-dot diff (`git diff develop-eyeseetea HEAD`) used as supporting evidence
- verified against code on 2026-04-02

This file is not a full raw diff dump. Its goal is to answer:
- which confirmed functional customizations still exist
- where they are implemented
- what their current technical status is

## Validated customization count

Originally 8 customizations were assumed. After verification against `develop-eyeseetea`:
- **5 confirmed, all active** (3 active, 1 active with SDK dependency, 1 reimplemented in 3.3.1)
- **3 removed**: #4 Notification translations (merged into #3), #5 Access to indicators from form (exists in baseline since 2019), #6 Events filter for text-type data elements (no diff found, never existed or was removed)

## 1. Direct WIDP flavor surface

### 1.1 WIDP flavor code

- `app/src/widp/java/org.dhis2.utils/CustomizableConstants.kt`
- `app/src/widp/java/org.dhis2.utils/granularsync/GranularSyncModule.kt`
- `app/src/widp/java/org/dhis2/data/user/UserComponentFlavor.kt`

### 1.2 WIDP flavor resources and branding

- `app/src/widp/` (google-services.json, launcher icons, menu, strings)
- `app/src/widpDebug/` (google-services.json, launcher icons, strings)
- `app/src/widpRelease/` (google-services.json, launcher icons)

## 2. Shared-code customization implementation points

### 2.1 Change Server URL

Status: `active`

Main implementation points (all new files, not in develop-eyeseetea):
- `app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt` — DialogFragment UI
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt` — logic (validate, save, update credentials)
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt` — Dagger DI module
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt` — Dagger subcomponent
- `app/src/main/res/layout/dialog_change_server_url.xml` — layout

Supporting files:
- `app/src/widp/res/menu/main_menu.xml` (menu entry for change URL)

DI wiring (shared files with WIDP-only bindings):
- `app/src/main/java/org/dhis2/App.java` — `ChangeServerURLComponent` holder + plus(ChangeServerURLModule)
- `app/src/main/java/org/dhis2/data/user/UserComponent.java` — `plus(ChangeServerURLModule)` subcomponent
- `app/src/main/res/values/strings.xml` — change server URL strings

### 2.2 Image upload without resizing

Status: `active`

Main implementation points:
- `form/src/main/java/org/dhis2/form/data/FormValueStore.kt` (line ~274)

Technical note:
- Marked with `// EyeSeeTea customization no resize`. Changes `saveFileResource(filePath, valueType == ValueType.IMAGE)` to `saveFileResource(filePath, false)`, disabling resize for all image uploads.

### 2.3 Notifications system

Status: `active`

Data layer (all new files):
- `app/src/main/java/org/dhis2/data/notifications/NotificationD2Repository.kt` — repository with sync, filtering, read/write logic
- `app/src/main/java/org/dhis2/data/notifications/NotificationsApi.kt` — HTTP client for `dataStore/notifications/notifications`
- `app/src/main/java/org/dhis2/data/notifications/NotificationDTO.kt` — DTOs (NotificationDTO, ReadByDTO, RecipientsDTO, RefDTO, UserGroupsDTO, PermissionsDTO)
- `app/src/main/java/org/dhis2/data/notifications/UserD2Repository.kt` — current user provider

Domain layer (all new files):
- `app/src/main/java/org/dhis2/usescases/notifications/domain/Notification.kt` — domain models (Notification, ReadBy, Recipients, Ref, UserGroups, Permissions)
- `app/src/main/java/org/dhis2/usescases/notifications/domain/NotificationRepository.kt` — repository interface
- `app/src/main/java/org/dhis2/usescases/notifications/domain/GetNotifications.kt` — use case
- `app/src/main/java/org/dhis2/usescases/notifications/domain/MarkNotificationAsRead.kt` — use case
- `app/src/main/java/org/dhis2/usescases/notifications/domain/User.kt` — user model
- `app/src/main/java/org/dhis2/usescases/notifications/domain/UserRepository.kt` — user repository interface

Presentation layer (all new files):
- `app/src/main/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenter.kt` — presenter + ShowNotifications singleton
- `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsModule.kt` — Dagger DI module
- `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsComponent.kt` — commented out subcomponent

UI integration (modified existing files):
- `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` — implements NotificationsView, renders dialogs, handles translations via `getNotificationContent()`, Markwon rendering
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — calls `syncNotifications()` during metadata sync

DI wiring (shared files with WIDP-only bindings):
- `app/src/main/java/org/dhis2/App.java` — NotificationsModule instantiation in component builder
- `app/src/main/java/org/dhis2/AppComponent.java` — NotificationsModule in `@Component(modules)` + Builder method
- `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` — `notificationRepository` parameter
- `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` — `notificationRepository` parameter
- `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` — `notificationRepository` parameter
- `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` — `notificationRepository` parameter

SharedPreferences layer (all WIDP additions on top of commons):
- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — `NOTIFICATIONS` key
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProvider.kt` — interface (WIDP-only file)
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProviderImpl.kt` — impl (WIDP-only file)
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceModule.kt` — provides BasicPreferenceProvider
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProvider.kt` — interface methods for notifications
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProviderImpl.kt` — impl of notifications methods
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/providers/PreferenceConstants.kt` — `BASIC_SHARE_PREFS` constant

Build config:
- `commons/build.gradle.kts` — Markwon dependency

Tests:
- `app/src/test/java/org/dhis2/data/notifications/NotificationD2RepositoryTest.kt`

### 2.4 2FA support

Status: `active`

New files (not in develop-eyeseetea):
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt` — sealed class (TotpVerification, EmailVerification, SmsVerification) + TwoFactorType enum
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt` — exception with type and message

Modified files (EyeSeeTea customization markers):
- `login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt` — `handleTwoFactorError()` method (lines ~426-501), 2FA detection at login (line ~107)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt` — 2FA state management, resend logic, error/info message handling (lines ~255, ~269, ~287, ~433-531)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt` — TwoFactorContainer composable (lines ~212, ~507, ~773-896)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt` — 2FA fields (line ~18)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/LoginResult.kt` — TwoFactorError sealed variant (line ~14)
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt` — error messages for 2FA codes (lines ~227-256)
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/error/DomainErrorMapper.kt` — 2FA error code mapping
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepository.kt` — interface changes

SDK patch (in dhis2-android-sdk EyeSeeTea fork):
- `core/.../user/internal/LogInCall.kt` — `generate2FAErrorIfRequired()` (lines ~260-302)
- `core/.../user/internal/LoginPayload.kt` — `twoFactorCode: String?` parameter
- `core/.../maintenance/D2ErrorCode.java` — new error codes (lines ~96-104)

SDK wiring (build config — present only because of the SDK fork dependency):
- `settings.gradle.kts` — includeBuild of `dhis2-android-sdk` and `dhis2-rule-engine`
- `commons/build.gradle.kts` — SDK dependency pinned to EyeSeeTea fork
- `login/build.gradle.kts` — `widp` flavor declaration (needed for `TwoFASettingsActivity` to resolve)
- `app/build.gradle.kts` — `widp` product flavor declaration

Strings:
- `commonskmm/src/commonMain/composeResources/values/strings.xml` — 2FA string resources

### 2.5 URL data element field

Status: `active` — rendering reimplemented 2026-04-17 in `FieldUiModelExtensions.supportingText()` (appends URL to the description line in the Compose supporting text)

Original commit: `c556b7ab7` ("Implement show data element url", Nov 2022)

Data plumbing:
- `form/src/main/java/org/dhis2/form/data/EventRepository.kt` (line ~729) — reads `de?.url()` and passes to factory
- `form/src/main/java/org/dhis2/form/model/FieldUiModel.kt` (line ~74) — `val url: String?`
- `form/src/main/java/org/dhis2/form/model/FieldUiModelImpl.kt` — `override val url: String? = null`
- `form/src/main/java/org/dhis2/form/model/SectionUiModelImpl.kt` — `url` positional parameter in constructor
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactory.kt` — `url` parameter in interface
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactoryImpl.kt` — passes `url` through to model
- `form/src/main/java/org/dhis2/form/data/EnrollmentRepository.kt` — passes `url = null` (not applicable to enrollments)

Rendering (reimplemented 2026-04-17):
- `form/src/main/java/org/dhis2/form/extensions/FieldUiModelExtensions.kt` — `supportingText()` appends URL to description via `listOfNotNull(description, url).joinToString("\n")`. URL shows inline under the field in every Compose input instead of the legacy dialog.

## 3. Removed customizations (verified 2026-04-02)

### Notification translations (originally #4)
- **Merged into #3**: translations are an integral part of the notifications system, not a separate customization. The `translations: Map<String, String>?` field and locale resolution are documented within customization #3.

### Access to indicators from the form (originally #6)
- **Not a WIDP customization**: exists in `develop-eyeseetea` since May 2019 (commit `949911e22`). Zero diff between branches for indicator-related files.

### Events filter for text-type data elements (originally #7)
- **Does not exist in code**: zero diff between branches for filter/event-related files. No EyeSeeTea customization comments found. Was either never implemented or was removed before 3.3.0.1.

## 4. Notes

- This inventory reflects the verified branch state as of 2026-04-17 (post-merge to 3.3.1).
- The DI wiring, preferences layer, SDK wiring, and build config files listed per customization were added in the 2026-04-17 update after the upgrade merge revealed them as silent automerge casualties. They were missing from the original 2026-04-02 inventory, which focused on functional code only.
- If files are merged, renamed, reverted, or reworked, regenerate this file from the current code and the diff against `develop-eyeseetea`.
- The source of truth for functional behavior and canonical titles is `openspec/specs/<capability>/spec.md`. Each spec starts with `# <Title>`; that `<Title>` is the exact string to use here and in code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
