## ADDED Requirements

### Requirement: Sports fork onboarding follows the 8-phase guide
The upgrade process SHALL follow the onboarding fork guide phases in order: documentation (1-3), tooling (4-5), merge (6), tests (7), cleanup (8). Phases 1-3 SHALL complete before Phase 6 begins.

#### Scenario: Documentation exists before merge
- **WHEN** the merge of `develop-eyeseetea` into the sports branch is initiated
- **THEN** the following files SHALL already exist: `eyeseetea-docs/customizations/sports/customization-specs.md`, `eyeseetea-docs/customizations/sports/customization-files.md`, `eyeseetea-docs/upgrade/sports/upgrade-validation-checklist.md`

#### Scenario: Shared docs are present before client docs
- **WHEN** Phase 2 (client docs creation) begins
- **THEN** all shared `eyeseetea-docs/` structure from `develop-eyeseetea` (README, SDK_Setup, templates, scripts, conflict-rules) SHALL be present in the branch

### Requirement: Customization inventory completed before merge
All sports customizations SHALL be inventoried with functional title, intent, expected behavior, lifecycle status (`active`, `needs_validation`, `absorbed`, `removed`), and technical file list before the merge begins.

#### Scenario: Flavor-specific files inventoried
- **WHEN** the inventory is complete
- **THEN** `customization-files.md` SHALL list all files under `app/src/sports/` and `app/src/sportsDebug/` grouped by customization title

#### Scenario: Shared-code diffs classified
- **WHEN** the inventory is complete
- **THEN** every file that differs between `develop-sports` and `develop-eyeseetea` in shared code SHALL be classified as either (a) linked to a customization title, (b) technical drift / old code, or (c) `needs_validation`

#### Scenario: Unclassified diffs visible
- **WHEN** diffs exist without a confirmed customization title
- **THEN** they SHALL appear in section 3 of `customization-files.md`, not hidden or silently dropped

### Requirement: Conflict preclassification before editing
Immediately after merge, all conflicted files SHALL be classified into a preclassification table in `upgrade-<version>-notes.md` before any file is edited.

#### Scenario: Preclassification table format
- **WHEN** the merge produces conflicts
- **THEN** a table in `eyeseetea-docs/upgrade/sports/upgrade-three-three-one-notes.md` SHALL list each file with: path, classification (`accept_ours`/`accept_theirs`/`manual_reapply_on_theirs`/`defer_after_build_verification`), expected functional delta, linked customization title, and status

#### Scenario: Easy batch resolved first with pause
- **WHEN** `accept_ours` and `accept_theirs` conflicts are resolved
- **THEN** the agent SHALL stop and present results for developer review before resolving `manual_reapply_on_theirs` conflicts

### Requirement: Conflict resolution follows conflict-rules.md
All conflict resolution decisions SHALL follow the rules defined in `eyeseetea-docs/upgrade/conflict-rules.md`.

#### Scenario: Flavor files accept ours
- **WHEN** a conflicted file is under `app/src/sports/**` or `app/src/sportsDebug/**`
- **THEN** it SHALL be resolved as `accept_ours`

#### Scenario: Removed code accept theirs
- **WHEN** a conflict involves code removed in `develop-eyeseetea` (notifications, ChangeServerURL, BasicPreference, 2FA) and the code is not a confirmed active sports customization
- **THEN** it SHALL be resolved as `accept_theirs`

#### Scenario: Shared business logic manual reapply
- **WHEN** a shared file conflict contains confirmed sports-specific business logic
- **THEN** it SHALL be resolved by starting from `develop-eyeseetea` and reinserting only the minimum sports-specific lines

#### Scenario: Conflict minimization
- **WHEN** a manual conflict is resolved
- **THEN** the resulting diff SHALL match the expected functional delta. If it is significantly larger, the resolution SHALL be redone from `develop-eyeseetea` with only the minimum custom lines.

### Requirement: Only sports and eyeseetea flavors remain
After the merge, the build configuration SHALL define exactly: `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`, and `sports` product flavors.

#### Scenario: Build file contains correct flavors
- **WHEN** the merge is complete and conflicts resolved
- **THEN** `app/build.gradle.kts` defines exactly those five product flavors

#### Scenario: Removed flavor source sets absent
- **WHEN** the merge is complete
- **THEN** `app/src/psi/` and `app/src/widp/` do not exist, and `app/src/eyeseetea/` and `app/src/sports/` exist

### Requirement: Surviving customizations have code comments
Every sports customization surviving in shared code SHALL have a nearby comment: `// EyeSeeTea customization - [title]` using the exact title from `customization-specs.md`.

#### Scenario: Comment present in shared code
- **WHEN** a sports-specific behavior exists in a shared module file
- **THEN** a comment `// EyeSeeTea customization - [title]` SHALL be present near the custom code

### Requirement: Both flavors build successfully
Both the sports and eyeseetea debug builds SHALL compile without errors after merge.

#### Scenario: Sports debug build
- **WHEN** `./gradlew :app:assembleSportsDebug` is executed
- **THEN** the build completes without errors

#### Scenario: Eyeseetea debug build
- **WHEN** the eyeseetea flavor debug build task is executed
- **THEN** the build completes without errors

### Requirement: Tests pass and customizations have test coverage
All existing unit tests SHALL pass, and each active sports customization SHALL have at least one targeted test.

#### Scenario: Existing test suite passes
- **WHEN** `./gradlew test` is executed on the merged branch
- **THEN** all tests pass without failures

#### Scenario: Customization test coverage
- **WHEN** a sports customization is confirmed as `active` in `customization-specs.md`
- **THEN** at least one unit or UI test SHALL validate the specific customization behavior

### Requirement: No unexplained shared drift after cleanup
After Phase 8 cleanup, no unexplained differences SHALL remain in shared code between the sports branch and `develop-eyeseetea`.

#### Scenario: Clean diff
- **WHEN** `git diff develop-eyeseetea..develop-sports -- app/src/main/ commons/ form/ tracker/ aggregates/` is run after cleanup
- **THEN** every remaining difference SHALL be linked to a confirmed customization title in `customization-files.md` or to the `eyeseetea-docs/` sports-specific documentation
