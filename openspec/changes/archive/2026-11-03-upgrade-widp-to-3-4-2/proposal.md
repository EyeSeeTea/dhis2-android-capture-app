# Upgrade the WIDP fork to 3.4.2

## Why

The WIDP fork currently ships `3.3.1-widp-fork-1`. Oslo has released **3.4.2**, and the
shared EyeSeeTea baseline `develop-eyeseetea` already carries it as
`3.4.2-eyeseetea-fork-1` with SDK fork `1.14.2-eyeseetea-fork-1`.

WIDP must move to that baseline for three reasons:

1. **Two Oslo minor versions of drift.** 3.4.0 and 3.4.1 both landed architectural
   migrations (Dagger to Koin in the main screen, metadata sync extracted into the KMP
   `:sync` module, Gradle 8.13 to 9.x, Kotlin 2.2 to 2.3). The longer the fork stays
   behind, the more expensive each customization is to re-anchor.
2. **One customization loses its host.** The notifications download was hooked into
   `SyncPresenterImpl.syncMetadata()`. Oslo removed that entry point in 3.4.1. The
   baseline has already promoted the replacement extension point
   (`PostMetadataSyncAction` in `:commonskmm`), so WIDP can adopt a shared mechanism
   instead of inventing a fork-local one.
3. **The fork identity documentation is wrong today.** `CLAUDE.md` claims a version, an
   SDK tag, a Gradle version, a module count and a customization status that no longer
   match the tree. An upgrade is the moment to make it true again.

Target upstream version: **Oslo 3.4.2**, reached exclusively by merging
`origin/develop-eyeseetea` (commit `8e0200bcc`). Oslo is never merged into a client
branch directly.

## What Changes

- Merge `origin/develop-eyeseetea` into the WIDP branch and resolve the conflicts,
  preserving the five active WIDP capabilities.
- Re-anchor the notifications capability onto the two mechanisms Oslo's migrations
  require: Koin for the DI graph, `PostMetadataSyncAction` for the post-sync download.
- Add regression coverage for the notifications domain and for the WIDP flavor wiring.
- Bring the fork version to `3.4.2-widp-fork-1` on SDK `1.14.2-eyeseetea-fork-1`.
- Refresh the technical inventory and the fork identity file so they describe the tree
  that actually exists after the merge.
- Adopt the baseline convention where `CLAUDE.md` is Oslo's four-line bridge and the
  fork identity lives in `AGENTS-widp.md`.

**No capability changes.** Every requirement in `openspec/specs/` stays as written; only
the implementation baseline under them moves. Nothing the WIDP user sees is intended to
change.

## Impact

### Flavor-isolated (`app/src/widp/**`, `app/src/widpDebug/**`) — zero conflict surface

- `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` (new) — the notifications
  download registration, at level 1 of the placement hierarchy
- `app/src/widp/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` (new) —
  upstream turned this into a per-flavor file in 3.4.x; WIDP needs its own copy
- `app/src/widp/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — relocated out
  of the malformed `java/org.dhis2.utils/` directory and updated to the 3.4.x API
- `app/src/widp/res/menu/main_menu.xml` and `app/src/widpDebug/res/menu/main_menu.xml` —
  menu entries, duplicated across both source sets

### Shared code — the conflict surface reviewers should look at

- `app/src/main/java/org/dhis2/usescases/general/ActivityGlobalAbstract.java` —
  notifications presenter resolution moves from Dagger field injection to Koin
- `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` — WIDP menu actions on top
  of a screen Oslo migrated to Koin
- `app/src/main/java/org/dhis2/utils/session/**` — Change Server URL, WIDP-only files that
  nonetheless depend on shared strings and layouts
- `app/src/main/res/values/strings.xml` — resources Oslo deleted that WIDP layouts still
  reference
- `commons/`, `commonskmm/`, `form/`, `login/` — the notifications preference layer, the
  2FA error messages, the no-resize flag and the URL data element plumbing

`app/src/main/res/menu/main_menu.xml` gains a WIDP-only item id so that the other flavors
compile: `MainActivity` is shared and already references it, which means the defect it
fixes is older than this upgrade.

### Not touched

- `AGENTS.md` — Oslo's generic agent guide
- `eyeseetea-docs/customizations/eyeseetea/**` and every other shared baseline doc — taken
  from `develop-eyeseetea` as-is
- The SDK fork — WIDP consumes `1.14.2-eyeseetea-fork-1` from the baseline, unchanged
- Other clients' flavors — none are present on this branch and none are added
