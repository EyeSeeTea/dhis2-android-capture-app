# WIDP customization files vs develop-eyeseetea

Technical inventory of the WIDP customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `widp`
- Flavor: `widp`
- Base branch: `develop-eyeseetea`
- Base commit: `b1e8cdb9b`
- Generated on: `2026-03-25`
- Last updated: `2026-08-14`
- Working tree status: `clean` (post-merge to **3.4.1**, build OK, ktlint OK, unit tests OK).
  Manual validation still outstanding — see
  `eyeseetea-docs/upgrade/widp/upgrade-validation-checklist.md`.
- Upstream version: `3.4.1` / `3.4.1-widp-fork-1`, vCode 156, SDK `1.14.1-eyeseetea-fork-1`

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

- `app/src/widp/java/org/dhis2/utils/CustomizableConstants.kt`
- `app/src/widp/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — flavor-scoped Dagger module. Carries **no** client-specific logic; it must simply track the canonical version in `app/src/dhis2/java/…`
- `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` — required from 3.4: upstream ships this use case per flavor and provides it only for its own flavors
- `app/src/widp/java/org/dhis2/data/user/UserComponentFlavor.kt`

> The malformed path `app/src/widp/java/org.dhis2.utils/` (dots instead of directory separators)
> was corrected during the 3.4.1 upgrade.

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
- `app/src/main/java/org/dhis2/App.kt` — `changeServerURLComponent` holder, `createChangeServerULComponent()` and `releaseChangeServerURLComponent()` (was `App.java` until 3.4.1)
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — opens the change-server dialog from the WIDP main menu action
- `app/src/main/java/org/dhis2/data/user/UserComponent.java` — `plus(ChangeServerURLModule)` subcomponent
- `app/src/main/res/values/strings.xml` — change server URL strings, **plus `url_hint` and `login_https`**, which upstream deleted in 3.4.1 and the dialog layout still needs
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProvider.kt` — preference API used to persist the overridden server URL
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProviderImpl.kt` — preference implementation used by the presenter

### 2.2 Image upload without resizing

Status: `active`

Main implementation points:
- `form/src/main/java/org/dhis2/form/data/FormValueStore.kt` (line ~274)
- `app/src/main/java/org/dhis2/data/forms/dataentry/ValueStoreImpl.kt` — legacy pre-Compose value-store implementation from the original feature commit history
- `app/src/main/java/org/dhis2/data/server/ServerModule.kt` — historical SDK/server wiring touched by the final no-resize rollout

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
- `app/src/main/java/org/dhis2/usescases/notifications/domain/SyncNotifications.kt` — use case (added in 3.4.1; wraps `NotificationRepository.sync()` now that the metadata-sync hook is gone)
- `app/src/main/java/org/dhis2/usescases/notifications/domain/User.kt` — user model
- `app/src/main/java/org/dhis2/usescases/notifications/domain/UserRepository.kt` — user repository interface

Presentation layer (all new files):
- `app/src/main/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenter.kt` — presenter + ShowNotifications singleton
- `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsKoinModule.kt` — Koin DI module (`notificationsModule`). **Replaced the Dagger `NotificationsModule.kt` in 3.4.1**, when upstream migrated `MainActivity` to Koin and dropped the `inject()` that populated the presenter. `NotificationsModule.kt` and the commented-out `NotificationsComponent.kt` were deleted

UI integration (modified existing files):
- `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` — implements NotificationsView, renders dialogs, handles translations via `getNotificationContent()`, Markwon rendering, triggers `getNotificationsPresenter().refresh(this)` in `onCreate` and `onResume`. Since 3.4.1 the presenter is **resolved from Koin** by `getNotificationsPresenter()` instead of being a Dagger `@Inject` field — subclasses no longer run an `inject()` that would fill it. The Java getter keeps Kotlin subclasses reading it as the `notificationsPresenter` synthetic property
- `app/src/main/java/org/dhis2/usescases/main/MainViewModel.kt` — emits `HomeEffect.SyncNotifications` when a sync finishes (`running == false`)
- `app/src/main/java/org/dhis2/usescases/main/ui/model/HomeEffect.kt` — `SyncNotifications` effect
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — handles `HomeEffect.SyncNotifications` → `notificationsPresenter.syncNotifications()`; marks pending on `HomeEffect.SingleProgramNavigation`; marks pending on `sync_manager`, and marks pending + refreshes on `menu_home`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramFragment.kt` — historical integration point, no marker
No longer part of this customization after 3.4.1 (kept here so the inventory cross-check can
resolve the paths touched by the feat commits):

- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — **no longer customized**: upstream moved metadata sync to the `:sync` module and the class no longer takes `NotificationRepository`
- `app/src/main/java/org/dhis2/usescases/main/MainView.kt` — **deleted upstream** in 3.4 (MVP → MVVM); call path ported to the effects above
- `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` — **deleted upstream** in 3.4 (MVP → MVVM); call path ported to the effects above
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramFragment.kt` — historical program-dashboard integration point used by the original on-resume notification flow
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramModule.kt` — historical wiring for the program-dashboard notification flow
- `app/src/main/java/org/dhis2/usescases/main/MainModule.kt` — historical Dagger wiring for notifications in the main screen

DI wiring (shared files with WIDP-only bindings):
- `app/src/main/java/org/dhis2/di/KoinInitialization.kt` — registers `notificationsModule` in the Koin graph. **The only Oslo DI file this customization touches since 3.4.1**
- `app/src/main/java/org/dhis2/App.kt` — **no longer customized for notifications** (still customized for Change Server URL): the Dagger `.notificationsModule(...)` builder call is gone
- `app/src/main/java/org/dhis2/AppComponent.java` — **no longer customized**: `NotificationsModule` removed from `@Component(modules)` and the Builder
- `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` — **deleted upstream** in 3.4: worker moved to the `:sync` module and is registered with Koin, leaving this Dagger module without a consumer
- `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` — **deleted upstream** in 3.4, same reason
- `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` — **deleted upstream** in 3.4, same reason
- `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` — **no longer customized** (the `notificationRepository` argument went with `SyncPresenterImpl`)

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
- `app/src/test/java/org/dhis2/usescases/notifications/di/NotificationsModuleTest.kt` — resolves the whole graph from Koin. Added in 3.4.1: Dagger failed at compile time when a binding was missing, Koin only fails at runtime, and this capability crashed the app for exactly that reason

### 2.4 2FA support

Status: `active`

> 3.4.1 note: `login/build.gradle.kts` is no longer a customization point. Upstream migrated
> `:login` to the KMP `androidLibrary {}` DSL and removed the whole `productFlavors` block for
> every flavor. The `create("widp")` entry only declared a `LOGIN_TEST` buildConfigField identical
> to the other flavors'; the 2FA implementation lives in `commonMain`/`androidMain` and is
> unaffected.

New files (not in develop-eyeseetea):
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt` — sealed class (TotpVerification, EmailVerification, SmsVerification) + TwoFactorType enum
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt` — exception with type and message

Modified files (EyeSeeTea customization markers):
- `login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt` — `handleTwoFactorError()` method (lines ~426-501), 2FA detection at login (line ~107)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt` — 2FA state management, resend logic, error/info message handling (lines ~255, ~269, ~287, ~433-531)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt` — TwoFactorContainer composable (lines ~212, ~507, ~773-896)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt` — 2FA fields (line ~18)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/LoginResult.kt` — TwoFactorError sealed variant (line ~14)
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/usecase/BaseLogin.kt` — maps repository failures into `LoginResult.TwoFactorError`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/usecase/LoginUser.kt` — login use case flow carrying the two-factor code
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt` — error messages for 2FA codes (lines ~227-256)
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/error/DomainErrorMapper.kt` — 2FA error code mapping
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/error/DomainError.kt` — shared domain error model used by the 2FA error mapping
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepository.kt` — interface changes

Legacy Android login integration still present in HEAD:
- `app/src/main/java/org/dhis2/data/server/UserManager.java` — legacy Android login/session integration touched by the original 2FA rollout
- `app/src/main/java/org/dhis2/data/server/UserManagerImpl.java` — legacy Android login/session integration touched by the original 2FA rollout

SDK patch (in dhis2-android-sdk EyeSeeTea fork):
- `core/.../user/internal/LogInCall.kt` — `generate2FAErrorIfRequired()` (lines ~260-302)
- `core/.../user/internal/LoginPayload.kt` — `twoFactorCode: String?` parameter
- `core/.../maintenance/D2ErrorCode.java` — new error codes (lines ~96-104)

SDK wiring (build config — present only because of the SDK fork dependency):
- `settings.gradle.kts` — includeBuild of `dhis2-android-sdk` and `dhis2-rule-engine`
- `commons/build.gradle.kts` — SDK dependency pinned to EyeSeeTea fork

Strings:
- `commonskmm/src/commonMain/composeResources/values/strings.xml` — 2FA string resources

### 2.5 URL data element field

Status: `active` — rendering reimplemented 2026-04-17 in `FieldUiModelExtensions.supportingText()` (appends URL to the description line in the Compose supporting text)

Original commit: `c556b7ab7` ("Implement show data element url", Nov 2022)

Data plumbing:
- `form/src/main/java/org/dhis2/form/data/EventRepository.kt` (line ~729) — reads `de?.url()` and passes to factory
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/data/EventDetailsRepository.kt` — original event-details repository path from the pre-Compose implementation history
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

## 4. Feat commits (inventory cross-check)

For each customization, these are the original feature commits that introduced its surface. Run `git show <sha> --stat` on every SHA listed and cross-check that every file appears above under the corresponding section. If a file from a feat commit is missing from the inventory, the automerge verification rule will not fire on it — a silent automerge can drop wiring with no conflict markers and no detection. Used by `check_upgrade_docs.py` for the post-merge inventory completeness check.

### 2.1 Change Server URL
Status: `active`
- `61fec4f60` — feat: show dialog to change url
- `1024c356f` — feat: show warning to change server url
- `161d8f5ae` — Execute login after server url is changed
- `384286190` — feat: Avoid login and overwrite url in preferences and accounts

### 2.2 Image upload without resizing
Status: `active`
- `5015ae0d4` — Avoid resize images
- `696de3918` — Link commit to fix avoid resize images to download in SDK
- `e248c3347` — Avoid resize images using new param in SDK

### 2.3 Notifications system
Status: `active`
- `71655b603` — feat: create infrastructure to retrieve notifications from the data store
- `27c7a4e74` — feat: show notifications from program dashboard onResume
- `9cb2558cf` — feat: invoke sync notifications when sync metadata
- `4afb24308` — feat: request userGroups to the api
- `6b16ba128` — feat: mark notification as read when the user click on ok
- `3182eaf5f` — Restore notifications feature using new httpClient in the SDK with suspend functions + Flow
- `134532f92` — Launch notifications from base activity (wiring in `MainActivity`/`MainView`/`MainPresenter`; silently dropped by 3.3.1 automerge, restored 2026-04-20)
- `0ddcaed56` — new: Align new Notification response implementations
- `39beb59d9` — Implement notification translations
- `0c8b70cd9` — Fix serialization of notifications using new SDK Http client with Ktor

### 2.4 2FA support
Status: `active`
- `87c5da010` — Remove 2factor customization (best surviving provenance anchor for the current login-challenge files; used here because it touches the exact current paths later restored in WIDP)
- `64f8e168b` — chore: D2Error to DomainError (covers the domain error mapping path used by login 2FA)
- `78afccf05` — refactor: include error when device doesn't have network available on login screen (covers the current `D2ErrorMessageProviderImpl` / `CredentialsViewModel` login path)

### 2.5 URL data element field
Status: `active`
- `c556b7ab7` — Implement show data element url (original, Nov 2022; rendering reimplemented 2026-04-17 in `FieldUiModelExtensions.supportingText()`)

## 5. Notes

### Build-level marker: "Include SDK's modules"

`settings.gradle.kts` carries `// EyeSeeTea customization - Include SDK's modules`: the
composite-build support that resolves the DHIS2 SDK either from a local checkout or from JitPack,
driven by `dhis2.useLocalSdk`. See `eyeseetea-docs/SDK_Setup.md`.

It is documented here as a note rather than as a numbered customization section on purpose:
`openspec/config.yaml` puts the build system explicitly out of OpenSpec scope, and
`check_upgrade_docs.py` requires every numbered section to have a matching spec **and** a manual
validation entry. It is also inherited from the shared EyeSeeTea baseline, not WIDP-specific.

It was undocumented until the 3.4.1 upgrade flagged it.

### 3.4.1 upgrade — files that gained a marker

These four carried customization content but no `EyeSeeTea customization` comment, so a
marker-based audit could not see them. Markers added during the 3.4.1 upgrade:

- `app/src/main/java/org/dhis2/AppComponent.java` — `NotificationsModule` in the Dagger graph
- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — `NOTIFICATIONS` key
- `app/src/test/java/org/dhis2/data/notifications/NotificationD2RepositoryTest.kt` — wholly a customization
- `app/src/main/res/layout/dialog_change_server_url.xml` — wholly a customization

### 3.4.1 upgrade — audit method correction

The root `CLAUDE.md` says to diff every inventory file against `develop-eyeseetea`. For WIDP that
diff is meaningless: the baseline carries **none** of the five customizations. The comparison that
actually detects loss is against the pre-merge client branch (`develop-widp`), counting
customization markers per file.



- This inventory reflects the verified branch state as of 2026-04-20 (post-merge to 3.3.1, with Notifications wiring restored in `MainActivity`/`MainView`/`MainPresenter`).
- The DI wiring, preferences layer, SDK wiring, and build config files listed per customization were added in the 2026-04-17 update after the upgrade merge revealed them as silent automerge casualties. They were missing from the original 2026-04-02 inventory, which focused on functional code only.
- The `MainActivity`/`MainView`/`MainPresenter` wiring under 2.3 was added 2026-04-20 after the same automerge dropped it silently during manual testing — this also drove the extension of the verification rule to all inventoried files (not only files with conflict markers).
- If files are merged, renamed, reverted, or reworked, regenerate this file from the current code and the diff against `develop-eyeseetea`.
- The source of truth for functional behavior and canonical titles is `openspec/specs/<capability>/spec.md`. Each spec starts with `# <Title>`; that `<Title>` is the exact string to use here and in code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
