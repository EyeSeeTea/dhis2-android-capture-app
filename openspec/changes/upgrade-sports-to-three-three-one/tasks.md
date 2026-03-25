## 1. Phase 1 — Bring Shared Docs Into Sports Branch [SPORTS]

- [ ] 1.1 [BUILD] Cherry-pick or copy `eyeseetea-docs/` shared structure from `develop-eyeseetea`: README, SDK_Setup, new-fork.md, onboarding-fork-guide.md, upgrade/upgrade-plan-client-forks.md, upgrade/conflict-rules.md, upgrade/template/, customizations/eyeseetea/, customizations/template/, scripts/
- [ ] 1.2 [BUILD] Verify shared docs and templates are present in the working branch

## 2. Phase 2 — Create Sports Client Documentation [SPORTS]

- [ ] 2.1 [BE] Copy `customization-specs-template.md` to `eyeseetea-docs/customizations/sports/customization-specs.md`
- [ ] 2.2 [BE] Copy `customization-files-template.md` to `eyeseetea-docs/customizations/sports/customization-files.md`
- [ ] 2.3 [BE] Copy `upgrade-validation-checklist-template.md` to `eyeseetea-docs/upgrade/sports/upgrade-validation-checklist.md`
- [ ] 2.4 [BE] Fill mandatory headers in all three files (client: sports, flavor: sports, base branch: develop-eyeseetea, base commit, date)

## 3. Phase 3 — Inventory Sports Customizations [SPORTS]

- [ ] 3.1 [BE] Run `git diff develop-eyeseetea..develop-sports --stat` and classify all differing files as: flavor-specific, shared-code customization, technical drift, or absorbed
- [ ] 3.2 [BE] Separate flavor files (`app/src/sports/**`) from shared-code diffs — flavor files are auto-classified
- [ ] 3.3 [BE] Analyze sports flavor code: `CustomizableConstants.kt`, `UserComponentFlavor.kt`, `eventCaptureRepositoryFunctions.kt`, `GranularSyncModule.kt` — document functional intent
- [ ] 3.4 [BE] Populate `customization-specs.md` with functional titles, intent, expected behavior, and lifecycle status (`active`/`needs_validation`/`absorbed`/`removed`)
- [ ] 3.5 [BE] Populate `customization-files.md` with technical inventory grouped by customization title
- [ ] 3.6 [BE] Populate `upgrade-validation-checklist.md` with manual validation flows per active customization
- [ ] 3.7 [BE] List unclassified diffs (files differing without confirmed customization title) in section 3 of `customization-files.md`
- [ ] 3.8 [BE] **Developer review checkpoint**: confirm customization titles and statuses before proceeding

## 4. Phase 4 — OpenSpec Formalization [SPORTS]

- [ ] 4.1 [BE] Create `openspec/specs/<slug>/spec.md` for each active sports customization with MUST/SHALL requirements and Given-When-Then scenarios
- [ ] 4.2 [BE] Verify this change (`upgrade-sports-to-three-three-one`) references the inventory and conflict rules

## 5. Phase 5 — Claude Code Tooling Verification [SPORTS]

- [ ] 5.1 [BUILD] Verify `.claude/CLAUDE.md` references `eyeseetea-docs/` paths (shared docs, conflict-rules, client specs)
- [ ] 5.2 [BUILD] Verify PR #306 setup is compatible with the eyeseetea-docs structure

## 6. Phase 6 — Execute the Upgrade [SPORTS]

- [ ] 6.1 [BUILD] Create feature branch `feature-sports/upgrade-to-three-three-one` from `develop-sports`
- [ ] 6.2 [BUILD] Fetch latest `origin/develop-eyeseetea` and run `git merge origin/develop-eyeseetea`
- [ ] 6.3 [BUILD] Create `eyeseetea-docs/upgrade/sports/upgrade-three-three-one-notes.md` with preclassification table (file, classification, expected delta, customization title, status)
- [ ] 6.4 [BUILD] Classify every conflicted file per `conflict-rules.md` categories before editing any file
- [ ] 6.5 [BUILD] Resolve easy `accept_ours` conflicts: `app/src/sports/**`, `app/src/sportsDebug/**`
- [ ] 6.6 [BUILD] Resolve easy `accept_theirs` conflicts: removed code (notifications, ChangeServerURL, BasicPreference), pure upstream evolution, formatting-only
- [ ] 6.7 [BE] **Developer review checkpoint**: present easy batch results before manual conflicts
- [ ] 6.8 [BE] Resolve `manual_reapply_on_theirs` conflicts: start from `develop-eyeseetea`, reinsert only minimum sports-specific logic
- [ ] 6.9 [BE] For Java→Kotlin migrations: reimplement sports customization in the active Kotlin file, not the old Java file
- [ ] 6.10 [BE] Add `// EyeSeeTea customization - [title]` comments to all surviving shared-code customizations
- [ ] 6.11 [BE] Resolve `defer_after_build_verification` files tentatively as `develop-eyeseetea`, mark for verification
- [ ] 6.12 [BUILD] Update `customization-files.md` with confirmed surviving customizations only
- [ ] 6.13 [BUILD] Remove any leftover references to PSI/WIDP/simprints flavors

## 7. Phase 6 (cont.) — Build & Test Validation [SPORTS]

- [ ] 7.1 [BUILD] Build sports debug APK: `./gradlew :app:assembleSportsDebug`
- [ ] 7.2 [BUILD] Build eyeseetea debug APK (verify correct task name)
- [ ] 7.3 [BUILD] Build default debug APK: `./gradlew :app:assembleDhis2Debug`
- [ ] 7.4 [BUILD] Fix any compilation errors
- [ ] 7.5 [BE] Run unit tests: `./gradlew test` — fix failures
- [ ] 7.6 [BUILD] Run lint: `./gradlew ktlintCheck` — fix new violations
- [ ] 7.7 [BE] Verify `defer_after_build_verification` files: reintroduce sports code only if behavior is missing

## 8. Phase 7 — Add Tests for Sports Customizations [SPORTS]

- [ ] 8.1 [BE] For each active customization in `customization-specs.md`, create at least one targeted unit test validating the specific behavior
- [ ] 8.2 [BE] Use Given-When-Then scenarios from specs/checklist as test specification
- [ ] 8.3 [BE] Prioritize customizations with largest code surface and those touching shared code
- [ ] 8.4 [BE] Run tests and verify they pass: `./gradlew test`

## 9. Phase 8 — Cleanup [SPORTS]

- [ ] 9.1 [BE] Remove files belonging to absorbed or removed customizations
- [ ] 9.2 [BE] Remove leftover files from previous forks (WIDP/PSI artifacts) not part of any active customization
- [ ] 9.3 [BE] Confirm section 3 of `customization-files.md` is empty or contains only items with explicit reason and next action
- [ ] 9.4 [BE] Ensure code comments use exact titles from `customization-specs.md`
- [ ] 9.5 [BUILD] Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client sports` to validate consistency
- [ ] 9.6 [BE] Verify no unexplained shared drift remains: `git diff develop-eyeseetea..HEAD -- app/src/main/ commons/ form/ tracker/ aggregates/`

## 10. Finalize [SPORTS]

- [ ] 10.1 [BUILD] Commit merge resolution: `feat(sports): upgrade to v3.3.1 via develop-eyeseetea merge`
- [ ] 10.2 [BUILD] Push feature branch and create PR targeting `develop-sports`
- [ ] 10.3 [BE] Update ClickUp task [869cm2606](https://app.clickup.com/t/869cm2606) status
- [ ] 10.4 [BE] Update upgrade-notes with final summary: resolved by theirs, resolved by ours, manually merged, still uncertain
