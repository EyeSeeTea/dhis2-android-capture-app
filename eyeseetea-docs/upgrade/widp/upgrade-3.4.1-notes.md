# Upgrade Notes — WIDP 3.4.1

Temporary working notes for the WIDP 3.3.1 → 3.4.1 upgrade. Conflict decisions, open questions
and follow-up checks live here while the merge is active. Stable rules belong in
`eyeseetea-docs/upgrade/conflict-rules.md`; the final file inventory belongs in
`eyeseetea-docs/customizations/widp/customization-files.md`.

Functional plan and rationale: `openspec/changes/upgrade-widp-to-3-4-1/`.

## Header

- Client: `widp`
- Target version: `3.4.1` (from `origin/upstream/3.4.1`)
- Coming from: `3.3.1-widp-fork-1` (vCode 151, SDK `1.13.1-eyeseetea-fork-3`)
- Base branch: `develop-eyeseetea` (reference only — never merged into this branch)
- Upgrade branch: `feature/upgrade_widp_to_3_4_1`
- Started on: `2026-08-13`
- Status: `in_progress`

## Reference upgrade

PR #323 `[EyeSeeTea] upgrade 3.4.1` (merged into `develop-eyeseetea`), plus PR #311 for 3.4.0.

**Reference for mechanics only.** `develop-eyeseetea` carries none of WIDP's five customizations,
verified by file inventory. Its resolutions must never be copied where a WIDP customization is
involved — see the 2FA entries below.

## Environment

- SDK resolution mode: **JitPack**. `local.properties` does not exist and `dhis2.useLocalSdk` is
  not set in `gradle.properties`, so `settings.gradle.kts` falls through to its default (`false`).
  The composite build against a local SDK checkout is **not** active; the upgrade is being built
  against the published `com.github.EyeSeeTea:dhis2-android-sdk` artifact.
- Target SDK tag: `1.14.1-eyeseetea-fork-1` (must carry the 2FA patch surface; the baseline
  compiles against it while referencing the added `D2ErrorCode` 2FA values).

## Pre-merge baseline snapshot

- 45 files carry a `// EyeSeeTea customization - <Title>` marker. That list, not the raw
  `git diff origin/develop-eyeseetea..HEAD` (1385 files, dominated by the 3.3.1 → 3.4.1 version
  gap), is the checklist for the post-merge audit.
- `develop-eyeseetea` exists only as `origin/develop-eyeseetea` in this checkout. Every audit
  command from `conflict-rules.md` / root `CLAUDE.md` needs the `origin/` prefix here.
- **Inventory gap found:** `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt`
  carries a customization marker in code but is not cited in `customization-files.md`. To fold
  into task 5.2 / 9.1.

## Progress

- baseline prepared: `yes`
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `yes`
- validation started: `no`

## Predicted conflict surface

27 files, from `git merge-tree --write-tree develop-widp origin/upstream/3.4.1` run before the
merge: 21 content conflicts, 6 modify/delete. Plus **8 files that auto-merge silently** despite
being customized and touched upstream — those are the highest-risk items because nothing prompts
a review.

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `app/src/androidTest/assets/databases/dhis_test.db` | accept_theirs | upstream test fixture | n/a | pending | binary, no WIDP content |
| `gradle.properties` | accept_theirs | build tuning | n/a | pending | |
| `CLAUDE.md` | accept_ours | fork identity doc | n/a | pending | add/add conflict |
| `gradle/libs.versions.toml` | manual_reapply_on_theirs | SDK tag + version + vCode | fork identity | pending | `dhis2sdk=1.14.1-eyeseetea-fork-1`, `vName=3.4.1-widp-fork-1`, `vCode=156` |
| `app/build.gradle.kts` | manual_reapply_on_theirs | widp flavor block | fork identity | pending | applicationId `com.eyeseetea.widp` |
| `login/build.gradle.kts` | manual_reapply_on_theirs | 2FA deps | 2FA support | pending | |
| `app/src/main/res/values/strings.xml` | manual_reapply_on_theirs | WIDP strings appended | several | pending | |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | WIDP strings appended | several | pending | |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt` | manual_reapply_on_theirs | Oslo search patch | n/a (Oslo fix) | pending | from PR #315 |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` | manual_reapply_on_theirs | Oslo search patch | n/a (Oslo fix) | pending | from PR #315 |
| `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` | manual_reapply_on_theirs | notifications ctor arg | Notifications system | pending | |
| `login/src/androidMain/.../LoginRepositoryImpl.kt` | manual_reapply_on_theirs | real `twoFactorCode` arg | 2FA support | pending | ⚠ baseline passes `null` — **do not copy** |
| `login/src/commonMain/.../LoginUser.kt` | manual_reapply_on_theirs | 2FA result plumbing | 2FA support | pending | |
| `login/src/commonMain/.../CredentialsUiState.kt` | manual_reapply_on_theirs | 2FA state fields | 2FA support | pending | |
| `login/src/commonMain/.../CredentialsViewModel.kt` | manual_reapply_on_theirs | type detection, resend, cooldown | 2FA support | pending | |
| `login/src/commonMain/.../CredentialsScreen.kt` | manual_reapply_on_theirs | 2FA UI per type | 2FA support | pending | |
| `commonskmm/.../PreferenceConstants.kt` | manual_reapply_on_theirs | server URL pref key | Change Server URL | pending | |
| `app/src/main/java/org/dhis2/data/user/UserComponent.java` | manual_reapply_on_theirs | `plus(ChangeServerURLModule)` | Change Server URL | pending | file survives upstream |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | manual_reapply_on_theirs | menu entry + notif calls | Change Server URL, Notifications | pending | holds `notificationsPresenter` |
| `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` | manual_reapply_on_theirs | `syncNotifications()` | Notifications system | pending | verify the trigger survives |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | manual_reapply_on_theirs | provides `NotificationRepository` | Notifications system | pending | DI landing site, survives |
| `app/src/main/java/org/dhis2/App.java` | **port** | Dagger wiring → `App.kt` | Notifications, Change Server URL | pending | deleted upstream (Kotlin migration) |
| `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` | **port** | call path → `MainViewModel` | Notifications system | pending | deleted upstream (ANDROAPP-7340) |
| `app/src/main/java/org/dhis2/usescases/main/MainView.kt` | **port** | interface → activity state | Notifications system | pending | deleted upstream |
| `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` | **port** | provider consolidated | Notifications system | pending | worker removed upstream |
| `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` | **port** | provider consolidated | Notifications system | pending | worker removed upstream |
| `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` | **port** | provider consolidated | Notifications system | pending | worker removed upstream |

### Silent automerge — audit required (no conflict will be raised)

| File | Customization | Status | Notes |
|------|---------------|--------|-------|
| `commonskmm/.../D2ErrorMessageProviderImpl.kt` | 2FA support | pending | ⚠ baseline maps the 7 2FA codes to `defaultError()`; WIDP must keep its real messages |
| `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` | Change Server URL | pending | settings menu entry |
| `login/src/commonMain/.../LoginRepository.kt` | 2FA support | pending | interface method |
| `login/src/commonMain/composeResources/values/strings.xml` | 2FA support | pending | 2FA copy |
| `login/src/commonTest/.../CredentialsViewModelTest.kt` | 2FA support | pending | |
| `form/src/main/java/org/dhis2/form/data/EnrollmentRepository.kt` | URL data element | pending | plumbing only, rendering out of scope |
| `form/src/main/java/org/dhis2/form/data/EventRepository.kt` | URL data element | pending | plumbing only, rendering out of scope |
| `app/src/test/java/.../SearchTEIViewModelTest.kt` | n/a (Oslo fix) | pending | PR #315 patches |

## Notifications port — semantics to preserve

Extracted from `MainPresenter.checkSingleProgramNavigation()` before the merge deletes it:

- `markShowNotificationsAsPending()` fires on **both** branches.
- `refreshNotifications()` fires **only** when the app is *not* auto-navigating into a single
  program.

Both are implemented in `MainActivity` (which survives) and delegate to the Dagger-provided
`notificationsPresenter`. The Dagger graph is not moving; only the call path is.

## Oslo patches to replicate

WIDP already carries ANDROAPP-6844, the stale-search-results fix and the search spinner fix
(PR #315). Missing, present in the baseline:

| Commit | Subject | Status |
|---|---|---|
| `1e4149f01` | ANDROAPP-7661 granular sync image download race | pending |
| `48058eb9a` | ANDROAPP-7666 completed-event dialog always shown | pending |
| `726f3bd7e` | empty list on return from TEI after search | pending |

## Open Questions

- Where in `MainViewModel` / `MainActivity` the 3.4.1 single-navigation outcome becomes
  observable, so the two notification calls keep their branch asymmetry.
- Whether each of the three Oslo patches still applies to 3.4.1 or was fixed upstream.
- Whether `SyncPresenterImpl.syncNotifications()` still has a live invocation path now that the
  three sync workers that drove it were removed upstream.

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
