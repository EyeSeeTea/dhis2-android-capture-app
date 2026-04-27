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

(none — PR 01 ships no Kotlin/Java in `app/src/unicefTjkElmis/java/`. Kotlin/Java sources arrive with the first functional customization.)

### 1.2 Flavor resources and branding

- `app/src/unicefTjkElmis/AndroidManifest.xml` — declares `android:networkSecurityConfig="@xml/network_security_config"` for the cleartext exception
- `app/src/unicefTjkElmis/res/values/strings.xml` — `app_name = "UNICEF TJK eLMIS"`, placeholder `logo_text` and `logo_number`
- `app/src/unicefTjkElmis/res/xml/network_security_config.xml` — cleartext exception scoped to `172.16.0.99`

**Launcher icons (deferred):** `app/src/unicefTjkElmis/res/mipmap-*/`, `app/src/unicefTjkElmisDebug/res/mipmap-*/`, `app/src/unicefTjkElmisRelease/res/mipmap-*/`, and `ic_launcher-web.png` are intentionally **not created** in PR 01. UNICEF generic logo assets are not yet available, and the final UNICEF / MoH TJK / combined logo decision is still pending. PR 01 inherits the Oslo default launcher icons from `app/src/main/res/mipmap-*/`. A dedicated branding PR replaces them once the logo decision lands.

### 1.3 Build wiring

- `app/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { ... } }` block declaring `applicationId = "org.unicef.tjk.elmis"`, `dimension = "default"`, `versionCode = libs.versions.vCode.get().toInt()`, `versionName = libs.versions.vName.get()`
- `login/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { buildConfigField("String", "LOGIN_TEST", "\"test\"") } }` block

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
