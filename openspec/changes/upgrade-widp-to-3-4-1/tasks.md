## 1. Prerequisites

- [x] 1.1 Create `eyeseetea-docs/upgrade/widp/upgrade-3.4.1-notes.md` from `eyeseetea-docs/upgrade/template/upgrade-notes-template.md` for live conflict decisions
- [x] 1.2 Confirm SDK source mode in `local.properties` (`useLocalSdk`) and record which mode the upgrade is being built in, per `eyeseetea-docs/SDK_Setup.md` — JitPack (property unset, `settings.gradle.kts` defaults to `false`)
- [x] 1.3 Snapshot the pre-merge baseline for later comparison: save `git diff origin/develop-eyeseetea..HEAD --stat` and the `customization-files.md` inventory as the checklist for task 5 — 45 files carry a customization marker; note that `develop-eyeseetea` exists only as `origin/develop-eyeseetea` in this checkout

## 2. Merge and mechanical resolution

- [x] 2.1 Merge `origin/upstream/3.4.1` into `feature/upgrade_widp_to_3_4_1` without committing; record the actual conflict list and diff it against the 27 predicted in design.md D2
- [x] 2.2 Resolve `accept_theirs`: `app/src/androidTest/assets/databases/dhis_test.db`, `gradle.properties`
- [x] 2.3 Resolve `accept_ours`: `CLAUDE.md` (add/add — keep the WIDP fork identity doc)
- [x] 2.4 Resolve fork identity in `gradle/libs.versions.toml`: `dhis2sdk = 1.14.1-eyeseetea-fork-1`, `vName = 3.4.1-widp-fork-1`, `vCode = 156`
- [x] 2.5 Resolve `app/build.gradle.kts`: upstream body + `widp` flavor block, `com.eyeseetea.widp` applicationId, fork versionName
- [x] 2.6 Resolve `login/build.gradle.kts`: upstream module config + WIDP 2FA dependencies
- [x] 2.7 Resolve the Oslo search-patch conflicts: `SearchTEList.kt` (fix absorbed upstream → accept_theirs), `SearchTEIViewModel.kt` (ANDROAPP-6844 re-applied on the new `queryDataList` API, matches the baseline byte for byte). `SyncPresenterTest.kt` moved to task 4.5 — it is notifications wiring, not a search patch
- [x] 2.8 Resolve the two string resource conflicts: `app/src/main/res/values/strings.xml`, `commonskmm/src/commonMain/composeResources/values/strings.xml`
- [ ] 2.9 **Developer review checkpoint** — walk the mechanical resolutions before starting judgement-heavy work

## 3. Preserve customization: 2FA

- [x] 3.1 Resolve `LoginRepositoryImpl.kt` on the upstream body, passing the **real** `twoFactorCode` to the 4-arg `blockingLogIn` — explicitly NOT the baseline's `null`
- [x] 3.2 Resolve `LoginUser.kt`, `CredentialsUiState.kt` keeping WIDP's 2FA state model
- [x] 3.3 Resolve `CredentialsViewModel.kt` on the upstream body, re-applying WIDP's 2FA logic (type detection, resend, 30s cooldown, rate-limit)
- [x] 3.4 Resolve `CredentialsScreen.kt` on the upstream body, re-applying WIDP's 2FA UI (per-type field labels, resend buttons, info messages)
- [x] 3.5 Audit `D2ErrorMessageProviderImpl.kt` (silent automerge): confirm WIDP's seven real 2FA messages survived and were NOT replaced by the baseline's `defaultError()` mapping
- [x] 3.6 Audit the remaining 2FA silent-automerge files: `LoginRepository.kt`, `login/src/commonMain/composeResources/values/strings.xml`, `CredentialsViewModelTest.kt`
- [x] 3.7 Verify every requirement in `openspec/specs/two-factor-auth/spec.md` still has an implementation site — all present (`TwoFactorState`/`TwoFactorType` in the domain model, `TwoFactorContainer` composable at `CredentialsScreen.kt:802`, resend + cooldown in the ViewModel, 7 error codes mapped). Test run deferred to task 6.3 (needs the whole tree to compile)

## 4. Preserve customization: Change Server URL, notifications, image upload, URL data element

- [x] 4.1 Change Server URL — `UserComponent.java` (kept `plus(ChangeServerURLModule)`), `PreferenceConstants.kt`, and the `App.java` → `App.kt` re-anchoring (`changeServerURLComponent` field + `createChangeServerULComponent()`). Menu item untouched: it lives in `app/src/widp/res/menu/main_menu.xml`
- [x] 4.2 Audit `ActivityGlobalAbstract.java` (silent automerge) — belongs to **Notifications**, not Change Server URL: both `notificationsPresenter.refresh(this)` hooks (`onCreate`, `onResume`) survived intact; only upstream's `analyticsHelper` → `getAnalyticsHelper()` refactor differs. The Change Server URL menu entry lives in `MainActivity` (task 4.5)
- [x] 4.3 Notifications — resolved: there is **no** replacement site. `SyncPresenterImpl` no longer takes `NotificationRepository` at all (upstream moved full sync to `:sync`), so the three Dagger modules were accepted as deleted. Stopped and asked the developer; the download was re-anchored to a sync-finished effect (option A). See the notes file
- [x] 4.4 Notifications — asymmetry ported: `HomeEffect.SingleProgramNavigation` marks pending only; `initCurrentScreen()`'s `R.id.menu_home` marks pending **and** refreshes. `MainView`'s indirection dropped — calls go straight to `notificationsPresenter`
- [x] 4.5 Notifications — `NotificationsModule` re-anchored in `App.kt`'s builder (`AppComponent.java` already carried it). `MainActivity.kt` (7 hunks) resolved; `SyncPresenterImpl.kt`/`SyncGranularRxModule.kt`/`SyncPresenterTest.kt` accepted from upstream since the notification argument disappeared with the sync extraction. New: `SyncNotifications` use case + `NotificationsPresenter.syncNotifications()` + `HomeEffect.SyncNotifications`
- [x] 4.6 Notifications — the 14 files under `usescases/notifications/**` and `data/notifications/**` are intact; `NotificationD2RepositoryTest` passes
- [x] 4.7 URL data element — audited: `EnrollmentRepository.kt` (6 markers) and `EventRepository.kt` (5) survived the automerge unchanged. Rendering untouched, as scoped
- [x] 4.8 Image upload without resizing — confirmed present in `form/src/main/java/org/dhis2/form/data/FormValueStore.kt`; no conflict, no drift
- [x] 4.9 Audited `SearchTEIViewModelTest.kt` — the ANDROAPP-6844 test survived the automerge

## 5. Mandatory automerge audit

- [x] 5.1 Audit done — **method corrected**: `CLAUDE.md` says to diff against `develop-eyeseetea`, but that baseline carries none of WIDP's customizations, so the diff is meaningless here. Compared marker counts per file against `develop-widp` (pre-merge) instead. 48 marked files before → 50 after. The only 4 files that lost all markers are the deliberately handled ones (`App.java` ported to `App.kt`; `MainPresenter`/`MainView` deleted upstream; `SyncPresenterTest` lost the ctor arg that no longer exists). One partial loss, `MainActivity` 7→6, fully accounted for: 3 removed (dead flag, its `backPressed` branch, the `MainView` override) and 2 added
- [x] 5.2 Cross-checked the 17 feat commits → 45 code files. 10 no longer exist (6 are `.java` files converted to `.kt` long before this merge; 4 handled by this merge). 11 exist without a marker — **none of them had one before the merge either**, so no silent loss. Of those 11, 4 carry real customization content and were missing the comment; markers added (see 5.3). The other 5 no longer hold WIDP content
- [x] 5.3 All 5 titles used in code match the 5 spec headings exactly; no marker sits on an import line. Added the 4 missing markers found in 5.2: `AppComponent.java` (`NotificationsModule` in the Dagger graph), `Preference.kt` (`NOTIFICATIONS` key), `NotificationD2RepositoryTest.kt` and `dialog_change_server_url.xml` (both files are wholly customizations). 50 → 54 marked files
- [x] 5.4 Identity verified: `com.eyeseetea.widp`, `vName=3.4.1-widp-fork-1`, `vCode=156`, SDK `1.14.1-eyeseetea-fork-1` (the Oslo `org.hisp.dhis:android-core` line stays commented out). Source sets intact (widp 32 / widpDebug 28 / widpRelease 27 files). **Zero** files from another client's flavor touched by this merge

## 6. Build and complete the merge

- [x] 6.1 `./gradlew assembleWidpDebug` **green** after four rounds. Four distinct build-breaking faults found and fixed — see the notes file: (1) two strings a customization layout depended on, deleted upstream; (2) stale WIDP flavor source set (malformed path + outdated `GranularSyncModule` + `menu_dev` missing from both menu overrides); (3) `DownloadNewVersion`, a new per-flavor class upstream ships for its own flavors only; (4) `releaseSessionComponent()` gone with `SessionComponent`
- [x] 6.2 `./gradlew ktlintCheck` green — two rounds of violations, all in WIDP 2FA code that predated the stricter 3.4.1 ktlint rules. Fixed with `ktlintFormat` scoped to `:login` and `:commonskmm`; diff verified as purely cosmetic
- [x] 6.3 `./gradlew testWidpDebugUnitTest` green. One failure triaged: `SearchTEIViewModelTest` still used the pre-refactor API (`onParameterIntent`/`FormIntent`/`queryData`) — it survived the automerge textually but not semantically. Adapted to `onValueChange`/`queryDataList`, replicating the baseline's `ecb1c9310`
- [x] 6.4 SDK verified: resolves to `com.github.EyeSeeTea:dhis2-android-sdk:1.14.1-eyeseetea-fork-1` from JitPack. APK built as `dhis2-v3.4.1-widp-fork-1-feature-upgrade_widp_to_3_4_1.apk`
- [ ] 6.5 Commit the merge with the developer's git identity

## 7. Oslo patches missing from WIDP

- [x] 7.1 **Skipped — absorbed upstream.** The patch swapped `mergeWith` for `concatWith` in three granular-sync methods; `origin/upstream/3.4.1` already ships `concatWith` in all three. Verified against the upstream ref, not just the merged tree
- [x] 7.2 **Applied.** Bug confirmed still present in 3.4.1: in the `EventStatus.COMPLETED` branch the `if` returning `FormActions.OnFinish` is not the last expression, so its value is discarded and `resultAction` is always returned. Cherry-picked with its test; `customizations-eyeseetea.md` deliberately excluded (another client's doc)
- [x] 7.3 **Skipped — absorbed upstream.** The patch added a `lastSearchPagingData` identity guard; `origin/upstream/3.4.1` ships exactly that guard, plus `collectLatest`
- [x] 7.4 `assembleWidpDebug` + `testWidpDebugUnitTest` + `ktlintCheck` green after the cherry-pick

## 8. Manual validation

- [x] 8.1 Checklist extended: new sections 3.a (download trigger moved — 4 flows including the known background-sync deviation) and 3.b (menu entry points), plus a warning at the top of section 4 about the 2FA re-application and the `defaultError()` trap
- [ ] 8.2 Validate notifications end-to-end on an emulator/device (highest risk — pending badge, refresh after sync, mark as read)
- [ ] 8.3 Validate Change Server URL (menu entry visible, warning dialog, URL actually switches)
- [ ] 8.4 Validate 2FA against `preprod-indiv`: TOTP, Email and SMS flows, resend, 30s cooldown, rate-limit message
- [ ] 8.5 Validate image upload without resizing
- [ ] 8.6 Validate login + send an event against DHIS2 2.41 and 2.43 (`dev.eyeseetea`) with the provided test user
- [ ] 8.7 Record every executed flow and its result in `upgrade-3.4.1-notes.md`

## 9. Close out

- [x] 9.1 Inventory updated: header (3.4.1 / vCode 156 / SDK tag), corrected flavor paths and the new `DownloadNewVersion.kt`, `SyncNotifications.kt`, the `MainViewModel`/`HomeEffect` wiring, the files that stopped being customizations, the restored `url_hint`/`login_https` strings, the `:login` flavor-block note, the four files that gained a marker, and the audit-method correction
- [x] 9.2 `check_upgrade_docs.py --client widp` run. Fixed the 4 feat-commit path warnings. **One known issue left, deliberately unfixed**: the build-level marker `Include SDK's modules` in `settings.gradle.kts` can never satisfy the script, because it demands a spec while `openspec/config.yaml` puts the build system out of OpenSpec scope. Inherited from the baseline; fixing it in a client PR would create cross-branch drift. Documented with a proposed fix in the notes file
- [x] 9.3 `openspec validate --strict` green: 1 change + 5 specs, 0 failures
- [ ] 9.4 Open the PR against `develop-widp` with the clean incremental diff, listing target version, SDK tag, customizations verified, and validation flows executed
- [x] 9.5 Follow-ups recorded in the notes file: (1) decouple the notification download from sync (option B, needs a spec update); (2) move notifications wiring into `app/src/widp/` to shrink the Oslo footprint; (3) restore `url-data-element` rendering; (4) promote the four static pre-build checks to `conflict-rules.md`; (5) fix the build-level marker inconsistency and the `check_upgrade_docs.py` error handling in the baseline
