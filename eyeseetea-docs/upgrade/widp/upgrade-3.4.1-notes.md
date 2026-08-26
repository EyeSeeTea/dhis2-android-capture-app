# WIDP upgrade 3.4.1 notes

Use this file as the temporary working notes for the WIDP upgrade to 3.4.1.

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
- Target version: `3.4.1`
- Base branch: `develop-eyeseetea`
- Base commit: `938b819597` (PR #323 "feature-eyeseetea/upgrade_3.4.1" — Oslo 3.4.1 already integrated). Deliberately not `origin/develop-eyeseetea` HEAD (`32ac673d0`, already 3.4.2) — this upgrade targets 3.4.1 only.
- Upgrade branch: `feature-widp/upgrade_3.4.1-new`
- Started on: `2026-08-26`
- Status: `in_progress`

Note: a prior attempt on this same branch merged Oslo (`upstream/3.4.1`, commit `14089cffd`)
directly instead of `develop-eyeseetea` — a violation of the golden rule in
`conflict-rules.md` ("never merge Oslo directly into a client branch"). Caught before any
conflict was resolved (only `CLAUDE.md` was `AA`, untouched); the merge was aborted with
`git merge --abort` and restarted correctly against `develop-eyeseetea@938b819597`.

There is a second, separate branch, `feature/upgrade_widp_to_3_4_1`, produced by another
developer working directly with Claude without following this project's onboarding/rules. It
also merged Oslo directly (confirmed: its merge commit `2cd5fc9e8` is
`Merge remote-tracking branch 'origin/upstream/3.4.1' into feature/upgrade_widp_to_3_4_1`).
Per the user's direction, this upgrade is being redone correctly from scratch on
`feature-widp/upgrade_3.4.1-new`; the two branches will be diffed afterwards to identify
problems in the other branch instead of reviewing it manually file by file.

## Progress

- baseline prepared: `yes`
- merge started: `yes` (2026-08-26, restarted after aborting the Oslo-direct attempt)
- easy conflicts resolved: `yes` — all 27 original conflicts resolved
- manual conflicts pending: `no`
- build verified: `yes` — `./gradlew :app:assembleWidpDebug` **BUILD SUCCESSFUL** (2026-08-26)
- tests run: `no` — next step
- validation started: `no`

## Preclassification (2026-08-26)

`git merge 938b819597 --no-commit --no-ff` → 27 conflicted files: 1 `AA`, 6 `UD`, 20 `UU`.

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `.github/workflows/eyeseetea-main.yml` | defer_after_build_verification | classify flavor CI vs baseline drift | n/a | resolved_manual_merge | Resolved by developer. Verified: keeps `testWidpDebugUnitTest`, only remaining diff is a trailing-newline fix. Correct. |
| `CLAUDE.md` | manual_reapply_on_theirs | move fork identity out of the now-upstream-owned file | fork identity | resolved_manual_merge | Same problem Simprints hit (B1 in their notes): Oslo introduced its own `CLAUDE.md` + `AGENTS.md` in 3.4. Resolved: took Oslo's `CLAUDE.md` as-is + one `@AGENTS-widp.md` import line; moved WIDP content to new `AGENTS-widp.md`; `AGENTS.md` verified byte-identical to baseline. Also fixed the baseline docs that still told forks to overwrite `CLAUDE.md` (Simprints' B1 was never promoted) — see "Baseline fix applied" below. |
| `app/build.gradle.kts` | defer_after_build_verification | classify flavor definition vs baseline drift | n/a | resolved_manual_merge | Resolved by developer. Verified: baseline structure taken, `// EyeSeeTea customization - Notifications system` block (`eyeseetea.markwon`) preserved. Correct. |
| `app/src/main/java/org/dhis2/App.java` | manual_reapply_on_theirs (Java→Kotlin migration, per conflict-rules.md) | reapply Change Server URL + Notifications wiring onto `App.kt` | Change Server URL, Notifications | resolved_manual_merge | `UD`. Confirmed clean 1:1 rename: baseline commit `772732141` "build: Migrate App to kotlin" (#4928) replaced `App.java` with `App.kt` (336 lines). WIDP delta against the pre-migration `App.java` has 4 blocks: (1) `MultiDexApplication`/`MultiDex.install()` — **not** a WIDP customization, no `EyeSeeTea customization` marker; baseline dropped the `multidex` dependency entirely (`minSdk` 21→23 in this same upgrade) — discard. (2) Notifications: `NotificationsModule` import + `.notificationsModule(...)` builder call — reapply. (3) Change Server URL: `ChangeServerURLComponent`/`Module` + `createChangeServerULComponent()` — reapply. (4) `SessionComponent`/`PinModule`/`createSessionComponent()`/`releaseSessionComponent()` — **not WIDP either**, no marker, no inventory entry. Investigated: `SessionComponent.kt` is pure Oslo legacy PIN-dialog Dagger wiring (`@Subcomponent(modules=[PinModule::class])`, injects `PinDialog`), unrelated to Change Server URL. Baseline's automerge already deleted `PinDialog.kt`/`PinModule.kt`/`PinPresenter.kt`/`PinView.kt`/`PinExtensions.kt`/`SessionComponent.kt` cleanly (replaced by the KMP `login/.../pin/di/PinModule.kt`), leaving one dangling call `app().releaseSessionComponent()` in `ChangeServerUrlDialog.kt:114` — dead/copy-pasted code, not functionally tied to Change Server URL (`ChangeServerURLComponent` is a fully separate `@Subcomponent`, doesn't reference `SessionComponent`). Discard the call along with the rest of block 4. |
| `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` | accept deletion | confirm no WIDP dependency before accepting deletion | Notifications (`notificationRepository` param) | resolved_keep_theirs | Same verdict as Simprints: worker moved to `:sync`, registered via Koin `workerOf`, no consumer left for the Dagger module. Deleted. |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | accept_theirs | classify drift | n/a (not in inventory) | resolved_keep_theirs | Verified byte-identical to Simprints' final version, which is itself byte-identical to `938b819597` baseline. No WIDP content ever lived here. |
| `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` | accept deletion | confirm no WIDP dependency before accepting deletion | Notifications (`notificationRepository` param) | resolved_keep_theirs | Same as `SyncDataWorkerModule.kt`. Deleted. |
| `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` | accept deletion | confirm no WIDP dependency before accepting deletion | Notifications (`notificationRepository` param) | resolved_keep_theirs | Same as `SyncDataWorkerModule.kt`. Deleted. |
| `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` | accept_theirs | reapply `syncNotifications()` call during metadata sync | Notifications | resolved_keep_theirs | Confirmed `PostMetadataSyncAction` did **not** exist in `938b819597` — ported Simprints' B4 design instead of reapplying the old `syncNotifications()` call here (see "B4 ported" section below). `SyncPresenterImpl.kt` itself verified byte-identical to baseline, same as Simprints. |
| `app/src/main/java/org/dhis2/data/user/UserComponent.java` | manual_reapply_on_theirs | reinsert `plus(ChangeServerURLModule)` subcomponent | Change Server URL | resolved_manual_merge | Same `PinModule`/`SessionComponent` vs `ChangeServerURLComponent` mix as `App.java`. `plus(ChangeServerURLModule)` at line 176 was outside the conflicted hunk and already correct; only the import block needed resolving — `PinModule`/`SessionComponent` imports dropped, `ChangeServerURLComponent`/`Module` kept with the customization comment. |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | manual_reapply_on_theirs | reinsert notification pending/refresh triggers + change-url menu wiring | Notifications, Change Server URL | resolved_manual_merge | 7 hunks. Baseline structure taken (`mainViewModel.onChangeScreen`/`onChangeToHome`, PIN/back-press fully moved to `MainViewModel`+composable). Reapplied: `notificationsPresenter.markShowNotificationsAsPending()` on `R.id.sync_manager` and `HomeEffect.SingleProgramNavigation`; `markShowNotificationsAsPending()` + `refresh()` on `R.id.menu_home`; new `HomeEffect.MarkNotificationsAsPending`/`RefreshNotifications` cases in the effect handler; `onChangeServerURL()` + `R.id.change_url` kept, `isChangeServerURLVisible` flag removed (dead — `ChangeServerUrlDialog` is `isCancelable = false`, the old `backPressed()` that read it no longer exists). See "B4 ported" section for why notification *download* itself is not triggered from here anymore. |
| `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` | accept deletion | reapply pending/refresh notification triggers in `checkSingleProgramNavigation()` | Notifications | resolved_keep_theirs | Confirmed deleted in `938b819597` (MVP→MVVM migration, PR #4763-4766). No content beyond Notifications survives — deleted. Logic re-homed into `MainViewModel`/`MainActivity` (see above). |
| `app/src/main/java/org/dhis2/usescases/main/MainView.kt` | accept deletion | reapply `markShowNotificationsAsPending()` / `refreshNotifications()` contract methods | Notifications | resolved_keep_theirs | Deleted — was pure indirection to `notificationsPresenter`; `MainActivity` now calls it directly, same as the other WIDP branch did. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` | defer_after_build_verification | classify drift, not in inventory | n/a | resolved_keep_theirs | Resolved by developer. Verified byte-identical to `938b819597` — pure baseline refactor noise, same as `SearchTEList.kt`. Confirms no WIDP customization lives here. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt` | defer_after_build_verification | classify drift, not in inventory | n/a | resolved_keep_theirs | Resolved by developer. Verified byte-identical to `938b819597` — pure baseline refactor noise, no WIDP content. Confirms this was not an inventory gap. |
| `app/src/main/res/values/strings.xml` | manual_reapply_on_theirs | reinsert change-server-url strings | Change Server URL | resolved_manual_merge | Resolved by developer. |
| `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` | accept_theirs | adapt test to whatever SyncPresenterImpl resolution produces | Notifications (indirect) | resolved_keep_theirs | Verified byte-identical to baseline, same as Simprints — no `notificationRepository` mock to remove, none was ever added since `SyncPresenterImpl.kt` took baseline structure. |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | reinsert 2FA string resources | 2FA support | resolved_manual_merge | Resolved by developer. |
| `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/providers/PreferenceConstants.kt` | manual_reapply_on_theirs | reinsert `BASIC_SHARE_PREFS` constant | Notifications | resolved_manual_merge | Resolved by developer. Verified: `BASIC_SHARE_PREFS` reinserted at end of file with customization comment. Same constant Simprints lost silently (their "Fourth automerge casualty") — here it surfaced as a real conflict instead. |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | accept_theirs | baseline inventory, must not be edited from a client fork | n/a | resolved_keep_theirs | Resolved by developer. Verified byte-identical to `938b819597`. Correct. |
| `gradle/libs.versions.toml` | defer_after_build_verification | classify release identity vs dependency drift | n/a | resolved_manual_merge | Resolved by developer. Verified: baseline structure taken; `vName = 3.4.1-widp-fork-1`, `vCode = 156`, `dhis2sdk = 1.14.1-eyeseetea-fork-1`, and the `#Eyeseetea` / `eyeseetea-markwon` block preserved. Correct — matches the post-merge fork identity check. |
| `login/build.gradle.kts` | defer_after_build_verification | classify flavor/2FA drift | 2FA support | pending | Still unresolved — not touched in this batch. |
| `login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt` | manual_reapply_on_theirs | reapply `handleTwoFactorError()` + 2FA detection at login | 2FA support | resolved_manual_merge | Resolved by developer. Verified: 3 customization comments present at the expected points (2FA detection, error handling, `handleTwoFactorError()`). |
| `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/usecase/LoginUser.kt` | manual_reapply_on_theirs | reapply two-factor code carrying flow | 2FA support | resolved_manual_merge | Resolved by developer. Verified: `twoFactorCode` parameter threaded through to `LoginRepository.loginUser()`. No customization comment on this one — acceptable per conflict-rules.md (additive, default-`null` parameter, does not change base flow), already listed in `customization-files.md` §2.4. |
| `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/screen/CredentialsScreen.kt` | manual_reapply_on_theirs | reapply `TwoFactorContainer` composable | 2FA support | resolved_manual_merge | Resolved by developer. Verified: 6 customization comments present. |
| `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/state/CredentialsUiState.kt` | manual_reapply_on_theirs | reapply 2FA fields | 2FA support | resolved_manual_merge | Resolved by developer. Verified: `twoFactorState`, `twoFactorCode`, `infoMessage` fields present with customization comment. |
| `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/ui/viewmodel/CredentialsViewModel.kt` | manual_reapply_on_theirs | reapply 2FA state management, resend logic | 2FA support | resolved_manual_merge | Resolved by developer. Verified: 10 customization comments present. |
| `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepository.kt` | manual_reapply_on_theirs | reapply `twoFactorCode` parameter on interface | 2FA support | resolved_manual_merge | Not in the original 27-conflict list (clean automerge) but part of the same chain — verified `twoFactorCode: String? = null` present on the interface, matching `LoginRepositoryImpl`/`LoginUser`. |

## Automerge casualties found so far

- `app/src/main/java/org/dhis2/utils/session/PinDialog.kt`, `PinExtensions.kt`, `PinModule.kt`,
  `PinPresenter.kt`, `PinView.kt`, `SessionComponent.kt` — deleted cleanly by the merge with no
  conflict marker (baseline replaced the legacy Dagger PIN dialog with the KMP
  `login/.../pin/di/PinModule.kt`). Not a WIDP customization — pure Oslo legacy code, confirmed
  by reading `SessionComponent.kt` (Oslo copyright header, `@Subcomponent(modules =
  [PinModule::class])`, injects `PinDialog` only — unrelated to
  `ChangeServerURLComponent`, which is a fully separate `@Subcomponent`). Left one dangling call,
  `app().releaseSessionComponent()` in `ChangeServerUrlDialog.kt:114` (not itself a conflicted
  file) — dead/copy-pasted code, removed. Would not have compiled if left in.
- `app/src/main/java/org/dhis2/App.java`'s `MultiDexApplication`/`MultiDex.install()` block —
  not an automerge casualty exactly (it was on our side of a real `UD` conflict), but same class
  of risk: no `EyeSeeTea customization` marker, easy to mistake for WIDP-specific. Baseline
  dropped the `multidex` dependency entirely this same upgrade (`minSdk` 21→23) — confirmed not
  needed, discarded.

## B4 ported: `PostMetadataSyncAction` (Notifications sync trigger)

Confirmed `PostMetadataSyncAction` does **not** exist in `938b819597` — Simprints implemented it
on their own fork branch (2026-08-08) but never promoted it. Analyzed three options for
re-anchoring the lost `syncNotifications()` trigger (baseline cut it along with
`SyncPresenterImpl.syncMetadata()`, same root cause as Simprints' B4):

- **A** — re-anchor to `MainViewModel`/`HomeEffect`, the approach `feature/upgrade_widp_to_3_4_1`
  took (their notes, "Notifications sync re-anchoring" section). Zero `:sync`/`:commonskmm`
  changes, but only fires while `MainActivity` is alive — explicitly accepted by them as a known
  regression for periodic background sync with the app closed.
- **B** — port Simprints' `PostMetadataSyncAction`, hook runs inside `SyncMetadata.kt` in `:sync`.
  Covers all 4 scenarios (login, manual, foreground periodic, background periodic), validated on
  device by Simprints. Costs 3 files touched in `:sync` (`SyncMetadata.kt`, `SyncModule.android.kt`,
  `SyncMetadataTest.kt`) vs. option A's 4 files in `app/` — fewer files, but all in a shared KMP
  module neither fork owns, vs. `app/` which is fork territory. Reusable by any fork once
  promoted (already serves Simprints' biometrics-config case).
- **C** — observe `WorkManager` from `App.kt` (process-scoped, not Activity-scoped) instead of
  hooking `:sync` directly. Would close A's background-sync gap without touching `:sync`.
  Investigated for viability (WorkManager initializes via `androidx.startup` before
  `Application.onCreate()`; `D2` is already activated in `App.onCreate()`;
  `NotificationRepository.sync()` is defensive) but **not implemented** — no device validation,
  kept as a future baseline-improvement candidate, not a competitor to A/B for this upgrade.

**Decided: option B.** Ported Simprints' design as-is, with two small hardening additions found
during code review (documented in the fork's memory, not repeated here): a dedicated logging tag
constant instead of reusing `SYNC_METADATA_NAME`, and an explanatory comment on the
`factory { }` in `SyncModule.android.kt` warning against reverting to `factoryOf(::SyncMetadata)`
(which silently drops the default-`emptyList()` parameter).

Files:
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/PostMetadataSyncAction.kt`
  (new, no marker — generic extension point)
- `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/domain/SyncMetadata.kt` (3rd constructor
  param defaulting to `emptyList()`, `runPostMetadataSyncActions()` called at `input(50)`)
- `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/di/SyncModule.android.kt` (explicit
  `factory { }` replacing `factoryOf(::SyncMetadata)`)
- `sync/src/commonTest/kotlin/org/dhis2/mobile/sync/domain/SyncMetadataTest.kt` (4 tests ported:
  ordering, not-run-on-failure, isolation for returned/thrown action failures)
- `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` (new, registers `NotificationD2Repository.sync()`)
- Empty equivalents in `app/src/{dhis2,dhis2PlayServices,dhis2Training,eyeseetea}/java/org/dhis2/di/PostMetadataSyncModule.kt`
- `app/src/main/java/org/dhis2/di/KoinInitialization.kt` (+1 line, `postMetadataSyncModule`)

**To promote to `develop-eyeseetea` alongside Simprints' B4** — same contract, avoids two forks
shipping slightly divergent copies of the same mechanism.

## Notifications DI migrated from Dagger to Koin (build-breaking without this)

Reapplying the notification triggers in `MainActivity.kt` surfaced the same NPE risk
`feature/upgrade_widp_to_3_4_1` documented: `ActivityGlobalAbstract.notificationsPresenter` was
`@Inject public NotificationsPresenter`, populated only as a side effect of each subclass calling
Dagger `mainComponent.inject(this)`. `MainActivity` no longer does that (migrated to Koin in
`938b819597`), so the field would stay `null`.

**Fix applied** (same shape as the other WIDP branch): moved the whole Notifications DI graph from
Dagger to Koin.
- Deleted `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsModule.kt` (Dagger)
  and `NotificationsComponent.kt` (already dead/commented out).
- New `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsKoinModule.kt`.
- `ActivityGlobalAbstract.java`: field replaced with a private field + `getNotificationsPresenter()`
  getter resolving from `KoinJavaComponent.get(...)`. Kotlin callers (`MainActivity.kt`) read it
  as the `notificationsPresenter` synthetic property unchanged.
- `App.kt`/`AppComponent.java`: reverted the `.notificationsModule(NotificationsModule())` /
  `Builder notificationsModule(...)` wiring added earlier in this session — no longer needed, this
  is a Koin module now, not Dagger.
- `KoinInitialization.kt`: `notificationsModule` registered alongside `postMetadataSyncModule`.
- `NotificationsPresenter.kt`: also picked up the `refresh()` fix `feature/upgrade_widp_to_3_4_1`
  found and fixed — the pending flag was consumed unconditionally, before knowing whether the
  downloaded list was non-empty, so any activity resuming while the download was still in flight
  (the common case for single-program users, WIDP's production profile) silently ate the
  notification. Now only consumed when something is actually rendered.

## Baseline bugs found while verifying `assembleWidpDebug` (none introduced by this upgrade)

All four confirmed present in pure `938b819597` and/or pre-existing in this branch before today's
session — not caused by the B4/Koin work above, but blocking compilation once the merge reached
that code path.

**Cross-checked all five against `feature/upgrade_widp_to_3_4_1` (2026-08-26).** 4 of 5 are fixed
there too, confirming these are real baseline-vs-WIDP gaps independently hit by both efforts, not
mistakes introduced on this branch. Only #4 is not fixed on their side — see below.

1. **`app/src/widp/java/org.dhis2.utils/granularsync/GranularSyncModule.kt` outdated.** Missing
   the `org.dhis2.utils.granularsync.data.GranularSyncRepository` import (package moved) and the
   `mapper: SyncUiStateMapper` 9th constructor argument baseline added to
   `GranularSyncViewModelFactory`. Fixed by replacing the whole file with the `eyeseetea` flavor's
   version (verified byte-identical to `dhis2Training`'s, confirmed no client-specific logic).
   **Confirmed fixed in `feature/upgrade_widp_to_3_4_1`** — same fix, same verification, and they
   additionally corrected the malformed source path (`app/src/widp/java/org.dhis2.utils/` — dots
   instead of directory separators — their notes call this out explicitly as fault #1 of the same
   three-fault table). **Fixed here too**, after being flagged: moved both files
   (`CustomizableConstants.kt`, `GranularSyncModule.kt`) from
   `app/src/widp/java/org.dhis2.utils/` to `app/src/widp/java/org/dhis2/utils/`. It compiled
   either way — Kotlin resolves packages from the `package` declaration, not the directory — but
   the malformed path is confusing and worth avoiding. Re-verified `assembleWidpDebug` still
   `BUILD SUCCESSFUL` after the move. `CustomizableConstants.kt`'s only content (`DEFAULT_URL`) is
   unreferenced anywhere in the tree — confirmed dead code (baseline already documented removing
   this exact customization from `develop-eyeseetea` in `73a7eb8f0`), left in place since deleting
   it was out of scope for this fix.
2. **`DownloadNewVersion` missing for `widp`.** Baseline made it per-flavor
   (`dhis2`/`dhis2PlayServices`/`dhis2Training`/`eyeseetea` only); `MainModule`/`MainViewModel`
   require every flavor to supply one. Copied from `eyeseetea` (direct file download, not the
   Play Store variant) into `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt`.
   **Confirmed fixed in `feature/upgrade_widp_to_3_4_1`** the same way.
3. **`R.id.menu_dev` unresolved — missing from *both* `app/src/widp/res/menu/main_menu.xml` and
   `app/src/widpDebug/res/menu/main_menu.xml`.** Both files are full-file overrides created for
   `change_url` (Change Server URL, original commit `963d58b42` "Add menu to change server url" —
   confirmed via `git log` on the file, this is not a new customization, `change_url` was left
   untouched). Baseline later added a `menu_dev` item to the shared `main_menu.xml`; the full-file
   overrides silently dropped it — the same class of drift `conflict-rules.md` warns about for
   flavor overlays. Re-added `menu_dev` to both files, verbatim from baseline. **Confirmed fixed
   in `feature/upgrade_widp_to_3_4_1`**, in both files, matching their own notes ("`menu_dev` was
   missing from `app/src/widp/` and `app/src/widpDebug/`"). **Root cause is actually deeper — see
   point 4.**
4. **`ic_doctor_positive` drawable does not exist anywhere in the repo**, including
   `app/src/main/res/`. `app/src/main/res/menu/main_menu.xml` itself (baseline, no WIDP override)
   references it for `menu_dev`. AAPT2 does not hard-fail on this — it silently drops the `id`
   symbol from `R.txt`, which is why `R.id.menu_dev` was unresolved even after fix #3 restored the
   XML item. This is a **baseline bug** (confirmed present in pure `938b819597`), not
   WIDP-specific — any fork with its own `main_menu.xml` override that lists `menu_dev` explicitly
   will hit it once resources are built from a clean cache. **Confirmed NOT fixed in
   `feature/upgrade_widp_to_3_4_1`** — `git ls-tree` on that branch has zero matches for "doctor"
   anywhere in the tree, and their `main_menu.xml` overrides (which do have `menu_dev`, per #3)
   still reference `@drawable/ic_doctor_positive`. Since their reported build was
   `BUILD SUCCESSFUL` after their four fixes, this specific failure mode either did not fire for
   them (stale/warm resource cache carried an old `R.txt` with the symbol, or their toolchain
   version handles the missing-drawable case differently) or fired and went unnoticed/unreported.
   Either way this is the one place their branch would very likely fail the same way ours did on a
   genuinely clean build. Fixed here by adding
   `app/src/main/res/drawable/ic_doctor_positive.xml` (copied `ic_troubleshooting.xml`'s vector as
   a placeholder — the menu item is `android:visible="false"`, restricted to
   debug/`dhis2Training`, so the actual icon artwork doesn't matter short-term). **Candidate to
   report/fix directly in `develop-eyeseetea`**, since the missing asset is in the shared baseline
   file, not any fork's.
5. **`url_hint`/`login_https` strings missing, breaking `dialog_change_server_url.xml` (Change
   Server URL layout, shared `app/src/main/res/`).** Baseline commit `846943496
   "translations: remove-unused-strings-resources"` deleted both from
   `app/src/main/res/values/strings.xml` as part of an automated unused-strings cleanup — it did
   not know WIDP's Change Server URL dialog still referenced them, because the layout file itself
   was not in conflict during the merge (only files git actually flagged were checked against the
   automerge verification rule; the layout wasn't one of them). Restored both strings with their
   original 3.3.1 text (`url_hint` = "Server url", `login_https` = "https://") and an
   `EyeSeeTea customization - Change Server URL` comment. **Process lesson:** the automerge
   verification rule as applied so far only re-diffed files already in `customization-files.md`'s
   file list — it did not catch a resource *reference* in one of those files pointing to a
   resource deleted from a *different*, non-conflicted file. Worth adding to `conflict-rules.md`
   as a known gap: verifying a customized layout/code file's own diff is not enough when it
   depends on resources defined elsewhere.

## Not yet classified / to verify

- `2.5 URL data element field` (form module) — no conflict appeared in this file list, needs
  the automerge verification rule run on `FieldUiModelExtensions.kt` and related files once
  the merge is otherwise resolved, since it is the customization most likely to be a silent
  casualty (already fragile per `customization-files.md`).
- Feat commits section is still missing for WIDP's `customization-files.md` (same gap Simprints
  flagged as B2) — inventory completeness for the automerge verification rule relies on the
  section list above being exhaustive, not on commit SHAs.
- Whether other resource references in shared `app/src/main/res/` files depend on strings/drawables
  that later baseline cleanups removed, following the same failure pattern as point 5 above —
  not systematically audited, only found by hitting the compiler error.

## Baseline fix applied (2026-08-26) — needs promoting to `develop-eyeseetea`

While resolving the `CLAUDE.md` conflict, found that Simprints' B1 finding (their
`upgrade-3.4-notes.md`, "Improvements to promote to `develop-eyeseetea`") was written up but
never actually promoted — `eyeseetea-docs/templates/CLAUDE.md.template` and the onboarding docs
still instructed forks to overwrite Oslo's `CLAUDE.md`. Fixed directly on this branch (per user
decision, rather than a separate baseline branch first) so the WIDP conflict resolution has
something correct to follow; **must be extracted into its own commit/PR against
`develop-eyeseetea`** before or independently of this upgrade closing, since none of it is
WIDP-specific.

Files changed, all in `eyeseetea-docs/` (shared, not WIDP-owned):
- `templates/CLAUDE.md.template` renamed to `templates/AGENTS-FLAVOR.md.template`, with the
  internal "CLAUDE.md rule" self-reference in the Automation extraction rule section updated to
  `AGENTS-{{FLAVOR}}.md rule`.
- `new-fork.md` — step 4 and the "Done when" checklist updated to create `AGENTS-<client>.md` +
  add one import line to `CLAUDE.md`, instead of overwriting it.
- `onboarding-fork-guide.md` — Phase 5 ("CLAUDE.md (required)" section, "Done when" checklist,
  the `templates/` bring-list description, and the developer/AI role table) updated the same way.
- `README.md` — the new-fork step list, its mermaid diagram, and the templates list updated.
- `upgrade/conflict-rules.md` — new section "`CLAUDE.md` ownership change (Oslo 3.4+)" added
  (after "File migration rule: Java to Kotlin"), documenting the resolution for forks that
  already existed before Oslo 3.4 and hit this as a merge conflict rather than at fork creation.

Also applied on this branch (fork-specific, stays here): `CLAUDE.md` now Oslo's version plus the
one `@AGENTS-widp.md` import line; new `AGENTS-widp.md` carries the WIDP content that used to be
in `CLAUDE.md`; `AGENTS.md` untouched.

Not done yet: `templates/openspec-config.yaml.template` and other onboarding artifacts were not
audited for the same kind of staleness — only the `CLAUDE.md` path was in scope here.

## Open Questions

- Does `App.java` still exist as the DI entry point in `938b819597`, or was it replaced (Simprints
  saw similar Dagger→Koin migration pressure on login; check whether the same happened to App-level
  DI)?
- `SearchTEIViewModel.kt` / `SearchTEList.kt` conflicts are not explained by any WIDP customization
  in the inventory — confirm before resolving whether this is pure baseline architecture churn
  (accept_theirs) or an undocumented inventory gap.

## Validation Notes

- build: not started
- targeted tests: not started
- manual flows checked: not started

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `yes` (whole merge still open)

## Comparison plan against `feature/upgrade_widp_to_3_4_1`

Once this upgrade is validated and closed, diff the two branches (this one vs.
`feature/upgrade_widp_to_3_4_1`) to identify problems in the other branch instead of manually
reviewing it file by file. Candidate checks:
- files where the other branch's resolution differs from ours for the same customization
- customization code present in `customization-files.md` but missing from the other branch
  (silent automerge casualty, same class of bug Simprints hit repeatedly)
- fork identity drift (version naming, flavor definitions, dependencies) per the post-merge
  fork identity check in `conflict-rules.md`
- whether the other branch merged Oslo directly at any point after `2cd5fc9e8`, which would mean
  it also missed any `develop-eyeseetea`-only fixes/extension points (e.g. Simprints' B4
  `PostMetadataSyncAction`, if promoted before this upgrade closes)
