# Design — Upgrade WIDP fork to upstream 3.4.1

## Context

See `proposal.md — Why` for motivation. The constraints that shape this design:

- **`develop-eyeseetea` carries none of the five WIDP customizations.** Verified by file
  inventory across the three refs: `usescases/notifications` (widp 9 files / baseline 0),
  `ChangeServerURL*` (6 / 0), `TwoFactorState` + `TwoFactorRequiredException` (2 / 0). PR #323
  is therefore a reference for *mechanics* (merge shape, SDK bump, build fixes, Oslo patches),
  and an **anti-reference** for customizations.
- Upstream 3.4.0 landed ANDROAPP-7340 (`MainPresenter`/`MainView` → `MainViewModel`) and the
  `App.java` → `App.kt` migration, deleting four of the notification customization's anchor
  points and one of Change Server URL's.
- The predicted conflict surface (`git merge-tree develop-widp origin/upstream/3.4.1`) is
  **27 files: 21 content, 6 modify/delete**, plus **8 files that auto-merge silently** despite
  being customized and touched upstream.
- Merge rules, resolution categories and comment conventions are defined in
  `eyeseetea-docs/upgrade/conflict-rules.md`; this design classifies against them rather than
  restating them.

## Goals / Non-Goals

**Goals:**

- Land 3.4.1 on `develop-widp` with all five capabilities behaviorally unchanged.
- Produce a conflict classification that is decided *before* the merge, not discovered during it.
- Make every silent-automerge file an explicit, auditable step.
- Leave the notifications customization on a footing that costs less in the next upgrade.

**Non-Goals:**

- Restoring `url-data-element` rendering (see `proposal.md` — out of scope). Its plumbing in
  `EnrollmentRepository`/`EventRepository` is preserved and audited, nothing more.
- Refactoring customization placement beyond the files the merge already forces us to rewrite.
  `conflict-rules.md` forbids placement refactors during an active merge.
- Upgrading `develop-eyeseetea` to 3.4.2 (separate track, separate PR).

## Decisions

### D1. Merge `origin/upstream/3.4.1` directly, in one step

`git merge origin/upstream/3.4.1` on `feature/upgrade_widp_to_3_4_1`, cut from `develop-widp`.

*Why:* it is the fork's established mechanic (`Merge remote-tracking branch
'origin/upstream/3.3.1' into feature-widp/bring_last_changes_3_3_1`) and it structurally cannot
import baseline or other-client code, satisfying the hard rule in `.claude/CLAUDE.md`.

*Alternatives considered:*
- *Merge `develop-eyeseetea`* — rejected outright: drags in baseline customizations and the
  baseline's **deletions** of code WIDP still needs.
- *Two steps (3.4.0 then 3.4.1)*, mirroring PR #311 + #323 — rejected. It would split the
  conflict batch, but every conflict lands in WIDP-specific code that has no baseline reference
  anyway, so the second pass buys reviewability we can get more cheaply by staging the
  resolution (D2) inside one merge.
- *Cherry-picking upstream commits individually* — rejected: ~100 upstream commits, and the
  release branch is already the curated unit.

### D2. Resolve in four staged passes, not file-by-file

Classification against the `conflict-rules.md` categories, decided up front:

| # | File | Category | Rationale |
|---|---|---|---|
| 1 | `app/src/androidTest/assets/databases/dhis_test.db` | `accept_theirs` | Upstream test fixture, no WIDP content. |
| 2 | `gradle.properties` | `accept_theirs` | Build tuning, no customization. |
| 3 | `CLAUDE.md` (add/add) | `accept_ours` | Fork identity doc; WIDP's version is authoritative. |
| 4 | `app/build.gradle.kts` | `manual_reapply_on_theirs` | Take upstream, re-add the `widp` flavor block + applicationId + fork versionName. Reference: PR #323 did the same for `eyeseetea`. |
| 5 | `gradle/libs.versions.toml` | `manual_reapply_on_theirs` | Take upstream, then set `dhis2sdk = "1.14.1-eyeseetea-fork-1"`, `vName = "3.4.1-widp-fork-1"`, `vCode = "156"`. |
| 6 | `login/build.gradle.kts` | `manual_reapply_on_theirs` | Upstream module config + WIDP 2FA deps. |
| 7–11 | `SearchTEList.kt`, `SearchTEIViewModel.kt`, `SyncPresenterTest.kt`, `app/.../res/values/strings.xml`, `commonskmm/.../strings.xml` | `manual_reapply_on_theirs` | Upstream body + WIDP additions re-appended. |
| 12–17 | `LoginRepositoryImpl.kt`, `LoginUser.kt`, `CredentialsScreen.kt`, `CredentialsUiState.kt`, `CredentialsViewModel.kt`, `PreferenceConstants.kt` | `manual_reapply_on_theirs` | See D4. |
| 18–21 | `SyncGranularRxModule.kt`, `SyncPresenterImpl.kt`, `UserComponent.java`, `MainActivity.kt` | `manual_reapply_on_theirs` | See D3/D5. |
| 22–27 | `App.java`, `MainPresenter.kt`, `MainView.kt`, `SyncDataWorkerModule.kt`, `SyncInitWorkerModule.kt`, `SyncMetadataWorkerModule.kt` | **port, not merge** | Deleted upstream. See D3. |

Pass order: (a) `accept_theirs`/`accept_ours` and build identity → (b) 2FA → (c) Change Server
URL → (d) Notifications port. Developer review checkpoint after (a), because everything after it
is judgement-heavy. `defer_after_build_verification` is deliberately empty at planning time; any
file that lands there during execution gets recorded in the notes file rather than resolved by
guesswork.

### D3. Notifications: port the call path, leave the Dagger graph alone

Verified against `origin/upstream/3.4.1` before planning, not assumed. **3.4.1 runs Dagger and
Koin side by side**: `App.kt` still builds `DaggerAppComponent` and still exposes
`ServerComponent`/`UserComponent`, while `MainViewModel` is Koin-provided
(`viewModel { params -> MainViewModel(...) }` in `mainModule`, dependencies resolved with `get()`).

This matters because it decides how much has to move. The answer is: very little. The
customization's own 14 files under `usescases/notifications/**` and `data/notifications/**` do not
conflict, and **`MainActivity` — which is where the customization actually does its work — survives
the merge**:

```kotlin
// MainActivity.kt (WIDP today, file survives 3.4.1 as a content conflict)
override fun markShowNotificationsAsPending() { notificationsPresenter.markShowNotificationsAsPending() }
override fun refreshNotifications()           { notificationsPresenter.refresh(this) }
```

`MainView`/`MainPresenter` were only the indirection between the lifecycle event and that
activity. So the port replaces a call path, and the Dagger notifications graph is untouched —
**no Koin bridge is needed for the notifications use cases**.

```
 3.3.1 (WIDP)                              3.4.1 (target)
 ───────────────────────────────           ─────────────────────────────────────
 App.java                                  App.kt          (Dagger survives)
   .notificationsModule(...)         ──►     .notificationsModule(...)
 MainPresenter.checkSingleProgramNavigation()
   ├─ single program  → markPending  ──►   MainViewModel + its CheckSingleNavigation
   └─ otherwise       → markPending          use case: same branch, same two calls
                        + refresh
 MainView.kt (interface)             ──►   deleted; MainActivity keeps the two methods
                                            and reacts to MainViewModel state instead
 MainActivity.notificationsPresenter ──►   unchanged (Dagger)
 usescases/notifications/**  (14 files) ─► unchanged, no conflict
```

**The exact semantics to preserve**, extracted from `MainPresenter.checkSingleProgramNavigation()`
before the merge deletes the file: `markShowNotificationsAsPending()` fires on **both** branches;
`refreshNotifications()` fires **only** when the app is *not* auto-navigating into a single
program. Losing that asymmetry is the specific regression this port risks.

*Why faithful over opportunistic:* the notifications capability is the highest-risk WIDP
customization and has no baseline reference. Changing behavior and architecture in the same pass
would make a regression impossible to attribute. Placement improvements are a follow-up change,
consistent with `conflict-rules.md`'s ban on placement refactors mid-merge.

*Alternative considered:* extract a `NotificationsBridge` into `app/src/widp/` to shrink the Oslo
footprint. Deferred, not discarded — recorded as a follow-up once 3.4.1 is validated.

**DI landing site — resolved, no longer open.** `SyncGranularRxModule.kt` (the Dagger module that
provides `NotificationRepository` to `SyncPresenterImpl`) **survives** 3.4.1 as a content conflict,
as do `SyncPresenterImpl.kt`, `SyncPresenter.java` and `SyncGranularRxComponent.java`. The three
deleted `Sync{Data,Init,Metadata}WorkerModule` files were parallel Dagger providers for
`SyncDataWorker`/`SyncInitWorker`/`SyncMetadataWorker`, which upstream consolidated: 3.4.1
registers only `workerOf(::SyncGranularWorker)` and `workerOf(::CheckVersionWorker)` in Koin, and
`SyncGranularWorker` still reaches back into Dagger via
`userComponent().plus(SyncGranularRxModule(...)).inject(this)`.

So the *provision* of `NotificationRepository` needs no new home. What does need verification is
the **invocation**: `SyncPresenterImpl.syncNotifications()` (a `runBlocking` collect on
`notificationRepository.sync()`) must still be reached now that the three workers that used to
drive those code paths are gone. Provision surviving is not the same as the trigger surviving.

### D4. 2FA: adapt to SDK 1.14.1, and explicitly reject the baseline's resolution

Two changes are forced by the SDK fork bump, and their correct WIDP resolution differs from the
baseline's:

| Site | Upstream 3.4.1 | Baseline (PR #323) | **WIDP must do** |
|---|---|---|---|
| `LoginRepositoryImpl.blockingLogIn` | 3 args | `(user, pass, url, null)` | pass the **real** `twoFactorCode` |
| `D2ErrorMessageProviderImpl` exhaustive `when` | no 2FA branches | 7 branches → `defaultError()` | keep WIDP's **real messages** |

The baseline's `defaultError()` mapping exists only to make an exhaustive `when` compile in a
build that has no 2FA UI. Copying it into WIDP would compile cleanly, pass CI, and silently
destroy every scenario in the `two-factor-auth` spec — the resend prompts, the per-type error
copy, and the rate-limit message. This is the single most likely way this upgrade breaks
something without anyone noticing, which is why it is called out here rather than left to review.

`D2ErrorMessageProviderImpl.kt` is in the **silent-automerge** set (D6), so it will not raise a
conflict to prompt the check.

*SDK patch availability is confirmed, not assumed:* the baseline compiles against
`1.14.1-eyeseetea-fork-1` while referencing `D2ErrorCode.INCORRECT_TWO_FACTOR_CODE_TOTP`,
`EMAIL_TWO_FACTOR_CODE_SENT`, `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`,
`INCORRECT_TWO_FACTOR_CODE_SMS` and `TWO_FACTOR_MANY_SEND_ATTEMPTS` — so the fork tag carries the
patch surface the `two-factor-auth` spec requires (`LoginPayload.twoFactorCode`, the added
`D2ErrorCode` values). `LogInCall.generate2FAErrorIfRequired()` is not observable from the app
side and is verified at runtime by the `preprod-indiv` validation flow.

### D5. Change Server URL: re-anchor on `App.kt`, keep `UserComponent`

`ChangeServerURLComponent`/`Module`/`Presenter`/`Dialog` and `BasicPreferenceProvider*` do not
conflict. Only three anchors do: the Dagger field and component builder in the deleted
`App.java`, the `plus(ChangeServerURLModule)` in the surviving `UserComponent.java`, and
`PreferenceConstants.kt`. `ActivityGlobalAbstract.java` (the menu entry) auto-merges — audited
under D6.

### D6. Treat the eight silent-automerge files as first-class work items

Customized by WIDP, changed upstream, **no conflict raised**:

```
ActivityGlobalAbstract.java              → change-server-url menu entry
D2ErrorMessageProviderImpl.kt            → 2FA messages          ⚠ see D4
LoginRepository.kt                       → 2FA interface
login/…/composeResources/values/strings.xml → 2FA copy
CredentialsViewModelTest.kt              → 2FA tests
EnrollmentRepository.kt / EventRepository.kt → url-data-element plumbing
SearchTEIViewModelTest.kt                → Oslo search patch tests
```

Each gets an explicit `git diff develop-widp -- <path>` plus a
`grep -n "EyeSeeTea customization"` check after the merge. This is the failure mode the root
`CLAUDE.md` automerge rule exists for: git can apply upstream hunks that delete customization
wiring with no marker. The audit covers **every** file in `customization-files.md`, not only
these eight — these are simply the ones already known to be at risk.

### D7. Cherry-pick the three missing Oslo patches after the merge is green

WIDP already carries ANDROAPP-6844, the stale-search-results fix and the search spinner fix
(PR #315). Missing, and present in the baseline: `1e4149f01` (ANDROAPP-7661 granular sync image
download race), `48058eb9a` (ANDROAPP-7666 completed-event dialog), `726f3bd7e` (empty list on
return from TEI). These are generic Oslo bug patches, not EyeSeeTea customizations, so
replicating them into WIDP is within the fork rules.

Applied **after** the merge builds, so a cherry-pick failure cannot be confused with a merge
failure. Each is re-verified against the 3.4.1 code — an Oslo patch may have been fixed upstream
in the meantime, in which case it is skipped and the skip is recorded.

## Risks / Trade-offs

- **2FA degraded silently by copying the baseline's `defaultError()` mapping** → D4 makes it an
  explicit, named task; the `preprod-indiv` 2FA flow is a release gate, not an optional check.
- **Notifications port loses a lifecycle nuance** (e.g. refresh no longer fires on the same
  event) → port with the spec's scenarios open; unit-test the `MainViewModel` entry points;
  manual validation flow is mandatory before the PR.
- **Silent automerge drops customization wiring** → D6 audit over the full
  `customization-files.md` inventory, cross-checked against each customization's feat commit.
- **One-step merge produces a large, hard-to-review diff** → accepted trade-off from D1;
  mitigated by the staged resolution order and a developer checkpoint after the mechanical pass.
- **`SyncPresenterImpl.syncNotifications()` silently loses its trigger** because the three deleted
  workers were what drove it → D3; verify the invocation path, not just the Dagger provision, and
  cover it in the notifications validation flow (a background sync that produces no notification
  refresh is invisible in a build log).
- **`url-data-element` regresses further while out of scope** → its two plumbing files are in the
  D6 audit set, so "unchanged" is verified rather than assumed.
- **Build-green mistaken for done** → every customization task's completion criterion is its
  entry in `upgrade-validation-checklist.md`, not compilation.

## Migration Plan

Branch `feature/upgrade_widp_to_3_4_1` (already cut from `develop-widp`) → merge → staged
resolution → build → cherry-picks → tests → manual validation → PR against `develop-widp`.

Rollback is `git merge --abort` before the merge commit, or discarding the branch after it;
`develop-widp` is never touched directly, and nothing is pushed until validation passes.

## Open Questions

- Exactly where in `MainViewModel` / `MainActivity` the 3.4.1 single-navigation outcome becomes
  observable, so the two notification calls can hang off it with the branch asymmetry intact
  (D3). Deferrable: the approach, scope and task list are fixed; only the anchor line is open.
- Whether the three Oslo patches (D7) still apply to 3.4.1 or were fixed upstream. Resolved per
  patch during execution; either outcome is recorded, neither changes the plan.

## Environment prerequisites

- SDK source mode (`useLocalSdk` in `local.properties`) must be confirmed before trusting any
  dependency-resolution check — see `eyeseetea-docs/SDK_Setup.md`.
