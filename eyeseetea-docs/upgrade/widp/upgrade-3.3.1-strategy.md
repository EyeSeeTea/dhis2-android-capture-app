# WIDP Upgrade to 3.3.1 Strategy

Concrete strategy for upgrading the WIDP fork to the 3.3.1 baseline from `develop-eyeseetea`.

This document applies the phases from `eyeseetea-docs/onboarding-fork-guide.md` to the WIDP case.

## Current state

- Source branch: `feature-widp/bring_last_changes_3_3_0_1`
- Upgrade branch: `feature-widp/bring_last_changes_3_3_1`
- Base branch: `develop-eyeseetea`
- Merge-base with develop-eyeseetea: `64058e739`
- Total files differing from develop-eyeseetea (two-dot diff): ~538
- Flavor-specific files (app/src/widp/, app/src/widpDebug/): ~89
- Shared-code diffs: ~450
- Current version: `3.3.0.1-widp-fork-1` (needs updating to 3.3.1)
- SDK version: `1.13.0.1-eyeseetea-fork-1`

## Known WIDP customizations (verified 2026-04-02)

Originally 8 customizations were assumed. After verification against `develop-eyeseetea`:
- **5 confirmed** (3 active, 1 active with SDK dependency, 1 broken)
- **3 removed**: notifications translations merged into #3, indicators from form exists in baseline, events text filter does not exist in code

### 1. Change Server URL

- Status: `active`
- Risk: **medium**
- Reason: touches settings/menu integration that upstream may refactor
- Key files: `ChangeServerUrlDialog.kt`, `ChangeServerURLPresenter.kt`, `ChangeServerURLModule.kt`, `ChangeServerURLComponent.kt`, `dialog_change_server_url.xml`, widp menu
- Testing strategy: unit test for presenter logic, manual validation for dialog flow

### 2. Image upload without resizing

- Status: `active`
- Risk: **low**
- Reason: single file, single line change, clearly marked comment
- Key files: `FormValueStore.kt` (line ~274)
- Testing strategy: unit test for value store save behavior

### 3. Notifications system

- Status: `active`
- Risk: **high**
- Reason: largest customization surface (~17 files), complete standalone module (data, domain, presentation, DI), integration with sync flow and base activity
- Key files: `NotificationD2Repository.kt`, `NotificationsPresenter.kt`, `NotificationsModule.kt`, domain models, API clients, `ActivityGlobalAbstract.java`, `SyncPresenterImpl.kt`
- Testing strategy: unit tests for repository filtering logic, use cases, and presenter. Existing test: `NotificationD2RepositoryTest.kt`
- Priority: **highest** — test this first
- Note: includes translation handling (translations map, locale resolution, Markwon rendering)

### 4. 2FA support

- Status: `active`
- Risk: **medium**
- Reason: touches login flow across multiple modules (login, commonskmm) plus SDK patch. 17 EyeSeeTea customization comments. Supports TOTP, Email, and SMS 2FA types.
- Key files: `LoginRepositoryImpl.kt`, `CredentialsViewModel.kt`, `CredentialsScreen.kt`, `D2ErrorMessageProviderImpl.kt`, `DomainErrorMapper.kt`, `TwoFactorState.kt`, `TwoFactorRequiredException.kt`, `LoginResult.kt`, `CredentialsUiState.kt`
- SDK dependency: `LogInCall.kt`, `LoginPayload.kt`, `D2ErrorCode.java` in EyeSeeTea SDK fork
- Testing strategy: unit test for error code mapping, 2FA state management, resend logic

### 5. URL data element field

- Status: `broken` — data plumbing intact, rendering lost in upstream Compose migration
- Risk: **medium**
- Reason: form rendering is conflict-prone per conflict-rules.md. Original rendering (`onDescriptionClick` + `ShowDescriptionLabelDialog`) was removed in upstream refactor.
- Key files: `EventRepository.kt` (line ~729), `FieldUiModel.kt`, `FieldUiModelImpl.kt`, `FieldViewModelFactoryImpl.kt`, `EnrollmentRepository.kt`
- Original commit: `c556b7ab7` ("Implement show data element url", Nov 2022)
- Testing strategy: after reimplementation, unit test for URL concatenation to description
- Action required: reimplement URL display in current Compose UI (find where description/info dialog is rendered)

## Removed customizations (verified 2026-04-02)

| Original # | Name | Reason |
|---|---|---|
| 4 | Notification translations | Merged into #3 — translations are integral to the notifications system |
| 6 | Access to indicators from the form | Exists in `develop-eyeseetea` since 2019 (commit `949911e22`), zero diff |
| 7 | Events filter for text-type data elements | No diff found, no EyeSeeTea comments — never implemented or removed |

## Cleanup items

### PSI leftovers
The WIDP fork may contain files from a previous PSI fork that are not part of any active WIDP customization. These should be identified and removed so the branch matches `develop-eyeseetea` in non-customized areas.

### URL data element rendering (#5)
The rendering part of this customization was lost during the upstream Compose migration. The data plumbing (reading URL from SDK, storing in FieldUiModel) is intact but the display logic needs reimplementation.

### SMS 2FA string typo (#4)
The string for `SMS_TWO_FACTOR_CODE_SENT` may show "Email with two factor code sent" instead of "SMS with two factor code sent". Verify and fix if confirmed.

## Execution phases

### Phase A: Documentation

Status: `completed` (2026-04-02)

Steps:
1. ~~Bring shared eyeseetea-docs from develop-eyeseetea~~ (done)
2. ~~Create customizations/widp/customization-specs.md~~ (done)
3. ~~Create customizations/widp/customization-files.md~~ (done)
4. ~~Create upgrade/widp/upgrade-validation-checklist.md~~ (done)
5. ~~Complete the technical inventory by analyzing the full diff~~ (done — verified all 8 original customizations against develop-eyeseetea using two-dot diff)
6. ~~Validate customization existence~~ (done — reduced from 8 to 5 confirmed customizations)
7. ~~Developer reviews and confirms~~ (done — reviewed with Jorge in conversation, confirmed all findings)

Findings:
- Reduced from 8 to 5 customizations after verification
- #4 (translations) merged into #3 (notifications) as a single customization
- #5 (indicators from form) confirmed as baseline feature, not WIDP-specific
- #6 (events text filter) confirmed as non-existent in code
- #5 (URL data element) confirmed as broken — plumbing exists, rendering lost
- All file paths verified against actual two-dot diff
- All EyeSeeTea customization comments located (17 for 2FA, 1 for image resize)

8. ~~Review and fix customization comments~~ (done — added/normalized comments in 31 files):
   - #1 Change Server URL: added to 4 `.kt` files
   - #2 Image upload without resizing: updated from "no resize" to full spec title
   - #3 Notifications system: added to 13 new files
   - #4 2FA support: normalized 11 comments from various descriptions to spec title
   - #5 URL data element field: added to EventRepository.kt

### Phase B: OpenSpec formalization

Status: `pending`

Steps:
1. Install OpenSpec CLI: `npm install -g @fission-ai/openspec@latest`
2. Initialize: `openspec init`
3. Adapt `openspec/config.yaml` from Sports PR #306 — add:
   - customization code placement hierarchy rule (flavor > new file > end of file > inline)
   - reference to WIDP flavor and customization-specs.md
4. Adapt `openspec/project.md` from Sports PR #306
5. Create specs for the 5 confirmed customizations
6. Each spec includes: purpose, MUST/SHALL requirements, Given-When-Then scenarios
7. Model the upgrade as a change: `openspec/changes/upgrade-widp-to-3.3.1/`

### Phase C: Claude Code tooling

Status: `pending`

Source: Sports PR #306 (`feature-sports/setup-claude-dev-environment`) provides a complete `.claude/` setup.

Steps:
1. Adapt `CLAUDE.md` from Sports PR #306. Add:
   - WIDP-specific identity (flavor: `widp`, paths: `app/src/widp/`, `app/src/widpDebug/`)
   - customization code placement hierarchy rule
   - reference to WIDP customization-specs.md and upgrade strategy
2. Bring the 3 upgrade agents from Sports PR #306
3. Bring the 4 OpenSpec commands from Sports PR #306
4. Bring selectively: android-testing skill, android-flavor-management skill, OpenSpec skills, settings.json
5. Do NOT bring: design agents, Pencil workflow, ClickUp PM skill, UX wireframing
6. Verify a fresh Claude session can orient using CLAUDE.md

### Phase D: Upgrade execution

Status: `pending`

Steps:
1. Create `eyeseetea-docs/upgrade/widp/upgrade-3.3.1-notes.md` from template
2. Merge `develop-eyeseetea` into this branch
3. Classify conflicts using `conflict-rules.md`:
   - `accept_ours` for `app/src/widp/**` and `app/src/widpDebug/**`
   - `accept_theirs` for pure formatting, import, and shared test changes
   - `manual_reapply_on_theirs` for shared files with confirmed customizations
   - `defer_after_build_verification` for unclear cases
4. Resolve easy conflicts first
5. Pause for developer review after the easy batch
6. Resolve manual conflicts by reapplying minimum client-specific logic
7. Fix URL data element rendering (#5) — reimplement in Compose UI
8. Fix SMS 2FA string typo (#4) if confirmed
9. Clean up PSI leftovers
10. Update customization-files.md with confirmed surviving customizations

### Phase E: Tests and cleanup

Status: `pending`

Steps:
1. Add unit tests per customization, priority order:
   - **#3 Notifications system** (highest risk, largest surface)
   - **#1 Server URL change** (medium risk, testable presenter)
   - **#4 2FA support** (medium risk, testable error mapping and state management)
   - **#2 Image upload** (low risk, single behavior)
   - **#5 URL data element** (after reimplementation)
2. Run full build
3. Execute manual validation per checklist
4. Run `check_upgrade_docs.py --client widp`
5. Ensure code comments use `// EyeSeeTea customization - [title]` with exact spec titles

## Risk areas

| Area | Risk | Reason |
|------|------|--------|
| Notifications (#3) | high | largest surface, ~17 files, DI wiring, sync integration |
| 2FA (#4) | medium | 8+ files across login/commonskmm, SDK patch dependency, 3 2FA types |
| URL data element (#5) | medium | rendering lost, needs reimplementation in Compose |
| Form module changes | medium | conflict-prone area per conflict-rules.md |
| Settings/menu integration (#1) | medium | upstream may have refactored settings |

## Dependencies

- `develop-eyeseetea` must already contain the target Oslo version
- SDK fork must be compatible (check EyeSeeTea SDK tag in libs.versions.toml)
- OpenSpec CLI must be available for Phase B
- Node.js 20.19.0+ required for OpenSpec

## Success criteria

- all 5 confirmed customizations have a final status (active / broken-fixed / absorbed)
- no unexplained shared drift remains
- build passes for the `widp` flavor
- unit tests exist for at least the top 3 customizations (#3, #1, #4)
- manual validation checklist executed for all active customizations
- documentation passes `check_upgrade_docs.py --client widp`
- code comments, specs, checklist, and inventory describe the same final state
- URL data element rendering (#5) reimplemented or explicitly deferred
