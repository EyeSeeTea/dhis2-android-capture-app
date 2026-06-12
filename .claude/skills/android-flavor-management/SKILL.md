---
name: android-flavor-management
description: >
  Skill for managing Android product flavors and client-specific forks.
  Covers flavor configuration, source sets, resource overrides, and
  branch-flavor mapping. Trigger on: flavor, fork, client customization,
  build variant, or source set requests.
---

# Android Flavor & Fork Management

## Flavor-Branch Mapping

| Flavor | Branch | Client |
|--------|--------|--------|
| dhis2 | develop | Default (upstream UiO) |
| dhis2PlayServices | - | With Google Play Services |
| dhis2Training | - | Training/demo |
| widp | develop-widp | WIDP |
| psi | develop-psi | PSI |
| sports | develop-sports | Sports tracking |
| simprints | develop-simprints | Simprints biometrics |

## Source Set Structure

```
app/src/
  main/           # Shared across ALL flavors
  dhis2/          # Default flavor overrides
  widp/           # WIDP-specific code, layouts, resources
  psi/            # PSI-specific
  sports/         # Sports-specific
  sportsDebug/    # Sports debug-only (e.g., test credentials)
  widpDebug/      # WIDP debug-only
  widpRelease/    # WIDP release-only (e.g., production URLs)
```

## Adding Flavor-Specific Code

### 1. Resource Overrides
Place in `app/src/<flavor>/res/`:
```
app/src/widp/res/
  values/strings.xml     # Override app name, labels
  drawable/              # Custom icons, logos
  layout/                # Override specific layouts
```

### 2. Source Code
Place in `app/src/<flavor>/java/org/dhis2/`:
```kotlin
// app/src/widp/java/org/dhis2/WidpCustomFeature.kt
// This class is only compiled for the WIDP flavor
```

### 3. Manifest Entries
`app/src/<flavor>/AndroidManifest.xml` — merged with main manifest.

## Flavor Configuration in build.gradle.kts

```kotlin
productFlavors {
    create("widp") {
        applicationId = "org.dhis2.widp"
        dimension = "default"
        // Flavor-specific build config fields
        buildConfigField("String", "SERVER_URL", "\"https://widp.dhis2.org\"")
    }
}
```

## Working on a Fork

1. **Check your branch**: `git branch` — must be on `develop-<fork>`
2. **Build the right flavor**: `./gradlew :app:assemble<Flavor>Debug`
3. **Test the right flavor**: `./gradlew :app:test<Flavor>DebugUnitTest`
4. **Keep shared code in main**: Only put truly flavor-specific code in flavor source sets
5. **Feature branches**: Branch from `develop-<fork>` as `feature-<fork>/description`

## Best Practices
- Minimize flavor-specific code — extract shared logic to `commons/` or `form/`
- Use build config fields for flavor-specific constants (URLs, feature flags)
- Override resources (strings, drawables) rather than duplicating layouts
- Test changes against the default `dhis2` flavor too, to avoid breaking shared code
