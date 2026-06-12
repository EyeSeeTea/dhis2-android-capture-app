# Sports Flavor Build Configuration

## Context

The sports fork is distributed as a separate Android product flavor within the DHIS2 Android Capture App multi-flavor build. The `sports` flavor defines its own `applicationIdSuffix`, source sets, and flavor-specific stub files that satisfy compile-time contracts expected by the shared codebase.

After the upgrade to v3.3.1 via `develop-eyeseetea`, only the following product flavors SHALL remain: `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`, and `sports`. All other client flavors (`psi`, `widp`, `simprints`) SHALL be absent from the sports branch.

## Requirements

### REQ-FB-01: Sports product flavor defined in build configuration

The `app/build.gradle.kts` file MUST define a `sports` product flavor within the existing flavor dimension.

The `sports` flavor MUST specify an `applicationIdSuffix` that distinguishes it from other flavors on the same device.

### REQ-FB-02: Sports source sets present

The build MUST include the following source set directories:
- `app/src/sports/` -- flavor-specific code and resources
- `app/src/sportsDebug/` -- debug build type overlay for the sports flavor

### REQ-FB-03: Required flavor stub files

The `sports` flavor source set MUST provide the following stub/implementation files that satisfy shared-code compile-time contracts:

- `CustomizableConstants.kt` in package `org.dhis2.utils` -- MUST define `DEFAULT_URL` as an empty string
- `UserComponentFlavor.kt` in package `org.dhis2.data.user` -- MUST define the `UserComponentFlavor` interface (may be empty)
- `eventCaptureRepositoryFunctions.kt` in package `org.dhis2.usescases.eventsWithoutRegistration.eventCapture` -- MUST provide the `getProgramStageName(d2: D2, eventUid: String): String` function
- `GranularSyncModule.kt` in package `org.dhis2.utils.granularsync` -- MUST provide the Dagger `@Module` for `GranularSyncViewModelFactory`, `DispatcherProvider`, `GranularSyncRepository`, and `SMSSyncProvider`

### REQ-FB-04: No removed flavor references

The sports branch MUST NOT contain source set directories or build configuration entries for removed flavors (`psi`, `widp`, `simprints`).

The `app/build.gradle.kts` MUST define exactly these product flavors: `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`, and `sports`.

### REQ-FB-05: Sports flavor compiles successfully

The `sports` flavor MUST compile without errors for both `debug` and `release` build types.

## Scenarios

### Scenario: Sports debug APK builds successfully

- **GIVEN** the sports branch has all required flavor stubs and build configuration
- **WHEN** `./gradlew :app:assembleSportsDebug` is executed
- **THEN** the build completes without errors and produces an APK

### Scenario: Sports release APK builds successfully

- **GIVEN** the sports branch has all required flavor stubs and build configuration
- **WHEN** `./gradlew :app:assembleSportsRelease` is executed
- **THEN** the build completes without errors and produces an APK

### Scenario: No removed flavor source sets exist

- **GIVEN** the merge and cleanup are complete
- **WHEN** the contents of `app/src/` are inspected
- **THEN** directories `psi/`, `widp/`, and `simprints/` do NOT exist
- **AND** directories `sports/`, `sportsDebug/`, and `eyeseetea/` DO exist

### Scenario: Build file defines exactly the expected flavors

- **GIVEN** the merge and cleanup are complete
- **WHEN** `app/build.gradle.kts` is inspected
- **THEN** exactly five product flavors are defined: `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`, `sports`

### Scenario: CustomizableConstants provides empty default URL

- **GIVEN** the sports flavor is selected
- **WHEN** `org.dhis2.utils.DEFAULT_URL` is referenced at runtime
- **THEN** its value is an empty string

### Scenario: getProgramStageName resolves stage name from SDK

- **GIVEN** an event UID that corresponds to a valid event with a program stage
- **WHEN** `getProgramStageName(d2, eventUid)` is called
- **THEN** it returns the display name of the associated program stage
- **AND** returns an empty string if the program stage has no display name
