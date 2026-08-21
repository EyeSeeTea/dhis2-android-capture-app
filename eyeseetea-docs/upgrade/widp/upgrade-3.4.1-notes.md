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
- merge started: `yes` (merge of `origin/upstream/3.4.1`, uncommitted)
- easy conflicts resolved: `yes` (10 of 27 — mechanical pass done)
- manual conflicts pending: `yes` (17: 6 login/2FA, 10 notifications + change-server-url, 1 prefs)
- validation started: `no`

## Predicted conflict surface

27 files, from `git merge-tree --write-tree develop-widp origin/upstream/3.4.1` run before the
merge: 21 content conflicts, 6 modify/delete. **The real merge produced exactly those 27 files** —
the prediction was accurate, no surprises. Plus **8 files that auto-merge silently** despite
being customized and touched upstream — those are the highest-risk items because nothing prompts
a review.

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `app/src/androidTest/assets/databases/dhis_test.db` | accept_theirs | upstream test fixture | n/a | resolved_keep_theirs | binary, no WIDP content |
| `gradle.properties` | accept_theirs | build tuning | n/a | resolved_keep_theirs | verified: WIDP had no customization here vs merge-base |
| `CLAUDE.md` | accept_ours | fork identity doc | n/a | resolved_keep_ours | add/add. Upstream's new file only does `@AGENTS.md`; WIDP fork identity kept. Version header still says 3.3.0.1 — fix in task 9.1 |
| `gradle/libs.versions.toml` | manual_reapply_on_theirs | SDK tag + version + vCode | fork identity | resolved_manual_merge | upstream values + `vName=3.4.1-widp-fork-1`, `dhis2sdk=1.14.1-eyeseetea-fork-1`. **`markwon=4.6.0` preserved** — WIDP-only (used by `app/build.gradle.kts`), absent from the baseline, would have been lost by copying its resolution |
| `app/build.gradle.kts` | manual_reapply_on_theirs | widp flavor block | fork identity | resolved_manual_merge | kept upstream koin test deps + `implementation(libs.eyeseetea.markwon)`. `create("widp")` flavor auto-merged intact |
| `login/build.gradle.kts` | accept_theirs (reclassified) | flavor block absorbed upstream | 2FA support | resolved_keep_theirs | ⚠ **a customization marker was dropped here.** Upstream migrated `:login` to the KMP `androidLibrary {}` DSL and removed the whole `android {}` / `productFlavors` block for *every* flavor, not just widp. The `// EyeSeeTea customization - 2FA support` comment sat on `create("widp") { buildConfigField("LOGIN_TEST") }`, which only existed so the module had a matching flavor dimension; the 2FA implementation lives in `commonMain`/`androidMain` and is unaffected. Baseline resolved this identically (its file is byte-identical to upstream). **Needs developer confirmation** that no widp-only `buildConfigField` was relied on. |
| `app/src/main/res/values/strings.xml` | manual_reapply_on_theirs | WIDP strings appended | Change Server URL | resolved_manual_merge | both sides kept, WIDP strings appended after upstream block. XML validated |
| `commonskmm/src/commonMain/composeResources/values/strings.xml` | manual_reapply_on_theirs | WIDP strings appended | 2FA support | resolved_manual_merge | 6 2FA strings kept; comment normalised from `<!--EyeSeeTea customization-->` to the convention `<!-- EyeSeeTea customization - 2FA support -->`. XML validated |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt` | accept_theirs (reclassified) | **absorbed upstream** | n/a (Oslo fix) | resolved_keep_theirs | Oslo 3.4.1 now ships `lastSearchPagingData` + `collectLatest` + `hideStaleProgramResults()`. The WIDP comment said "Remove when Oslo clears liveAdapter…" — that condition is met. `onInitDataLoaded()`/`CoroutineTracker` preserved in upstream's refactor |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` | manual_reapply_on_theirs | Oslo search patch | n/a (Oslo fix) | resolved_manual_merge | ANDROAPP-6844 still present in 3.4.1 (`values.isNullOrEmpty()` does not filter blanks). Re-applied `nonBlankValues` onto the new `queryDataList` API — result is byte-identical to the baseline resolution |
| `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` | accept_theirs (reclassified) | ctor arg gone | Notifications system | resolved_keep_theirs | `SyncPresenterImpl` no longer takes `NotificationRepository` (see below) |
| `login/src/androidMain/.../LoginRepositoryImpl.kt` | manual_reapply_on_theirs | real `twoFactorCode` arg | 2FA support | resolved_manual_merge | only the import conflicted; body auto-merged keeping `blockingLogIn(username, password, serverUrl, twoFactorCode)`. Baseline's `null` **not** copied |
| `login/src/commonMain/.../LoginUser.kt` | manual_reapply_on_theirs | 2FA result plumbing | 2FA support | resolved_manual_merge | upstream's `username.trim()` + WIDP's `twoFactorCode` argument |
| `login/src/commonMain/.../CredentialsUiState.kt` | manual_reapply_on_theirs | 2FA state fields | 2FA support | resolved_manual_merge | upstream `oAuthEnable` + the 3 WIDP fields |
| `login/src/commonMain/.../CredentialsViewModel.kt` | manual_reapply_on_theirs | type detection, resend, cooldown | 2FA support | resolved_manual_merge | 3 hunks: 2 state constructions + `onLoginClicked`, where upstream's OAuth branch now wraps the login job and the 2FA code is passed inside the `else` |
| `login/src/commonMain/.../CredentialsScreen.kt` | manual_reapply_on_theirs | 2FA UI per type | 2FA support | resolved_manual_merge | upstream wrapped `CredentialsContainer` in `if (!oAuthEnable)`; `TwoFactorContainer` kept outside that branch (`twoFactorState` is only populated by the password path) |
| `commonskmm/.../PreferenceConstants.kt` | manual_reapply_on_theirs | `BASIC_SHARE_PREFS` | Notifications system (not Change Server URL — marker corrected) | resolved_manual_merge | upstream constants kept, WIDP const appended |
| `app/src/main/java/org/dhis2/data/user/UserComponent.java` | manual_reapply_on_theirs | `plus(ChangeServerURLModule)` | Change Server URL | resolved_manual_merge | kept the 2 ChangeServerURL imports; dropped `PinModule`/`SessionComponent` imports — upstream deleted both classes and the body no longer references them |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | manual_reapply_on_theirs | menu entry + notif calls | Change Server URL, Notifications | resolved_manual_merge | 7 hunks. Upstream moved the screen to Compose but kept the drawer (`binding.navView` + `initCurrentScreen()`), so `R.id.change_url` still works — the menu item itself lives in `app/src/widp/res/menu/main_menu.xml` (flavor source set, untouched). Dropped `isPinLayoutVisible`/`mainNavigator`/`backPressed`/`onLockClick`/`setFilters`/`hideFilters` (deleted upstream, PIN moved to the composable) and with them `isChangeServerURLVisible`, whose only reader was `backPressed` |
| `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` | accept_theirs (reclassified) | **hook lost, re-anchored** | Notifications system | resolved_keep_theirs | Upstream reduced this class to granular sync only; `syncMetadata()` (and with it the `syncNotifications()` call) moved to the `:sync` module. See "Notifications sync re-anchoring" below |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | accept_theirs (reclassified) | provider no longer needed | Notifications system | resolved_keep_theirs | `SyncPresenterImpl` stopped taking `NotificationRepository`, so the provider argument went with it |
| `app/src/main/java/org/dhis2/App.java` | **port** | Dagger wiring → `App.kt` | Notifications, Change Server URL | resolved_manual_merge | ported to `App.kt`: `.notificationsModule(NotificationsModule())` in the builder, the `changeServerURLComponent` field and `createChangeServerULComponent()` (following the existing `createDashboardComponent` pattern). `AppComponent.java` already carried `NotificationsModule` through the automerge. Comments moved off import lines |
| `app/src/main/java/org/dhis2/usescases/main/MainPresenter.kt` | **port** | call path → effects | Notifications system | resolved_manual_merge | `checkSingleProgramNavigation()`'s asymmetry maps onto 3.4.1's effects: the single-program branch is `HomeEffect.SingleProgramNavigation` (mark pending only), the other branch is `initCurrentScreen()`'s `R.id.menu_home` (mark + refresh) |
| `app/src/main/java/org/dhis2/usescases/main/MainView.kt` | **port** | interface removed | Notifications system | resolved_manual_merge | `markShowNotificationsAsPending()`/`refreshNotifications()` were only indirection; calls now go straight to `notificationsPresenter` from `MainActivity` |
| `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` | accept deletion | consumer gone | Notifications system | resolved_keep_theirs | its worker moved to the new `:sync` module and is registered with Koin (`workerOf`), leaving this Dagger module without a consumer |
| `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` | accept deletion | consumer gone | Notifications system | resolved_keep_theirs | its worker moved to the new `:sync` module and is registered with Koin (`workerOf`), leaving this Dagger module without a consumer |
| `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` | accept deletion | consumer gone | Notifications system | resolved_keep_theirs | its worker moved to the new `:sync` module and is registered with Koin (`workerOf`), leaving this Dagger module without a consumer |

### Silent automerge — audit results

All 2FA and Change Server URL entries below were **verified intact after the merge**; the
`defaultError()` degradation did not materialise.

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

## Notifications sync re-anchoring (behaviour deviation — read this)

Upstream 3.4 extracted the full sync into the new KMP module `:sync`. `SyncPresenterImpl` kept
only granular sync; `syncMetadata()` — whose `doOnComplete` called `syncNotifications()` — is gone.
The notifications capability lives in `:app`, and `:app` depends on `:sync`, not the reverse, so
the download could not be re-hooked inside the metadata sync.

**Re-anchored as:** `MainViewModel.handleDownloadProcess()` emits a new
`HomeEffect.SyncNotifications` when a sync finishes (`running == false`); `MainActivity` handles
that effect and calls `notificationsPresenter.syncNotifications()`, backed by a new
`SyncNotifications` use case (new file, level 2 of the placement hierarchy).

**Known deviation:** the spec says notifications are fetched *"during metadata sync"*. They are now
fetched *after a sync completes, with the main screen alive*. This covers initial sync after login,
manual sync, and periodic sync while the app is in the foreground. It does **not** cover a periodic
background sync with the app closed — in that case notifications arrive at the next sync with the
app open. Decision taken with the developer on 2026-08-13: accept for this upgrade, revisit in a
separate change that may decouple the download from sync entirely (and update the spec).

## Notifications port — semantics to preserve

Extracted from `MainPresenter.checkSingleProgramNavigation()` before the merge deletes it:

- `markShowNotificationsAsPending()` fires on **both** branches.
- `refreshNotifications()` fires **only** when the app is *not* auto-navigating into a single
  program.

Both are implemented in `MainActivity` (which survives) and delegate to `notificationsPresenter`.

> **Correction (2026-08-14).** This section originally read *"delegate to the Dagger-provided
> `notificationsPresenter`. The Dagger graph is not moving; only the call path is."* That was
> written in the planning commit (`fe6d21b59`, 2026-08-13), a day **before** the merge, and it was
> wrong — see "Notifications DI re-anchoring" below. The call path was re-anchored in task 4.5 but
> this premise was never re-verified against the merged tree.

## Notifications DI re-anchoring (runtime crash — read this)

**Symptom.** The app crashed with an NPE on entering the main screen. Build green, unit tests
green, ktlint green.

**Cause.** `ActivityGlobalAbstract` declared `@Inject public NotificationsPresenter
notificationsPresenter`. Nothing injected that field directly: it was populated because each
concrete activity ran `mainComponent.inject(this)` and Dagger also fills inherited fields.
Upstream 3.4.1 migrated `MainActivity` to Koin and removed its Dagger injection entirely, so the
field stayed `null`. The base class guarded it with `if (notificationsPresenter != null)` — which
silently disabled notifications rather than failing — but `MainActivity` reads it directly in four
places and crashed.

The customization was therefore depending on an **implementation detail of the Oslo host** (that
subclasses run a Dagger `inject()`), not on a contract. The fix removes that dependency instead of
refilling the hole.

**Re-anchored as:** the whole notifications graph moved from Dagger to Koin, following the
direction upstream is already taking (`:login`, `:sync`, `:commonskmm` are KMP and Koin-only).

| File | Change | Net effect on Oslo footprint |
|---|---|---|
| `usescases/notifications/di/NotificationsKoinModule.kt` | **new** — `notificationsModule` Koin graph (level 2) | — |
| `usescases/notifications/di/NotificationsModule.kt` | **deleted** — Dagger module, no consumer left | — |
| `usescases/notifications/di/NotificationsComponent.kt` | **deleted** — subcomponent, commented out and dead | — |
| `di/KoinInitialization.kt` | +2 lines (import + registration) | **+1 file** |
| `AppComponent.java` | −3 lines (import, `@Component(modules)`, builder method) | **−1 file** |
| `App.kt` | −2 lines (import, `.notificationsModule(...)`) | −1 line (still customized for Change Server URL) |
| `ActivityGlobalAbstract.java` | field is now private + `getNotificationsPresenter()` resolving from Koin | already customized |
| `MainActivity.kt` | **unchanged** | already customized |

Oslo files carrying notifications wiring: **4 → 2**.

Two deliberate choices in the Koin module:

- **`get<D2>()`, not `D2Manager.getD2()`** — Koin already publishes `D2` in `di/ServerModule.kt`.
  The old Dagger module reached for the static, bypassing the container.
- **`BasicPreferenceProvider` built inline, not published** — it is an Oslo `commons` type we do
  not own. Declaring `single<BasicPreferenceProvider>` would clash the day upstream registers it
  in Koin. It is a stateless wrapper over the same `BASIC_SHARE_PREFS` file, so a second instance
  is harmless.

`getNotificationsPresenter()` is a Java getter, so Kotlin subclasses keep reading it as the
`notificationsPresenter` synthetic property — `MainActivity`'s four call sites did not change, and
that file's merge surface did not grow.

**Regression guard:** `app/src/test/java/org/dhis2/usescases/notifications/di/NotificationsModuleTest.kt`.
Dagger failed at compile time when a dependency was missing; Koin fails at runtime. This test buys
that guarantee back. Verified by mutation: removing one `factory` from the module fails all three
tests.

**Process lesson.** A planning-phase assumption about the host's DI became a documented fact and
was never re-verified after the merge. Proposed rule for `conflict-rules.md` in the follow-ups.

## Manual validation findings (2026-08-21) — notifications

Found on device against `portal-uat.who.int/dhis2-indiv` with a **single-program** user. The
capability downloads and filters correctly; it failed at the point of *showing* the result.

**Evidence.** Logcat, 13:08:32: `Notifications: [Notification(content=test2, ...),
Notification(content=test3, ...)]` and both present in `BASIC_SHARE_PREFS`. No dialog appeared.
The notification only surfaced after navigating into a screen and back to Home.

### Finding 1 — nothing refreshed once the asynchronous download landed (FIXED)

`MainViewModel` emits `HomeEffect.SyncNotifications` when a sync finishes; the download then runs
on `Dispatchers.IO`. Nothing re-checked when it completed, so the result only surfaced if the user
happened to pass through `initCurrentScreen()`'s `R.id.menu_home` branch afterwards — the only path
that marks pending **and** refreshes in one step.

Until 3.4.0 this was implicit: the download ran inside `syncMetadata()` and finished before the
user navigated. Re-anchoring it in task 4.5 turned an ordering guarantee into a race.

**Fix:** `syncNotifications()` now marks pending and refreshes the view when the download
completes. `MainActivity` passes itself; the parameter is optional so the download still works
without a view.

### Finding 2 — the pending flag was consumed even with nothing to show (FIXED)

`refresh()` cleared `ShowNotifications.isPending` before knowing whether the list was empty. Every
activity in the authenticated area extends `ActivityGlobalAbstract`, so
`ProgramEventDetailActivity.onCreate()` burned the flag milliseconds after
`HomeEffect.SingleProgramNavigation` set it — while the download was still in flight.

**This made the failure systematic for single-program users**, which is the WIDP production
profile: the flag was always consumed before the data arrived.

**Fix:** the flag is only consumed when something is actually rendered.

Both fixes live in `NotificationsPresenter.kt` (level 2, ours) plus one argument at the
`MainActivity` call site. Covered by `NotificationsPresenterTest` (6 tests, both fixes verified by
mutation). The presenter now takes its dispatchers as constructor parameters, defaulted to
`Dispatchers.IO`/`Dispatchers.Main`, so the behaviour is testable without Android.

**No spec change required.** `openspec/specs/notifications/spec.md` already requires notifications
to be shown *"on activity resume ... when there is at least one pending notification"*. The
behaviour on the branch violated that requirement; these fixes restore conformance.

### Finding 3 — permission failures are silent (NOT fixed, deliberate)

`GET users/{id}?fields=userGroups` returned `403 E1006` for over an hour. The `catch` in
`getUserGroups()` returns an empty list, so every group-targeted notification was silently
discarded with no user-visible signal and no way to tell it apart from "you have no groups".
Diagnosis required reading logcat over adb.

Left out on purpose: `spec.md` has an explicit scenario stating that a datastore error SHALL
complete without failing. Changing it is a product decision and needs its own OpenSpec change.
Note the current code is *worse* than that scenario allows — it overwrites the stored
notifications with an empty list rather than leaving them untouched.

### Finding 4 — simultaneous notifications stack (NOT fixed, deliberate)

`ActivityGlobalAbstract.renderNotifications()` loops over the list and calls `.show()` per
notification, so N pending notifications produce N stacked dialogs and only the last is visible.
Observed with test2/test3: only test3 appeared. Needs a spec addition; the spec describes a single
dialog and says nothing about several at once.

## Manual validation executed (2026-08-21)

On device (Samsung SM-S928B) against `portal-uat.who.int/dhis2-indiv`, build
`3.4.1-widp-fork-1`, with a **single-program** user.

| Flow | Result |
|---|---|
| E — fresh start, log in, reach Home without crashing | **pass** — confirms the Koin DI re-anchoring on device |
| 2FA login (TOTP) | **pass** |
| Metadata sync, program downloaded | **pass** |
| Create, save and upload an event | **pass** |
| H — notification dialog appears without navigating away | **pass**, after the two display fixes |
| Image data element: capture with camera, save, upload | **pass** (upload path only — see caveat) |

Caveat on the image flow: it proves the path works, not that the image keeps its original
resolution, which is what the customization exists for. A resized upload would have succeeded
too. Indirect evidence that the no-resize behaviour is live: `FormValueStoreTest > Should try to
resize image` fails precisely because `fileController.resize()` is never called. Closing this
properly needs a dimension comparison between the source photo and the stored file resource.

Still not executed: the 2FA Email/SMS/resend/cooldown/rate-limit flows, Change Server URL (known
to crash, see below), the notification read-back (`readBy`), and login + event against DHIS2 2.41
and 2.43.

## Pre-existing defects surfaced during validation (not caused by this upgrade)

All four were verified against `origin/develop-widp` and are already in production. Listed here so
the upgrade PR is not blamed for them and so they can be picked up as a separate `fix-widp/`
branch.

1. **2FA required but not enrolled crashes the app.** With the DHIS2 server fork's `R_ENABLE_2FA`
   role restriction, login returns `{"loginStatus":"REQUIRES_TWO_FACTOR_ENROLMENT"}`. In
   `LogInCall.generate2FAErrorIfRequired()` of the SDK fork that value falls through to
   `else -> null`, so no `D2Error` is raised. The failure surfaces as the CustomActivityOnCrash
   screen, which reads like a handled error but is a crash. Verified in the SDK source; tag
   `1.14.2-eyeseetea-fork-1` does not fix it. The fix belongs in `EyeSeeTea/dhis2-android-sdk`,
   and the `else -> null` will reproduce this for any future `loginStatus` DHIS2 adds.

2. **Change Server URL crashes on success.** `ChangeServerURLPresenter.updateUrlInPreference()`
   calls `view.closeDialog()`, detaching the fragment; `renderSuccess()` then runs
   `activity as ActivityGlobalAbstract` on a null activity. `ChangeServerURLPresenter.kt` is
   byte-identical to `develop-widp`. The follow-up `Snackbar.make` NPE in `MainActivity` is Oslo
   code (unchanged from upstream) and a consequence of the first crash, not an independent defect.

3. **The server list is written under a key nobody reads.** Change Server URL persists to
   `Constants.PREFS_URLS` (`"pref_urls"`) while the login screen reads `PREF_URLS`
   (`"PREF_URLS"`). After switching servers and logging out, the login screen still suggests the
   old one. Broken since upstream `5a41530db` (30 Sep 2025) rewrote the login as a KMP module.

4. **Mis-tap from Log in to Recover account during 2FA.** `CredentialActions` lays the two
   full-width buttons out with `spacedBy(Spacing.Spacing0)` — Oslo code — and the screen has no
   `verticalScroll`. When the 2FA field and its info message appear, everything below reflows and
   a tap aimed at Log in lands on Recover account. Oslo's spacing, but only reachable because of
   the WIDP 2FA flow.

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

## Build failures found and fixed

### 1. Change Server URL dialog lost two strings (build-breaking)

```
dialog_change_server_url.xml:91: error: resource string/url_hint not found.
dialog_change_server_url.xml:103: error: resource string/login_https not found.
```

Upstream 3.4.1 deleted `url_hint` ("Server url") and `login_https` ("https://") from
`app/src/main/res/values/strings.xml` as part of its translation cleanup — they belonged to the
old login screen, which was rewritten. `layout/dialog_change_server_url.xml` (Change Server URL
customization) was their last consumer, so the resource link failed.

Restored both in the Change Server URL block of the shared `strings.xml`. They cannot live in
`app/src/widp/res/` — the layout sits in shared code, so every flavor compiles it.

**Audit lesson (worth promoting to `conflict-rules.md`):** the automerge audit checks files that
*carry* a customization marker. This failure was in a file that carries no marker but that a
customization *depends on*. A deleted plain resource is invisible to a marker-based audit. Add a
step: for every customization, resolve the resource references of its layouts/files against the
post-merge resource set. Here it surfaced at build time only because a layout referenced it — a
deleted string used solely from Kotlin would have failed at runtime instead.

### 2. WIDP flavor source set was stale (build-breaking, three separate faults)

`app/src/widp/**` never conflicts — which also means upstream refactors never reach it. Three
faults accumulated silently and only surfaced at build time:

| Fault | Detail | Fix |
|---|---|---|
| Malformed source path | `app/src/widp/java/org.dhis2.utils/` used dots instead of directory separators | renamed to `app/src/widp/java/org/dhis2/utils/` |
| `GranularSyncModule.kt` outdated | upstream moved `GranularSyncRepository` to `…granularsync.data`, moved `SyncUiStateMapper` to `…granularsync.ui`, added a `provideSyncUiStateMapper` provider and a 9th `mapper` argument to `GranularSyncViewModelFactory` | replaced with the canonical flavor version (`app/src/dhis2/…`, byte-identical to `dhis2Training`). Verified beforehand that the WIDP copy held **no** client-specific logic — the pre-merge differences were purely stylistic (trailing commas, expression bodies) |
| `main_menu.xml` overrides outdated | `menu_dev` was missing from `app/src/widp/` and `app/src/widpDebug/`; `MainActivity.initCurrentScreen()` references `R.id.menu_dev`, and a full-file override removes the shared definition | added the `menu_dev` item to both, **keeping** `change_url` |

The baseline hit the same three faults in its own flavor (`8f7bb2679 fix: restore eyeseetea flavor
build after 3.4.1 merge`) and resolved the menu one by **deleting** its overrides. WIDP cannot do
that: `change_url` (Change Server URL) lives in that file.

Note: `app/src/eyeseetea/**` in this branch still carries the same staleness. It is another
client's flavor, out of scope for this PR, and already fixed on `develop-eyeseetea`.

**Audit lesson:** flavor source sets are the safest placement against merge conflicts and the most
dangerous against silent drift. Add to the post-merge checklist: diff every
`app/src/<flavor>/**` override against its shared counterpart (resource IDs, and class shape for
per-flavor DI modules). A sweep of all 29 WIDP overrides found no further dangling IDs.

### 3. `DownloadNewVersion` missing for the WIDP flavor (build-breaking)

Upstream 3.4 introduced `DownloadNewVersion` as a **per-flavor** use case, shipping it only for
`dhis2`, `dhis2PlayServices` and `dhis2Training`. `MainModule`/`MainViewModel` in shared code
require it, so every flavor must supply one. Added
`app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt`, copied from the
`dhis2` variant (non-Play-Services; byte-identical to `dhis2Training`). Deliberately **not**
copied from the eyeseetea flavor, to avoid pulling another client's code.

### 4. `releaseSessionComponent()` removed with `SessionComponent` (build-breaking)

`ChangeServerUrlDialog.dismiss()` called `app().releaseSessionComponent()`. That method only did
`sessionComponent = null` and belonged to the PIN dialog; this dialog called it by copy-paste
inheritance. Upstream deleted `SessionComponent` and `PinModule` entirely, so it was replaced with
a new `App.releaseChangeServerURLComponent()` that releases the dialog's **own** subcomponent —
which is what it should have been doing all along.

## Validation Notes

- build: `./gradlew assembleWidpDebug` **BUILD SUCCESSFUL** (after the four fixes above).
  APK: `dhis2-v3.4.1-widp-fork-1-feature-upgrade_widp_to_3_4_1.apk`.
- SDK resolved: `com.github.EyeSeeTea:dhis2-android-sdk:1.14.1-eyeseetea-fork-1` (JitPack).
- ktlint: pending
- unit tests: pending
- manual flows checked: none yet — the whole checklist in
  `upgrade-validation-checklist.md` is still outstanding.

## Known tooling inconsistency (pre-existing, not introduced by this upgrade)

`check_upgrade_docs.py --client widp` ends with one remaining issue:

```
code comment title 'Include SDK's modules' not documented in selected client docs
```

`settings.gradle.kts` carries `// EyeSeeTea customization - Include SDK's modules` on the
composite-build block. The script requires every comment title to exist as a **heading** in the
inventory or specs, and every such heading to have both a spec and a manual-validation entry. But
`openspec/config.yaml` states that the build system is explicitly out of OpenSpec scope, so this
marker can never satisfy the rule.

Not fixed here, deliberately: `settings.gradle.kts` and the comment are inherited from the shared
EyeSeeTea baseline. Changing them in a client PR would create exactly the cross-branch drift the
fork rules exist to prevent.

**Proposed fix, for the baseline:** either reserve the `EyeSeeTea customization - <Title>` prefix
for functional capabilities and use a different marker for build tooling, or teach
`check_upgrade_docs.py` an explicit allowlist for build-level titles.

Also worth fixing in the script: when `develop-eyeseetea` does not exist as a **local** branch, the
git error message is used as a file path, producing `OSError: File name too long` instead of a
clear diagnostic.

## Process improvement proposed for `conflict-rules.md`

Every one of the four build failures was findable statically, in minutes, instead of through
seven-minute build cycles. Proposed pre-build verification step for the post-merge checklist:

1. **Resource references** — resolve every `@string/`, `@drawable/`, `@id/` used by customization
   files against the post-merge resource set (catches fault 1).
2. **Flavor override drift** — diff each `app/src/<flavor>/**` file against its shared or
   sibling-flavor counterpart: resource IDs and, for per-flavor DI modules, the class shape
   (catches fault 2).
3. **New per-flavor obligations** — list classes that upstream provides once per flavor and check
   the client flavor has its own (catches fault 3).
4. **Deleted symbols still referenced** — grep customization files for symbols that no longer
   exist anywhere in the tree (catches fault 4).

## Follow-ups after this upgrade

1. **Decouple the notification download from sync** — trigger it on app start instead. Same
   implementation cost, needs `openspec/specs/notifications/spec.md` updated (it currently says
   *"during metadata sync"*) and the change to declare a modified capability. Discussed with the
   developer; deliberately kept out of this upgrade so any regression stays attributable.
2. ~~**Move the notifications wiring into `app/src/widp/`** to shrink the Oslo footprint (deferred
   from design.md D3).~~ **Closed as not viable (2026-08-14).** Android flavor source sets override
   *resources* only; two classes with the same FQN in `main` and `widp` fail the build as a
   duplicate class. The wiring lives in `ActivityGlobalAbstract` and `MainActivity`, both in
   `main`, so they cannot move. Only the files that are already 100% ours could relocate — and
   those touch no Oslo file, so there is no footprint to gain, while they would stop receiving
   upstream refactors (see fault 2 above). The Dagger→Koin move did the intended job instead:
   Oslo files carrying notifications wiring went 4 → 2.

6. **Add a `conflict-rules.md` rule for planning-phase assumptions** — this upgrade shipped a
   runtime NPE because a note written before the merge (*"the Dagger graph is not moving"*) was
   treated as a fact afterwards. Proposed rule: any statement in the notes file about **how the
   Oslo host wires a customization** (DI graph, injection site, lifecycle hook) must be re-checked
   against the merged tree before its task is ticked, and the note updated with the verification.
   Build/test/lint green does not verify DI wiring in a Koin world.
3. **Restore `url-data-element` rendering**, lost in the upstream Compose migration before this
   upgrade. Plumbing is preserved and audited; rendering is still broken.
4. **Promote the four static pre-build checks to `conflict-rules.md`** (see the section below).
5. **For the baseline:** fix the build-level marker inconsistency and the
   `check_upgrade_docs.py` error handling described above.

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
