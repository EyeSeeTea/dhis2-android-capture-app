# Upgrade WIDP fork to upstream 3.4.1

## Why

`develop-widp` is pinned to upstream **3.3.1** (`3.3.1-widp-fork-1`, vCode 151, SDK
`1.13.1-eyeseetea-fork-3`) while upstream has released **3.4.0** and **3.4.1**. The shared
EyeSeeTea baseline `develop-eyeseetea` has already moved to **3.4.1**
(`3.4.1-eyeseetea-fork-1`, SDK `1.14.1-eyeseetea-fork-1`) via PR #311 and PR #323, so WIDP is
now the branch holding the fork back. Each release WIDP skips makes the next merge more
expensive, and 3.4.0 contains an architectural refactor (MVP → MVVM on the main screen) that
directly deletes the files the WIDP notifications customization is wired into — the cost of
deferring is already visible.

The upgrade must land **without losing any of the five WIDP customizations**, and this is not
a copy of the baseline's work: verified by file inventory, `develop-eyeseetea` carries **none**
of them, so PR #323 is a reference for the upgrade mechanics only, never for preservation.

## What Changes

- Merge `origin/upstream/3.4.1` into `develop-widp` in a single step (upstream release branch,
  **not** a merge of `develop-eyeseetea`), on branch `feature/upgrade_widp_to_3_4_1`.
- Bump the fork identity: version `3.3.1-widp-fork-1` → `3.4.1-widp-fork-1`, vCode 151 → 156.
- Bump the SDK fork: `1.13.1-eyeseetea-fork-3` → `1.14.1-eyeseetea-fork-1`.
- Resolve 27 predicted conflicts, 6 of them modify/delete, preserving all five customizations.
- **BREAKING (internal wiring, not behavior)**: the Notifications customization is re-wired
  from the deleted `MainView`/`MainPresenter`/`App.java`/`Sync*WorkerModule` onto the new
  `MainViewModel` and `App.kt`. External behavior is unchanged.
- Re-anchor the Change Server URL customization on `App.kt` and the surviving `UserComponent`.
- Adapt the 2FA customization to the SDK fork 1.14.1 API (`blockingLogIn` 4-argument signature,
  new exhaustive `D2ErrorCode` branches) **keeping WIDP's real 2FA messages** — the baseline
  maps those codes to `defaultError()`, which would silently degrade WIDP and must not be copied.
- Cherry-pick the three Oslo patches the baseline has and WIDP lacks: ANDROAPP-7661 (granular
  sync image download race), ANDROAPP-7666 (completed-event dialog always shown), and the
  empty-list-on-return-from-TEI regression fix.
- Update `customization-files.md`, the validation checklist, and add
  `upgrade-3.4.1-notes.md`.

Out of scope: restoring the broken `url-data-element` rendering (lost in the upstream Compose
migration before this upgrade). Its data plumbing is preserved; the rendering gap is unchanged
by this change and is tracked separately.

## Capabilities

### New Capabilities

None. This change moves the implementation baseline; it introduces no new WIDP behavior.

### Modified Capabilities

None. All five active capabilities — `change-server-url`, `image-upload-no-resize`,
`notifications`, `two-factor-auth`, `url-data-element` — keep their requirements **verbatim**.
The Notifications port to `MainViewModel` and the 2FA adaptation to the SDK fork 1.14.1 API are
implementation relocations behind unchanged requirements, so per the project rule they belong in
`design.md`, not in a spec delta. `.openspec.yaml` therefore sets `skip_specs: true`; no
requirement is invented to satisfy validation.

If the port turns out to be impossible to do behavior-preserving (for example if `MainViewModel`
cannot express the pending-notification state), that becomes a spec delta and this proposal is
amended rather than the requirement being quietly dropped.

## Impact

**Upstream target version: 3.4.1** (`origin/upstream/3.4.1`), coming from 3.3.1.

Predicted conflict surface — 27 files from `git merge-tree develop-widp origin/upstream/3.4.1`:

| Area | Files | Where they live | Reference in PR #323 |
|---|---|---|---|
| Notifications wiring | 11 | shared code | none — baseline has no notifications |
| 2FA / login | 8 | shared code (`login/`, `commonskmm/`) | partial — compile-fix only |
| Change Server URL | 3 | shared code (`app/`, `commons/`) | none |
| URL data element | 2 | shared code (`form/`) | none |
| Build / identity | 4 | `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, `login/build.gradle.kts` | yes |
| Strings / test DB / docs | 5 | shared resources | partial |

**Nothing in `app/src/widp/**` conflicts.** The entire conflict surface is in shared Oslo code,
which is exactly the cost the placement hierarchy exists to reduce; the notifications port is an
opportunity to move some of it back into the flavor source set, evaluated per file in `design.md`.

Deleted upstream, still customized by WIDP (modify/delete conflicts):
`app/src/main/java/org/dhis2/App.java`, `usescases/main/MainPresenter.kt`,
`usescases/main/MainView.kt`, `data/service/SyncDataWorkerModule.kt`,
`data/service/SyncInitWorkerModule.kt`, `data/service/SyncMetadataWorkerModule.kt`.

Dependencies: EyeSeeTea SDK fork `1.14.1-eyeseetea-fork-1` (must carry the 2FA
`LoginPayload.twoFactorCode` patch and the added `D2ErrorCode` values, per the
`two-factor-auth` spec). AGP/Gradle alignment per `eyeseetea-docs/SDK_Setup.md`.

Validation: DHIS2 2.41 and 2.43, `dev.eyeseetea`, and a `preprod-indiv` connection to confirm
2FA still works end-to-end.
