# Android Modernization Checklist

Use this reference as a practical checklist while modernizing this repository.
Version numbers and compatibility rules are time-sensitive; query the official
Android/Kotlin documentation and Maven metadata during the task, not this file.

## Discovery

Inspect before editing:

- `gradle/libs.versions.toml` — single source of truth. Key entries:
  - `gradle` → AGP version (yes, the key is named `gradle`)
  - `kotlin` → Kotlin + Compose compiler plugin (`kotlin-compose-compiler` uses `version.ref = "kotlin"`)
  - `ksp` → format `<kotlinVersion>-<kspRelease>`; Kotlin prefix must equal the `kotlin` entry
  - `sdk` → compileSdk and targetSdk (single value), `minSdk` → 21 (do not touch)
  - `vName` / `vCode` → app version (fork-specific, e.g. `3.3.1-sports-fork-1`); never change during modernization
  - `dhis2sdk` → EyeSeeTea SDK fork via JitPack; coordinated upgrade only, see `EyeSeeTea.md`
- `gradle/wrapper/gradle-wrapper.properties` — Gradle distribution.
- Root `build.gradle.kts` — buildscript classpath (AGP, Kotlin plugin via catalog), ktlint config (ktlint engine version is pinned inline: `version.set("...")`), a `resolutionStrategy` that pins jacoco, sonarqube, cyclonedx.
- `settings.gradle.kts` — composite build: includes the local `dhis2-android-sdk` when `dhis2.useLocalSdk=true` in `local.properties`/`gradle.properties`. Modernization must keep both modes (JitPack and local SDK) working. The local SDK checkout has its own build files — do not modernize those from this repo.
- Module build files: `app/`, `commons/`, `form/`, `tracker/`, `aggregates`, `login/`, `sync/`, `dhis_android_analytics/`, `dhis2_android_maps/`, `ui-components/`, `compose-table/`, `commonskmm/`, `stock-usecase/`, `dhis2-mobile-program-rules/`.
- `.github/workflows/` — `eyeseetea-main.yml` (Java 17 setup, runs `:app:testEyeseeteaDebugUnitTest`), `continuous-delivery.yml`, release workflows. Any Java/Gradle change must be mirrored here.
- `gradle.properties` — JVM args, AndroidX flags.

Record baseline status of:

```bash
./gradlew :app:assembleDhis2Debug
./gradlew :app:testDhis2DebugUnitTest
./gradlew ktlintCheck
```

If the baseline is red, fix or document before attributing failures to your changes.

## Gradle Wrapper

1. Read the AGP↔Gradle compatibility matrix:
   https://developer.android.com/build/releases/gradle-plugin#updating-gradle
   Pick the newest Gradle supported by the **current** AGP (or the AGP you are
   about to move to — wrapper first, then AGP).
2. Upgrade via the wrapper task (never hand-edit `gradle-wrapper.properties`):

   ```bash
   ./gradlew wrapper --gradle-version <X.Y>
   ./gradlew wrapper   # second run regenerates scripts/jar with the new version
   ```

3. Surface plugin incompatibilities and deprecations early:

   ```bash
   ./gradlew :app:assembleDhis2Debug --warning-mode all
   ```

   Watch third-party plugins pinned in the catalog: ktlint (`org.jlleitschuh.gradle.ktlint`),
   sonarqube, sentry, cyclonedx — they often lag behind new Gradle majors.
4. Crossing a Gradle major: read the official Gradle upgrade guide for each
   crossed major; check configuration-cache and deprecated-API notes.
5. Verify build + tests + ktlint. Commit the wrapper bump alone.

## Android Gradle Plugin

1. Confirm the current Gradle wrapper satisfies the target AGP's minimum Gradle
   and minimum JDK.
2. Bump only the `gradle` key in `[versions]` of `gradle/libs.versions.toml`.
3. Read AGP release notes for behavior changes: default values flipped, removed
   DSL, new lint checks, manifest merger changes, resource shrinker changes.
4. Build with `--warning-mode all`; fix newly deprecated DSL usage in module
   build files you touch.
5. Verify, commit separately from Kotlin changes. **Never bump AGP and Kotlin in
   the same commit** — when something breaks you must know which one did it.

## Kotlin + KSP (+ Compose compiler)

These move together, but separately from AGP:

1. Check Kotlin↔KSP compatibility: KSP releases are versioned
   `<kotlin>-<ksp>` (e.g. `2.2.21-x.y.z`). Find the KSP release whose prefix
   matches the target Kotlin at https://github.com/google/ksp/releases.
2. In the catalog, update `kotlin` and `ksp` in the same change. The Compose
   compiler plugin (`org.jetbrains.kotlin.plugin.compose`) tracks `kotlin`
   automatically via `version.ref`.
3. Check `composePluginVersion` (JetBrains Compose Multiplatform plugin, used by
   `commonskmm`) for compatibility with the new Kotlin — it has its own matrix.
4. Watch the pinned compatibility hostages noted in the catalog comments, e.g.
   `kotlinxDatetime = "0.7.1-0.6.x-compat"` must stay compatible with the rule
   engine — do not bump it casually.
5. New Kotlin minors can introduce warnings-as-errors in some modules and
   stricter inference; fix code, do not suppress globally.
6. Verify full unit tests (`./gradlew test`), not just `app` — Kotlin changes
   affect every module.

## Version Catalog Hygiene

- Every dependency and plugin version belongs in `gradle/libs.versions.toml`.
- Find offenders in modules you are already touching:

  ```bash
  grep -rnE '"[a-zA-Z0-9.-]+:[a-zA-Z0-9.-]+:[0-9]' --include=build.gradle.kts . | grep -v dhis2-android-sdk
  ```

- Move hardcoded coordinates into `[libraries]` with a `version.ref`, reuse
  existing `[bundles]` where one fits.
- Known inline pins to be aware of (leave unless the slice requires them):
  ktlint engine version in root `build.gradle.kts` `ktlint { version.set(...) }`,
  the jacoco `eachDependency` pin in root `resolutionStrategy`.

## compileSdk / targetSdk

1. Both read the single `sdk` key in the catalog (`compileSdk = targetSdk`).
2. Bump **one API level at a time**. For each level, review:
   - Behavior changes (all apps): https://developer.android.com/about/versions/<N>/behavior-changes-all
   - Behavior changes (targeting N): https://developer.android.com/about/versions/<N>/behavior-changes-<N>
3. High-risk areas for this app: foreground services + WorkManager sync,
   notification permissions/channels, exact alarms, storage/scoped-storage
   (file/image capture), `PendingIntent` mutability, broadcast receiver export
   flags, SQLCipher/native libs page alignment (16 KB pages on newer levels).
4. compileSdk can lead targetSdk: bumping compileSdk alone (to use new APIs /
   newer AndroidX) is lower risk than bumping targetSdk (runtime behavior
   changes on devices).
5. `minSdk = 21` is a product decision — out of scope for modernization.
6. After the bump, build all flavors and run unit tests; flag any new lint
   `NewApi`/`UnusedAttribute` findings.

## Java Toolchain

Currently Java 17 (`JavaVersion.VERSION_17` + `JvmTarget.JVM_17` per module,
`java-version: '17'` in workflows, documented in CLAUDE.md).

If raising (only when AGP/Kotlin require or the user asks):

1. Check AGP's required/recommended JDK for the AGP in use.
2. Update consistently: every module's `compileOptions` + `jvmTarget`, all
   `.github/workflows/*` `java-version`, and CLAUDE.md / EyeSeeTea.md docs.
3. Re-check `desugar_jdk_libs` (catalog key `desugar_jdk_libs`) — still needed
   for minSdk 21, verify version compatibility with the new toolchain.
4. Full build of all flavors + tests.

## Dependency Updates

No `com.github.ben-manes.versions` plugin is configured. Options:

- Manual: check Maven Central / Google Maven metadata for the catalog entries
  in the group being updated.
- Or ask the user before adding the versions plugin (it touches the shared root
  build file — fork-conflict surface).

Strategy:

1. Group patch/minor bumps by ecosystem and verify per group:
   - AndroidX core (appcompat, corektx, lifecycle, work, fragmentktx, ...)
   - Compose (compose, material3, activityCompose, viewModelCompose, navigationCompose)
   - Test (junit, mockito, espresso, androidx_test*, truth, turbine)
   - Tooling plugins (ktlint, sonarqube, sentryPlugin, cyclonedx)
   - Runtime utilities (gson, okhttp, glide, timber, ...)
2. Major upgrades: one dependency at a time, read its changelog/migration guide,
   verify, commit individually.
3. **Do not touch as routine modernization** (coordinated upgrades with their
   own process): `dhis2sdk`, `designSystem` (DHIS2 mobile design system),
   `ruleEngine`, `expressionParser`, `kotlinxDatetime` (rule-engine-compat pin),
   and the legacy RxJava 2 stack (rxjava/rxandroid — frozen at 2.x by design).
4. Dagger (`dagger`) majors can change generated-code requirements — pair with
   the kapt/KSP state of each module.

## Vulnerable Dependencies

1. List Dependabot alerts:

   ```bash
   gh api repos/{owner}/{repo}/dependabot/alerts --jq '.[] | select(.state=="open") | {pkg: .dependency.package.name, severity: .security_advisory.severity, range: .security_vulnerability.vulnerable_version_range}'
   ```

2. Triage each alert:
   - Direct dependency, non-breaking fix → bump in the catalog.
   - Direct dependency, major required → individual upgrade with changelog review.
   - Transitive → prefer bumping the direct parent; otherwise pin in the root
     `build.gradle.kts` `resolutionStrategy` (the jacoco pin there is the
     existing pattern) with a comment naming the CVE and the parent:

     ```kotlin
     // EyeSeeTea customization - force <lib> >= X.Y for CVE-XXXX-NNNN (pulled by <parent>)
     force("group:artifact:X.Y.Z")
     ```

   - No compatible fix → document residual risk in the PR; do not force a
     breaking version blindly.
3. Confirm resolution: `./gradlew :app:dependencies --configuration dhis2DebugRuntimeClasspath | grep <artifact>`.

## Deprecation Cleanup

### kapt → KSP

Modules still on kapt: `app`, `commons`, `dhis_android_analytics` (the
`dhis2-android-sdk` checkout also uses kapt — out of scope). Processor is
Dagger 2.

1. Confirm the Dagger version in the catalog fully supports KSP for the
   features used (components, subcomponents, multibindings, `@AssistedInject`).
2. Migrate one module per slice: replace `kotlin("kapt")` with
   `alias(libs.plugins.ksp)` and `kapt(...)` deps with `ksp(...)`.
3. Remove `kapt {}` blocks; map needed processor args to `ksp { arg(...) }`.
4. Build the module and check `build/generated/ksp/` output; Dagger component
   resolution errors at compile time mean a processor-feature gap — revert the
   module and document.
5. Only remove the kapt plugin from a module when no `kapt(...)` dependency
   remains in it.

### Deprecated Gradle/AGP APIs

```bash
./gradlew :app:assembleDhis2Debug --warning-mode all 2>&1 | grep -i deprecat
```

Fix deprecations in this repo's build files; report (don't fix) deprecations
coming from third-party plugins, and consider plugin upgrades instead.

## Fork-Conflict Hygiene (every slice)

- Land on a branch off `develop-eyeseetea` (e.g. `feature-eyeseetea/modernize-gradle-8x`); client forks pick it up via their normal upgrade.
- Before editing shared build files, check whether upstream `develop` already
  contains the same upgrade: `git log upstream/develop --oneline -- gradle/libs.versions.toml` (fetch upstream first). Taking upstream's version of the change is always cheaper than a parallel local change.
- Keep diffs surgical: version values only, no reordering/reformatting of
  catalog entries or build files.
- Any non-version logic added to shared build files gets
  `// EyeSeeTea customization - [title]`.
- One slice = one commit (`build:` or `chore(deps):` conventional commit) so
  future merges can classify conflicts per-slice.

## Final Verification

```bash
./gradlew :app:assembleDhis2Debug :app:assembleWidpDebug :app:assemblePsiDebug :app:assembleSportsDebug
./gradlew test
./gradlew ktlintCheck
```

If the local SDK composite build is part of the developer workflow, also smoke
one build with `dhis2.useLocalSdk=true` if a local SDK checkout exists; otherwise
note it as unverified.

## Reporting

In the final response include:

- Slices completed, with old → new versions.
- Commands run per slice and pass/fail status.
- Deprecation warnings or Dependabot alerts left open, and why.
- Shared-build-file changes made (the fork-conflict surface) and the comments
  added to mark them.
- Anything intentionally not upgraded (SDK fork, design system, rule engine,
  RxJava 2, minSdk) so reviewers don't flag them as missed.
