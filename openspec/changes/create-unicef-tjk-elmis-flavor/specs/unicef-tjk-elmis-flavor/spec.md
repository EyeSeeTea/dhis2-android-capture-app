# UNICEF TJK eLMIS Flavor

## Purpose

Defines the UNICEF Tajikistan eLMIS deployment target as a buildable Android product flavor of the DHIS2 Android Capture app. Establishes the flavor's identity (name, application ID, launcher name), the build wiring across the Gradle modules that ship per-flavor code, and the network configuration required to reach the UNICEF DHIS2 server over plain HTTP through the UNICEF VPN.

This capability is the entry point for every subsequent UNICEF TJK eLMIS customization: functional behavior, branding, and integrations all attach to the flavor declared and scaffolded here. The flavor MUST live entirely in flavor-scoped surfaces (build flavor blocks and `app/src/unicefTjkElmis/`) so that the deployment target adds zero conflict surface to shared code.

## ADDED Requirements

### Requirement: Flavor identity SHALL be unique and immutable

The fork SHALL define a product flavor named `unicefTjkElmis` with application ID `org.unicef.tjk.elmis`. The application ID, once published, MUST NOT change for the lifetime of the published app, because Android distribution channels identify the app by application ID and changing it forces a new listing with no upgrade path for installed users.

#### Scenario: Flavor variants are addressable by Gradle

- **WHEN** a developer runs `./gradlew tasks --all` against the fork repository
- **THEN** the task list includes `assembleUnicefTjkElmisDebug` and `assembleUnicefTjkElmisRelease`
- **AND** the task list includes `testUnicefTjkElmisDebugUnitTest`

#### Scenario: Application ID resolves to org.unicef.tjk.elmis

- **WHEN** a debug variant of the flavor is assembled
- **THEN** the resulting APK reports application ID `org.unicef.tjk.elmis.debug` (the Oslo-wide `.debug` suffix applied by the debug build type)
- **AND** the resulting release variant reports application ID `org.unicef.tjk.elmis`

### Requirement: Launcher name SHALL render as `UNICEF TJK eLMIS` in every supported locale

The flavor SHALL display `UNICEF TJK eLMIS` as the launcher entry name on the user's home screen, regardless of the device's system locale. The brand does not translate, so the same string MUST appear whether the device is set to English, Russian, Tajik, French, Spanish, Arabic, or any other locale supported by the upstream baseline.

For every locale where `app/src/main/res/values-<locale>/strings.xml` declares `app_name`, `logo_text`, or `logo_number` (the three identity keys), the flavor MUST provide a matching `app/src/unicefTjkElmis/res/values-<locale>/strings.xml` with the UNICEF values. Without this mirroring, a non-English-locale device renders the upstream `Dhis2` brand because Android picks the locale-specific main strings before falling back to the flavor's default-locale strings.

#### Scenario: Launcher entry shows the UNICEF TJK eLMIS name on a default-locale device

- **WHEN** the user installs the flavor's APK on an Android device set to English
- **AND** the user opens the device launcher
- **THEN** an entry labelled `UNICEF TJK eLMIS` appears in the launcher

#### Scenario: Launcher entry shows the UNICEF TJK eLMIS name on a Russian-locale device

- **WHEN** the user installs the flavor's APK on an Android device set to Russian
- **AND** the user opens the device launcher
- **THEN** an entry labelled `UNICEF TJK eLMIS` appears in the launcher (the flavor's `values-ru/strings.xml` overrides the main locale strings)

#### Scenario: Debug-variant launcher entry shows the UNICEF TJK eLMIS name, not the upstream Dhis2 Dev label

- **WHEN** the user installs the `unicefTjkElmisDebug` APK on a device or emulator
- **AND** the user opens the device launcher
- **THEN** the launcher entry reads `UNICEF TJK eLMIS`, NOT `Dhis2 Dev` (the flavor+buildType override under `app/src/unicefTjkElmisDebug/res/values/strings.xml` outranks the build-type-only override under `app/src/debug/res/values/strings.xml`)

### Requirement: The flavor SHALL provide every flavor-coupled Dagger module the Oslo build references

The flavor SHALL provide a `GranularSyncModule` in package `org.dhis2.utils.granularsync` so the Oslo Dagger graph (referenced from shared code under `app/src/main/java/`) can be resolved at annotation-processing time. Every other flavor in this repo provides the same module under its own source set; failing to ship it MUST cause the build to fail at the `kspUnicefTjkElmisDebugKotlin` task with `ComponentProcessingStep was unable to process 'org.dhis2.AppComponent' because 'GranularSyncModule' could not be resolved`.

The contents of the flavor's `GranularSyncModule` MUST NOT introduce flavor-specific behavior in this change — the module is build-wiring boilerplate. Functional customizations to sync behavior, if any, land in their own change proposals with matching specs.

#### Scenario: Annotation processing resolves the Dagger graph

- **WHEN** a developer runs `./gradlew :app:kspUnicefTjkElmisDebugKotlin`
- **THEN** KSP completes without `Unable to process 'org.dhis2.AppComponent' because 'GranularSyncModule' could not be resolved`
- **AND** the resulting Dagger components compile successfully

### Requirement: The flavor SHALL declare itself in every Gradle module that ships per-flavor code

Every Gradle module that ships per-flavor source sets MUST declare the `unicefTjkElmis` flavor inside its `productFlavors` block. Missing the declaration in any such module MUST cause the Gradle configuration to fail, because Android resolves variants by intersecting flavor declarations across modules.

#### Scenario: Build configures cleanly

- **WHEN** a developer runs `./gradlew assembleUnicefTjkElmisDebug` from the repository root
- **THEN** Gradle resolves the `unicefTjkElmis` variant in every module that participates in the build
- **AND** the build completes without `Cannot resolve dependencies for configuration` errors related to the flavor

### Requirement: The flavor SHALL reach the UNICEF DHIS2 server over plain HTTP

The flavor SHALL successfully complete authenticated requests to `http://172.16.0.99:18081` over plain HTTP. The flavor SHALL NOT introduce a per-host cleartext whitelist of its own; HTTP access is provided by the SDK fork's bundled `network_security_configuration.xml` (`<base-config cleartextTrafficPermitted="true">`), inherited via the merged manifest by every flavor in this repo. Tightening the cleartext stance is tracked separately and is out of scope here.

#### Scenario: Login over HTTP to the UNICEF server succeeds

- **WHEN** a user launches the installed flavor's APK with the UNICEF VPN active
- **AND** the user enters server URL `http://172.16.0.99:18081` and admin credentials
- **THEN** the network request reaches the server and authentication completes
- **AND** the device's logcat does not contain `CLEARTEXT communication ... not permitted` for `172.16.0.99`

### Requirement: The flavor SHALL display a UNICEF launcher icon at every supported density

The flavor SHALL provide a launcher icon set that renders the UNICEF brand mark at every density Android queries for, including the adaptive icon foreground used on Android API 26 and higher. The release and debug variants SHALL share the same launcher visual; coexistence on a single device is achieved via the debug build type's `applicationIdSuffix=".debug"`, not through icon differentiation.

#### Scenario: Launcher icon shows the UNICEF logo on release variants

- **WHEN** the installed `unicefTjkElmisRelease` APK is opened in the device launcher
- **THEN** the launcher entry displays the UNICEF brand mark on a white background
- **AND** the launcher mask (rounded square, circle, or squircle, depending on the launcher) does not crop the central UNICEF mark out of view

#### Scenario: Debug install renders the same UNICEF launcher icon as release

- **WHEN** both the `unicefTjkElmisDebug` and `unicefTjkElmisRelease` APKs are installed on the same device running Android API 26 or higher
- **THEN** both launcher entries display the UNICEF brand mark on a white background (the adaptive icon resolves `@color/ic_launcher_background = #FFFFFF` for both variants)
- **AND** the two installs are addressable as separate apps via their application IDs (`org.unicef.tjk.elmis.debug` for debug, `org.unicef.tjk.elmis` for release)

### Requirement: The flavor SHALL inherit shared resources unchanged when no override is provided

The flavor SHALL rely on Android resource merging: any resource not present in `app/src/unicefTjkElmis/res/` or `app/src/unicefTjkElmis<BuildType>/res/` MUST be inherited from `app/src/main/res/`. Resource overrides introduced by this flavor MUST be limited to the launcher icon set, the `app_name`/`logo_text`/`logo_number` strings, the network security config, and the `ic_launcher_background` color resource.

#### Scenario: Non-overridden resources fall back to the shared defaults

- **WHEN** the installed flavor renders any UI surface that consumes a resource not declared in the flavor source set (for example, a string in the login screen, a drawable in the home screen)
- **THEN** the rendered resource is the value defined in `app/src/main/res/`

### Requirement: The flavor SHALL NOT modify shared code

This change MUST NOT modify any file under `app/src/main/`, MUST NOT modify any other flavor's source set, and MUST NOT introduce SDK fork dependencies. The only contact with shared build files is appending the flavor block at the end of the existing `productFlavors` lists in `app/build.gradle.kts` and `login/build.gradle.kts`, which is unavoidable because Gradle product flavors must be declared in each module that ships per-flavor code.

#### Scenario: Diff against baseline shows only flavor-scoped additions

- **WHEN** a maintainer runs `git diff develop-eyeseetea..feat/new_unicefTjkElmis_flavor -- app/src/main/`
- **THEN** the diff is empty
- **AND** running the same diff against any other flavor's source set (e.g., `app/src/eyeseetea/`, `app/src/dhis2/`) is also empty
- **AND** running it against `app/build.gradle.kts` and `login/build.gradle.kts` shows only the appended `unicefTjkElmis` flavor blocks

#### Scenario: No flavor-scoped manifest is introduced

- **WHEN** a maintainer runs `find app/src/unicefTjkElmis app/src/unicefTjkElmisDebug app/src/unicefTjkElmisRelease -name "AndroidManifest.xml"`
- **THEN** the listing is empty (this change adds no flavor manifest; HTTP access relies on the baseline global `usesCleartextTraffic="true"`)
