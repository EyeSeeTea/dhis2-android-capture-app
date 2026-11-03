# Design — WIDP upgrade to 3.4.2

## Context

Source of the upgrade: `origin/develop-eyeseetea` at `8e0200bcc`
(`3.4.2-eyeseetea-fork-1`, SDK `1.14.2-eyeseetea-fork-1`). Starting point:
`develop-widp` at `3252c0681` (`3.3.1-widp-fork-1`, SDK `1.13.1-eyeseetea-fork-3`).
Merge base: `8a4866305`. 355 commits and ~1200 files separate the two sides.

All conflict resolution follows `eyeseetea-docs/upgrade/conflict-rules.md`. The
per-file record of what was actually decided lives in
`eyeseetea-docs/upgrade/widp/upgrade-3.4.2-notes.md`; this file records the strategy and
the decisions that are not per-file.

## Decisions

### D1. The upgrade arrives by merging the baseline, not by replicating it

`develop-eyeseetea` is merged into the WIDP branch with a real merge commit. Oslo is
never merged into a client branch, and no other client's fork is cherry-picked from.

Consequence worth stating explicitly: the baseline's own customizations are
flavor-isolated under `app/src/eyeseetea/`, so the merge brings a second client flavor
onto this branch by design. The branch is expected to end with exactly the three Oslo
flavors (`dhis2`, `dhis2PlayServices`, `dhis2Training`) plus `eyeseetea` and `widp`, and
no flavor belonging to any other client.

### D2. Commit granularity: does it have to be there for the tree to compile?

The merge commit carries the conflict resolution **and nothing else except what the
branch needs in order to build**. Anything that is a design decision rather than a build
requirement gets its own commit on top, so a reviewer can read it separately from ~1200
merged files.

This splits the notifications work in two: the Dagger-to-Koin re-anchoring and the
post-sync download are both runtime concerns — the tree compiles without either — so
both are separate commits, and the empty `PostMetadataSyncModule` that the build does
require ships inside the merge.

### D3. SDK fork dependency

WIDP depends on the EyeSeeTea SDK fork for 2FA:

- `D2ErrorCode` values `EMAIL_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`,
  `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_SMS`,
  `TWO_FACTOR_MANY_SEND_ATTEMPTS` and the TOTP codes
- `LogInCall.generate2FAErrorIfRequired()` and `LoginPayload.twoFactorCode`

The tag comes from the baseline (`1.14.2-eyeseetea-fork-1`); this upgrade does not patch
the SDK. The fork-side surface that consumes those codes is
`commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/resources/D2ErrorMessageProviderImpl.kt`,
which is a **known trap**: the baseline routes every 2FA code to the generic
`defaultError()`. Accepting the baseline version of that file compiles, passes tests, and
silently degrades every 2FA message to "unexpected error". The guard is a grep count, not
a build.

### D4. Notifications DI moves to Koin rather than keeping a Dagger island

Oslo migrated `MainActivity` to Koin and stopped running the Dagger `inject()` that
populated the inherited `notificationsPresenter` in `ActivityGlobalAbstract`. Keeping
Dagger alive for this one graph would mean re-introducing an injection point Oslo
deleted. The presenter is published in Koin instead and resolved from
`ActivityGlobalAbstract`, which is where every screen that renders a notification dialog
already reads it.

### D5. The post-sync download uses the baseline's extension point, not a fork hook

`SyncPresenterImpl.syncMetadata().doOnComplete {}` is gone: metadata sync now lives in the
KMP `:sync` module, which cannot depend on `:app`. The baseline already promoted
`PostMetadataSyncAction` (`:commonskmm`), invoked by `SyncMetadata`, registered per
flavor. WIDP registers its download there — level 1 of the placement hierarchy, zero Oslo
files touched. The mechanism and its gotchas are documented in
`eyeseetea-docs/customization-techniques.md` §T2.

`PostMetadataSyncModule.kt` must exist in **every** flavor source set, empty where unused:
there is no shared default, because in Koin 4 the winner between two definitions depends
on module load order.

### D6. Fork identity moves to `AGENTS-widp.md`

Since Oslo 3.4, `CLAUDE.md` is an Oslo file: a four-line bridge to `AGENTS.md`. Carrying
77 lines of WIDP identity in it guarantees a whole-file conflict on every upgrade. The
identity moves to `AGENTS-widp.md`, per `eyeseetea-docs/README.md` and
`onboarding-fork-guide.md` Phase 5. `AGENTS.md` is Oslo's generic guide and is never
edited.

This is convention adoption, not upgrade work — it is the last commit so it can be
dropped without touching anything else.

## Expected conflict classification

Predicted before the merge, using the four categories from `conflict-rules.md`. The
actual per-file outcome is recorded in the notes file.

| Category | Expected areas | Reasoning |
|---|---|---|
| `accept_ours` | `app/src/widp/**`, `app/src/widpDebug/**`, `app/src/widpRelease/**` | Flavor-owned surface. Normalized only where a 3.4.x API forces it. |
| `accept_theirs` | `eyeseetea-docs/**` shared docs and templates, `.claude/` scaffolding, Oslo build/config files, tests that only follow baseline refactors | Baseline-owned. WIDP's copies are stale by definition. |
| `manual_reapply_on_theirs` | `ActivityGlobalAbstract.java`, `MainActivity.kt`, `commons/.../prefs/**`, `commonskmm/.../D2ErrorMessageProviderImpl.kt`, `form/.../FormValueStore.kt`, `form/` URL plumbing, `app/src/main/res/values/strings.xml` | Shared files where Oslo changed the structure and WIDP added behavior. Start from the baseline, reinsert the minimum delta. |
| `defer_after_build_verification` | Dagger modules whose graph Oslo migrated, `SyncPresenterImpl.kt` and anything else whose customization may already be absorbed or relocated | Low confidence that the WIDP delta is still needed. Keep the baseline, verify, reintroduce only if behavior is missing. |

## Risks

| Risk | Why it matters | Mitigation |
|---|---|---|
| Silent automerge deletion | The baseline contains commits that removed customization wiring; git applies them without conflict markers. This is how the 3.3.1 upgrade lost the notifications wiring in `MainActivity`/`MainView`/`MainPresenter`. | Two-dot diff every file in `customization-files.md` after resolving, not only the conflicted ones. |
| 2FA messages degrade silently | See D3. Compiles and tests green while every 2FA message becomes a generic error. | `grep -c 'two_factor' D2ErrorMessageProviderImpl.kt` must be 6. |
| The `eyeseetea` flavor does not compile | CI only builds `widp`, so a break there is invisible. It was already broken before this upgrade. | `:app:compileEyeseeteaDebugKotlin` is part of every verification run, alongside `:app:assembleWidpDebug`. |
| A notifications runtime break looks like success | The DI and sync changes produce no compile error when wrong — a null presenter or an action that never fires. | Validate on a real device against the installed package name and version, not only with unit tests. |
| Manual validation recorded against the wrong build | Already happened: results were recorded from the `dhis2` flavor, which carries no WIDP customization at all. | Record `dumpsys package com.eyeseetea.widp.debug | grep versionName` **and** the package name next to every manual result. |

## Out of scope

- Any change to WIDP-visible behavior. If the merge would change what the user sees, it
  goes to Open Questions in the notes file instead of being decided here.
- Patching the DHIS2 SDK fork.
- The pre-existing 2FA mandatory-enrolment error. It reproduces on the previous build, so
  it is not a regression of this upgrade, and changing it would be a functional decision
  WIDP has not been asked to make.
- Removing the orphan `app/src/dhisPlayServices/` directory (no flavor declares it). It
  exists in `develop-eyeseetea` too, so it is baseline work, not client work.
