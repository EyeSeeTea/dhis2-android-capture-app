## Why

The UNICEF Tajikistan eLMIS programme needs a dedicated Android client that connects to the UNICEF DHIS2 server at `http://172.16.0.99:18081` (HTTP plain, VPN-only). The fork branch `develop-unicef-tjk-elmis` currently has no flavor surface — it is byte-identical to `develop-eyeseetea` HEAD `8a4866305`. Without a buildable flavor, no functional customization (FRQ037, photo proof, upload policy, roles matrix, branding) has anywhere to land.

This change establishes the deployment target so the fork can produce an installable, launchable APK that connects to the UNICEF server. Subsequent change proposals add functional behavior on top of this scaffold.

## What Changes

- Declare new product flavor `unicefTjkElmis` in `app/build.gradle.kts` with `applicationId = "org.unicef.tjk.elmis"`, `dimension = "default"`, `versionCode = libs.versions.vCode.get().toInt()`, `versionName = libs.versions.vName.get()`. On this branch (`develop-unicef-tjk-elmis`) the catalog `vName` is set to `3.3.1-unicefTjkElmis-fork-1` because the branch only distributes the `unicefTjkElmis` flavor — see design.md Decision 8.
- Declare matching `unicefTjkElmis` flavor in `login/build.gradle.kts` with the standard `LOGIN_TEST` `buildConfigField` (the login module breaks the build if any flavor is missing here).
- Create flavor source set `app/src/unicefTjkElmis/` carrying:
  - `res/values/strings.xml` declaring `app_name = "UNICEF TJK eLMIS"` and placeholder `logo_text` / `logo_number`.
  - Launcher icon set under `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/` (`ic_launcher.webp`, `ic_launcher_round.webp`, `ic_launcher_foreground.webp`), the adaptive icon wrappers under `res/mipmap-anydpi-v26/`, the `ic_launcher_background` color resource under `res/values/`, and the 512×512 Play Store icon at the source set root, all built from the UNICEF logo on a white canvas. The icons are placeholder-stable (UNICEF brand, no MoH TJK content) — a future branding change replaces them once the final logo source decision (UNICEF / MoH TJK / combined) is made.
- Create build-type-scoped flavor source set `app/src/unicefTjkElmisDebug/` with the same launcher icon set as the release variant. Debug and release installs share the visual identity on the launcher; they remain installable side-by-side via the `applicationIdSuffix=".debug"` declared in the debug build type, which gives them distinct application IDs (`org.unicef.tjk.elmis.debug` vs `org.unicef.tjk.elmis`) without requiring a different icon.
- One Kotlin source file under `app/src/unicefTjkElmis/java/`: `org/dhis2/utils/granularsync/GranularSyncModule.kt`. This is **not** a functional customization — it is build-wiring boilerplate that the Oslo Dagger graph requires every flavor to provide; failing to ship it makes KSP/Dagger annotation processing fail at `assemble` time. The contents are copied verbatim from the `dhis2` flavor's version (upstream shape) because no UNICEF-specific behavior changes are warranted at this stage.
- No modification to `app/src/main/`, no modification to other flavors. HTTP access to the UNICEF server is enabled via the global `android:usesCleartextTraffic="true"` already declared in `app/src/main/AndroidManifest.xml`; this change does not introduce a flavor-scoped network security config. Tightening the global cleartext stance is a multi-flavor refactor tracked separately and out of scope here.

## Capabilities

### New Capabilities

- `unicef-tjk-elmis-flavor`: the buildable deployment target for the UNICEF TJK eLMIS programme — flavor identity (name, applicationId, launcher name), build wiring across the Gradle modules that ship per-flavor code, and the network configuration required to reach the UNICEF DHIS2 server over plain HTTP through the VPN.

### Modified Capabilities

(none — `openspec/specs/` is empty in the current baseline; this is the first capability in the tree.)

## Impact

**Files added (flavor surface):**
- `app/src/unicefTjkElmis/res/values/strings.xml` (default-locale identity strings)
- `app/src/unicefTjkElmis/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` (per-locale override of the three identity keys)
- `app/src/unicefTjkElmis/res/values/ic_launcher_background.xml`
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp`
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.webp`
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.webp`
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/unicefTjkElmis/ic_launcher-playstore.png`
- `app/src/unicefTjkElmisDebug/res/values/strings.xml` (debug-build-type identity strings; required to override `app/src/debug/res/values/strings.xml`'s `app_name="Dhis2 Dev"`)
- `app/src/unicefTjkElmisDebug/res/values/ic_launcher_background.xml`
- `app/src/unicefTjkElmisDebug/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp`
- `app/src/unicefTjkElmisDebug/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.webp`
- `app/src/unicefTjkElmisDebug/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.webp`
- `app/src/unicefTjkElmisDebug/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` (debug-variant per-locale override of the three identity keys, same values as the release flavor)
- `app/src/unicefTjkElmisDebug/ic_launcher-playstore.png`
- `app/src/unicefTjkElmis/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` (Dagger module boilerplate required by every flavor; copied from the `dhis2` flavor version)

**Files modified (build wiring):**
- `app/build.gradle.kts` — append a `create("unicefTjkElmis") { ... }` block inside `productFlavors {}` after the existing `eyeseetea` block. Both `versionCode` and `versionName` come from the version catalog (`libs.versions.vCode` and `libs.versions.vName`).
- `login/build.gradle.kts` — append a `create("unicefTjkElmis") { ... }` block inside `productFlavors {}`, after the existing `eyeseetea` block.
- `gradle/libs.versions.toml` — change `vName` from `"3.3.1-eyeseetea-fork-1"` to `"3.3.1-unicefTjkElmis-fork-1"` (branch-wide rename; see design.md Decision 8).

These two `build.gradle.kts` edits are the only contact with shared build files in this change, and they are unavoidable: Gradle product flavors must be declared in each module that ships per-flavor code. Per the placement hierarchy in `openspec/config.yaml`, no higher tier is feasible — flavor declarations cannot live inside `app/src/unicefTjkElmis/` because the source set is keyed by the declaration. Both edits append a new block at the end of an existing list (no inline modification of upstream lines), keeping conflict surface minimal.

**Files NOT modified:**
- `app/src/main/AndroidManifest.xml` — the global `android:usesCleartextTraffic="true"` already permits HTTP for every flavor in the repo, including `unicefTjkElmis`; no flavor manifest is created in this change.
- Other flavors' source sets — no cross-flavor leakage.
- `settings.gradle.kts` — no SDK fork is introduced in this change.

**Build verification:**
- `./gradlew assembleUnicefTjkElmisDebug` must succeed.
- `./gradlew ktlintCheck` must succeed.
- `./gradlew testUnicefTjkElmisDebugUnitTest` must succeed (no flavor-specific tests added; this proves the new flavor does not break the existing test surface).

**Manual verification (operator, with VPN):**
- Install APK, launch — confirm `UNICEF TJK eLMIS` launcher name and icon.
- Enter `http://172.16.0.99:18081` + admin credentials — login completes and the home screen renders (HTTP works because of the global `usesCleartextTraffic="true"` already in the baseline manifest).

**Dependencies introduced:** none. No new Gradle dependencies, no new SDK fork, no new permissions in the manifest beyond what `app/src/main/AndroidManifest.xml` already declares.
