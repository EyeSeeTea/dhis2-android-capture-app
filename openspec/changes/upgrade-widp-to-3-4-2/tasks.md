# Tasks — WIDP upgrade to 3.4.2

Commit boundaries are stated explicitly per group, so that anyone picking this file up
without the original conversation can tell where each commit ends. The rule that decides
them: **does the tree need this in order to compile? Then it belongs in the merge commit.
Otherwise it is its own commit.**

## 1. Plan the upgrade

- [x] 1.1 Read the shared fork documentation from `develop-eyeseetea` (it is stale on the
  client branch): `upgrade-plan-client-forks.md`, `conflict-rules.md`, `README.md`,
  `customization-techniques.md`, `onboarding-fork-guide.md`.
- [x] 1.2 Write this OpenSpec change: proposal, design, tasks.
- [x] 1.3 Create `eyeseetea-docs/upgrade/widp/upgrade-3.4.2-notes.md` from
  `eyeseetea-docs/upgrade/template/upgrade-notes-template.md`.
- [x] 1.4 Verify the pre-merge tree still builds, so that a later failure is attributable
  to the merge: `./gradlew :app:assembleWidpDebug`.
  **Commit 1:** 1.1–1.4 are one commit, documentation only (`docs(upgrade):`).

## 2. Merge the baseline and make the tree compile

- [x] 2.1 Merge `origin/develop-eyeseetea` (`8e0200bcc`) into the upgrade branch. Do not
  merge Oslo directly.
- [x] 2.2 Before editing any file, classify every conflict into the four categories of
  `conflict-rules.md`, recording for each one the behavior that must survive. Write the
  table into `upgrade-3.4.2-notes.md`.
- [x] 2.3 Resolve the `accept_ours` and `accept_theirs` conflicts.
- [x] 2.4 Resolve the `manual_reapply_on_theirs` conflicts: start from the baseline file,
  reinsert only the WIDP delta, keep the `// EyeSeeTea customization - [Title]` comment.
- [x] 2.5 Run the post-merge fork identity check: version name and code, SDK tag, flavor
  source sets, flavor declaration in `app/build.gradle.kts`, per-client files
  (`google-services.json`) not overwritten by the baseline.
- [x] 2.6 Move `GranularSyncModule.kt` out of the malformed `app/src/widp/java/org.dhis2.utils/`
  directory into `app/src/widp/java/org/dhis2/utils/granularsync/`, on the 3.4.x API.
- [x] 2.7 Add `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt`:
  upstream made this a per-flavor file, and WIDP's behavior is the in-app APK download.
- [x] 2.8 Add an empty `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt`. It must
  exist in every flavor source set or the build fails; filling it is task 4.
- [x] 2.9 Add the `menu_dev` entry to the WIDP menu, in **both**
  `app/src/widp/res/menu/` and `app/src/widpDebug/res/menu/` — the debug source set
  shadows the other one in debug builds.
- [x] 2.10 Restore the string resources upstream deleted that the WIDP-owned
  `dialog_change_server_url.xml` still references, or resource linking fails.
- [x] 2.11 Make `R.id.change_url` resolvable for every flavor without editing an Oslo
  file. `MainActivity` is shared and references it; today only the WIDP menu declares it,
  so no other flavor compiles. Pre-existing defect, fixed here because the build needs it.
- [x] 2.12 Repair `SearchTEIViewModelTest.kt`: it automerges without conflict but keeps
  both the old and the new tests, with duplicate names and calls to a deleted API.
- [x] 2.13 Run the automerge verification rule: two-dot diff **every** file listed in
  `customization-files.md`, not only the conflicted ones, and recover anything the
  automerge dropped silently.
- [x] 2.14 Verify: `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin`.
  Both flavors, always — CI only builds `widp`, so an `eyeseetea` break is invisible.
  **Commit 2:** 2.1–2.14 are one commit, the merge itself. It contains the conflict
  resolution plus only what the tree needs in order to compile. Notifications stay on
  Dagger here: that compiles. Their migration is commits 3 and 4.

## 3. Re-anchor the notifications DI graph on Koin

- [x] 3.1 Publish the notifications graph (repository, user repository, use cases,
  presenter) as a Koin module and register it.
- [x] 3.2 Resolve the presenter from `ActivityGlobalAbstract` through Koin, replacing the
  Dagger field injection Oslo stopped running when it migrated `MainActivity`.
- [x] 3.3 Remove the notifications Dagger module and component now that nothing builds
  them.
- [x] 3.4 Verify: `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin`.
  **Commit 3:** 3.1–3.4 are one commit (`fix(notifications):`). Separate from the merge
  because it fixes a **runtime** failure, not a build failure: the inherited presenter is
  left null and the main screen crashes on entry. Nothing here is needed to compile.

## 4. Download notifications after a metadata sync

- [x] 4.1 Register the notifications download as a `PostMetadataSyncAction` in
  `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt`, replacing the removed
  `SyncPresenterImpl.syncMetadata().doOnComplete {}` hook.
- [x] 4.2 Stop the "a notification is pending" flag from being consumed when nothing was
  actually shown — with no screen in front of the download, consuming it loses the
  notification.
- [x] 4.3 Notify the visible screen when the download finishes, so the dialog appears
  without requiring the user to navigate away and back.
- [x] 4.4 Verify: `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin`.
  **Commit 4:** 4.1–4.3 are one commit (`fix(notifications):`). 4.2 and 4.3 ship with 4.1
  rather than separately: they are only reachable once the download has no screen in front
  of it, so they have no meaning without it.

## 5. Regression coverage for notifications

- [x] 5.1 Add presenter-level tests for the two display rules the download commit changes:
  the pending flag must survive an empty list, and marking pending must reach the screen
  that is currently up. (Repository filtering was already covered by
  `NotificationD2RepositoryTest`; nothing new was needed there.)
- [x] 5.2 Add a WIDP-flavor test asserting the post-metadata-sync wiring is registered, that
  the action downloads then marks pending, and that a failed download marks nothing.
- [x] 5.3 Verify: `./gradlew :app:testWidpDebugUnitTest` over the three notification test
  classes — 18 tests, 18 passed.
  **Commit 5:** 5.1–5.3 are one commit (`test(notifications):`), and it is its own commit
  rather than being folded into 3 or 4: this is coverage added over an implementation that
  already passes, not the green half of a red-green pair.

## 6. Validate on a device

- [x] 6.1 Install the WIDP debug build on a device and confirm **both** the package name
  and the version before recording anything:
  `adb shell dumpsys package com.eyeseetea.widp.debug | grep versionName`.
  A result recorded against another flavor is worse than no result.
- [x] 6.2 Exercise metadata sync and confirm the notifications download fires.
- [x] 6.3 Record every outcome — confirmed, unconfirmed, and invalid — in
  `upgrade-3.4.2-notes.md`, and move what is still untested into
  `upgrade-validation-checklist.md`.
  **Commit 6:** 6.1–6.3 are one commit (`docs(upgrade):`), documentation only.

## 7. Make the inventory and the fork identity true

- [x] 7.1 Rewrite `eyeseetea-docs/customizations/widp/customization-files.md` against the
  tree that exists after the merge: it currently lists files upstream deleted and at least
  one file that never existed.
- [x] 7.2 Correct the false statements in the fork identity: version, SDK tag, Gradle
  version, module count and list, the unit-test task name, and the status of
  `url-data-element`.
- [x] 7.3 Resolve `Shared drift still differing`: every file that differs from the baseline
  is either documented in the inventory or removed.
- [x] 7.4 Verify: `openspec validate --all --strict` and
  `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp`.
  **Commit 7:** 7.1–7.4 are one commit (`docs(upgrade):`), documentation only.

## 8. Adopt the `AGENTS-<client>.md` convention

- [ ] 8.1 Create `AGENTS-widp.md` from
  `eyeseetea-docs/templates/AGENTS-CLIENT.md.template`, filling the placeholders with the
  real post-merge values and adapting the parts of the template that do not apply (it
  references an `upgrade-<version>-strategy.md` that does not exist for 3.4.2, and Gradle
  task names that need checking).
- [ ] 8.2 Reduce `CLAUDE.md` to Oslo's four-line bridge importing `@AGENTS.md` and
  `@AGENTS-widp.md`. Never edit `AGENTS.md` itself.
  **Commit 8:** 8.1–8.2 are one commit (`docs:`), last in the series and removable on its
  own — it adopts a convention rather than doing upgrade work.

## Final verification

- [ ] 9.1 `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin :app:testWidpDebugUnitTest :login:allTests ktlintCheck`
- [ ] 9.2 Confirm each of the five capabilities survives **in code**, not on paper.
- [ ] 9.3 Confirm the branch declares only the three Oslo flavors plus `eyeseetea` and
  `widp`, and no other client's flavor.
