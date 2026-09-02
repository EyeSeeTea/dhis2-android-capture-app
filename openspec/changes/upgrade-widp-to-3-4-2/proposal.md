## Why

The WIDP fork is at `3.3.1-widp-fork-1` while the shared EyeSeeTea baseline (`develop-eyeseetea`) has moved to **`3.4.2-eyeseetea-fork-1`** (head `f87bec8c3`, SDK `1.14.2-eyeseetea-fork-1`). This change brings WIDP onto that baseline.

An earlier attempt targeted 3.4.1 by merging Oslo (`origin/upstream/3.4.1`) **directly**, skipping the baseline, and was redone. This change follows `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md` Phase 2 and `conflict-rules.md` step 4: merge `develop-eyeseetea`, never Oslo.

Target is **3.4.2, not 3.4.1**: the baseline is already there, so merging its head is one merge instead of two, and the `PostMetadataSyncAction` extension point that the notifications customization needs was promoted to baseline in `4e5635da5`, *after* the `3.4.1-eyeseetea-fork-1` tag.

## What Changes

- **Merge `origin/develop-eyeseetea` into a branch cut from `develop-widp`** (`feature-widp/bring_last_changes_3_4_2`). Oslo is never merged directly.
- **Preserve the 5 active WIDP customizations** by reapplying the minimum client-specific behavior on top of the baseline.
- **Re-anchor the notifications download to `PostMetadataSyncAction`**, registered from the widp flavor source set. This replaces the pre-3.4.0 hook in `SyncPresenterImpl.syncMetadata()` that upstream deleted when the sync moved to the `:sync` module.
- **Re-anchor the notifications presenter DI graph from Dagger to Koin**, which upstream's `MainActivity` migration made mandatory.
- **Migrate `CLAUDE.md` ownership** (Oslo 3.4+): take Oslo's file verbatim, move WIDP content to `AGENTS-widp.md`, import both.
- **Keep the `eyeseetea` flavor building** — this branch must carry both the `widp` and `eyeseetea` flavors, and only the `widp` one is covered by CI.
- **Version bump** `3.3.1-widp-fork-1` → `3.4.2-widp-fork-1`; **SDK** `1.13.1-eyeseetea-fork-3` → `1.14.2-eyeseetea-fork-1`.

## Capabilities

### New Capabilities

None. This is a baseline upgrade.

### Modified Capabilities

None at the requirements level. All 5 specs keep their current requirements.

Note on `notifications`: its spec already says the fetch happens **"during metadata sync"**. The 3.4.1 attempt hung the download off the *data* sync (`MainViewModel.onDataSuccess()`), which silently violated that requirement. Moving to `PostMetadataSyncAction` restores compliance with the spec as written — it is a fix, not a requirement change.

## Impact

- **Upstream target version: 3.4.2** (`3.4.2-eyeseetea-fork-1`, SDK `1.14.2-eyeseetea-fork-1`).
- **Conflict surface: 37 files.** Measured from merge-base `8a4866305`: the baseline changes 1227 files, `develop-widp` changes 177, and the intersection — the only place conflicts can arise — is 37. The baseline already resolved the other 1190.
- **Flavor-isolated files** (`app/src/widp/**`, `app/src/widpDebug/**`, `app/src/widpRelease/**`): zero overlap with the baseline's changes, so zero conflict risk. One **new** file is added there: `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt`.
- **Shared code carrying customizations**: 24 of the 37 overlapping files. The `login/` module (2FA) and `commonskmm/` error mapping concentrate the highest-risk work.
- **Baseline-owned files** in the overlap that must take the baseline version unconditionally: `eyeseetea-docs/upgrade/conflict-rules.md`, `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`.
- **Toolchain jump**: Gradle wrapper `8.13` → `9.3.1`, Kotlin `2.2.21` → `2.3.20`. Build script and CI adjustments are expected; this is the largest source of build-time (as opposed to behavioral) risk.
- **Second flavor**: `eyeseetea` must compile on this branch. It currently does not on the failed branch, and nothing in CI would have caught it.
