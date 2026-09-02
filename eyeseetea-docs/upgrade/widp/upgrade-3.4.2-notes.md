# WIDP Upgrade Notes — 3.4.2

Temporary working notes for the WIDP upgrade to 3.4.2. Conflict decisions, open questions and
follow-up checks live here until the upgrade closes.

Stable rules belong in `conflict-rules.md` (baseline-owned — do not edit from this branch).
The final customization inventory belongs in `customizations/widp/customization-files.md`.

## Header

- Client: `widp`
- Target version: `3.4.2-eyeseetea-fork-1` → `3.4.2-widp-fork-1`
- Base branch: `develop-eyeseetea` @ `f87bec8c3`
- Merge-base with baseline: `8a4866305`
- SDK: `1.13.1-eyeseetea-fork-3` → `1.14.2-eyeseetea-fork-1`
- Upgrade branch: `feature-widp/bring_last_changes_3_4_2`
- OpenSpec change: `openspec/changes/upgrade-widp-to-3-4-2/`
- Started on: `2026-09-02`
- Status: `in_progress`

## Why this is the second attempt

An earlier attempt merged Oslo (`origin/upstream/3.4.1`) **directly** instead of going through
`develop-eyeseetea`, and was redone. That branch is preserved locally.

Root cause: the local agent instructions in `.claude/CLAUDE.md` stated *"Never merge
`develop-eyeseetea` into a client branch… upgrades are replicated"*, which contradicts
`upgrade-plan-client-forks.md` Phase 2 and `conflict-rules.md` step 4. The rule conflated
*another client's fork* (never cherry-pick — correct) with *the shared baseline* (always merge).
Corrected on 2026-09-02.

Three defects traced to that one decision, all silent — no conflict, no compile error, no
failing test:

| Defect | Detail |
|---|---|
| Build tuning lost | `gradle.properties` reverted to `-Xmx4096M`; the fork's `-Xmx8g -XX:MaxMetaspaceSize=1g` was dropped. Merge-base, `develop-widp` and `develop-eyeseetea` all carry the same value, so **via the baseline there is no conflict at all** |
| `eyeseetea` flavor broken | `:app:kspEyeseeteaDebugKotlin FAILED` — `GranularSyncModule.kt` stayed pre-3.4.1 at the malformed path `app/src/eyeseetea/java/org.dhis2.utils/` |
| Three shared fork docs missed | `customization-techniques.md` (documents the `PostMetadataSyncAction` technique), `templates/AGENTS-CLIENT.md.template`, `customizations/template/customization-specs-template.md`; `onboarding-fork-guide.md` stayed pre-3.4. **`conflict-rules.md` was NOT stale** — `develop-widp` already carried the baseline version, step 4 included |

Plus the notifications download was hand-rolled onto the **data** sync
(`MainViewModel.onDataSuccess()`) instead of the baseline's `PostMetadataSyncAction`, which
silently violated the `notifications` spec's own wording ("during metadata sync").

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` is already at 3.4.2
- OpenSpec change created: `yes`
- merge started: `yes` — 27 conflicts, uncommitted
- easy conflicts resolved: `yes`
- manual conflicts pending: `no` — all 27 resolved
- validation started: `yes` — automated gates green, manual flows pending

## Conflict surface

Measured from merge-base `8a4866305`:

```
baseline changes:   1227 files
develop-widp:        177 files
overlap:              37 files   ← the only place conflicts can arise
```

Classification is in `openspec/changes/upgrade-widp-to-3-4-2/design.md`, decision D3.

## Decisions

27 files conflicted. 12 more files from `customization-files.md` merged **cleanly** but changed —
the silent-deletion candidates, all checked (see below). Every conflict hunk was read to fill the
**expected delta** column; three rows changed classification as a result and are marked ⚠.

### `accept_theirs` (5)

| File | Expected delta | Customization | Status |
|------|----------------|---------------|--------|
| `app/src/main/.../searchTrackEntity/SearchTEIViewModel.kt` | none — our side is plain Oslo code with no marker; baseline carries the newer `queryDataList` API | n/a | pending |
| `app/src/main/.../searchTrackEntity/listView/SearchTEList.kt` | none — the "stale search results" fix is a **baseline** Oslo patch that `develop-widp` had replicated. The baseline reimplemented it (`collectLatest` + `lastSearchPagingData` guard). **Verify the resolved file still calls `hideStaleProgramResults()`** | n/a (baseline Oslo patch) | pending |
| `app/src/test/.../SyncPresenterTest.kt` | ⚠ our side passes `notificationRepository` into `SyncPresenter` under a customization marker. Not "no customization" as first classified — it is **absorbed by D5**: with the trigger on `PostMetadataSyncAction`, `SyncPresenter` no longer needs the repository | Notifications system | pending |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | same as above — `notificationsRepository` → `syncStatusController`. Absorbed by D5. **Re-verified on this branch**, not inherited from the 3.4.1 notes | Notifications system | pending |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | none. Conflicts because **both sides grew section 5 independently**: `develop-widp` added it in `5ea3d192c` (2026-06-08) while replicating an Oslo patch, and the baseline added the same plus later entries. Editing this file from a client fork is what `conflict-rules.md` forbids — a pre-existing violation on `develop-widp`, not introduced here. Do not re-add the client-side entries | n/a (baseline-owned) | pending |

### `manual_reapply_on_theirs` (9)

| File | Expected delta — what must survive, and nothing more | Customization | Status |
|------|------------------------------------------------------|---------------|--------|
| `gradle/libs.versions.toml` | take the baseline wholesale, then two edits: `vName` → `3.4.2-widp-fork-1`, and keep `markwon = "4.6.0"` (Markdown rendering in notification dialogs), which the baseline does not have. **Note `minSdk` 21 → 23** — a real behavioral change, flag it to the client | Notifications system, fork identity | pending |
| `app/build.gradle.kts` | one hunk: keep `implementation(libs.eyeseetea.markwon)` **and** take the baseline's three new Koin/Compose test dependencies | Notifications system | pending |
| `app/src/main/res/values/strings.xml` | keep the 2 `change_server_url*` strings under their marker; take all of the baseline's new strings and plurals | Change Server URL | pending |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | keep the 6 2FA strings. Their marker is a bare `<!--EyeSeeTea customization-->` with **no title** — fix it to `EyeSeeTea customization - Two-factor authentication` per the comment convention | Two-factor authentication | pending |
| `commonskmm/.../providers/PreferenceConstants.kt` | ⚠ reclassified: this is **Notifications**, not Change Server URL. Keep `BASIC_SHARE_PREFS` plus its marker; take the baseline's new `EVENT_MAX` / `TEI_MAX` / `LIMIT_BY_*` / `MAX_RESERVED_VALUES` constants | Notifications system | pending |
| `app/src/main/java/org/dhis2/data/user/UserComponent.java` | keep the `ChangeServerURLComponent` / `ChangeServerURLModule` imports and the `plus()` method; drop `SessionComponent` and `PinModule` if upstream removed them | Change Server URL | pending |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | 7 hunks. Keep the Change Server URL dialog wiring (`isChangeServerURLVisible`, `CHANGE_SERVER_URL_DIALOG_TAG`, `ChangeServerUrlDialog`) and the notifications hooks, on top of the baseline's Koin migration. **This is the file whose Dagger `inject()` loss caused the NPE in the first attempt** — see D6 | Change Server URL, Notifications system | pending |
| `login/` — `LoginRepositoryImpl.kt` (3 markers, 31 2FA refs), `CredentialsScreen.kt` (6 / 36), `CredentialsUiState.kt` (1 / 3), `CredentialsViewModel.kt` (10 / 43, 3 hunks), `LoginUser.kt` (0 / 2) | the whole 2FA flow re-applied onto the baseline's rewritten login. Upstream added an OAuth branch to `onLoginClicked()` and wrapped `CredentialsContainer` in `if (!oAuthEnable)`. `LoginUser.kt` carries 2 2FA references with **no marker** — add one | Two-factor authentication | pending |
| `.github/workflows/eyeseetea-main.yml` | one hunk, a single path. **Decided: keep `testWidpDebugUnitTest`** (`accept_ours`) — GitHub tests the client flavor. Note the consequence: the `eyeseetea` flavor is then never compiled by CI, which is how the first attempt shipped it broken. Compiling it stays a **local** gate (task 5.2). `develop-sports` and `develop-unicef-tjk-elmis` keep the shared branch's `testEyeseeteaDebugUnitTest` instead | fork identity | pending |

### ⚠ Reclassified from `accept_theirs` (1)

| File | Why it changed | New classification | Status |
|------|----------------|--------------------|--------|
| `login/build.gradle.kts` | the 3.4.1 notes recorded it as "customization absorbed upstream". **That is wrong.** Our side declares 5 product flavors including `widp`; the baseline rewrote the file to the new KMP Android DSL (`alias(libs.plugins.android.kotlin.multiplatform.library)`, `android { }` inside the `kotlin` block) with no `productFlavors` at all. Taking theirs blindly drops the `widp` flavor from the `:login` module | `defer_after_build_verification` — take the baseline DSL, then let the build say whether the module still needs flavor declarations | pending |

### Modify/delete — the anchor is gone, port rather than resolve (6)

Upstream deleted the file the customization lived in. These cannot be "resolved"; the behavior must
be re-anchored somewhere that still exists.

| File | What is in it | New anchor | Status |
|------|---------------|-----------|--------|
| `app/src/main/java/org/dhis2/App.java` (395 lines) | notifications DI (`NotificationsModule`) and `ChangeServerURLComponent` + its `plus()` / release methods | `App.kt` | pending |
| `SyncDataWorkerModule.kt`, `SyncInitWorkerModule.kt`, `SyncMetadataWorkerModule.kt` (38 lines each) | each passes `NotificationRepository` into `SyncPresenter`. **None carries a customization comment** — a convention violation that hides them from any marker-based search. Expected outcome: **absorbed by D5**, nothing to port | `PostMetadataSyncAction` (design D5) | pending |
| `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` (407 lines) | the single-program branch asymmetry: one path calls `markShowNotificationsAsPending()`, the other also `refreshNotifications()`. This is the behavior that broke on device in the first attempt | `MainViewModel` (design D6) | pending |
| `app/src/main/java/org/dhis2/usescases/main/MainView.kt` (75 lines) | the two interface methods above | idem | pending |

### Ownership change (1)

| File | Decision | Status |
|------|----------|--------|
| `CLAUDE.md` | add/add conflict caused by a change of file ownership: since Oslo 3.4 `CLAUDE.md` upstream is a 4-line bridge to `AGENTS.md`, while `develop-widp` keeps 77 lines of WIDP identity at the same path. **Resolved `accept_ours` for this upgrade** — splitting it into `AGENTS-widp.md` is tooling work, not upgrade work. Consequence: the same whole-file conflict recurs on every future upgrade. Recorded as a follow-up. The rule lives in `eyeseetea-docs/README.md` (51, 265), `onboarding-fork-guide.md` Phase 5 (237-260) and `new-fork.md` (20-23) — **not** in `conflict-rules.md` | pending |

### Direct flavor files — no conflict

| Source set | Files changed by the merge | Notes |
|---|---|---|
| `app/src/widp/**`, `app/src/widpDebug/**`, `app/src/widpRelease/**` | **0** | the baseline never touches the widp flavor — zero conflict risk, as expected |
| `app/src/eyeseetea/**`, `app/src/eyeseeteaDebug/**` | 6 | arrive from the baseline. Includes `PostMetadataSyncModule.kt` (the notifications extension point) and `GranularSyncModule.kt`, **now at the correct package path** `app/src/eyeseetea/java/org/dhis2/utils/granularsync/` with no duplicate left behind — the merge fixed on its own what the first attempt broke |

Task 4.4 adds one **new** file under `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt`.

### Merged cleanly but changed — silent-deletion candidates, all CHECKED

Every one verified against the pre-merge version. **No customization was lost.**

| File | Verdict |
|------|---------|
| `commonskmm/.../D2ErrorMessageProviderImpl.kt` | **the 2FA trap did not fire.** All 7 `D2ErrorCode.*TWO_FACTOR*` branches still map to real strings, not `defaultError()`. Merge added 9 upstream lines |
| `login/.../data/LoginRepository.kt` | `twoFactorCode: String? = null` survives; the baseline has 0 occurrences. The 9 deleted lines are upstream's OpenID signature change |
| `commonskmm/.../error/DomainErrorMapper.kt` | +1 upstream line (`INVALID_CONFIGURATION`); the 7 2FA mappings intact |
| `app/src/main/.../general/ActivityGlobalAbstract.java` | both `// EyeSeeTea customization` markers intact |
| `form/.../EventRepository.kt`, `EnrollmentRepository.kt` | 5 markers intact; +2/-0 and +1/-1 upstream |
| `ValueStoreImpl.kt`, `ServerModule.kt`, `MainModule.kt`, `ProgramFragment.kt`, `ProgramModule.kt`, `commons/build.gradle.kts` | deletions are all the upstream Dagger→Koin refactor, no WIDP lines |
| `core/.../{D2ErrorCode.java,LogInCall.kt,LoginPayload.kt}` | not in this repo — SDK-fork inventory entries |

### Convention violations found while classifying

Customization code without the `// EyeSeeTea customization - <spec title>` comment, which makes it
invisible to any marker-based search. Fix while resolving:

| File | Issue |
|------|-------|
| `SyncDataWorkerModule.kt`, `SyncInitWorkerModule.kt`, `SyncMetadataWorkerModule.kt` | notifications wiring, no marker (moot if absorbed by D5) |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | bare `<!--EyeSeeTea customization-->` with no spec title |
| `login/.../domain/usecase/LoginUser.kt` | 2 2FA references, no marker |

### Found only by compiling the widp flavor (Phase 5)

Neither of these produces a conflict, and neither is visible in any diff. Both were found by
`:app:compileWidpDebugKotlin` failing.

| File | Finding |
|------|---------|
| `app/src/widp/.../granularsync/GranularSyncModule.kt` | the **same defect the first attempt shipped in the eyeseetea flavor, in ours**: the widp copy was still on the pre-3.4.1 signature (no `mapper: SyncUiStateMapper`, inline `DispatcherProvider`) and sat at the malformed path `app/src/widp/java/org.dhis2.utils/`. Verified byte-identical to the old eyeseetea copy, so it carries nothing widp-specific. Replaced with the baseline's 3.4.x version at the correct path `app/src/widp/java/org/dhis2/utils/granularsync/`. `CustomizableConstants.kt` is left at the malformed path because the baseline leaves its own equivalent there |
| `app/src/widp/.../main/domain/DownloadNewVersion.kt` | **did not exist on `develop-widp` at all** — upstream 3.4 made it a per-flavor file and `MainViewModel` now imports it, so every source set needs one. Two variants exist: the `dhis2` one downloads the APK in-app, the `eyeseetea` one opens a Play Store URL (baseline commit `8f586d847`). Copied the **`dhis2`** variant, because `develop-widp`'s `MainPresenter.downloadVersion()` called `versionRepository.download(...)` — the in-app flow — and WIDP is not distributed through the Play Store. **Confirm this with the client if in-app APK update is not what they expect.** |

| `app/src/widp/res/menu/main_menu.xml` **and** `app/src/widpDebug/res/menu/main_menu.xml` | the widp menu is **duplicated in two source sets**, and the `widpDebug` copy shadows the flavor one for every debug build. It arrived in `86bf27cf6` (a 2021 merge from another client branch) and the two files are byte-identical. Upstream added a `menu_dev` entry that both copies lacked, so `MainActivity` failed with `Unresolved reference 'menu_dev'`. Added to both. **Follow-up: delete the `widpDebug` copy** — it carries no build-type-specific content and every future menu change has to be made twice |
| `app/src/main/res/values/strings.xml` — `url_hint`, `login_https` | **a silent break of the Change Server URL customization.** Upstream deleted both strings when the login screen moved to the KMP `:login` module. They did not appear in the conflict hunk — the deletions applied cleanly — and the only remaining user is `dialog_change_server_url.xml`, which is ours. The resource linker caught it (`resource string/url_hint not found`), no review did. Both restored under the customization marker with a note explaining why they live there |

Note: `customization-files.md` on `develop-widp` already listed
`app/src/widp/.../DownloadNewVersion.kt` as a WIDP file although it did not exist. The inventory
was wrong; task 9.1 must correct it.

| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — `R.id.change_url` | **pre-existing, not caused by this upgrade.** The "Change server URL" menu entry exists only in the widp flavor's menu, but its handler lives in the *shared* `MainActivity`, so `R.id.change_url` does not resolve for any other flavor. `develop-widp` was already in this state (handler present in shared code, 0 occurrences in the shared menu), which is why `:app:compileEyeseeteaDebugKotlin` had never succeeded there either. Fixed by declaring the id in a **new** shared file `app/src/main/res/values/ids_eyeseetea.xml` — no Oslo resource file is touched, so it cannot conflict upstream |
| `app/src/test/.../SearchTEIViewModelTest.kt` | **a clean automerge that produced broken code.** The file never conflicted: git kept our two old-API tests *and* the baseline's adapted ones, giving duplicate function names (`Conflicting overloads`) and unresolved references to the removed `queryData` / `FormIntent` API. Taken wholesale from the baseline, which has both tests adapted. These are the same two tests an earlier review reported as "deleted" — they are present and adapted upstream |

**Lesson for `conflict-rules.md`** (propose separately, that file is baseline-owned): the
automerge-verification rule checks files listed in `customization-files.md`. It would not have
caught `url_hint` / `login_https`, because the broken file is a *shared* resource file and the
deleted symbols are only referenced from a customized layout. A resource-link and a per-flavor
compile are the only gates that catch this class.

### Pre-existing intermittent test failure (not introduced here)

`:app:testWidpDebugUnitTest` intermittently reports one failure with
`kotlinx.coroutines.test.UncaughtExceptionsBeforeTest` — an uncaught exception leaked by an
earlier test and attributed to whichever test starts next. The failing test alternates between
`MainViewModelTest.shouldSetVersionToUpdate` and
`MainViewModelIntegrationTest.should hide filter and sync buttons while sync is running`.

Attributed by elimination, not by guessing:

| Experiment | Result |
|---|---|
| full suite with both new test classes | 920/921 |
| full suite with a **trivial** class (`assertTrue(true)`) in `app/src/testWidp/` | 918/919 — **still fails** |
| same, and with the new presenter tests removed as well | 912/913 — **still fails with none of our code** |
| full suite with no `testWidp` source set at all | 918/918 once, 917/918 on a later run |

Both failing classes are **byte-identical to `develop-eyeseetea`** and were not touched by this
upgrade. Adding any class to a source set perturbs test ordering, which is what makes the latent
leak surface; the flake also occurs with no new class at all. It is inherited, intermittent, and
out of scope here — but it should be reported to the baseline, because a suite that fails one
test at random cannot gate anything.

## Open Questions

- Image upload without resizing: the requirement **is** documented — `openspec/specs/image-upload-no-resize/spec.md`
  is explicit (upload at captured resolution, no lossy re-encode, camera *and* gallery), and
  `customization-files.md` lists it as `active` with its implementation points. Nothing to confirm
  with the client. What is open is only whether the current implementation still satisfies it;
  that belongs to manual validation (checklist section 2), not to this merge.
- SDK `LogInCall.generate2FAErrorIfRequired()` returns `null` for
  `REQUIRES_TWO_FACTOR_ENROLMENT`, so a user required to enrol in 2FA crashes instead of
  seeing a message. Unchanged in `1.14.2`. Out of scope here — needs an SDK-fork decision.
- `.github/workflows/eyeseetea-main.yml` runs `:app:testWidpDebugUnitTest`, which covers 1 of
  13 modules and never runs the 2FA tests in `:login`. Worth fixing, but not in this change.

## Validation Notes

- build: `:app:assembleWidpDebug` **SUCCESSFUL**; `:app:compileEyeseeteaDebugKotlin` **SUCCESSFUL**
  — the second flavor builds for the first time on this branch, and on `develop-widp` it never did
- targeted tests: `:app:testWidpDebugUnitTest` 912/912; `:login:allTests` 194/194 (the 2FA tests,
  which `testWidpDebugUnitTest` does not run); `ktlintCheck` green across all 13 modules
- capabilities verified in code, not on paper: `saveFileResource(filePath, false)` still present;
  6 real 2FA messages (not `defaultError()`); `markwon` declared and used; `EventRepository` still
  differs from the baseline; `ChangeServerURLPresenter` present; notifications trigger isolated in
  the widp source set
- inventory cross-check (two-dot vs the baseline, all 78 paths): 53 still carry a WIDP-specific
  difference, 13 no longer differ (notifications wiring absorbed by `PostMetadataSyncAction`),
  9 deliberately deleted
- manual flows checked: **none yet** — see `upgrade-validation-checklist.md`

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no` — baseline-owned, propose separately
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
