# Upgrade Notes — WIDP to 3.3.1

## Purpose

This file is for:
- temporary upgrade progress
- conflict decisions taken during the current upgrade
- unresolved questions
- follow-up checks before closing the upgrade

This file is not for:
- stable merge rules
- final customization inventory
- long-term functional documentation

## Header

- Client: `widp`
- Target version: `3.3.1-widp-fork-1`
- Base branch: `develop-eyeseetea`
- Upgrade branch: `feature-widp/bring_last_changes_3_3_1`
- Started on: `2026-04-16`
- Status: `phase-d-complete` (Phase D closed 2026-04-17; Phase E tests + validation pending)
- Strategy: revert-the-revert (`git revert 7389d1043`) — develop-eyeseetea was accidentally merged and reverted in Phase A

## Progress

- baseline prepared: `yes` (develop-eyeseetea at 3.3.1-eyeseetea-fork-1)
- merge started: `yes` (commit `1af395c30`)
- easy conflicts resolved: `yes`
- manual conflicts pending: `no` (all resolved in commit `ecf4a1321`)
- build compiles: `yes` (`assembleWidpDebug` — build 10 successful)
- validation started: `no`

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| app/src/main/java/org/dhis2/data/notifications/NotificationD2Repository.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/NotificationDTO.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/NotificationsApi.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/UserD2Repository.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsComponent.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsModule.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/GetNotifications.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/MarkNotificationAsRead.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/Notification.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/NotificationRepository.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/User.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/UserRepository.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenter.kt | accept_ours | keep entire file | Notifications system | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt | accept_ours | keep entire file | Change Server URL | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt | accept_ours | keep entire file | Change Server URL | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt | accept_ours | keep entire file | Change Server URL | resolved | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt | accept_ours | keep entire file | Change Server URL | resolved | modify/delete — WIDP-only file |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt | accept_ours | keep entire file | 2FA support | resolved | modify/delete — WIDP-only file |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt | accept_ours | keep entire file | 2FA support | resolved | modify/delete — WIDP-only file |
| commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt | manual_reapply_on_theirs | reinsert 2FA error code mappings | 2FA support | resolved | content conflict |
| login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt | manual_reapply_on_theirs | reinsert handleTwoFactorError() + 2FA detection | 2FA support | resolved | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/LoginResult.kt | manual_reapply_on_theirs | reinsert TwoFactorError sealed variant | 2FA support | resolved | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt | manual_reapply_on_theirs | reinsert TwoFactorContainer composable | 2FA support | resolved | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt | manual_reapply_on_theirs | reinsert 2FA fields | 2FA support | resolved | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt | manual_reapply_on_theirs | reinsert 2FA state management + resend | 2FA support | resolved | content conflict |
| login/src/commonMain/composeResources/values/strings.xml | manual_reapply_on_theirs | reinsert 2FA string resources | 2FA support | resolved | content conflict |
| form/src/main/java/org/dhis2/form/data/EventRepository.kt | manual_reapply_on_theirs | reinsert url() read | URL data element | resolved | content conflict |
| form/src/main/java/org/dhis2/form/data/FormValueStore.kt | manual_reapply_on_theirs | reinsert false instead of valueType == IMAGE | Image upload without resizing | resolved | content conflict |
| .gitignore | manual_reapply_on_theirs | keep both WIDP + develop-eyeseetea additions | n/a | resolved | content conflict |
| eyeseetea-docs/onboarding-fork-guide.md | accept_ours | keep WIDP version (updated in Phase B) | n/a | resolved | content conflict — our version is more recent |

## Automerge casualties (recovered)

Files where git automerge silently dropped customization code in non-conflicting hunks. All recovered from `HEAD~1` and re-adapted to 3.3.1 APIs in `ecf4a1321`:

- `ActivityGlobalAbstract.java` — 65 lines of notifications + Markwon wiring
- `SyncPresenterImpl.kt` — `syncNotifications()`
- `Preference.kt` — `NOTIFICATIONS` key
- `LoginRepository.kt` / `LoginUser.kt` — `twoFactorCode` param
- `dialog_change_server_url.xml` — full file
- `NotificationD2RepositoryTest.kt` — full file
- `commonskmm/.../values/strings.xml` — 2FA strings
- `BasicPreferenceProvider.kt` + `BasicPreferenceProviderImpl.kt` — full files
- `PreferenceModule.kt`, `PreferenceProvider.kt`, `PreferenceProviderImpl.kt` — WIDP methods
- `AppComponent.java`, `App.java` — notifications + change server URL wiring
- `UserComponent.java` — change server URL subcomponent
- 4 `SyncWorkerModule`s — `notificationRepository` parameter

Root cause: `develop-eyeseetea` was created by copying WIDP and then explicitly removing customizations. The revert-the-revert re-applied those deletions as valid automerge. Should not recur in future upgrades now that baseline is clean.

## Post-merge fork identity fixes

- vName: `eyeseetea-fork-1` → `widp-fork-1`
- `app/src/widp/**` source sets: deleted → restored
- `markwon` dependency: deleted → restored
- `widp` flavor in `app/build.gradle.kts`: deleted → re-added
- `widp` flavor in `login/build.gradle.kts`: never existed → added (needed to keep the WIDP-specific login module wiring compiling after the 3.3.1 merge)

## Follow-ups resolved 2026-04-17

- URL data element rendering (#5) — reimplemented in `FieldUiModelExtensions.supportingText()` (URL appended inline below description)
- SMS 2FA string typo — fixed in `commonskmm/.../values/strings.xml:217`
- PSI leftovers — verified clean (residual `.idea/` refs only, gitignored)
- `customization-files.md` updated with DI wiring, preferences layer, SDK wiring, and new inline-rendering entry for #5

Still pending (moved to Phase E):

- Unit tests per customization

## New rules promoted to `conflict-rules.md`

- **Automerge verification rule**: after resolving any conflict, run `git diff develop-eyeseetea -- path/to/file` to verify ALL customization points survived, not only the marked hunks.
- **Post-merge fork identity check**: verify vName, source sets, flavor definitions, dependencies, and fork-specific build config after every merge.

## Validation Notes

- build: `assembleWidpDebug` OK (build 10)
- targeted tests: pending (Phase E)
- manual flows checked: `yes` (validated manually by Jorge after the 3.3.1 upgrade; active customizations exercised against the current build)
- `check_upgrade_docs.py --client widp`: `yes` (passes after inventory/doc alignment)

## Finalization

- surviving customizations moved to `customization-files.md`: `yes`
- stable rules moved to `conflict-rules.md`: `yes` (automerge verification + post-merge fork identity)
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `none known`
