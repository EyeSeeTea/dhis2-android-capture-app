# WIDP upgrade to 3.5.0 — working notes

Temporary working notes for one upgrade. Not stable documentation: reusable merge rules
belong in `eyeseetea-docs/upgrade/conflict-rules.md`, and the final customization inventory
belongs in `eyeseetea-docs/customizations/widp/customization-files.md`.

## Header

- Client: `widp`
- Target version: `3.5.0` (`3.5.0-widp-fork-1`)
- Base branch: `develop-eyeseetea` at `173334dbb` (`3.5.0-eyeseetea-fork-1`, vCode 159)
- Previous state: `feature-widp/redo_3_4_2` at `e2f13d13d` (`3.4.2-widp-fork-1`; **not yet merged**
  into `develop-widp`, which is still `3.3.1-widp-fork-1`)
- Merge base: `8e0200bcc` (the 3.4.2 baseline)
- SDK fork: `EyeSeeTea/dhis2-android-sdk` `1.15.0-eyeseetea-fork-1` (from the baseline, was `1.14.2`)
- Toolchain moved by the baseline: Gradle `9.0.1` → `9.3.1`, Kotlin `2.3.20` → `2.4.10`
- Upgrade branch: `feature-widp/upgrade_3.5.0` (from `feature-widp/redo_3_4_2`)
- Started on: `2026-11-03`
- Status: `in_progress`

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` already carries 3.5.0
- merge started: `no` — dry-run only (`git merge --no-commit`, then `--abort`); 14 conflicts
- easy conflicts resolved: `no`
- manual conflicts pending: `yes`
- validation started: `no`

## Dry-run summary

`git merge --no-commit --no-ff develop-eyeseetea`: 707 files changed (+20998 / −9965), **14 conflicts**.
22 of the 103 files in `customization-files.md` change in the merge; 14 of them conflict, the
other 8 change without a marker and must be diffed one by one (see "Silent changes").

What the baseline changed that matters to the fork:

- **Login rewrite.** `LoginRepository.loginUser(serverUrl, username, password)` no longer takes
  `isNetworkAvailable`; the repository maps `D2Error` through `DomainErrorMapper` into typed
  domain errors instead of calling `D2ErrorMessageProvider`. The screen is driven by
  `CredentialsEntryMode`; `CredentialsUiState` carries `isUserLoggedIn`. All seven 2FA
  `D2ErrorCode`s are routed to `defaultError()` in `D2ErrorMessageProviderImpl`.
- **Session renewal.** New `ForcedSessionExpiry`, `ForceSessionExpiryInterceptor`,
  `SessionRenewalDialog`, `SessionRenewalNotifier`, `GetSessionRenewalUrl` (ANDROAPP-7733).
  Overlaps with the SDK automatic logout the fork restored in `OpenIdSession`.
- **File upload rewrite** (ANDROAPP-7707, 7758): `saveFileResource(path, uid, isImage)`.
- `UserManager.java` / `UserManagerImpl.java` deleted.
- `AGENTS.md` gains the Sentry skills section.

## Decisions

Classified before any file is edited. The 14 conflicts, in the four categories of `conflict-rules.md`.

### Conflicted files

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `gradle/libs.versions.toml` | manual_reapply_on_theirs | take baseline `vCode`/`gradle`/`kotlin`/`dhis2sdk`; `vName` = `3.5.0-widp-fork-1`; keep the Markwon entries | Notifications system | pending | One hunk. Keep the "WIDP fork identity" comment above `vName`. |
| `login/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | keep both sides: the five 2FA hints/resend strings and the baseline `oauth_login_url_error` | 2FA support | pending | Pure union, no semantic conflict. |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | keep both sides: the six 2FA strings and the four new baseline error strings | 2FA support | pending | Union. The 2FA strings must stay in sync with whatever consumes them after the 2FA rework below. |
| `commonskmm/.../resources/D2ErrorMessageProviderImpl.kt` | manual_reapply_on_theirs | take the new OAuth2 branches; replace the baseline's `-> defaultError()` for the 2FA codes with the six 2FA messages | 2FA support | pending | Order matters: the baseline groups all 2FA codes in one branch; split it, do not duplicate the `when` arms (duplicate arms compile with a warning and the first wins). |
| `login/.../data/LoginRepository.kt` | manual_reapply_on_theirs | decide the 2FA input after the open question below | 2FA support | pending | Baseline signature has no `isNetworkAvailable`. Our `twoFactorCode: String? = null` is the only thing to keep. |
| `login/.../androidMain/.../data/LoginRepositoryImpl.kt` | manual_reapply_on_theirs | 4 hunks. Take the `DomainErrorMapper` flow; re-add 2FA at the end of the file | 2FA support | pending | **Highest risk.** Our `handleTwoFactorError` builds `TwoFactorOutcome` from `D2ErrorMessageProvider` + `isNetworkAvailable`; neither exists in the baseline path. Needs a re-anchor, not a re-paste. |
| `login/.../domain/usecase/LoginUser.kt` | manual_reapply_on_theirs | take the baseline; add `twoFactorCode` pass-through | 2FA support | pending | Baseline normalizes the URL scheme here and drops `.trim()`. Do not lose either. |
| `login/.../ui/state/CredentialsUiState.kt` | manual_reapply_on_theirs | keep `isUserLoggedIn`; append `twoFactorState`, `twoFactorCode`, `infoMessage` | 2FA support | pending | Baseline replaced `oAuthEnable` with `isUserLoggedIn`; our fields do not depend on it. |
| `login/.../ui/viewmodel/CredentialsViewModel.kt` | manual_reapply_on_theirs | 6 hunks. Take `CredentialsEntryMode`; re-hook 2FA into `NEW_ACCOUNT_BASIC` / `EXISTING_PASSWORD` login only | 2FA support | pending | The branch on `oAuthEnable` is gone. 2FA applies only to password login; OAuth and OpenID never reach it. |
| `login/.../ui/screen/CredentialsScreen.kt` | manual_reapply_on_theirs | keep the `TwoFactorState` import and the 2FA container wired in the new layout | 2FA support | pending | One import hunk, but the layout around it changed: verify the 2FA field, the info bar and the resend actions render. |
| `login/.../commonTest/.../CredentialsViewModelTest.kt` | defer_after_build_verification | rebuild on the baseline test; re-add the 2FA cases | 2FA support | pending | 14 hunks. Start from theirs and port each 2FA test; do not resolve hunk by hunk. |
| `app/.../data/server/OpenIdSession.kt` | manual_reapply_on_theirs | decide against the baseline's forced-expiry flow | 2FA support | pending | HEAD merges three log-out sources (OpenID, account disabled, SDK `logOutObservable`); baseline keeps only account deletion. See open question 2. |
| `app/.../usescases/main/MainActivity.kt` | manual_reapply_on_theirs | 6 hunks. Re-anchor the Notifications hooks on `GoToLogin` and `SingleProgramNavigation`, and the resume hook; re-add `R.id.change_url` | Notifications system / Change Server URL | pending | Baseline reduced those `when` branches (braces gone), so the hooks go inside the new bodies. Re-run the Notifications flow by hand afterwards. |
| `form/.../data/FormValueStore.kt` | manual_reapply_on_theirs | new call shape `saveFileResource(path = filePath, uid = uid, isImage = false)` | Image upload without resizing | pending | Keep the `// EyeSeeTea customization - Image upload without resizing` comment and the commented original line. Confirm `isImage = false` still means "no resize" after ANDROAPP-7758. |

### Silent changes in inventory files (no conflict marker)

Diff each against `develop-eyeseetea` after the merge and compare with `customization-files.md`:

| File | Why it matters |
|------|----------------|
| `app/.../data/user/UserComponent.java` | carries `plus(ChangeServerURLModule)` |
| `app/.../data/server/ServerModule.kt` | server wiring |
| `app/.../data/server/UserManager.java`, `UserManagerImpl.java` | **deleted** by the baseline; check nothing of Change Server URL depended on them |
| `app/.../usescases/general/SessionManagerActivity.kt` | session / logout wiring; sits next to the new renewal dialog |
| `app/.../usescases/login/LoginActivity.kt` | login entry |
| `app/.../usescases/main/MainModule.kt`, `program/ProgramModule.kt` | DI of the main screen |
| `app/.../data/forms/dataentry/ValueStoreImpl.kt`, `form/.../EnrollmentRepository.kt`, `EventRepository.kt` | other upload paths: the no-resize rule must hold here too |
| `commonskmm/.../error/DomainError.kt`, `DomainErrorMapper.kt` | the baseline error model the 2FA rework must use |
| `login/.../domain/model/LoginResult.kt` | 2FA outcome type |
| `login/build.gradle.kts` | `widp` flavor block, already marked `needs_validation` in 3.4.2 |
| `app/build.gradle.kts`, `app/src/main/res/values/strings.xml` | `widp` flavor and the appended WIDP block |
| `commonskmm/.../notifications/NotificationManager*.kt` | touched by the baseline; the Notifications customization depends on this area |

## Open Questions

1. **How does 2FA sit on the new typed errors?** Either (a) map the seven 2FA `D2ErrorCode`s in
   `DomainErrorMapper` to dedicated domain errors and let the repository return them, or (b) keep
   our `TwoFactorOutcome` and intercept before `DomainErrorMapper`. (a) is closer to the baseline
   but touches `DomainError.kt` (an Oslo file); (b) keeps the footprint at the end of
   `LoginRepositoryImpl`. Decide in `design.md`.
2. **SDK automatic logout vs the baseline session renewal.** The fork restored the SDK
   `logOutObservable` as `LogOutReason.UNAUTHORIZED` in `OpenIdSession`. Does the baseline's
   `ForcedSessionExpiry` / `SessionRenewalDialog` already cover that case in 1.15.0? If it does,
   drop our hook; if not, add it on top without touching the new classes.
3. **Does `isImage = false` still disable resizing?** Read the ANDROAPP-7758 change before
   keeping the one-line customization.
4. **What happens to the unmerged 3.4.2?** This upgrade is stacked on `feature-widp/redo_3_4_2`.
   Decide how the two reach `develop-widp` (one PR or two) before opening any.

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
