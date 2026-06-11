# UNICEF TJK eLMIS — Customization Files

Technical inventory of the UNICEF TJK eLMIS fork. Lists where each customization is implemented, separating direct flavor surface from shared-code implementation points, and tracks technical status against `develop-eyeseetea`.

This file is **not** for: raw full diff dumps, temporary upgrade progress, stable merge rules, or functional intent / business justification.

## Mandatory header

- Client: `unicefTjkElmis`
- Flavor: `unicefTjkElmis`
- Base branch: `develop-eyeseetea`
- Base commit: `8a4866305`
- Generated on: `2026-04-27`
- Working tree status: `clean` (untracked `dhis2-android-sdk/` and `dhis2-rule-engine/` are local SDK fork checkouts no longer needed by `develop-eyeseetea` since 2FA was removed; they do not impact this inventory)

## Scope

This inventory is based on:
- direct flavor files under `app/src/unicefTjkElmis/`, `app/src/unicefTjkElmisDebug/`, `app/src/unicefTjkElmisRelease/`
- shared-code implementation points marked with `// EyeSeeTea customization` (none in PR 01)
- current diffs against `develop-eyeseetea` used only as supporting evidence

## 1. Direct unicefTjkElmis flavor surface

### 1.1 Flavor code

- `app/src/unicefTjkElmis/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — Dagger module required by the Oslo build's annotation processor; every flavor ships its own copy. This is build-wiring boilerplate, not a customization. Contents are copied verbatim from the `dhis2` flavor's version.

(no other Kotlin/Java sources — UNICEF-specific Kotlin/Java arrives with the first functional customization PR.)

### 1.2 Flavor resources and branding

- `app/src/unicefTjkElmis/res/values/strings.xml` — default-locale `app_name`, `logo_text`, `logo_number`
- `app/src/unicefTjkElmis/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` — same three keys per locale, overriding the translated brand strings declared by `app/src/main/res/values-<locale>/strings.xml`. The UNICEF brand does not translate, so every locale uses identical values.
- `app/src/unicefTjkElmis/res/values/ic_launcher_background.xml` — `ic_launcher_background = #FFFFFF`
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` — UNICEF logo on white, square (48-192 px)
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.webp` — same logo, masked round at runtime
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.webp` — adaptive icon foreground, transparent canvas with logo in central safe zone (108-432 px)
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher.xml` — adaptive icon wrapper (background `@color/ic_launcher_background`, foreground `@mipmap/ic_launcher_foreground`)
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher_round.xml` — same wrapper for the round variant
- `app/src/unicefTjkElmis/ic_launcher-playstore.png` — 512×512 Play Store icon
- `app/src/unicefTjkElmisDebug/res/values/strings.xml` — debug-build-type override of `app_name`, `logo_text`, `logo_number`. Required because `app/src/debug/res/values/strings.xml` declares `app_name="Dhis2 Dev"` and Android resource merging applies build-type overrides after flavor overrides; without this file, the launcher renders `Dhis2 Dev` instead of `UNICEF TJK eLMIS` on debug installs.
- `app/src/unicefTjkElmisDebug/res/values/ic_launcher_background.xml` — `ic_launcher_background = #FFFFFF` (identical to the release flavor; debug installs are differentiated by `applicationIdSuffix=".debug"`, not by icon)
- `app/src/unicefTjkElmisDebug/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` — debug-variant locale overrides for `app_name`, `logo_text`, `logo_number` (same content as the release flavor; explicit per-locale override so debug installs render the UNICEF brand consistently across system locales)
- `app/src/unicefTjkElmisDebug/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher{,_round,_foreground}.webp` — debug-variant icon set. Note: the `ic_launcher.webp` and `ic_launcher_round.webp` legacy files were generated with a warm-tinted pixel background that pre-dates the decision to make debug visually identical to release. On Android API 26+ the adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` referencing `@color/ic_launcher_background = #FFFFFF`) takes precedence and the launcher renders pure white; on pre-API 26 devices the legacy webp would render with a warm tint. Not regenerated because the target population is Android 8+ where the adaptive icon wins.
- `app/src/unicefTjkElmisDebug/ic_launcher-playstore.png` — debug variant of the Play Store icon

**Branding placeholder note:** the launcher icon set is a UNICEF-only placeholder generated from a single source PNG. The final UNICEF / MoH TJK / combined branding decision is tracked in a separate change proposal; when that decision lands, the assets here are replaced wholesale (no merge or migration needed because every file in this list is flavor-scoped).

### 1.3 Build wiring

- `app/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { ... } }` block declaring `applicationId = "org.unicef.tjk.elmis"`, `dimension = "default"`, `versionCode = libs.versions.vCode.get().toInt()`, `versionName = libs.versions.vName.get()`
- `login/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { buildConfigField("String", "LOGIN_TEST", "\"test\"") } }` block
- `gradle/libs.versions.toml` — `vName = "3.3.1-unicefTjkElmis-fork-1"` (branch-wide; this branch only distributes the unicefTjkElmis flavor)

## 2. Shared-code customization implementation points

(none in PR 01 — functional customizations land in subsequent PRs as external blockers are resolved.)

## 3. Shared drift still differing

(empty — UNICEF branch matches `develop-eyeseetea` baseline at HEAD `8a4866305` for all shared code.)

## 4. Feat commits

Tracks the commits that implement each customization, for cross-checking against §2 during automerge verification.

PR 01 (`feat/new_unicefTjkElmis_flavor`):
- (commits will be listed by SHA after merge)

## 5. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
- The inventory must be completed before any baseline merge. Per `eyeseetea-docs/upgrade/conflict-rules.md`, the §"Automerge verification" rule runs `git diff develop-eyeseetea -- <file>` for every file listed here, so an incomplete inventory invites silent automerge loss after upgrades.
