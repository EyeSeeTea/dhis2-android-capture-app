# Design — WIDP upgrade to 3.5.0

## Context

Source of the upgrade: `develop-eyeseetea` at `173334dbb` (`3.5.0-eyeseetea-fork-1`, SDK
`1.15.0-eyeseetea-fork-1`, Gradle 9.3.1, Kotlin 2.4.10). Starting point:
`feature-widp/redo_3_4_2` at `e2f13d13d` (`3.4.2-widp-fork-1`). Merge base: `8e0200bcc`.
A dry-run (`git merge --no-commit`, then `--abort`) found 707 files changed and 14
conflicts.

All conflict resolution follows `eyeseetea-docs/upgrade/conflict-rules.md`. The per-file
record lives in `eyeseetea-docs/upgrade/widp/upgrade-3.5.0-notes.md`; this file records the
strategy and the decisions that are not per-file. See `proposal.md` for the motivation.

## Goals / Non-Goals

**Goals:**
- The five capabilities behave as before; the diff against the baseline shrinks or stays
  the same size, never grows.
- Every WIDP line in an Oslo file sits where `conflict-rules.md` wants it (end of file,
  new file or flavor source set before inline edits).

**Non-Goals:**
- Any change to WIDP-visible behavior.
- Patching the DHIS2 SDK fork.
- Deciding how `3.4.2` and `3.5.0` reach `develop-widp` (that is the merger's call).
- Adopting baseline features WIDP did not ask for (for example the session-renewal dialog
  is taken as the baseline ships it).

## Decisions

### D1. The upgrade arrives by merging the baseline

`develop-eyeseetea` is merged with a real merge commit. Oslo is never merged into a client
branch and no other client's fork is cherry-picked from. Same as 3.4.2.

### D2. Commit granularity: does the tree need it to compile?

The merge commit carries the conflict resolution **and nothing else except what the tree
needs to build**. A change that is a design decision rather than a build requirement gets
its own commit on top. For this upgrade that means:

- the merge, including re-applying 2FA, the no-resize flag and the `MainActivity` hooks,
  because the tree does not compile without the 2FA types the screen references;
- the 2FA test rebuild as a separate commit, because the merge compiles with the baseline
  test and the 2FA cases are coverage on top of it;
- the session-renewal reconciliation (D4) as a separate commit, because it is a runtime
  decision, not a build one.

### D3. 2FA: intercept before the mapper, do not extend the domain error model

The baseline calls `blockingLogIn(username, password, serverUrl, null)` — the fourth
argument is the two-factor code, already plumbed through the SDK — and maps every `D2Error`
through `DomainErrorMapper` into `AuthenticationError(message)`. That loses the error code,
and 2FA needs the code: the same SDK answer means "a code is required" with no code
submitted and "the code was rejected" with one.

Two options were considered:

- **(a) Add dedicated domain errors for the seven 2FA codes in `DomainError.kt` and
  `DomainErrorMapper.kt`.** Closest to the baseline's direction, but both are Oslo files
  in `commonskmm`, shared by every feature: two more places that conflict on every future
  upgrade.
- **(b) Catch the `D2Error` in `LoginRepositoryImpl` before the mapper**, hand 2FA codes to
  the fork's existing `isTwoFactorError()` / `handleTwoFactorError()` and let everything
  else fall through to `domainErrorMapper.mapToDomainError`. The fork code stays at the end
  of one file and the Oslo `catch` changes by one `if`.

**Decision: (b).** It follows the placement hierarchy and keeps the 3.4.2 `TwoFactorOutcome`
model, which already has tests. `handleTwoFactorError` used `isNetworkAvailable` only to
call `D2ErrorMessageProvider`; the 2FA messages do not depend on connectivity (the provider
selects them by code), so the parameter is dropped from the helper rather than re-added to
the Oslo signature. `LoginRepository.loginUser` keeps only the trailing
`twoFactorCode: String? = null`.

### D4. Session: keep the SDK automatic logout, verify it does not double-fire

The baseline's `OpenIdSession` listens to the account deletion only. It does not listen to
`accountManager().logOutObservable()`, which is what the fork restored in 3.4.2 so that a
session the server rejects sends the user back to login. The baseline's new session
renewal (`SessionRenewalRequiredError`, `SessionRenewalDialog`, `ForcedSessionExpiry`) is a
different case — expired OAuth/OpenID tokens — and `ForcedSessionExpiry` is a development
tool, not a runtime path.

**Decision: keep the fork's `logOutObservable` source**, appended to the baseline's
`Observable.merge` with the existing `// EyeSeeTea customization - 2FA support` comment, and
verify on a device that an unauthorized session still reaches login exactly once, with the
renewal dialog and the logout not stacking. If they stack, the fix is to filter ours, not to
edit the baseline's classes.

### D5. Image no-resize: keep the one-line flag, prove it on a device

In the baseline, `saveFileResource(path, uid, isImage)` selects
`ResourceContext.ImageContext.ProgramImageContext` when `isImage` is true and
`ResourceContext.FileContext` otherwise, and the image path is where processing happens.
Passing `isImage = false` therefore still bypasses the image treatment, which is what the
customization has always done. The one line is re-applied on the new signature with the same
`// EyeSeeTea customization - Image upload without resizing` comment and the original
commented out below it.

Because ANDROAPP-7707/7758 rewrote upload end to end, "reads right" is not enough: the
other upload paths touched by the merge (`ValueStoreImpl`, `EnrollmentRepository`,
`EventRepository`) are checked, and a picture is uploaded on a device and compared with the
original.

### D6. `MainActivity` hooks go inside the baseline's new branch bodies

The baseline collapsed the `HomeEffect` branches, so the notifications hooks (`GoToLogin`,
`SingleProgramNavigation`, resume) and the `R.id.change_url` action are reinserted inside
the new bodies, starting from the baseline file. Takes the baseline structure, adds only the
hook lines, keeps each `// EyeSeeTea customization - <Title>` comment right above its block.

### D7. SDK and toolchain come from the baseline

`dhis2sdk = "1.15.0-eyeseetea-fork-1"`, Gradle 9.3.1 and Kotlin 2.4.10 are taken as they
are. The app resolves the SDK from JitPack unless `dhis2.useLocalSdk` is set; the untracked
`dhis2-android-sdk/` checkout is at `1.12.1-eyeseetea-fork-1` and is not used by default.
Nothing in this upgrade patches the SDK.

## Expected conflict classification

Predicted from the dry-run. The actual per-file outcome is recorded in the notes file.

| Category | Files | Reasoning |
|---|---|---|
| `manual_reapply_on_theirs` | all 14 | Every conflict is a shared file where the baseline changed the structure and WIDP added behavior. Start from the baseline file, reinsert the minimum delta. |
| `defer_after_build_verification` | `CredentialsViewModelTest.kt` | 14 hunks. Take the baseline test, then port the 2FA cases one by one. |
| `accept_theirs` | (none predicted) | No conflict is baseline-owned only. `AGENTS.md` and the shared docs merge cleanly. |

## Risks / Trade-offs

| Risk | Why it matters | Mitigation |
|---|---|---|
| 2FA messages degrade silently | The baseline groups the seven 2FA codes in one `-> defaultError()` arm. Taking the baseline file compiles, passes tests, and turns every 2FA message into "unexpected error". Same trap as 3.4.2, new text. | Split the arm, and `grep -c 'two_factor' D2ErrorMessageProviderImpl.kt` must be 6. Duplicate `when` arms compile and the first wins: check the order, not only the count. |
| 2FA swallowed by the mapper | If the `catch` runs `mapToDomainError` first, the code is lost and the 2FA field never shows. | D3 checks `isTwoFactorError(code)` before the mapper. A unit test fixes this order. |
| Silent automerge deletion | The baseline removed `UserManager*.java` and changed `UserComponent.java`, `ServerModule.kt` and `SessionManagerActivity.kt` with no conflict marker. | Two-dot diff every file in `customization-files.md`, not only the conflicted ones. |
| Double logout | Our `logOutObservable` and the baseline's renewal flow both route to login. | D4: verify on a device; filter ours if they stack. |
| Image resized again | The upload path was rewritten. | D5: device check against the original picture, not only a code read. |
| The `eyeseetea` flavor does not compile | CI only builds `widp`. | `:app:compileEyeseeteaDebugKotlin` in every verification run. |
| A runtime break looks like success | Notifications, 2FA and logout produce no compile error when wrong. | Validate on a real device against package name **and** version, recorded next to every result. |
| Stacked on an unmerged branch | A change requested on the 3.4.2 PR forces a rebase of this branch. | Keep this branch's commits free of 3.4.2 content; rebase, do not re-merge. |

## Migration Plan

Same order as 3.4.2: documentation commit, merge commit, then one commit per decision
(`tasks.md` states the boundary of each). Rollback is `git reset` to the last commit before
the merge, since nothing is pushed to `develop-widp` from this branch.

## Open Questions

- How the two stacked upgrades (3.4.2 and 3.5.0) reach `develop-widp`: one PR or two. Does
  not change the tasks; belongs to whoever merges.
- Whether `login/build.gradle.kts` still needs the `widp` flavor block (`needs_validation`
  since 3.4.2). Answered by building both flavors; no decision needed before that.
