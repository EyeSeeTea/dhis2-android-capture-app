---
name: android-app-modernization
description: >
  Skill for modernizing this Android project's build toolchain and dependencies:
  Gradle wrapper, Android Gradle Plugin, Kotlin/KSP, compileSdk/targetSdk,
  Java toolchain, version catalog hygiene, outdated or vulnerable dependencies,
  and deprecation cleanup (kapt to KSP, deprecated Gradle APIs).
  Trigger on: Gradle upgrade, AGP upgrade, Kotlin version bump, SDK level bump,
  dependency update, Dependabot alert, kapt migration, or build modernization requests.
---

# Android App Modernization

## Overview

Use this skill to modernize the build toolchain and dependencies of this repo
(DHIS2 Android Capture App, EyeSeeTea fork) with tight verification after each
meaningful change. Prefer small upgrade slices, never batch unrelated upgrades,
and keep the app buildable between slices.

For detailed per-slice command/checklist guidance, read
[references/modernization-checklist.md](references/modernization-checklist.md) before editing.

## Fork Warning — Read First

This repo is a **fork of dhis2/dhis2-android-capture-app** with client branches
(`develop-widp`, `develop-psi`, `develop-sports`, ...) layered on top of
`develop-eyeseetea`. Every line changed in shared build files
(`gradle/libs.versions.toml`, root/module `build.gradle.kts`, `settings.gradle.kts`,
`gradle.properties`, CI workflows) is a future merge conflict with upstream Oslo.

Rules:

1. **Prefer waiting for upstream**: if Oslo's `develop` already has the upgrade, take it via the normal upgrade flow instead of redoing it locally.
2. **Land modernization in `develop-eyeseetea`**, never in a single client fork, so all forks inherit it on their next upgrade.
3. **Keep diffs minimal**: change only the version values needed; do not reformat or reorganize upstream build files.
4. **Comment surviving deviations** in shared code with `// EyeSeeTea customization - [title]` so merge conflict classification stays easy.
5. If an upgrade forces large build-file restructuring, stop and confirm with the user before proceeding.

## Workflow

1. **Inspect first**: `gradle/libs.versions.toml` (single source of truth for versions), `gradle/wrapper/gradle-wrapper.properties`, root `build.gradle.kts`, `settings.gradle.kts` (composite build with local SDK), module build files, `.github/workflows/*` (Java version, Gradle commands, flavor used in CI).
2. **Establish a baseline** before changing anything:
   ```bash
   ./gradlew :app:assembleDhis2Debug
   ./gradlew :app:testDhis2DebugUnitTest
   ./gradlew ktlintCheck
   ```
   If the baseline is broken, fix or document the blocker first; later failures must be attributable to your change.
3. **Apply one slice at a time** (see slices below). After each slice run the narrowest relevant verification, then move on only when green.
4. **When a check fails**, fix the root cause before the next slice; never stack a second upgrade on top of a red build.
5. **Finish with a full verification**: assemble all flavors, full unit tests, ktlint (see Verification Discipline).

## Upgrade Slices

Ordered by typical dependency: Gradle wrapper → AGP → Kotlin/KSP → SDK levels → dependencies.

### Gradle Wrapper

Check the AGP↔Gradle compatibility matrix first
(https://developer.android.com/build/releases/gradle-plugin#updating-gradle).
Upgrade with the wrapper task, never by editing the properties file by hand:

```bash
./gradlew wrapper --gradle-version <X.Y> && ./gradlew wrapper
```

(run twice so wrapper scripts and checksums regenerate). Then run a build to
surface deprecated Gradle API warnings from plugins.

### AGP and Kotlin — one at a time, never together

- AGP version lives in the catalog as `gradle` (`libs.gradlePlugin`, applied via `buildscript` classpath). Upgrade it alone, verify, commit.
- Kotlin version is `kotlin` in the catalog. **KSP must move with Kotlin**: the `ksp` catalog version is `<kotlinVersion>-<kspVersion>` and its Kotlin prefix must match the `kotlin` entry. The Compose compiler plugin (`kotlin-compose-compiler`) shares `version.ref = "kotlin"` and follows automatically.
- Read the AGP release notes and Kotlin compatibility notes for each crossed major/minor before editing.

### Version Catalog Hygiene

All versions belong in `gradle/libs.versions.toml` `[versions]` / `[libraries]` /
`[plugins]` / `[bundles]`. No hardcoded version strings in module `build.gradle.kts`
files; hunt them down and move them into the catalog when you touch a module
(Boy Scout Rule — only modules you are already modifying).

### compileSdk / targetSdk

Both come from the catalog `sdk` key (single value). Bump **one API level at a
time** and review the official behavior-changes page for that level — both
"all apps" and "apps targeting N" sections. Pay attention to permissions,
broadcast/PendingIntent rules, foreground services, and storage. `minSdk` (21)
is a product decision — never raise it as part of modernization.

### Java Toolchain

Java 17 is set via `compileOptions` + `jvmTarget` per module and `java-version`
in CI workflows. If raising it: update all three places consistently, check AGP
minimum-JDK requirements, and verify `desugar_jdk_libs` is still needed/compatible.

### Dependency Updates

No versions plugin is configured — check outdated deps manually per group
(see checklist). Strategy: patch/minor bumps grouped per ecosystem (AndroidX,
Compose, test libs, ...), majors individually with changelog review. Never bump
`dhis2sdk`, `designSystem`, `ruleEngine`, or `expressionParser` as routine
modernization — these are coordinated upgrades with their own process.

### Vulnerability Remediation

Triage GitHub Dependabot alerts (`gh api repos/{owner}/{repo}/dependabot/alerts`).
Direct deps: upgrade. Transitive: prefer upgrading the parent; otherwise pin via
`resolutionStrategy` in root `build.gradle.kts` with an explanatory comment.

### Deprecation Cleanup

- **kapt → KSP**: `app`, `commons`, and `dhis_android_analytics` still use kapt (Dagger). Migrate per module only when the processor supports KSP; verify generated Dagger components still compile.
- Fix deprecated Gradle/AGP API warnings surfaced by `./gradlew --warning-mode all`.

## Verification Discipline

After **each** slice:

```bash
./gradlew :app:assembleDhis2Debug
./gradlew :app:testDhis2DebugUnitTest   # or ./gradlew test for toolchain-wide changes
./gradlew ktlintCheck
```

Before finishing, run a final full pass:

```bash
./gradlew :app:assembleDhis2Debug :app:assembleWidpDebug :app:assemblePsiDebug :app:assembleSportsDebug
./gradlew test
./gradlew ktlintCheck
```

Flavor builds matter here: flavor source sets (`app/src/<flavor>/`) can break
independently of the default `dhis2` flavor. Treat new warnings about deprecated
APIs as items to record, not to silently ignore.

## Current Information

Compatibility matrices (AGP↔Gradle, Kotlin↔KSP↔Compose compiler, AGP↔JDK) and
"latest" versions change over time — check official Android/Kotlin docs or Maven
metadata during the task. Do not rely on version numbers baked into this skill.
