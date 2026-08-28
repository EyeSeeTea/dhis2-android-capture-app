# OCA customization files vs develop-eyeseetea

Technical inventory of the OCA customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `oca`
- Flavor: `oca`
- Base branch: `develop-eyeseetea`
- Base commit: `f87bec8c3`
- Generated on: `2026-08-28`
- Working tree status: `clean`

## Scope

This inventory is based on:
- direct flavor files under `app/src/oca/` and `app/src/ocaDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

This file is not a full raw diff dump. Its goal is to answer:
- which confirmed functional customizations exist for OCA
- where they are implemented
- what their current technical status is

## Validated customization count

**0 confirmed functional customizations.** The `oca` flavor exists (product flavor, `applicationId`, branding, and the boilerplate DI/extension-point files every flavor must carry), but nothing in it diverges functionally from `develop-eyeseetea` yet — see §1 below for the full flavor surface and why each file is boilerplate, not a customization.

## 1. Direct OCA flavor surface

### 1.1 OCA flavor code

All three files were created together in commit `89a61f78b "[EyeSeeTea] Add OCA flavor"`. None carries OCA-specific business logic — verified byte-for-byte against other flavors:

- `app/src/oca/java/org/dhis2/di/PostMetadataSyncModule.kt` — identical to `eyeseetea`'s (empty `module { }`). Required boilerplate: every flavor source set must carry this file so `KoinInitialization` can register the `PostMetadataSyncAction` extension point unconditionally (see `eyeseetea-docs/customization-techniques.md` — T2). OCA registers no actions.
- `app/src/oca/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — identical to `dhis2`'s (byte-for-byte, save for the license header). Required Dagger DI boilerplate every flavor must provide.
- `app/src/oca/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt` — was copied from `dhis2` (no-Play-Services pattern: `download()`/`DownloadMethod.File`) at flavor creation, but OCA is published to Google Play. Corrected in commit `57234a04a "fix: use Play Store update flow for oca flavor"` to match `dhis2PlayServices`'s pattern (`getUrl()`/`DownloadMethod.Url`) — this is baseline distribution-driven behavior, not an OCA customization. See `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` §1.1 for the criterion.

### 1.2 OCA flavor resources and branding

- `app/src/oca/res/values*/strings.xml` — `app_name` and localized strings (10 locale variants)
- `app/src/ocaDebug/res/values*/strings.xml` — debug build-type string overrides (9 locale variants)

## 2. Shared-code customization implementation points

None yet. Add a `### 2.N [Customization title]` section here, with a matching `openspec/specs/<capability>/spec.md`, when a real OCA business customization is confirmed.

## 3. Shared drift still differing

Empty — no unclassified diffs against `develop-eyeseetea` as of this writing.

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
