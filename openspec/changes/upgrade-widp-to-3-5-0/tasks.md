# Tasks — WIDP upgrade to 3.5.0

Commit boundaries are stated explicitly per group, so that anyone picking this file up
without the original conversation can tell where each commit ends. The rule that decides
them: **does the tree need this in order to compile? Then it belongs in the merge commit.
Otherwise it is its own commit.**

## 1. Plan the upgrade

- [x] 1.1 Dry-run the merge (`git merge --no-commit --no-ff develop-eyeseetea`, then
  `--abort`) and confirm the working tree is clean afterwards: 14 conflicts recorded.
- [x] 1.2 Create `eyeseetea-docs/upgrade/widp/upgrade-3.5.0-notes.md` from the template,
  with the 14 conflicts classified before any file is edited.
- [x] 1.3 Write this OpenSpec change (proposal, design, tasks) and verify
  `openspec validate upgrade-widp-to-3-5-0 --strict` passes.
- [x] 1.4 Verify the pre-merge tree still builds, so a later failure is attributable to the
  merge: `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin`.
  **Commit 1:** 1.1–1.4 are one commit, documentation only (`docs(upgrade):`).

## 2. Merge the baseline and make the tree compile

- [ ] 2.1 Merge `develop-eyeseetea` (`173334dbb`) into the upgrade branch with a merge
  commit. Verify `git merge-base --is-ancestor develop-eyeseetea HEAD` succeeds.
- [ ] 2.2 Resolve `gradle/libs.versions.toml`: baseline `vCode`/`gradle`/`kotlin`/`dhis2sdk`,
  `vName = "3.5.0-widp-fork-1"`, Markwon entries kept. Verify with
  `grep -E '^(vName|vCode|dhis2sdk|gradle|kotlin)' gradle/libs.versions.toml`.
- [ ] 2.3 Resolve both `strings.xml` (login and commonskmm) as a union of the 2FA strings and
  the baseline's new ones. Verify no conflict marker remains and the 2FA keys are present.
- [ ] 2.4 Resolve `D2ErrorMessageProviderImpl.kt`: take the baseline's OAuth2 branches and
  split the 2FA arm so the six messages return. Verify
  `grep -c 'two_factor' D2ErrorMessageProviderImpl.kt` is 6 and no 2FA code is matched by
  an earlier arm.
- [ ] 2.5 Resolve the login data and domain layers (`LoginRepository`, `LoginRepositoryImpl`,
  `LoginUser`) per design D3: `twoFactorCode` is passed to `blockingLogIn`, 2FA codes are
  handled before `DomainErrorMapper`, everything else falls through. Verify the module
  compiles: `./gradlew :login:compileAndroidMain`.
- [ ] 2.6 Resolve the login UI layer (`CredentialsUiState`, `CredentialsViewModel`,
  `CredentialsScreen`) on `CredentialsEntryMode`: 2FA applies to the password modes only.
  Verify the module compiles and the 2FA field is reachable from the screen.
- [ ] 2.7 Resolve `OpenIdSession.kt`: baseline merge plus the fork's `logOutObservable`
  source as `LogOutReason.UNAUTHORIZED`, per design D4. Verify
  `./gradlew :app:compileWidpDebugKotlin`.
- [ ] 2.8 Resolve `MainActivity.kt` (6 hunks): baseline structure, then the notifications
  hooks on `GoToLogin`, `SingleProgramNavigation` and resume, and `R.id.change_url`. Verify
  each customization comment sits right above its block.
- [ ] 2.9 Resolve `FormValueStore.kt`: `saveFileResource(path = filePath, uid = uid,
  isImage = false)` with the customization comment and the original commented out. Verify
  the call compiles against the new signature.
- [ ] 2.10 Take the baseline `CredentialsViewModelTest.kt` unchanged so the merge compiles;
  the 2FA cases come back in group 3.
- [ ] 2.11 Run the post-merge fork identity check: version name and code, SDK tag, flavor
  declaration in `app/build.gradle.kts`, per-client files (`google-services.json`) not
  overwritten by the baseline.
- [ ] 2.12 Run the automerge verification rule: two-dot diff **every** file listed in
  `customization-files.md` (`git diff develop-eyeseetea..HEAD -- <file>`), including the
  silent ones in the notes file, and recover anything the automerge dropped. Specifically
  confirm nothing of Change Server URL depended on the deleted `UserManager*.java`.
- [ ] 2.13 Verify: `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin`.
  Both flavors, always.
  **Commit 2:** 2.1–2.13 are one commit, the merge itself. It carries the conflict
  resolution plus only what the tree needs in order to compile.

## 3. Regression coverage for 2FA

- [x] 3.1 Port the 2FA cases onto the baseline `CredentialsViewModelTest.kt`: code required,
  code rejected, code sent, resend. Verify
  `./gradlew :login:testAndroidHostTest --tests "*CredentialsViewModelTest"` passes.
- [x] 3.2 Add a repository-level test that a 2FA `D2Error` reaches `TwoFactorOutcome` and is
  **not** mapped to `AuthenticationError`, and that any other `D2Error` still goes through
  `DomainErrorMapper`. Verify it fails when the order of the two branches is swapped.
- [x] 3.3 Verify `./gradlew :login:allTests`.
  **Commit 3:** 3.1–3.3 are one commit (`test(2fa):`). Coverage over an implementation that
  already passes, not the green half of a red-green pair.

## 4. Reconcile the session flows

- [ ] 4.1 Read how an unauthorized session and an expired OAuth/OpenID session each reach
  login on the merged tree, and write the finding into `upgrade-3.5.0-notes.md`.
- [ ] 4.2 If the two flows stack (login opened twice, or the renewal dialog followed by a
  logout), filter the fork's source in `OpenIdSession` without editing the baseline's
  classes. Verify with a unit test on the callback, or record "no change needed" with the
  evidence from 4.1.
  **Commit 4:** 4.1–4.2 are one commit (`fix(2fa):` or `docs(upgrade):` if nothing had to
  change).

## 5. Validate on a device

- [ ] 5.1 Install the WIDP debug build and confirm **both** package name and version before
  recording anything: `adb shell dumpsys package com.eyeseetea.widp.debug | grep versionName`
  must show `3.5.0-widp-fork-1`.
- [ ] 5.2 2FA: log in with a TOTP account, an email account and an SMS account; check the
  code is asked, a wrong code is reported, and resend works. Each outcome recorded.
- [ ] 5.3 Session: make the server reject the session and confirm the app returns to login
  exactly once (design D4).
- [ ] 5.4 Image upload: capture a picture in a data element and compare the stored file with
  the original; it must not be resized (design D5).
- [ ] 5.5 Notifications: sync metadata and confirm the dialog appears; navigate through the
  main screen to the single-program path and back.
- [ ] 5.6 Change Server URL and the URL data element: open the menu action, change the URL,
  and open a URL data element.
- [ ] 5.7 Record every outcome — confirmed, unconfirmed and invalid — in
  `upgrade-3.5.0-notes.md`, and move what is still untested into
  `upgrade-validation-checklist.md`.
  **Commit 5:** 5.1–5.7 are one commit (`docs(upgrade):`), documentation only.

## 6. Make the inventory and the fork identity true

- [ ] 6.1 Rewrite `eyeseetea-docs/customizations/widp/customization-files.md` against the
  merged tree: drop files the baseline deleted (`UserManager*.java`), add any new wiring file,
  and cross-check `git show <feat-commit> --stat` per customization.
- [ ] 6.2 Update `AGENTS-widp.md` (current version, SDK tag, Gradle and Kotlin versions, the
  upgrade notes path) and `openspec/config.yaml` (current version, upgrade branch, SDK).
- [ ] 6.3 Resolve `Shared drift still differing`: every file that differs from the baseline
  is either documented in the inventory or removed.
- [ ] 6.4 Verify: `openspec validate --all --strict` and
  `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp`.
  **Commit 6:** 6.1–6.4 are one commit (`docs(upgrade):`), documentation only.

## Final verification

- [ ] 7.1 `./gradlew :app:assembleWidpDebug :app:compileEyeseeteaDebugKotlin
  :app:testWidpDebugUnitTest :login:allTests ktlintCheck` all green.
