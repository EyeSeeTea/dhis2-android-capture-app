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

- Status: `active` — rendering reimplemented 2026-04-17 in Compose supporting text
- Risk: **medium**
- Reason: form rendering is conflict-prone per conflict-rules.md. Original rendering (`onDescriptionClick` + `ShowDescriptionLabelDialog`) was removed in upstream refactor; new rendering is inline supporting text.
- Key files: `EventRepository.kt` (line ~729), `FieldUiModel.kt`, `FieldUiModelImpl.kt`, `FieldViewModelFactoryImpl.kt`, `EnrollmentRepository.kt`, `FieldUiModelExtensions.kt` (new rendering)
- Original commit: `c556b7ab7` ("Implement show data element url", Nov 2022)
- Reimplementation: `FieldUiModelExtensions.supportingText()` — appends URL to description line
- Testing strategy: unit test for `supportingText()` URL concatenation behavior

## Removed customizations (verified 2026-04-02)

| Original # | Name | Reason |
|---|---|---|
| 4 | Notification translations | Merged into #3 — translations are integral to the notifications system |
| 6 | Access to indicators from the form | Exists in `develop-eyeseetea` since 2019 (commit `949911e22`), zero diff |
| 7 | Events filter for text-type data elements | No diff found, no EyeSeeTea comments — never implemented or removed |

## Cleanup items

### PSI leftovers — `resolved` (2026-04-17)
Verified already clean at the source level; PSI flavor files were removed in commit `2c49aa577`. Only stale `.idea/` references remain and those are gitignored.

### URL data element rendering (#5) — `resolved` (2026-04-17)
Reimplemented in `form/src/main/java/org/dhis2/form/extensions/FieldUiModelExtensions.kt` — `supportingText()` now appends `url` to the description line, so the URL renders inline under each field in the Compose supporting-text surface.

### SMS 2FA string typo (#4) — `resolved` (2026-04-17)
Fixed in `commonskmm/src/commonMain/composeResources/values/strings.xml:217`. `sms_two_factor_code_sent` now reads "SMS with two factor code sent".

## Execution phases

### Phase A: Documentation

Status: `completed` (2026-04-02)

Steps:
1. ~~Bring shared eyeseetea-docs from develop-eyeseetea~~ (done)
2. ~~Create customizations/widp/customization-specs.md~~ (done, then **deleted 2026-04-15** — replaced by `openspec/specs/` as part of Phase B)
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

Status: `completed` (2026-04-15)

Scope of this phase: install OpenSpec and migrate the current WIDP customizations to OpenSpec specs. **The upgrade proposal itself is NOT created here** — it is the first step of Phase D (Upgrade execution).

Steps:
1. ~~Install OpenSpec CLI: `npm install -g @fission-ai/openspec@latest`~~ (done, v1.2.0)
2. ~~Initialize: `openspec init --tools claude`~~ (done — scaffold in `openspec/`, Claude skills/commands in `.claude/`)
3. ~~Create `openspec/config.yaml`~~ (done — note: `project.md` is legacy in OpenSpec ≥1.2.0; the new location for project context is `config.yaml` under a `context:` field, plus per-artifact `rules:`). The config includes the customization code placement hierarchy rule (flavor > new file > end of file > inline) and the WIDP identity block.
4. ~~Create specs for the 5 confirmed customizations~~ (done — `openspec/specs/{change-server-url, image-upload-no-resize, notifications, two-factor-auth, url-data-element}/spec.md`). Each spec uses the exact human title from the prior `customization-specs.md` as its top-level heading so code comments and `customization-files.md` keep matching titles.
5. ~~Each spec includes: purpose, SHALL/MUST requirements, WHEN/THEN scenarios~~ (done, validated with `openspec validate --specs` → 5/5 passed).
6. ~~Delete `customizations/widp/customization-specs.md`~~ (done 2026-04-15 — `openspec/specs/` is now the single functional source of truth. References in `customization-files.md`, `upgrade-validation-checklist.md`, and this strategy were updated accordingly). This is a WIDP-specific deviation from the previous shared onboarding model; the change was promoted back to the shared `onboarding-fork-guide.md`, `new-fork.md`, `README.md`, templates, `conflict-rules.md`, `upgrade-plan-client-forks.md`, and `scripts/check_upgrade_docs.py` as part of this same phase.
7. ~~Align code comment titles and the technical inventory with the canonical OpenSpec titles~~ (done — 9 non-canonical `// EyeSeeTea customization - …` comments in `settings.gradle.kts`, `commons/build.gradle.kts`, `CredentialsScreen.kt`, `CredentialsViewModel.kt`, `login/.../strings.xml` renamed to `2FA support`; `customization-files.md` section `### 2.3 Notifications system (includes translations)` renamed to `### 2.3 Notifications system`). `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp` passes.

### Phase C: Claude Code tooling

Status: `completed` (2026-04-16)

Approach: minimalist setup from scratch (deliberately not copied from Sports PR #306, to compare approaches in the coding dojo). Only 2 new files — no custom agents, no additional skills, no new commands.

Steps:
1. ~~Create `CLAUDE.md` at repo root~~ (done — project identity, module structure, build commands, customization table, placement rules with Oslo-minimization principle, documentation index, upgrade context, automation extraction rule)
2. ~~Create `.claude/settings.json`~~ (done — shared permissions for gradle, openspec, git read-only, config files)
3. ~~Decided NOT to create custom agents or skills~~ (justified: no repetitive pattern demonstrated yet; if one emerges during Phase D/E, extract then)
4. ~~OpenSpec commands and skills already existed from Phase B~~ (4 commands + 4 skills)
5. ~~Added automation extraction rule to CLAUDE.md~~ (Claude will proactively suggest creating agents/skills when it detects 3+ repetitions of the same task structure)

### Phase D: Upgrade execution

Status: `completed` (2026-04-17)

Steps:
1. ~~Create the upgrade proposal as an OpenSpec change~~ (done — `openspec/changes/upgrade-widp-to-3-3-1/` with proposal, design, tasks, delta spec)
2. ~~Create `eyeseetea-docs/upgrade/widp/upgrade-3.3.1-notes.md` from template~~ (done — updated post-merge with automerge casualties, follow-ups, new rules)
3. ~~Merge `develop-eyeseetea` into this branch~~ (done — revert-the-revert `git revert 7389d1043`, commit `1af395c30`)
4. ~~Classify conflicts using `conflict-rules.md`~~ (done — 20 `accept_ours` + 10 `manual_reapply_on_theirs`)
5. ~~Resolve easy conflicts first~~ (done)
6. ~~Pause for developer review after the easy batch~~ (done)
7. ~~Resolve manual conflicts by reapplying minimum client-specific logic~~ (done — 8 build iterations recovered ~20 files silently dropped by automerge; captured as **Automerge verification rule** and **Post-merge fork identity check** in `conflict-rules.md`; build 10 passed)
8. ~~Fix URL data element rendering (#5)~~ (done — reimplemented in `FieldUiModelExtensions.supportingText()` appending URL to description, spec updated, status `active`)
9. ~~Fix SMS 2FA string typo (#4)~~ (done — `sms_two_factor_code_sent` in `commonskmm/.../values/strings.xml` corrected)
10. ~~Clean up PSI leftovers~~ (done — verified already clean; residual references only in `.idea/` which is gitignored)
11. ~~Update customization-files.md with confirmed surviving customizations~~ (done — added DI wiring, preferences layer, SDK wiring, and marked #5 as active)

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

### Phase F: Promote tooling to develop-eyeseetea

Status: `pending`

Goal: carry the infrastructure back to `develop-eyeseetea` so that any new fork starts with everything ready — no manual OpenSpec install or Claude setup needed. **No client-specific specs or changes should go to the baseline.**

Steps:
1. Decide exactly what to promote (candidates listed below, final decision at execution time):
   - `eyeseetea-docs/` — shared docs, templates, scripts, onboarding guide updated with OpenSpec phases
   - `openspec/` — CLI scaffold (`config.yaml` with generic context, empty `specs/`, empty `changes/archive/`). No WIDP specs.
   - `.claude/` — CLAUDE.md (generic EyeSeeTea baseline, no WIDP identity), commands, skills, settings. No WIDP-specific agents or rules.
   - `.gitignore` updates (`.claude/settings.local.json`, etc.)
2. Review each file for client-specific references and strip them (WIDP paths, WIDP flavor mentions, WIDP customization titles)
3. Cherry-pick or create a clean commit onto `develop-eyeseetea`
4. Verify that a fresh fork from `develop-eyeseetea` can run `openspec validate` and start a Claude session with working CLAUDE.md without errors
5. Verify no conflict is introduced for existing forks that merge `develop-eyeseetea`

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
