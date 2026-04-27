## 1. Gradle declarations

- [x] 1.1 In `app/build.gradle.kts`, append a `create("unicefTjkElmis") { ... }` block inside `productFlavors {}` after the existing `eyeseetea` block, with `applicationId = "org.unicef.tjk.elmis"`, `dimension = "default"`, `versionCode = libs.versions.vCode.get().toInt()`, `versionName = libs.versions.vName.get()`.
- [x] 1.1.1 In `gradle/libs.versions.toml`, change `vName` from `"3.3.1-eyeseetea-fork-1"` to `"3.3.1-unicefTjkElmis-fork-1"` (branch-wide rename; this branch only distributes the unicefTjkElmis flavor — see proposal/design Decision 8).
- [x] 1.2 In `login/build.gradle.kts`, append a `create("unicefTjkElmis") { ... }` block inside `productFlavors {}` after the existing `eyeseetea` block, with `buildConfigField("String", "LOGIN_TEST", "\"test\"")`.
- [x] 1.3 Run `./gradlew tasks --all | grep -i unicefTjkElmis` and confirm `assembleUnicefTjkElmisDebug`, `assembleUnicefTjkElmisRelease`, and `testUnicefTjkElmisDebugUnitTest` are listed.
- [ ] 1.4 Commit with message scope `feat(unicefTjkElmis):` describing the build-wiring slice.

## 2. Flavor source set, identity strings, and launcher icons

- [x] 2.1 Create directory `app/src/unicefTjkElmis/res/values/`.
- [x] 2.2 Create `app/src/unicefTjkElmis/res/values/strings.xml` declaring `<string name="app_name">UNICEF TJK eLMIS</string>`, `<string name="logo_text">UNICEF TJK eLMIS</string>`, and `<string name="logo_number"></string>`.
- [x] 2.2.1 For every locale where `app/src/main/res/values-<locale>/strings.xml` declares any of `app_name`, `logo_text`, `logo_number` (ar, ckb, cs, es, es-rES, fr, id, km, lo, nb, nl, pt, ru, sv, uk, uz, uz-rUZ, vi, zh, zh-rCN), create `app/src/unicefTjkElmis/res/values-<locale>/strings.xml` with the same three keys and identical UNICEF values. Mirror the same set of files under `app/src/unicefTjkElmisDebug/res/values-<locale>/strings.xml`.
- [x] 2.3 Create `app/src/unicefTjkElmis/res/values/ic_launcher_background.xml` declaring `<color name="ic_launcher_background">#FFFFFF</color>`.
- [x] 2.4 Place the generated UNICEF launcher icon set under `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/` (`ic_launcher.webp` 48-192 px, `ic_launcher_round.webp` 48-192 px, `ic_launcher_foreground.webp` 108-432 px).
- [x] 2.5 Place the adaptive icon wrappers `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`, both referencing `@color/ic_launcher_background` and `@mipmap/ic_launcher_foreground`.
- [x] 2.6 Place the 512×512 Play Store icon at `app/src/unicefTjkElmis/ic_launcher-playstore.png`.
- [x] 2.7 Place the debug-variant icon set under `app/src/unicefTjkElmisDebug/res/mipmap-*/`, `ic_launcher_background.xml` (`#FFFFFF`, identical to the release flavor), `ic_launcher-playstore.png` at the source set root, and `app/src/unicefTjkElmisDebug/res/values/strings.xml` declaring the same three identity keys (required to override `app/src/debug/res/values/strings.xml`'s `app_name="Dhis2 Dev"`; Android applies build-type overrides AFTER flavor overrides).
- [x] 2.8 Run `./gradlew assembleUnicefTjkElmisDebug` and confirm the build succeeds.
- [x] 2.9 Copy `app/src/dhis2/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` to `app/src/unicefTjkElmis/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` verbatim — required by the Oslo Dagger graph for KSP annotation processing to succeed.
- [ ] 2.10 Commit with scope `feat(unicefTjkElmis):` describing the flavor source set / identity strings / launcher icons / Dagger boilerplate slice.

## 4. Verification

- [x] 4.1 Run `./gradlew assembleUnicefTjkElmisDebug` from the repository root — it must succeed.
- [x] 4.2 Run `./gradlew ktlintCheck` — `ktlintUnicefTjkElmisSourceSetCheck` succeeds. The aggregate `ktlintCheck` task fails on `ktlintEyeseeteaSourceSetCheck` due to pre-existing style violations in `app/src/eyeseetea/java/.../GranularSyncModule.kt` (baseline state, not introduced by this change).
- [x] 4.3 Run `./gradlew testUnicefTjkElmisDebugUnitTest` — succeeds (848 tests passed, 0 failed).
- [ ] 4.4 (Operator, requires VPN) Install the produced APK on a device or emulator. Open the launcher and confirm the entry shows `UNICEF TJK eLMIS` with the UNICEF launcher icon.
- [ ] 4.5 (Operator, requires VPN) Launch the app, enter `http://172.16.0.99:18081` plus admin credentials, and confirm login succeeds and the home screen renders.
- [ ] 4.6 (Operator, requires VPN) Inspect logcat during step 4.5 and confirm no `CLEARTEXT communication ... not permitted` error appears for `172.16.0.99`.

## 5. Inventory and PR

- [x] 5.1 Confirm `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md` §1 lists every file added by tasks 2 and 3 (already prepared in the doc-scaffolding commit; verify nothing drifted).
- [x] 5.2 Confirm `eyeseetea-docs/upgrade/unicefTjkElmis/upgrade-validation-checklist.md` §1 ("Flavor scaffold — first install and login") still describes the flow exercised in tasks 4.4-4.6.
- [ ] 5.3 Push the working branch `feat/new_unicefTjkElmis_flavor` and open a PR targeting `develop-unicef-tjk-elmis`. The PR body MUST link to `openspec/changes/create-unicef-tjk-elmis-flavor/proposal.md` and list the manual verification evidence.
