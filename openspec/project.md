# OpenSpec — DHIS2 Android Capture App (EyeSeeTea)

## Project Identity

- **Name**: DHIS2 Android Capture App (EyeSeeTea Forks)
- **Type**: Android mobile application
- **Organization**: EyeSeeTea
- **Upstream**: University of Oslo (UiO) DHIS2 Android Capture App
- **Repository**: Multi-fork repository with client-specific branches

## Purpose

Maintain and extend multiple customized forks of the DHIS2 Android Capture App for different clients (WIDP, PSI, Simprints, Sports, etc.), each requiring specific features on top of the upstream DHIS2 functionality.

## Architecture Principles

### Clean Architecture
- **Presentation Layer**: Activities, Fragments, ViewModels, Compose Screens
- **Domain Layer**: Use cases, repository interfaces, domain models
- **Data Layer**: Repository implementations, SDK interactions, data sources

### Functional Programming
- Favor immutability: `val` over `var`, `List` over `MutableList`, `data class` for models
- Use `Either`/`Result` for error handling instead of throwing exceptions
- Pure functions for mappers and transformations
- Avoid side effects in domain layer

### Dependency Injection
- **Koin** for new modules and Compose-based features
- **Dagger 2** for existing features (maintain, don't introduce new Dagger modules)

### Reactive Programming
- **Kotlin Coroutines + Flow** for all new asynchronous code
- **RxJava 2** only when extending existing Rx-based chains
- Do not mix Rx and Coroutines in the same feature — pick one

### UI Development
- **Jetpack Compose** for all new screens
- **XML + Data Binding** only when modifying existing View-based screens
- **Material 3** design system
- State modeled as `sealed interface` with exhaustive `when` handling

## Module Structure

| Module | Purpose |
|--------|---------|
| `app` | Main application, features, flavors |
| `commons` | Shared utilities, extensions, base classes |
| `form` | DHIS2 form rendering and handling |
| `login` | Authentication flow |
| `tracker` | DHIS2 Tracker program features |
| `aggregates` | Data aggregation features |
| `dhis_android_analytics` | Analytics and charts |
| `dhis2_android_maps` | Map rendering |
| `ui-components` | Shared UI components library |
| `compose-table` | Compose-based data tables |
| `commonskmm` | Kotlin Multiplatform shared code |
| `stock-usecase` | Stock/inventory management |

## Coding Conventions

- **Language**: Kotlin for all new code. Java only when modifying existing Java files.
- **Naming**: Follow Kotlin conventions (`camelCase` for functions/properties, `PascalCase` for classes)
- **Package**: `org.dhis2.*` for all code
- **Tests**: `<ClassName>Test.kt` with descriptive backtick method names
- **Strings**: All user-facing strings in `strings.xml` (not hardcoded)
- **Accessibility**: Content descriptions on all images/icons, 48dp minimum touch targets

## Fork Management

- Each fork lives on its own `develop-<fork>` branch
- Feature branches: `feature-<fork>/<description>`
- Shared improvements go to `develop` and are merged into fork branches
- Flavor-specific code in `app/src/<flavor>/`
- Minimize flavor code — prefer shared code with configuration

## SDK

- EyeSeeTea fork: `com.github.EyeSeeTea:dhis2-android-sdk`
- Version managed in `gradle/libs.versions.toml` (`dhis2sdk` key)
- Supports local development via Composite Build
- See `EyeSeeTea.md` for full configuration details

## OpenSpec Workflow

1. **Specs** (`openspec/specs/`): Feature specifications describing what needs to be built
2. **Changes** (`openspec/changes/`): Change proposals with:
   - `proposal.md` — problem statement, proposed solution, alternatives considered
   - `design.md` — technical design, architecture decisions, API contracts
   - `tasks.md` — implementation task breakdown with role assignments
3. Changes reference specs, agents execute tasks via ClickUp integration
