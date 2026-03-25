## Why

The sports-tracker branch (`develop-sports`) is based on an outdated upstream version and lacks the `eyeseetea-docs/` documentation structure required for safe, repeatable upgrades. The `develop-eyeseetea` branch already carries the upstream DHIS2 Android Capture App **v3.3.1** release, EyeSeeTea-specific cleanup (dead code removal, flavor consolidation), and a complete documentation + tooling framework for managing forks.

Per the [onboarding fork guide](https://github.com/EyeSeeTea/dhis2-android-capture-app/blob/develop-eyeseetea/eyeseetea-docs/onboarding-fork-guide.md), forks without documentation must complete an 8-phase process: document first, then upgrade. This ensures customizations are inventoried, conflicts are classified systematically, and nothing is lost silently.

## What Changes

### Phase 1-3: Documentation & Inventory (prerequisite)
- **Bring shared `eyeseetea-docs/` into sports branch**: templates, conflict rules, upgrade plan, scripts
- **Create sports-specific documentation**: `customization-specs.md`, `customization-files.md`, `upgrade-validation-checklist.md`
- **Inventory sports customizations**: classify each diff as intentional customization, technical drift, or absorbed code

### Phase 4-5: Tooling (partially done)
- **OpenSpec formalization**: this change itself models the upgrade; create specs per active sports customization
- **Claude Code tooling**: already set up via PR #306 — verify CLAUDE.md references docs

### Phase 6: Execute the Upgrade
- **Merge `develop-eyeseetea` into `develop-sports`**: Bring 83 commits including upstream 3.3.1, flavor cleanup, and documentation
- **Classify conflicts** per `conflict-rules.md` categories: `accept_ours`, `accept_theirs`, `manual_reapply_on_theirs`, `defer_after_build_verification`
- **Resolve easy conflicts first**, pause for review, then resolve manual conflicts
- **Flavor consolidation**: After merge, only `sports` and `eyeseetea` flavors remain alongside base `dhis2` flavors

### Phase 7-8: Tests & Cleanup
- **Add tests** for each active sports customization
- **Clean up** absorbed/removed code, align comments with `customization-specs.md` titles

## Capabilities

### New Capabilities

- `sports-fork-onboarding`: Covers the full 8-phase onboarding process — documentation, inventory, merge, validation, tests, and cleanup — following the EyeSeeTea fork guide.
- `sports-customization-inventory`: Documents the sports-specific customizations (flavor config, app branding, Koin setup) and classifies shared-code diffs as intentional vs. drift vs. absorbed.

### Modified Capabilities

_(No existing specs to modify)_

## Impact

- **Documentation**: New `eyeseetea-docs/customizations/sports/` and `eyeseetea-docs/upgrade/sports/` directories
- **Build configuration**: `app/build.gradle.kts` — only `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`, and `sports` flavors
- **Source sets**: `app/src/sports/` preserved (63 files: icons, strings, flavor code). `app/src/psi/` and `app/src/widp/` removed.
- **Shared code**: ~130 files in shared code differ between branches. Most are old code removed by eyeseetea (notifications, ChangeServerURL, BasicPreference, layouts). Sports-specific shared code changes are minimal.
- **Dependencies**: SDK, Gradle plugins, AndroidX updated to 3.3.1 levels
- **CI/CD**: GitHub Actions workflows updated
- **Related task**: [Upgrade to version 3.3.1 using develop-eyeseetea branch](https://app.clickup.com/t/869cm2606)
- **Related PR**: [PR #306 — Claude dev environment setup](https://github.com/EyeSeeTea/dhis2-android-capture-app/pull/306)
