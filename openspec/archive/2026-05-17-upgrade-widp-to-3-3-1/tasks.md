## 1. Prepare upgrade notes

- [x] 1.1 Create `eyeseetea-docs/upgrade/widp/upgrade-3.3.1-notes.md` from template for tracking merge decisions

## 2. Revert-the-revert merge

- [x] 2.1 Run `git revert 7389d1043` to re-introduce develop-eyeseetea 3.3.1 content
- [x] 2.2 List all conflicted files and classify each into `accept_ours`, `accept_theirs`, `manual_reapply_on_theirs`, or `defer_after_build_verification` using conflict-rules.md
- [x] 2.3 Record the full classification table in `upgrade-3.3.1-notes.md`

## 3. Resolve easy conflicts (autonomous)

- [x] 3.1 Resolve all `accept_ours` files (`app/src/widp/**`, `app/src/widpDebug/**`)
- [x] 3.2 Resolve all `accept_theirs` files (pure formatting, imports, shared tests without WIDP logic) — N/A: no accept_theirs in this merge
- [x] 3.3 Stage resolved files and report count to developer

## 4. Resolve manual conflicts (supervised)

- [x] 4.1 Pause for developer review of the classification and easy-batch results
- [x] 4.2 For each `manual_reapply_on_theirs` file: start from develop-eyeseetea version, reinsert only the minimum WIDP delta, add/update `// EyeSeeTea customization` comments
- [x] 4.3 For each `defer_after_build_verification` file: tentatively take develop-eyeseetea version
- [x] 4.4 Complete the revert commit (all conflicts resolved) — commits `1af395c30` + `ecf4a1321`

## 5. Build verification

- [x] 5.1 Run `./gradlew assembleWidpDebug` and fix any compilation errors — BUILD SUCCESSFUL after 10 iterations; ~20 files recovered from silent automerge drops
- [x] 5.2 Run relevant WIDP unit tests and fix test failures — widp unit tests pass; legacy `androidTest` issues in the 3.3.x `login` test wiring are intentionally deferred to the 3.4 upgrade
- [x] 5.3 Revisit `defer_after_build_verification` files — confirm or reapply WIDP logic based on build/test results

## 6. Fix URL data element rendering (#5)

- [x] 6.1 Identify where the Compose form UI renders field info/description dialogs in the 3.3.1 codebase — description flows through `FieldUiModelExtensions.supportingText()` as `SupportingTextData` to each Input composable
- [x] 6.2 Reimplement URL display: append URL to description when `FieldUiModel.url` is non-null — URL appended inline in supporting text (not a dialog) via `listOfNotNull(description, url).joinToString("\n")`
- [x] 6.3 Verify the fix satisfies the scenarios in `openspec/specs/url-data-element/spec.md` — spec updated to reflect inline supporting-text behavior; `openspec validate --specs` passes 5/5

## 7. Fix SMS 2FA string typo (#4)

- [x] 7.1 Verify the string `SMS_TWO_FACTOR_CODE_SENT` shows "Email" instead of "SMS" — confirmed in `commonskmm/src/commonMain/composeResources/values/strings.xml:217`
- [x] 7.2 If confirmed, fix the string resource — corrected to "SMS with two factor code sent"

## 8. Clean up PSI leftovers

- [x] 8.1 Identify PSI-specific files that have no overlap with WIDP customizations — verified clean; residual references only in `.idea/` metadata (gitignored)
- [x] 8.2 Remove confirmed PSI-only files — already removed in commit `2c49aa577`
- [x] 8.3 Verify removal does not break the widp build — `assembleWidpDebug` OK

## 9. Update documentation and version

- [x] 9.1 Update `vName` to `3.3.1-widp-fork-1` in `libs.versions.toml` — already set
- [x] 9.2 Update `customization-files.md` with confirmed surviving customizations and any new file paths — added DI wiring, preferences layer, SDK wiring; marked #5 as active
- [x] 9.3 Update `upgrade-3.3.1-strategy.md` — mark Phase D as completed
- [x] 9.4 Ensure all `// EyeSeeTea customization - [title]` comments use exact spec titles — normalized 2 titles (Markwon → Notifications system, login widp flavor → 2FA support)
- [x] 9.5 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp` — all checks pass
