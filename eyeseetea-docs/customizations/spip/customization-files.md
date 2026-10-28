# spIP customization files vs develop-eyeseetea

Technical inventory of the spIP customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `spip`
- Flavor: `spIP`
- Base branch: `develop-eyeseetea`
- Base commit: `d87193d003` (merge-base with `develop-eyeseetea`, upstream 3.3.1; spIP is currently `3.3.1-spIP-fork-3`)
- Generated on: `2026-10-28`
- Working tree status: `clean`

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch
- this file documents the spIP-specific implementation points

## Scope

This inventory is incremental. Only customizations created after the onboarding of `eyeseetea-docs/` are documented here, one at a time, as they are built. Pre-existing spIP customizations are **not** inventoried yet, see the pending item below.

## Pending: document pre-existing customizations

TODO (do it during the next upgrade of spIP, no date set): spIP existed before `eyeseetea-docs/` was introduced and its older customizations are undocumented here (for example Send Welcome SMS, image sync, file download limit).

When the upgrade starts, follow Phase 3 and Phase 4 of `eyeseetea-docs/onboarding-fork-guide.md`:
- list the surface with `git diff develop-eyeseetea..develop-spip --stat` (on 2026-10-28: about 105 files under `app/src/spIP/` and about 526 outside it; spIP is on 3.3.1 while `develop-eyeseetea` is on 3.4.2)
- confirm the real business customizations with the developer who owns them, do not infer functional titles from the diff
- write one `openspec/specs/<capability>/spec.md` per active customization and add the `// EyeSeeTea customization - [Title]` comments
- fill section 2 and the validation checklist with the confirmed customizations
- only then run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client spip`; it is expected to fail until the inventory is complete, so it is used only during the upgrade

## 1. Direct spIP flavor surface

### 1.1 Flavor code

- `app/src/spIP/`

### 1.2 Flavor resources and branding

- `app/src/spIP/res/`
- `app/src/spIPDebug/`

## 2. Shared-code customization implementation points

No customization documented yet. Entries are added here as they are built, using the exact `# Title` of the matching OpenSpec spec.

## 3. Shared drift still differing

Pending, see "Pending: document pre-existing customizations". Nothing has been classified yet, so every difference against `develop-eyeseetea` other than the customizations in section 2 is unclassified drift.

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
