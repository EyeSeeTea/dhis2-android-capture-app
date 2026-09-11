# WIDP customization files vs develop-eyeseetea

Technical inventory of the WIDP customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `widp`
- Flavor: `widp`
- Base branch: `develop-eyeseetea`
- Base commit: `8e0200bcc` (`3.4.2-eyeseetea-fork-1`)
- Generated on: `2026-03-25`
- Last updated: `2026-09-11`
- Working tree status: `clean` (post-merge to 3.4.2; both flavors build; notifications re-anchored on Koin and on the post-metadata-sync extension point)

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- that file documents the shared EyeSeeTea reference branch
- this file documents the WIDP-specific implementation points that still survive in code

## Scope

This inventory is derived from:
- direct flavor files under `app/src/widp/`, `app/src/widpDebug/` and `app/src/widpRelease/`
- shared-code implementation points marked with `EyeSeeTea customization`
- the two-dot diff `git diff develop-eyeseetea..HEAD`, checked file by file on 2026-09-11

Every path below was checked against the tree on that date. The 3.4.2 rewrite dropped the
entries that no longer exist at all — `App.java`, `MainPresenter.kt`, `MainView.kt`, the
three `Sync*WorkerModule.kt` and the Dagger notifications module and component were deleted
upstream — and removed `app/src/widp/.../DownloadNewVersion.kt`, which the previous version
listed as a WIDP file when no such file had ever existed. That path does exist now, for a
different reason: upstream made `DownloadNewVersion` per-flavor in 3.4.x.

Files that still exist but no longer carry any WIDP delta are **kept**, under a separate
"No delta today" list in each section. They are there because the feat-commit cross-check
walks the files the original feature commits touched and expects to find every one of them
in the inventory; dropping them would make that check fail and quietly weaken the automerge
rule. Keeping them in their own list is the compromise: the check still passes, and a reader
looking for what must survive is not sent chasing files that can never differ.

## Validated customization count

**5 confirmed, all active.** Three were retired earlier and stay retired — see section 3.

## 1. Direct WIDP flavor surface

### 1.1 WIDP flavor code

- `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` — registers the notifications download as a `PostMetadataSyncAction` (see 2.3)
- `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` — per-flavor since 3.4.x. WIDP downloads the APK in-app, so this is the `dhis2` variant, not the `eyeseetea` one, which opens a URL
- `app/src/widp/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — flavor scaffolding, same substance as the baseline's own copy. Moved in 3.4.2 out of a directory literally named `org.dhis2.utils`
- `app/src/widp/java/org/dhis2/data/user/UserComponentFlavor.kt` — empty interface, referenced by nothing on either branch. Inert; listed so it is not mistaken for live wiring

### 1.2 WIDP flavor resources and branding

- `app/src/widp/res/menu/main_menu.xml` — flavor menu: adds `change_url` (see 2.1) and carries `menu_dev`, because it shadows the shared menu
- `app/src/widpDebug/res/menu/main_menu.xml` — the same file again. It shadows `app/src/widp/` in debug builds, so the two must always be edited together
- `app/src/widp/`, `app/src/widpDebug/`, `app/src/widpRelease/` — launcher icons and flavor strings

## 2. Shared-code customization implementation points

### 2.1 Change Server URL

Status: `active`

New files, none of which exist in `develop-eyeseetea`:

- `app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt` — the DialogFragment
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt` — validate, persist, re-authenticate
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt` — Dagger module
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt` — Dagger subcomponent
- `app/src/main/res/layout/dialog_change_server_url.xml` — layout
- `app/src/main/res/drawable/ic_edit.xml` — icon
- `app/src/main/res/values/ids_eyeseetea.xml` — declares `change_url`. Added in 3.4.2 so flavors other than `widp` compile: `MainActivity` is shared and handles that id while only the WIDP menu declares it. Deliberately not named `ids.xml`, so it cannot collide with a file Oslo might add later

Shared files carrying a WIDP delta:

- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — `onChangeServerURL()` and the `R.id.change_url` branch of `initCurrentScreen()`
- `app/src/main/java/org/dhis2/data/user/UserComponent.java` — `plus(ChangeServerURLModule)`
- `app/src/main/res/values/strings.xml` — `change_server_url`, `change_server_url_warning`, plus `url_hint` and `login_https`, which upstream deleted in 3.4.x while our own layout still references them
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProvider.kt` — `updateServerURL()`
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceProviderImpl.kt` — its implementation

3.4.2 note: the dialog builds its subcomponent from `app().userComponent()!!.plus(...)`. It used
to call `App.createChangeServerULComponent()`, but upstream deleted `App.java`; going through
`UserComponent` directly means this customization now needs nothing at all added to the
application class. `dismiss()` also lost its `releaseSessionComponent()` call, because upstream
removed Dagger's `SessionComponent` entirely. Neither casualty produced a conflict — only the
compiler found them.

### 2.2 Image upload without resizing

Status: `active`

- `form/src/main/java/org/dhis2/form/data/FormValueStore.kt` — `saveFileResource(filePath, false)` in place of `saveFileResource(filePath, valueType == ValueType.IMAGE)`

One argument on one line, which makes it the easiest customization in the fork to lose to an
automerge. The check is `grep -c 'saveFileResource(filePath, false)'`, which must be 1.

No delta today, listed for the feat-commit cross-check:

- `app/src/main/java/org/dhis2/data/forms/dataentry/ValueStoreImpl.kt` — the pre-Compose value store
- `app/src/main/java/org/dhis2/data/server/ServerModule.kt` — SDK wiring touched by the original rollout

### 2.3 Notifications system

Status: `active`

Data layer (all new files):

- `app/src/main/java/org/dhis2/data/notifications/NotificationD2Repository.kt` — sync, filtering, read/write
- `app/src/main/java/org/dhis2/data/notifications/NotificationsApi.kt` — `dataStore/notifications/notifications`, plus `UserGroupsApi`
- `app/src/main/java/org/dhis2/data/notifications/NotificationDTO.kt` — DTOs
- `app/src/main/java/org/dhis2/data/notifications/UserD2Repository.kt` — current user

Domain layer (all new files):

- `app/src/main/java/org/dhis2/usescases/notifications/domain/Notification.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/domain/NotificationRepository.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/domain/GetNotifications.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/domain/MarkNotificationAsRead.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/domain/User.kt`
- `app/src/main/java/org/dhis2/usescases/notifications/domain/UserRepository.kt`

Presentation (new file):

- `app/src/main/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenter.kt` — presenter, the `ShowNotifications` flag and its `onPending` listener, and the `NotificationsView` contract

DI — moved from Dagger to Koin in 3.4.2:

- `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsKoinModule.kt` — the whole graph, in one new file
- `app/src/main/java/org/dhis2/di/KoinInitialization.kt` — one line registering it
- `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` — the flavor's post-sync action

The Dagger module and component are gone, along with their entries in `App.java` and
`AppComponent.java`. Upstream migrated `MainActivity` to Koin and stopped running the `inject()`
that populated the presenter inherited by `ActivityGlobalAbstract`, leaving the field null on
every screen.

UI integration:

- `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` — implements `NotificationsView`, resolves the presenter from Koin, refreshes on create and resume, registers the `onPending` listener while resumed, renders the dialog with Markwon and resolves the locale translation

Sync integration — the hook moved in 3.4.2:

- `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` — the download runs from the baseline's `PostMetadataSyncAction` extension point. Until 3.4.0 it ran from `SyncPresenterImpl.syncMetadata().doOnComplete {}`, which upstream removed when metadata sync moved into the `:sync` module. Mechanism documented in `eyeseetea-docs/customization-techniques.md` T2

Preferences:

- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — the `NOTIFICATIONS` key
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProvider.kt` — WIDP-only file
- `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProviderImpl.kt` — WIDP-only file
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/providers/PreferenceConstants.kt` — `BASIC_SHARE_PREFS`

`commons/src/main/java/org/dhis2/commons/prefs/PreferenceModule.kt` is **no longer customized**:
the Koin module provides `BasicPreferenceProvider` now, which puts that file back to its baseline
content and takes one more shared file out of the conflict surface.

Build config:

- `app/build.gradle.kts` — the Markwon dependency
- `gradle/libs.versions.toml` — the Markwon version and library alias

Tests:

- `app/src/test/java/org/dhis2/data/notifications/NotificationD2RepositoryTest.kt` — filtering and persistence
- `app/src/test/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenterTest.kt` — the display rules: the pending flag survives an empty list, and marking pending reaches the screen that is up
- `app/src/testWidp/java/org/dhis2/di/PostMetadataSyncModuleTest.kt` — the flavor wiring: exactly one action, it downloads then marks pending, and a failed download marks nothing

No delta today, listed for the feat-commit cross-check:

- `app/src/main/java/org/dhis2/AppComponent.java` — held the Dagger module entry until 3.4.2
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — hosted the pre-3.4.0 `doOnComplete` hook
- `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` — passed the repository to it
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — carried the pending/refresh calls on the `sync_manager` and `menu_home` menu actions until 3.4.2, when `MainView` and `MainPresenter` were deleted upstream. Its remaining delta belongs to 2.1, not here
- `app/src/main/java/org/dhis2/usescases/main/MainModule.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramFragment.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramModule.kt`

### 2.4 2FA support

Status: `active` (depends on the EyeSeeTea SDK fork)

New files:

- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt` — the sealed state and `TwoFactorType`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt`

Shared files carrying a WIDP delta:

- `login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt` — passes the code to `blockingLogIn`, plus `isTwoFactorError()` and `handleTwoFactorError()` at the end of the file
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepository.kt` — `twoFactorCode` on the interface
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/usecase/LoginUser.kt` — passes it through
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/usecase/BaseLogin.kt` — maps the exception to `LoginResult.TwoFactorError`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/LoginResult.kt` — the `TwoFactorError` variant
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt` — `twoFactorState`, `twoFactorCode`, `infoMessage`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt` — the `TwoFactorError` branch and the resend logic
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt` — `TwoFactorContainer` and the info bar
- `login/src/commonMain/composeResources/values/strings.xml` — the five 2FA UI strings
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt` — the six 2FA message branches
- `commonskmm/src/commonMain/composeResources/values/strings.xml` — those six messages

Tests:

- `login/src/commonTest/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModelTest.kt`

**Check this one first after any merge.** The baseline routes all seven
`D2ErrorCode.*TWO_FACTOR*` codes to `defaultError()`. Taking its version of
`D2ErrorMessageProviderImpl.kt` compiles, passes every test, and turns every 2FA message into
"unexpected error" with nothing to show for it. The check is `grep -c 'two_factor'` on that
file, and it must return **6**.

`login/build.gradle.kts` is **no longer customized**. It used to declare a `widp` product
flavor; in 3.4.2 upstream moved the module to the KMP `androidLibrary` DSL and dropped product
flavors from it altogether. Earlier notes claimed this file had been "absorbed upstream" when it
had not. It has now, and the build is the evidence.

SDK patch — in the `EyeSeeTea/dhis2-android-sdk` fork at `1.14.2-eyeseetea-fork-1`, not in this
repository:

- `core/.../user/internal/LogInCall.kt` — `generate2FAErrorIfRequired()`
- `core/.../user/internal/LoginPayload.kt` — `twoFactorCode`
- `core/.../maintenance/D2ErrorCode.java` — the Email, SMS and rate-limit codes

No delta today, listed for the feat-commit cross-check:

- `app/src/main/java/org/dhis2/data/server/UserManager.java`
- `app/src/main/java/org/dhis2/data/server/UserManagerImpl.java`
- `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/error/DomainErrorMapper.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/error/DomainError.kt`

### 2.5 URL data element field

Status: `active` — rendered as inline supporting text since 2026-04-17

- `form/src/main/java/org/dhis2/form/data/EventRepository.kt` — reads `de?.url()` and passes it to the factory
- `form/src/main/java/org/dhis2/form/data/EnrollmentRepository.kt` — passes `url = null`, since enrollment attributes carry no URL
- `form/src/main/java/org/dhis2/form/model/FieldUiModel.kt` — `val url: String?`
- `form/src/main/java/org/dhis2/form/model/FieldUiModelImpl.kt` — the override
- `form/src/main/java/org/dhis2/form/model/SectionUiModelImpl.kt` — the override
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactory.kt` — the parameter
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactoryImpl.kt` — passes it through
- `form/src/main/java/org/dhis2/form/extensions/FieldUiModelExtensions.kt` — `supportingText()` appends the URL under the description

No delta today, listed for the feat-commit cross-check:

- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/data/EventDetailsRepository.kt` — the pre-Compose event-details path

## 3. Retired customizations (verified 2026-04-02, unchanged 2026-09-11)

### Notification translations (originally #4)

Merged into 2.3. The `translations` map and the locale lookup are part of the notifications
system, not a separate capability.

### Access to indicators from the form (originally #6)

Not a WIDP customization: present in `develop-eyeseetea` since May 2019 (`949911e22`).

### Events filter for text-type data elements (originally #7)

No trace in code, no diff against the baseline, no customization comment. Either never
implemented or removed before 3.3.0.1.

## 4. Feat commits (inventory cross-check)

The original feature commits behind each customization. Run `git show <sha> --stat` on every
SHA and confirm each code file it touched appears in the matching section above. If a file from
a feat commit is missing from the inventory, the automerge verification rule cannot fire on it,
and a silent automerge can drop its wiring with no conflict markers and no detection — which is
exactly how the 3.3.1 upgrade lost the notifications wiring in `MainActivity`, `MainView` and
`MainPresenter`. `eyeseetea-docs/scripts/check_upgrade_docs.py` automates this check.

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
- `134532f92` — Launch notifications from base activity (wiring in `MainActivity`/`MainView`/`MainPresenter`; silently dropped by the 3.3.1 automerge, restored 2026-04-20, and retired in 3.4.2 when upstream deleted those files)
- `0ddcaed56` — new: Align new Notification response implementations
- `39beb59d9` — Implement notification translations
- `0c8b70cd9` — Fix serialization of notifications using new SDK Http client with Ktor

### 2.4 2FA support
Status: `active`
- `87c5da010` — Remove 2factor customization (best surviving provenance anchor for the current login-challenge files; it touches the exact paths later restored in WIDP)
- `64f8e168b` — chore: D2Error to DomainError (covers the domain error mapping used by login 2FA)
- `78afccf05` — refactor: include error when device doesn't have network available on login screen (covers the current `D2ErrorMessageProviderImpl` / `CredentialsViewModel` path)

### 2.5 URL data element field
Status: `active`
- `c556b7ab7` — Implement show data element url (original, Nov 2022; rendering reimplemented 2026-04-17 in `FieldUiModelExtensions.supportingText()`)

## 5. Fork identity and scaffolding (not customizations)

These differ from the baseline but implement no client behavior. They are listed so that the
two-dot diff against `develop-eyeseetea` has no unexplained entries.

- `gradle/libs.versions.toml` — `vName = 3.4.2-widp-fork-1`. `vCode` and the SDK tag come from the baseline unchanged
- `app/build.gradle.kts` — the `widp` product flavor (the Markwon dependency in the same file belongs to 2.3)
- `.github/workflows/eyeseetea-main.yml` — runs the WIDP unit tests where the baseline runs the `eyeseetea` ones
- `.gitignore` — ignores `.journal/`, a local tooling directory
- `AGENTS-widp.md` — fork identity for coding agents. Since Oslo 3.4, `CLAUDE.md` is an Oslo
  file (a four-line bridge to `AGENTS.md`), so the identity lives in its own file and
  `CLAUDE.md` differs from the baseline by exactly one line: the `@AGENTS-widp.md` import.
  `AGENTS.md` is Oslo's generic guide and is never edited
- `eyeseetea-docs/customizations/widp/`, `eyeseetea-docs/upgrade/widp/` — this client's documentation
- `openspec/` — this client's specs, config, change proposals and archive

`eyeseetea-docs/customizations/template/customization-specs-template.md` is **absent on
purpose**: deleted in `d46d74acf` because the onboarding guide retires that intermediate draft
at the end of Phase 4. Git carries the deletion through merges. Do not restore it.

## 6. Notes

- This inventory reflects the branch as of 2026-09-11, after the merge of `develop-eyeseetea`
  at `8e0200bcc` (3.4.2) and after the notifications graph was re-anchored on Koin and on the
  post-metadata-sync extension point.
- Each "No delta today" list exists for one reason: the feat-commit cross-check walks the files
  those commits touched and expects to find them here. They are separated from the live
  implementation points on purpose — a reader checking what must survive should not have to
  guess which entries can never differ.
- The source of truth for behavior and for canonical titles is `openspec/specs/<capability>/spec.md`.
  Each spec starts with `# <Title>`, and that `<Title>` is the exact string to use here and in
  code comments.
- If code comments and functional titles diverge, the spec wins; update the comment.
- If files are merged, renamed, reverted or reworked, regenerate this file from the current code
  and the two-dot diff against `develop-eyeseetea`.
