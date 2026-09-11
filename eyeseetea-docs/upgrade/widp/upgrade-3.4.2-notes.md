# WIDP upgrade to 3.4.2 — working notes

Temporary working notes for one upgrade. Not stable documentation: reusable merge rules
belong in `eyeseetea-docs/upgrade/conflict-rules.md`, and the final customization
inventory belongs in `eyeseetea-docs/customizations/widp/customization-files.md`.

## Header

- Client: `widp`
- Target version: `3.4.2` (`3.4.2-widp-fork-1`)
- Base branch: `develop-eyeseetea` at `8e0200bcc` (`3.4.2-eyeseetea-fork-1`)
- Previous state: `develop-widp` at `3252c0681` (`3.3.1-widp-fork-1`)
- Merge base: `8a4866305`
- SDK fork: `EyeSeeTea/dhis2-android-sdk` `1.14.2-eyeseetea-fork-1` (from the baseline)
- Upgrade branch: `feature-widp/redo_3_4_2`
- Started on: `2026-09-11`
- Status: `in_progress`

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` already carries 3.4.2
- merge started: `yes` — 27 conflicts
- easy conflicts resolved: `yes`
- manual conflicts pending: `no`
- validation started: `yes`

## Decisions

Classified before any file was edited. The 27 conflicts git reported, in the four
categories of `conflict-rules.md`, each with the behavior that has to survive.

### Conflicted files

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `.github/workflows/eyeseetea-main.yml` | accept_ours | keep the widp unit-test task and its report path | n/a (fork CI) | resolved_keep_ours | Baseline runs the `eyeseetea` flavor; this branch must run `widp`. See Open Questions: it should run both. |
| `CLAUDE.md` | accept_ours | keep the WIDP identity as it stands | n/a (fork identity) | resolved_keep_ours | add/add conflict. Oslo owns this file since 3.4 and reduced it to a four-line bridge. Resolved as ours so the merge loses no information; commit 7 corrects the false facts in it and commit 8 moves the identity to `AGENTS-widp.md`. Doing it here would have deleted fork identity inside a commit whose job is the merge. |
| `app/build.gradle.kts` | manual_reapply_on_theirs | re-add the `widp` product flavor and the Markwon dependency | Notifications system | resolved_manual_merge | Baseline declares `dhis2*` + `eyeseetea` only. |
| `app/src/main/java/org/dhis2/App.java` | accept_theirs (delete) | none — file replaced by `App.kt` | Notifications system | resolved_keep_theirs | modify/delete. The Dagger `notificationsModule(...)` call dies with the file; the graph moves to Koin in commit 3. |
| `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` | accept_theirs (delete) | none | Notifications system | resolved_keep_theirs | modify/delete. Sync workers moved to the `:sync` module. |
| `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` | accept_theirs (delete) | none | Notifications system | resolved_keep_theirs | modify/delete, same reason. |
| `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` | accept_theirs (delete) | none | Notifications system | resolved_keep_theirs | modify/delete, same reason. |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | accept_theirs | drop the `notificationRepository` constructor argument | Notifications system | resolved_keep_theirs | The parameter only existed to feed `SyncPresenterImpl.syncNotifications()`, which no longer exists. |
| `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` | accept_theirs | drop `syncNotifications()` and its constructor parameter | Notifications system | resolved_keep_theirs | Metadata sync moved to `:sync`; the `doOnComplete` hook is gone. Replaced by `PostMetadataSyncAction` in commit 4. **This is the customization most at risk in this upgrade.** |
| `app/src/main/java/org/dhis2/data/user/UserComponent.java` | manual_reapply_on_theirs | re-add `plus(ChangeServerURLModule)` | Change Server URL | resolved_manual_merge | Two lines plus the import. |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | manual_reapply_on_theirs | re-add the change-URL menu action and dialog state; notifications hooks re-anchored | Change Server URL / Notifications system | resolved_manual_merge | Oslo migrated the screen to Koin + `MainViewModel`. `MainView`/`MainPresenter` are gone, so the two view-contract overrides disappear with them. |
| `app/src/main/java/org/dhis2/usescases/main/MainView.kt` | accept_theirs (delete) | none | Notifications system | resolved_keep_theirs | modify/delete. Contract replaced by `MainViewModel` state. |
| `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` | accept_theirs (delete) | none | Notifications system | resolved_keep_theirs | modify/delete. The post-sync `markShowNotificationsAsPending()` call dies here and is re-created in commit 4 on the sync extension point. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` | accept_theirs | none — absorbed | n/a (`EyeSeeTea fix`) | resolved_keep_theirs | The blank-value guard is already in the baseline, comment included. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt` | accept_theirs | none — absorbed and improved | n/a (`EyeSeeTea fix`) | resolved_keep_theirs | Baseline has the same two helpers plus a `lastSearchPagingData` guard and `collectLatest`. Taking ours would be a regression. |
| `app/src/main/res/values/strings.xml` | manual_reapply_on_theirs | keep the appended WIDP block and restore two strings Oslo deleted | Change Server URL | resolved_manual_merge | `url_hint` and `login_https` were removed from every `values*` file upstream; the WIDP-owned `dialog_change_server_url.xml` still references them, so resource linking fails without them. |
| `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` | accept_theirs | drop the notifications mock | Notifications system | resolved_keep_theirs | Follows `SyncPresenterImpl`. |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | keep the six 2FA strings | 2FA support | resolved_manual_merge | Must stay in sync with `D2ErrorMessageProviderImpl`. |
| `commonskmm/.../providers/PreferenceConstants.kt` | manual_reapply_on_theirs | keep `BASIC_SHARE_PREFS` | Notifications system | resolved_manual_merge | One constant. |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | accept_theirs | none | n/a (baseline-owned) | resolved_keep_theirs | Rule: a client fork never edits the baseline inventory. |
| `gradle/libs.versions.toml` | manual_reapply_on_theirs | keep the baseline version and SDK tag, rename the fork, keep the Markwon entries | Notifications system | resolved_manual_merge | `vName` becomes `3.4.2-widp-fork-1`; `vCode` and `dhis2sdk` come from the baseline. |
| `login/build.gradle.kts` | defer_after_build_verification | probably none — drop the `widp` flavor block | 2FA support | needs_validation | Baseline moved the module to the KMP `androidLibrary` DSL and has no `productFlavors` at all, so our flavor block has nothing to attach to. Verified by building both flavors. |
| `login/.../data/LoginRepositoryImpl.kt` | manual_reapply_on_theirs | re-add `twoFactorCode` on `loginUser`, `isTwoFactorError()` and `handleTwoFactorError()` | 2FA support | resolved_manual_merge | The helpers live at the end of the file, per the placement convention. |
| `login/.../domain/usecase/LoginUser.kt` | manual_reapply_on_theirs | re-add the `twoFactorCode` parameter and pass it through | 2FA support | resolved_manual_merge | |
| `login/.../ui/screen/CredentialsScreen.kt` | manual_reapply_on_theirs | re-add `TwoFactorContainer`, the info-bar branch and the `loginInfoMessage` parameter | 2FA support | resolved_manual_merge | Baseline reworked the screen; start from theirs. |
| `login/.../ui/state/CredentialsUiState.kt` | manual_reapply_on_theirs | re-add `twoFactorState`, `twoFactorCode`, `infoMessage` | 2FA support | resolved_manual_merge | |
| `login/.../ui/viewmodel/CredentialsViewModel.kt` | manual_reapply_on_theirs | re-add the `TwoFactorError` branch, the three 2FA fields in every state builder, and the resend logic | 2FA support | resolved_manual_merge | Largest single reapply of the upgrade. |

### Files that merged without a conflict but carry a customization

Git resolves these silently, so they are the ones the automerge verification rule exists
for. Checked with a two-dot diff after resolving, against `customization-files.md`:

| File | Customization | What had to survive | Result |
|------|---------------|---------------------|--------|
| `commonskmm/.../resources/D2ErrorMessageProviderImpl.kt` | 2FA support | the six 2FA message branches, not the baseline's single `defaultError()` | see Validation Notes — this is the trap that compiles and silently degrades every 2FA message |
| `app/src/main/.../general/ActivityGlobalAbstract.java` | Notifications system | the presenter field, the resume hook, the dialog, the translations lookup | survived |
| `form/src/main/.../data/EventRepository.kt` | URL data element field | `de?.url()` read and passed into the field factory | survived |
| `form/src/main/.../data/EnrollmentRepository.kt` | URL data element field | `url = null` on every enrollment field | survived |
| `login/src/commonMain/composeResources/values/strings.xml` | 2FA support | the five 2FA UI strings | survived |
| `login/.../main/data/LoginRepository.kt` | 2FA support | `twoFactorCode` on the interface | survived |
| `app/src/test/.../SearchTEIViewModelTest.kt` | n/a (`EyeSeeTea fix`) | nothing — the baseline carries equivalent tests on the new API | resolved to the baseline version; the automerged mix had duplicate test names and calls into a removed API |


### Work inside the merge commit that was not a conflict

Git reported no conflict on any of these — they are consequences of baseline deletions and
API moves, and the tree does not build without them. That is the whole test for what
belongs in this commit.

| Change | Why the build needs it | Customization |
|---|---|---|
| `ChangeServerUrlDialog.kt` now builds its subcomponent from `app().userComponent()!!.plus(...)` | `App.java` is deleted upstream, and it hosted `createChangeServerULComponent()`. **This casualty is invisible to the automerge check**: the dialog is a WIDP-only file that git never touched, so nothing flagged it — only the compiler does. The replacement needs nothing added back to `App.kt`, which makes the customization one Oslo file smaller than it was. | Change Server URL |
| `ChangeServerUrlDialog.dismiss()` no longer calls `app().releaseSessionComponent()` | Second casualty of the same deletion, and only the compiler found it. Upstream removed Dagger's `SessionComponent` entirely in 3.4.x. The dialog never owned that component — the call was copied from `PinDialog` — so there is nothing to release. | Change Server URL |
| `app/src/widp/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` (new path, 3.4.x API) | The old copy sat in a directory literally named `org.dhis2.utils` and predated 3.4.1: it lacks `SyncUiStateMapper`, which `GranularSyncViewModelFactory` now requires. Content taken from the baseline's own flavor copy. | n/a (flavor scaffolding) |
| `app/src/widp/java/org.dhis2.utils/CustomizableConstants.kt` deleted | Unreferenced on both sides. The baseline deleted its identical copy from `app/src/eyeseetea/` in this same upgrade; keeping the WIDP one would have left one dead file alone in a malformed directory. | n/a |
| `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` (new) | Upstream made this a per-flavor file in 3.4.x. Two variants exist: `dhis2` downloads the APK in-app, `eyeseetea` opens a URL. WIDP used `versionRepository.download(...)`, so it takes the `dhis2` variant. | n/a (flavor scaffolding) |
| `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` (new, empty) | `KoinInitialization` registers `postMetadataSyncModule` unconditionally, so the file must exist in every flavor source set. Empty here; commit 4 fills it. | Notifications system |
| `menu_dev` added to `app/src/widp/res/menu/main_menu.xml` **and** `app/src/widpDebug/res/menu/main_menu.xml` | `MainActivity` calls `findItem(R.id.menu_dev).isVisible = true` on every debug build. The WIDP menus shadow the shared one and lacked the item, so the call would NPE. Both files need it: the debug source set shadows the other in debug builds. | n/a |
| `url_hint` and `login_https` restored in `app/src/main/res/values/strings.xml` | Upstream deleted them from every `values*` file. The only remaining consumer is `dialog_change_server_url.xml`, which is ours, so resource linking fails without them. | Change Server URL |
| `app/src/main/res/values/ids.xml` (new) | `MainActivity` is shared and handles `R.id.change_url`, but only the WIDP menu declares the item, so **no other flavor compiled** — a defect older than this upgrade. A new shared resource file is level 2 of the placement hierarchy and touches no Oslo file. | Change Server URL |

### Customizations that ended the merge with no delta at all

These are the ones to look at first if something is missing at runtime. Each is
intentional, and each is replaced by something else rather than simply dropped.

| File | What went | Replaced by |
|---|---|---|
| `SyncPresenterImpl.kt` | `syncNotifications()` and its constructor parameter | `PostMetadataSyncAction` — commit 4 |
| `SyncGranularRxModule.kt` | the `notificationRepository` argument | nothing needed; it only fed the line above |
| `App.java` | the Dagger `notificationsModule(...)` call | the Koin module — commit 3 |
| `MainView.kt` / `MainPresenter.kt` | `markShowNotificationsAsPending()` / `refreshNotifications()` | the post-sync action marks it instead — commit 4 |
| `MainActivity.kt` | the notification hooks on `sync_manager` / `menu_home` | same |
| `SearchTEIViewModel.kt` / `SearchTEList.kt` | both `EyeSeeTea fix` patches | absorbed into the baseline, which also improved the second one |


## Open Questions

Decisions that need a person: anything that changes behavior the client sees, touches the
SDK, removes a capability, or departs from the baseline. None of these blocked the upgrade.

1. **Two `EyeSeeTea fix` patches were living on the client branch, against the rule.**
   `conflict-rules.md` says an `EyeSeeTea fix` must land in `develop-eyeseetea` and reach a
   client through a merge, never be carried by the client. `develop-widp` was carrying two
   (TEI search blank-value filter, stale search results). **No action is needed now** —
   both are already in the baseline, so the merge absorbed them and the drift is gone. It
   is recorded because the same thing will happen again: whoever writes the next
   `EyeSeeTea fix` here should open it against the baseline instead.

2. **CI builds and tests only the `widp` flavor, but the branch now carries two.**
   `.github/workflows/eyeseetea-main.yml` was resolved as ours, which runs
   `:app:testWidpDebugUnitTest` where the baseline runs the `eyeseetea` one. That is correct
   for a client branch, but it means nothing in CI ever compiles `eyeseetea` on this branch
   — which is exactly how that flavor came to be broken before this upgrade without anyone
   noticing. Adding `:app:compileEyeseeteaDebugKotlin` to the workflow is a one-line change
   to a fork-owned file. It is not done here because changing a client's CI is the client's
   call, not a conflict resolution.

3. **`app/src/widp/java/org/dhis2/data/user/UserComponentFlavor.kt` is dead code.**
   An empty interface, referenced by nothing on either branch. The baseline's own
   `eyeseetea` flavor does not carry it. Kept because removing it is not needed for
   anything and it is not in the way; it is documented in the inventory rather than
   silently deleted.

4. **ktlint silently stopped checking every Android module.** This is the most consequential
   thing found in this upgrade that nobody was looking for.

   After the toolchain bump the baseline brought (AGP 9.0.1, Gradle 9.3.1), the ktlint Gradle
   plugin no longer discovers Android source sets. `./gradlew :app:ktlintCheck --dry-run` now
   lists only `ktlintKotlinScriptCheck`; on `develop-widp` the same command listed dozens of
   per-source-set tasks. Measured across the tree:

   | Still checked (KMP modules) | No longer checked (Android modules) |
   |---|---|
   | `:tracker`, `:aggregates`, `:commonskmm`, `:login`, `:sync` | `:app`, `:commons`, `:form`, `:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`, `:stock-usecase` |

   It fails open: the task reports success, so CI stays green while eight modules — including
   `:app` — are not linted at all. That is why the violation count fell from 460 to 53; the
   code did not improve, the checker stopped looking.

   **This is not a WIDP problem and must not be fixed here.** It affects every fork equally,
   which by `conflict-rules.md` makes it an `EyeSeeTea fix` that has to land in
   `develop-eyeseetea` and reach the clients through a merge. Recorded here so the next person
   to touch the baseline has the diagnosis rather than the symptom.

5. **2FA with mandatory enrolment not activated shows an error pointing at the
   administrator.** Reproduced on the previous build (`3.4.1-widp-fork-1`), so it is
   pre-existing and not a regression of this upgrade. Changing it would be a functional
   decision WIDP has not been asked to make. Out of scope, recorded so it is not
   rediscovered as an upgrade defect.

## Validation Notes

### Builds actually run

| When | Command | Result |
|---|---|---|
| before the merge | `./gradlew :app:assembleWidpDebug` | BUILD SUCCESSFUL in 29m 39s |
| merge, first attempt | `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin` | FAILED in 14m 35s — `ChangeServerUrlDialog.kt:117 Unresolved reference 'releaseSessionComponent'` |
| merge, after removing that call | `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin` | BUILD SUCCESSFUL in 5m 58s |
| notifications on Koin | `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin` | BUILD SUCCESSFUL in 6m 4s |
| post-sync download | `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin` | BUILD SUCCESSFUL in 51s |
| final | `./gradlew :app:testWidpDebugUnitTest` | 921 tests, **920 passed, 1 failed** |
| final | `./gradlew :login:allTests` | 97 tests, 97 passed |
| final | `./gradlew ktlintCheck --continue` | FAILED — 53 violations, all pre-existing. See below |
| after the comparison changes | `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin` | BUILD SUCCESSFUL in 1m 53s — `dhis2-v3.4.2-widp-fork-1-…apk` |
| after the comparison changes | `./gradlew :app:testWidpDebugUnitTest :login:allTests` | 1115 tests, 1114 passed, the same one failure |

The single unit-test failure is `MainViewModelIntegrationTest > should hide filter and sync
buttons while sync is running`. It is an Oslo test about the home screen's filter and sync
buttons, it touches nothing this upgrade changed, and it is the known flaky failure recorded
for this suite. Not investigated further, and not claimed as passing.

### ktlint: red before and after, and checking far less than it used to

`ktlintCheck` fails. It also failed on `develop-widp` before any of this. Measured, not
assumed — `develop-widp` was checked out into a separate worktree and the same task run
there:

| | violations | files |
|---|---|---|
| `develop-widp` (pre-merge) | 460 | 41 |
| this branch | 53 | 7 |

Every one of the 53 is in a file that was already violating before the merge: the 2FA
customization (`TwoFactorState.kt`, `TwoFactorRequiredException.kt`, `CredentialsScreen.kt`,
`CredentialsViewModel.kt`, `LoginRepositoryImpl.kt`, `D2ErrorMessageProviderImpl.kt`) plus
one baseline-owned file, `DomainErrorMapper.kt`, which is byte-identical to
`develop-eyeseetea` and therefore fails there too. **No file added or edited during this
upgrade violates ktlint.**

The drop from 460 to 53 is **not** an improvement, and reading it as one would be the wrong
conclusion. See the Open Questions entry below: ktlint has stopped checking eight of the
fourteen modules.

Both flavors are built every time, on purpose: CI on this branch runs `widp` only, which
is how `eyeseetea` came to be broken before this upgrade without anyone noticing.

### Customizations checked in code, not on paper

Run after resolving, against the working tree:

| Check | Expected | Got |
|---|---|---|
| `grep -c 'two_factor' commonskmm/.../D2ErrorMessageProviderImpl.kt` | 6 | 6 |
| `grep -c 'saveFileResource(filePath, false)' form/.../FormValueStore.kt` | 1 | 1 |
| `grep -c 'markwon' app/build.gradle.kts` | ≥1 | 1 |
| `ls app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` | exists | exists |
| `ls app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt` | exists | exists |
| `grep -c 'de?.url()' form/.../EventRepository.kt` | 1 | 1 |
| `grep -c 'url?.takeIf' form/.../FieldUiModelExtensions.kt` | 1 | 1 |
| `grep -c 'BASIC_SHARE_PREFS' commonskmm/.../PreferenceConstants.kt` | 1 | 1 |
| `grep -c 'updateServerURL' commons/.../PreferenceProvider{,Impl}.kt` | 1 each | 1 each |

The 2FA one is the check that matters: the baseline's version of that file compiles and
passes every test while silently turning all 2FA messages into "unexpected error".

### Automerge verification

Every file in `customization-files.md` was diffed against the baseline before and after
the merge and the two deltas compared. No customization line was lost. Every delta that
changed size is accounted for in the tables above.

One thing the rule did **not** catch, because the file is not in the inventory:
`app/src/test/.../SearchTEIViewModelTest.kt` automerged into a file holding both the old
WIDP tests and the baseline's, with duplicate test names and calls into an API the
baseline had removed. Resolved to the baseline version, which carries equivalent tests on
the new API. Worth remembering that `git checkout --theirs` silently does nothing on a
file git did not mark as conflicted — the fix has to be
`git checkout <baseline> -- <path>`.

### Device validation — 2026-09-11

Measured on a **Samsung SM-S928B**, package `com.eyeseetea.widp.debug`, version
`3.4.2-widp-fork-1`, installed at 11:54.

Before writing any of this down, both the version **and the package name** were checked:

```
adb shell dumpsys package com.eyeseetea.widp.debug | grep versionName
```

That is not a formality. Two results recorded earlier in this upgrade had to be thrown
away for exactly this reason — see "Results discarded as invalid" below.

#### Confirmed

**The notifications download after a metadata sync works.** This is the highest-risk change
in the upgrade, because the hook it used to run from no longer exists.
Evidence: `last_meta_sync = 11/09/2026 12:00` with `last_meta_sync_status = true`, and
`shared_prefs/BASIC_SHARE_PREFS.xml` written at 12:00 — six minutes after install — holding
the datastore notifications and their `readBy` lists. Nothing else in the app writes that
file, so the `PostMetadataSyncAction` fired.

**Metadata sync brings new metadata down from the server.** A program added to a new user
group on the server appeared in the app after syncing.

#### Not confirmed, and why

**The on-screen dialog.** Both notifications in the datastore were already marked as read by
that user the same morning (09:27 and 09:44), so *not* showing them is the correct
behavior — the spec requires exactly that. Exercising the dialog needs an unread
notification, which this account did not have. The download half is confirmed; the display
half is not, and it is the half the two fixes in the download commit are about.

**2FA with mandatory enrolment not activated** shows an error pointing the user at the
administrator. Reproduced on the previous build (`3.4.1-widp-fork-1`), so it is
**pre-existing, not a regression**. Changing it would be a functional decision WIDP has not
been asked to make. Out of scope — see Open Questions.

#### Results discarded as invalid

Two earlier manual results were thrown out rather than reported:

- a 2FA login that looked successful had actually been run against `3.4.1-widp-fork-1`,
  installed on 21/08 — the wrong build
- a notifications check had been run on the **`dhis2` flavor** (`com.dhis2.debug`), which
  carries no WIDP customization at all. That is why no notification appeared: the feature is
  not in that build

The second one is the dangerous one. The wrong flavor looks exactly like the right app on
the device. Always record the package name next to the version.

#### Still to test on this build

Moved into `upgrade-validation-checklist.md` rather than left here:

- 2FA over TOTP, Email and SMS against this build
- the notification dialog with an unread notification
- background sync with the app closed
- Change Server URL
- image upload without resizing
- the URL data element field
- login against a DHIS2 2.41 server

- manual flows checked: the two confirmed items above; everything else is listed as pending.

## Shared drift still differing

**Empty.** Every file that differs from `develop-eyeseetea` is accounted for, either as a
customization in `customization-files.md` §1–2 or as fork identity in §5 of the same file.
Checked with `git diff develop-eyeseetea..HEAD --stat` over the whole tree.

Three things moved out of the drift during this upgrade, and none of them was a deliberate
deletion of client behavior:

- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` and
  `listView/SearchTEList.kt` — both `EyeSeeTea fix` patches are in the baseline now, so the
  fork carries no delta. The baseline's version of the second one is the better of the two.
- `commons/src/main/java/org/dhis2/commons/prefs/PreferenceModule.kt` — back to its baseline
  content, because the notifications Koin module provides `BasicPreferenceProvider` now.
- `login/build.gradle.kts` — back to its baseline content, because upstream moved the module
  to the KMP `androidLibrary` DSL and dropped product flavors from it. The previous notes
  claimed this had already happened in 3.3.1; it had not. It has now, and the build is the
  evidence rather than the claim.

Two files differ without being a customization and are deliberately kept as fork identity:
`.github/workflows/eyeseetea-main.yml` (runs the WIDP tests) and `.gitignore` (ignores the
local `.journal/` directory). Both are listed in §5 of the inventory.

Expected and correct: `eyeseetea-docs/SDK_Setup.md` and
`eyeseetea-docs/templates/openspec-config.yaml.template` come straight from the two
documentation-only commits the baseline added after `f87bec8c3` (`3cd19be2e`, `8e0200bcc`).
They match the baseline exactly.

## Comparison with the independent resolution

This upgrade was resolved twice, independently. The other resolution is
`feature-widp/bring_last_changes_3_4_2_attempt`; it was not consulted while these conflicts
were being resolved. The two branches agree on every conflict classification and on every
customization that had to survive. Where they differ, each difference is a decision, and the
decisions went both ways.

### Where the other resolution was right, and this branch adopted it

| Difference | Why theirs wins |
|---|---|
| `get<D2>()` instead of `D2Manager.getD2()` in the notifications Koin module | The Koin definition in `serverModule` *instantiates* D2 when it is not up yet, so resolving the graph before login cannot throw. The static can. Also the house DI style. |
| `factory` rather than `single` for `GetNotifications` and `MarkNotificationAsRead` | Stateless use cases; matches how the baseline registers its own. |
| `ids_eyeseetea.xml` rather than `ids.xml` | The whole point of that file is that it can never conflict with Oslo. `ids.xml` is a name Oslo could plausibly add one day; the fork-prefixed one cannot collide. |
| A comment on the Markwon entries in `libs.versions.toml` | The comment convention asks for the customization title. `#Eyeseetea` is not one. |
| Saying in `CredentialsScreen.kt` why `TwoFactorContainer` sits inside the `!oAuthEnable` branch | It records a real decision — the second factor belongs to the username/password flow, not to the OAuth one — that this branch had made silently. |

### Where this branch is right, and kept its own

| Difference | Why |
|---|---|
| `App.kt` untouched | The other resolution re-added `createChangeServerULComponent()` and a `releaseChangeServerURLComponent()` to `App.kt` — about 20 lines in an Oslo file. Building the subcomponent straight from `UserComponent.plus()` needs none of it, and there is no stored reference to release, so the lifetime problem their version manages does not exist here. Level 2 of the placement hierarchy instead of level 4. |
| `AppComponent.java` byte-identical to the baseline | Theirs drops the trailing comma after the last `@Component(modules)` entry, which the baseline has. One line of drift in a shared file, for nothing. |
| `PreferenceModule.kt` back to baseline content | Theirs keeps the Dagger `BasicPreferenceProvider` provider *and* builds another one inline in Koin — two providers for one type, and a shared Oslo file left customized. Publishing it once in the Koin module removes the file from the conflict surface entirely. |
| `commonskmm/.../composeResources/values/strings.xml` drops `openid_login_cancelled` | The baseline deleted that string when the login screen moved to the `:login` module, which now owns it. Theirs keeps it: an unused string, and drift with no customization behind it. |
| `DomainErrorMapper.kt` untouched | Theirs adds a trailing comma there to satisfy ktlint. That file is baseline-owned and carries no WIDP delta; editing it creates exactly the unexplained shared drift the rules forbid, and the fix belongs in `develop-eyeseetea` as an `EyeSeeTea fix`. |
| `app/src/widp/java/org.dhis2.utils/CustomizableConstants.kt` deleted | Unreferenced on both sides, and the baseline deleted its identical copy from `app/src/eyeseetea/` in this same upgrade. Keeping it would leave one dead file alone in a directory named `org.dhis2.utils`. |
| `ActivityGlobalAbstract` guards presenter resolution on the server component | Theirs resolves unconditionally in `onCreate`, on every screen including the ones before login. It does not crash, because `get<D2>()` instantiates D2 — but it means a blocking D2 instantiation can be triggered from a screen that has no use for notifications. The guard costs three lines. |
| The comment on `DownloadNewVersion.kt` | Theirs carries `// EyeSeeTea customization - Change Server URL` on a file that has nothing to do with Change Server URL. A wrong title is worse than none: the convention exists so a reader can map a comment to a spec. This branch labels it as flavor scaffolding, which is what it is. |

### Cosmetic differences, left alone

Where `onChangeServerURL()` sits in `MainActivity`, whether the pending-flag rule lives in
`ShowNotifications.markPending()` or in the presenter, blank lines in the two menus and in
`strings.xml`, and comment wording throughout. None of it changes behavior.

### The one real disagreement left open: ktlint formatting

The other resolution reformatted the 2FA customization — `TwoFactorState.kt`,
`TwoFactorRequiredException.kt`, `CredentialsScreen.kt`, `CredentialsViewModel.kt`,
`LoginRepositoryImpl.kt`, `D2ErrorMessageProviderImpl.kt` — to satisfy ktlint. This branch
reapplied it verbatim, on the rule in `conflict-rules.md`: do not clean up or reformat during
a merge.

Their formatting is better code, and if the choice were only about our own files it would be
worth taking. It is not, and that is what settles it: even after reformatting all six, the
gate stays red on `DomainErrorMapper.kt`, which is baseline-owned. The other branch only gets
to green by editing that baseline file — the drift ruled out above. So neither branch can pass
`ktlintCheck` without breaking a rule, and this one prefers a red gate with a written reason
over green with unexplained drift.

The real fix is upstream anyway, and bigger than formatting: see the Open Question about
ktlint no longer checking eight of the fourteen modules at all.

### Expected differences, not defects

The two documentation-only commits the baseline added after the other branch merged
(`3cd19be2e`, `8e0200bcc`) mean `eyeseetea-docs/SDK_Setup.md` and
`eyeseetea-docs/templates/openspec-config.yaml.template` differ between the branches. This
branch merged the current tip, as intended. The inventory, the corrected fork identity and
`AGENTS-widp.md` also differ, because the other branch left that work pending.

## Finalization

- surviving customizations moved to `customization-files.md`: `yes` — rewritten against the
  post-merge tree on 2026-09-11
- stable rules moved to `conflict-rules.md`: `n/a` — this upgrade produced no new reusable
  rule. The two habits worth keeping are already written there: verify every inventory file
  with a two-dot diff, not only the conflicted ones, and keep the inventory complete enough
  for that rule to fire
- temporary notes ready to archive/remove: `no` — keep until the pending device validation
  in `upgrade-validation-checklist.md` is done
- unexplained shared drift remaining: `no`

### Worth promoting to the shared docs, but not done here

Two findings from this upgrade generalise beyond WIDP, and both belong in
`eyeseetea-docs/upgrade/conflict-rules.md`, which is baseline-owned. Writing them from a
client branch would break the same rule this upgrade is enforcing, so they are recorded here
for whoever next touches the baseline:

1. **`git checkout --theirs` silently does nothing on a file git did not mark as
   conflicted.** It fails quietly and leaves the automerged mixture in place. The reliable
   form is `git checkout <base-branch> -- <path>`. This cost one broken test file that
   compiled fine as a merge result and only failed once the tests were run.
2. **The automerge rule cannot see a customization whose host file was deleted upstream.**
   `App.java` hosted two pieces of WIDP wiring; when upstream deleted it, the callers —
   WIDP-only files git never touched — were left referencing methods that no longer existed.
   No conflict, no inventory entry, no diff to inspect. Only the compiler found them. The
   rule should say: after a merge, also check the **callers** of anything the baseline
   deleted, not just the files the inventory lists.
