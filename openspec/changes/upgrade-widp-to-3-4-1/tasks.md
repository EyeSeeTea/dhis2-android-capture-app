## 1. Prerequisites

- [x] 1.1 Create `eyeseetea-docs/upgrade/widp/upgrade-3.4.1-notes.md` from `eyeseetea-docs/upgrade/template/upgrade-notes-template.md` for live conflict decisions
- [x] 1.2 Confirm SDK source mode in `local.properties` (`useLocalSdk`) and record which mode the upgrade is being built in, per `eyeseetea-docs/SDK_Setup.md` — JitPack (property unset, `settings.gradle.kts` defaults to `false`)
- [x] 1.3 Snapshot the pre-merge baseline for later comparison: save `git diff origin/develop-eyeseetea..HEAD --stat` and the `customization-files.md` inventory as the checklist for task 5 — 45 files carry a customization marker; note that `develop-eyeseetea` exists only as `origin/develop-eyeseetea` in this checkout

## 2. Merge and mechanical resolution

- [ ] 2.1 Merge `origin/upstream/3.4.1` into `feature/upgrade_widp_to_3_4_1` without committing; record the actual conflict list and diff it against the 27 predicted in design.md D2
- [ ] 2.2 Resolve `accept_theirs`: `app/src/androidTest/assets/databases/dhis_test.db`, `gradle.properties`
- [ ] 2.3 Resolve `accept_ours`: `CLAUDE.md` (add/add — keep the WIDP fork identity doc)
- [ ] 2.4 Resolve fork identity in `gradle/libs.versions.toml`: `dhis2sdk = 1.14.1-eyeseetea-fork-1`, `vName = 3.4.1-widp-fork-1`, `vCode = 156`
- [ ] 2.5 Resolve `app/build.gradle.kts`: upstream body + `widp` flavor block, `com.eyeseetea.widp` applicationId, fork versionName
- [ ] 2.6 Resolve `login/build.gradle.kts`: upstream module config + WIDP 2FA dependencies
- [ ] 2.7 Resolve the Oslo search-patch conflicts: `SearchTEList.kt`, `SearchTEIViewModel.kt`, `SyncPresenterTest.kt` (upstream body, re-apply WIDP's PR #315 patches)
- [ ] 2.8 Resolve the two string resource conflicts: `app/src/main/res/values/strings.xml`, `commonskmm/src/commonMain/composeResources/values/strings.xml`
- [ ] 2.9 **Developer review checkpoint** — walk the mechanical resolutions before starting judgement-heavy work

## 3. Preserve customization: 2FA

- [ ] 3.1 Resolve `LoginRepositoryImpl.kt` on the upstream body, passing the **real** `twoFactorCode` to the 4-arg `blockingLogIn` — explicitly NOT the baseline's `null`
- [ ] 3.2 Resolve `LoginUser.kt`, `CredentialsUiState.kt` keeping WIDP's 2FA state model
- [ ] 3.3 Resolve `CredentialsViewModel.kt` on the upstream body, re-applying WIDP's 2FA logic (type detection, resend, 30s cooldown, rate-limit)
- [ ] 3.4 Resolve `CredentialsScreen.kt` on the upstream body, re-applying WIDP's 2FA UI (per-type field labels, resend buttons, info messages)
- [ ] 3.5 Audit `D2ErrorMessageProviderImpl.kt` (silent automerge): confirm WIDP's seven real 2FA messages survived and were NOT replaced by the baseline's `defaultError()` mapping
- [ ] 3.6 Audit the remaining 2FA silent-automerge files: `LoginRepository.kt`, `login/src/commonMain/composeResources/values/strings.xml`, `CredentialsViewModelTest.kt`
- [ ] 3.7 Verify every requirement in `openspec/specs/two-factor-auth/spec.md` still has an implementation site; run `./gradlew :login:testDebugUnitTest`

## 4. Preserve customization: Change Server URL, notifications, image upload, URL data element

- [ ] 4.1 Change Server URL — re-anchor the Dagger wiring from the deleted `App.java` onto `App.kt`; resolve `UserComponent.java` (`plus(ChangeServerURLModule)`) and `PreferenceConstants.kt`
- [ ] 4.2 Change Server URL — audit `ActivityGlobalAbstract.java` (silent automerge): confirm the settings menu entry survived
- [ ] 4.3 Notifications — determine which 3.4.1 DI module replaces the deleted `Sync{Data,Init,Metadata}WorkerModule` for providing `NotificationRepository` (design.md open question); stop and ask if there is no clean site
- [ ] 4.4 Notifications — port `markShowNotificationsAsPending()` / `refreshNotifications()` from the deleted `MainView.kt` / `MainPresenter.kt` onto `MainViewModel`, preserving the lifecycle moment they fired at
- [ ] 4.5 Notifications — re-anchor `NotificationsModule` in the Dagger graph on `App.kt`; resolve `MainActivity.kt`, `SyncPresenterImpl.kt`, `SyncGranularRxModule.kt`
- [ ] 4.6 Notifications — confirm the 14 untouched files under `usescases/notifications/**` and `data/notifications/**` are intact and reachable; run `./gradlew :app:testWidpDebugUnitTest --tests '*NotificationD2RepositoryTest*'`
- [ ] 4.7 URL data element — audit `EnrollmentRepository.kt` / `EventRepository.kt` (silent automerge): confirm the data plumbing survived unchanged; do NOT restore rendering (out of scope)
- [ ] 4.8 Image upload without resizing — verify the customization against `customization-files.md`; it has no predicted conflict, so confirm rather than assume
- [ ] 4.9 Audit `SearchTEIViewModelTest.kt` (silent automerge) against the PR #315 search patches

## 5. Mandatory automerge audit

- [ ] 5.1 Run `git diff develop-eyeseetea -- <path>` for **every** file listed in `customization-files.md`, not only the conflicted ones; recover anything git dropped silently
- [ ] 5.2 Cross-check each customization's feat commits (`customization-files.md` section 4) with `git show <commit> --stat` so no wiring file is missing from the audit
- [ ] 5.3 Verify every customized file still carries `// EyeSeeTea customization - <Title>` with the title matching its `openspec/specs/<capability>/spec.md` heading, and none on import lines
- [ ] 5.4 Post-merge fork identity check per `conflict-rules.md`: applicationId, versionName/vCode, `app/src/widp*` source sets intact, SDK fork dependency (not `org.hisp.dhis:android-core`), and no file that arrived from `develop-eyeseetea`

## 6. Build and complete the merge

- [ ] 6.1 `./gradlew assembleWidpDebug` green; resolve any `defer_after_build_verification` files left open and record each decision in the notes file
- [ ] 6.2 `./gradlew ktlintCheck` green
- [ ] 6.3 `./gradlew testWidpDebugUnitTest` green; triage every failure as upstream-expected vs customization regression
- [ ] 6.4 Verify the resolved SDK: `./gradlew :app:dependencies --configuration widpDebugRuntimeClasspath | grep dhis2-android-sdk` shows `1.14.1-eyeseetea-fork-1`
- [ ] 6.5 Commit the merge with the developer's git identity

## 7. Oslo patches missing from WIDP

- [ ] 7.1 Cherry-pick `1e4149f01` (ANDROAPP-7661, granular sync image download race); verify it still applies to 3.4.1 or record why it was skipped
- [ ] 7.2 Cherry-pick `48058eb9a` (ANDROAPP-7666, completed-event dialog always shown); same verification
- [ ] 7.3 Cherry-pick `726f3bd7e` (empty list on return from TEI after search); same verification
- [ ] 7.4 Re-run `assembleWidpDebug` + `testWidpDebugUnitTest` after the cherry-picks

## 8. Manual validation

- [ ] 8.1 Add or update the `upgrade-validation-checklist.md` entry for each customization touched in tasks 3 and 4, so every preservation task has a matching manual flow
- [ ] 8.2 Validate notifications end-to-end on an emulator/device (highest risk — pending badge, refresh after sync, mark as read)
- [ ] 8.3 Validate Change Server URL (menu entry visible, warning dialog, URL actually switches)
- [ ] 8.4 Validate 2FA against `preprod-indiv`: TOTP, Email and SMS flows, resend, 30s cooldown, rate-limit message
- [ ] 8.5 Validate image upload without resizing
- [ ] 8.6 Validate login + send an event against DHIS2 2.41 and 2.43 (`dev.eyeseetea`) with the provided test user
- [ ] 8.7 Record every executed flow and its result in `upgrade-3.4.1-notes.md`

## 9. Close out

- [ ] 9.1 Update `eyeseetea-docs/customizations/widp/customization-files.md` with the surviving file surface after the port (line anchors changed for notifications and 2FA)
- [ ] 9.2 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp`
- [ ] 9.3 Run `openspec validate --strict`
- [ ] 9.4 Open the PR against `develop-widp` with the clean incremental diff, listing target version, SDK tag, customizations verified, and validation flows executed
- [ ] 9.5 Record the follow-up: move notifications wiring into `app/src/widp/` to shrink the Oslo footprint (deferred from design.md D3), and the separate `url-data-element` rendering restoration
