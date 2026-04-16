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
- Status: `in_progress`
- Strategy: revert-the-revert (`git revert 7389d1043`) — develop-eyeseetea was accidentally merged and reverted in Phase A

## Progress

- baseline prepared: `yes` (develop-eyeseetea at 3.3.1-eyeseetea-fork-1)
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `no`
- validation started: `no`

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| app/src/main/java/org/dhis2/data/notifications/NotificationD2Repository.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/NotificationDTO.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/NotificationsApi.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/data/notifications/UserD2Repository.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsComponent.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsModule.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/GetNotifications.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/MarkNotificationAsRead.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/Notification.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/NotificationRepository.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/User.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/domain/UserRepository.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/usescases/notifications/presentation/NotificationsPresenter.kt | accept_ours | keep entire file | Notifications system | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt | accept_ours | keep entire file | Change Server URL | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt | accept_ours | keep entire file | Change Server URL | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt | accept_ours | keep entire file | Change Server URL | pending | modify/delete — WIDP-only file |
| app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt | accept_ours | keep entire file | Change Server URL | pending | modify/delete — WIDP-only file |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt | accept_ours | keep entire file | 2FA support | pending | modify/delete — WIDP-only file |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt | accept_ours | keep entire file | 2FA support | pending | modify/delete — WIDP-only file |
| commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt | manual_reapply_on_theirs | reinsert 2FA error code mappings | 2FA support | pending | content conflict |
| login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt | manual_reapply_on_theirs | reinsert handleTwoFactorError() + 2FA detection | 2FA support | pending | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/LoginResult.kt | manual_reapply_on_theirs | reinsert TwoFactorError sealed variant | 2FA support | pending | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt | manual_reapply_on_theirs | reinsert TwoFactorContainer composable | 2FA support | pending | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt | manual_reapply_on_theirs | reinsert 2FA fields | 2FA support | pending | content conflict |
| login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt | manual_reapply_on_theirs | reinsert 2FA state management + resend | 2FA support | pending | content conflict |
| login/src/commonMain/composeResources/values/strings.xml | manual_reapply_on_theirs | reinsert 2FA string resources | 2FA support | pending | content conflict |
| form/src/main/java/org/dhis2/form/data/EventRepository.kt | manual_reapply_on_theirs | reinsert url() read | URL data element | pending | content conflict |
| form/src/main/java/org/dhis2/form/data/FormValueStore.kt | manual_reapply_on_theirs | reinsert false instead of valueType == IMAGE | Image upload without resizing | pending | content conflict |
| .gitignore | manual_reapply_on_theirs | keep both WIDP + develop-eyeseetea additions | n/a | pending | content conflict |
| eyeseetea-docs/onboarding-fork-guide.md | accept_ours | keep WIDP version (updated in Phase B) | n/a | pending | content conflict — our version is more recent |

## Open Questions

- Will the revert-the-revert produce the same conflict set as a normal merge?
- Is SDK fork `1.13.1-eyeseetea-fork-2` compatible with existing 2FA patches?

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
