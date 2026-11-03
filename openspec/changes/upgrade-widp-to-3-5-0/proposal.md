# Upgrade the WIDP fork to 3.5.0

## Why

The WIDP fork is on `3.4.2-widp-fork-1`. Oslo has released **3.5.0**, and the shared
EyeSeeTea baseline `develop-eyeseetea` already carries it as `3.5.0-eyeseetea-fork-1`
(`173334dbb`, vCode 159) with SDK fork `1.15.0-eyeseetea-fork-1`.

Moving now matters for two reasons:

1. **The login module was rewritten, and 2FA lives there.** The baseline replaced the
   `D2ErrorMessageProvider` path in `LoginRepositoryImpl` with typed domain errors
   (`DomainErrorMapper`), dropped `isNetworkAvailable` from `loginUser`, and drove the
   credentials screen by `CredentialsEntryMode`. 10 of the 14 merge conflicts are in the
   2FA surface. Every release the fork waits, that gap widens.
2. **Two customizations had their host code rewritten.** File upload was reworked
   upstream (ANDROAPP-7707, 7758), which moves the line that keeps images from being
   resized. A session-renewal flow (ANDROAPP-7733) now sits next to the SDK automatic
   logout the fork restored in 3.4.2.

Target upstream version: **Oslo 3.5.0**, reached exclusively by merging
`develop-eyeseetea` (`173334dbb`). Oslo is never merged into a client branch directly.

## What Changes

- Merge `develop-eyeseetea` into the WIDP branch and resolve the 14 conflicts found in the
  dry-run, preserving the five active WIDP capabilities.
- Re-anchor 2FA on the baseline's login flow: start from the baseline files and reinsert
  the minimum delta, instead of replaying the 3.4.2 code.
- Re-apply the image no-resize flag on the new `saveFileResource(path, uid, isImage)`
  signature, and verify the other upload paths still honor it.
- Re-anchor the notifications hooks and the change-URL menu action in `MainActivity` on
  the structure the baseline left.
- Decide, with evidence on a device, whether the SDK automatic logout coexists with the
  baseline's session renewal.
- Bring the fork version to `3.5.0-widp-fork-1` on SDK `1.15.0-eyeseetea-fork-1`, with
  the toolchain the baseline moved (Gradle 9.3.1, Kotlin 2.4.10).
- Refresh the technical inventory and the fork identity file against the merged tree.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. **No capability changes.** Every requirement in `openspec/specs/` stays as written;
only the implementation baseline under them moves. Nothing the WIDP user sees is intended
to change. If a conflict resolution would change a requirement, it becomes an Open
Question in `upgrade-3.5.0-notes.md`, not a silent delta.

## Impact

### Where the work is

| Area | Files in conflict | Capability |
|------|-------------------|------------|
| Login and 2FA | 10: `LoginRepository`, `LoginRepositoryImpl`, `LoginUser`, `CredentialsUiState`, `CredentialsViewModel`, `CredentialsScreen`, `CredentialsViewModelTest`, both `strings.xml`, `D2ErrorMessageProviderImpl` | two-factor-auth |
| Session | `OpenIdSession` | two-factor-auth |
| Main screen | `MainActivity` (6 hunks) | notifications, change-server-url |
| Upload | `FormValueStore` | image-upload-no-resize |
| Build | `gradle/libs.versions.toml` | version, SDK |

Other inventory files change **without** a conflict marker (`UserComponent.java`,
`ServerModule.kt`, `SessionManagerActivity.kt`, `LoginActivity.kt`, `MainModule.kt`,
`ProgramModule.kt`, `ValueStoreImpl.kt`, `EnrollmentRepository.kt`, `EventRepository.kt`,
`DomainError*`, `NotificationManager*`, and the two deleted `UserManager*.java`); they are
diffed one by one after the merge. The per-file record lives in
`eyeseetea-docs/upgrade/widp/upgrade-3.5.0-notes.md`.

### Not touched

- `AGENTS.md` — Oslo's generic agent guide (the baseline adds its Sentry section)
- `eyeseetea-docs/customizations/eyeseetea/**` and every other shared baseline doc —
  taken from `develop-eyeseetea` as-is
- The SDK fork — WIDP consumes `1.15.0-eyeseetea-fork-1` from the baseline, unchanged
- Other clients' flavors — none are present on this branch and none are added

### Stacking

This branch is stacked on `feature-widp/redo_3_4_2`, which has not reached `develop-widp`
yet. Both are delivered by whoever merges; this change does not decide how.
