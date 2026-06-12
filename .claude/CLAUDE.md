# CLAUDE.md — DHIS2 Android Capture App (EyeSeeTea Forks)

## Project Overview

Fork of the **DHIS2 Android Capture App** (University of Oslo) maintained by **EyeSeeTea**. We maintain multiple client-specific forks as branches (`develop-widp`, `develop-psi`, `develop-simprints`, `develop-sports`, etc.), each adding custom features on top of the upstream app.

- **Language**: Kotlin (preferred) + Java (legacy, progressive migration)
- **UI**: Jetpack Compose (new screens) + Android Views/XML/Data Binding (existing screens)
- **DI**: Koin (new modules) + Dagger 2 (legacy)
- **Async**: Kotlin Coroutines/Flow (new code) + RxJava 2 (legacy)
- **SDK**: EyeSeeTea fork of DHIS2 Android SDK (`com.github.EyeSeeTea:dhis2-android-sdk`)
- **Min SDK**: 21 | **Target SDK**: 36 | **Java**: 17
- **Build**: Gradle Kotlin DSL with version catalog (`gradle/libs.versions.toml`)

## Git Workflow

- **Default branch per fork**: `develop-<fork>` (e.g., `develop-widp`, `develop-sports`)
- **Baseline branch**: `develop-eyeseetea` is the EyeSeeTea reference. Oslo upgrades go into `develop-eyeseetea` first; client forks upgrade from it, **never directly from Oslo**.
- **Branch naming**: `feature-<fork>/<name>`, `fix-<fork>/<name>` (e.g., `feature-widp/custom-enrollment`)
- **Conventional Commits**: `feat(scope):`, `fix(scope):`, `refactor(scope):`, `test(scope):`, `docs(scope):`
- **Important**: Always branch from and PR into the relevant `develop-<fork>` branch, NOT master
- Shared improvements go to `develop` and are merged into fork branches

## Pull Requests

Every PR must include a link to related issue tracker tasks:
```markdown
## Related Tasks
- [Task name](<url>/<id>)
```

## Boy Scout Rule

> Leave every file cleaner than you found it.

- Refactor what you touch, not the whole codebase
- Scope: only files you're already modifying
- Ensures incremental convergence to standards
- Applies to code, tests, resources, and documentation

## Architecture — Clean Architecture

```
Domain (entities, repository interfaces, use cases)
    ↑ depends on
Data (repository implementations, SDK interactions, data sources, mappers)
    ↑ wired via
Presentation (Activities, Fragments, ViewModels, Compose screens)
```

### Hard Rules

1. **Dependency Rule**: outer layers depend on inner, never reverse. Domain has zero Android/framework imports.
2. **Repository Pattern**: all external data access (SDK, network, storage) goes through repository interfaces defined in domain.
3. **Presentation is wiring only**: Activities/Fragments collect state and delegate to composables. ViewModels orchestrate use cases.
4. **No duplicated logic** across components — extract to use cases or shared modules.

### Module Structure

| Module | Purpose |
|--------|---------|
| `app` | Main application, features (`usescases/`), flavors |
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

## Code Style

### Functional Programming & Immutability

- Use `map`, `filter`, `flatMap`, `fold`, `reduce` over `for` + mutable accumulators
- Avoid mutating function arguments
- Prefer `val` over `var`, `List` over `MutableList`, immutable `data class`
- Use `sealed class`/`sealed interface` for modeling state and errors
- Use `Either`/`Result` for error handling instead of throwing exceptions
- Prefer composition over inheritance

### Kotlin

- Kotlin for all new code; Java only when modifying existing Java files
- Strict null safety — no `!!` operator unless absolutely justified with a comment
- Use `when` exhaustively with sealed types
- Extension functions for utility operations
- Coroutines + Flow for new async code; RxJava only when extending existing Rx chains
- Do not mix Rx and Coroutines in the same feature

### Android / Compose

- Jetpack Compose for all new screens
- Model UI state as `sealed interface` with `Loading`, `Content`, `Error` variants
- ViewModel exposes `StateFlow`, Compose collects with `collectAsStateWithLifecycle()`
- Compose previews (`@Preview`) for all new composables
- Accessibility: content descriptions, 48dp minimum touch targets
- All user-facing strings in `strings.xml`, never hardcoded

## Test Quality

- Assert concrete values — no `assertTrue(result != null)` or `assertNotNull(x)`
- Group tests with `describe`/nested classes
- Use helpers to reduce repetition; extract constants for repeated strings
- Test naming: backtick descriptive names `` `should do X when Y` ``
- Write tests for every use case and mapper
- Compose UI tests with `ComposeTestRule`; Espresso for View-based UI
- Turbine for Flow testing

## Build & Run

```bash
# Build debug APK (default dhis2 flavor)
./gradlew :app:assembleDhis2Debug

# Build a specific flavor
./gradlew :app:assembleWidpDebug
./gradlew :app:assemblePsiDebug
./gradlew :app:assembleSportsDebug

# Run unit tests
./gradlew test
./gradlew :app:testDhis2DebugUnitTest

# Run Android instrumented tests
./gradlew :app:connectedDhis2DebugAndroidTest

# Lint / code style
./gradlew ktlintCheck
```

## Fork Documentation (eyeseetea-docs)

EyeSeeTea maintains a documentation structure for fork management in `eyeseetea-docs/` (on `develop-eyeseetea`):

- **`eyeseetea-docs/README.md`** — Documentation model overview
- **`eyeseetea-docs/onboarding-fork-guide.md`** — 8-phase guide for onboarding forks without docs
- **`eyeseetea-docs/upgrade/conflict-rules.md`** — Conflict classification and resolution rules for merges
- **`eyeseetea-docs/upgrade/upgrade-plan-client-forks.md`** — Full upgrade runbook
- **`eyeseetea-docs/customizations/<client>/customization-specs.md`** — Functional customization inventory per client
- **`eyeseetea-docs/customizations/<client>/customization-files.md`** — Technical file inventory per client
- **`eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`** — Manual validation flows
- **`eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`** — Temporary merge progress tracking

### Sports Fork Documentation

- **`eyeseetea-docs/customizations/sports/customization-specs.md`** — Sports functional customization inventory
- **`eyeseetea-docs/customizations/sports/customization-files.md`** — Sports technical file inventory
- **`eyeseetea-docs/upgrade/sports/upgrade-validation-checklist.md`** — Sports upgrade validation flows

### Key rules from conflict-rules.md
- Prefer `develop-eyeseetea` by default; only keep confirmed client-specific logic
- Classify conflicts as: `accept_ours`, `accept_theirs`, `manual_reapply_on_theirs`, `defer_after_build_verification`
- Classify ALL conflicts BEFORE editing any file
- Resolve easy batch first, pause for developer review, then manual conflicts
- Surviving customizations in shared code must have: `// EyeSeeTea customization - [title]`

## SDK Configuration

See `EyeSeeTea.md` for full details:
- SDK version in `gradle/libs.versions.toml` (`dhis2sdk` key)
- Published from https://github.com/EyeSeeTea/dhis2-android-sdk via JitPack
- Local SDK development: set `dhis2.useLocalSdk=true` in `local.properties`
- Never commit `local.properties`

## Flavor Management

| Flavor | Branch | Client |
|--------|--------|--------|
| dhis2 | develop | Default (upstream UiO) |
| dhis2PlayServices | - | With Google Play Services |
| dhis2Training | - | Training/demo mode |
| widp | develop-widp | WIDP |
| psi | develop-psi | PSI |
| sports | develop-sports | Sports tracking |

Flavor-specific code goes in `app/src/<flavor>/`. Keep flavor code minimal — extract shared logic to `commons/` or `form/`.

## UI Design Workflow

1. **Design before implementation** — create wireframes/mockups in Pencil `.pen` files
2. **Design artifacts** live in `openspec/designs/`:
   - `wireframes/` — low-fidelity `.pen` source files
   - `mockups/` — high-fidelity `.pen` source files
   - `exports/` — PNG/SVG exports for review
3. **Design is part of the proposal** — include design tasks in OpenSpec change proposals
4. **Design tasks tagged `[GD]`** must complete before `[UI]` implementation tasks
5. **Commit `.pen` source files** — PNGs in exports/ are derived artifacts
6. **Export naming**: `[feature]-[screen]-[state].[format]` (e.g., `enrollment-form-loading.png`)

## After Every Feature Change

1. Update PR description
2. Update OpenSpec specs if behavior changed
3. Update UI designs if screens changed
4. Update `EyeSeeTea.md` if SDK/build config changed

## Pre-Commit Self-Review

Before every commit, verify:
1. **Architecture** — no dependency rule violations (domain imports framework? presentation has business logic?)
2. **Pattern consistency** — follows existing patterns in the module
3. **Functional style** — no mutable accumulator loops where `map`/`filter`/`fold` works
4. **Test quality** — assertions check concrete values, not just truthiness
5. **No duplication** — logic appears in exactly one place
6. **Separation of concerns** — business logic not in Activities/Fragments/Composables
7. **Immutability** — `val` preferred, no unnecessary `var` or `Mutable*`
8. **Boy Scout Rule** — files you touched are at least as clean as before
