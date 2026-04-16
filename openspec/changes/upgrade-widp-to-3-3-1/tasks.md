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

- [ ] 4.1 Pause for developer review of the classification and easy-batch results
- [ ] 4.2 For each `manual_reapply_on_theirs` file: start from develop-eyeseetea version, reinsert only the minimum WIDP delta, add/update `// EyeSeeTea customization` comments
- [ ] 4.3 For each `defer_after_build_verification` file: tentatively take develop-eyeseetea version
- [ ] 4.4 Complete the revert commit (all conflicts resolved)

## 5. Build verification

- [ ] 5.1 Run `./gradlew assembleWidpDebug` and fix any compilation errors
- [ ] 5.2 Run `./gradlew testDebugUnitTest` and fix test failures
- [ ] 5.3 Revisit `defer_after_build_verification` files — confirm or reapply WIDP logic based on build/test results

## 6. Fix URL data element rendering (#5)

- [ ] 6.1 Identify where the Compose form UI renders field info/description dialogs in the 3.3.1 codebase
- [ ] 6.2 Reimplement URL display: append URL to description when `FieldUiModel.url` is non-null, make it tappable
- [ ] 6.3 Verify the fix satisfies the scenarios in `openspec/specs/url-data-element/spec.md`

## 7. Fix SMS 2FA string typo (#4)

- [ ] 7.1 Verify the string `SMS_TWO_FACTOR_CODE_SENT` shows "Email" instead of "SMS"
- [ ] 7.2 If confirmed, fix the string resource

## 8. Clean up PSI leftovers

- [ ] 8.1 Identify PSI-specific files that have no overlap with WIDP customizations
- [ ] 8.2 Remove confirmed PSI-only files
- [ ] 8.3 Verify removal does not break the widp build

## 9. Update documentation and version

- [ ] 9.1 Update `vName` to `3.3.1-widp-fork-1` in `libs.versions.toml`
- [ ] 9.2 Update `customization-files.md` with confirmed surviving customizations and any new file paths
- [ ] 9.3 Update `upgrade-3.3.1-strategy.md` — mark Phase D as completed
- [ ] 9.4 Ensure all `// EyeSeeTea customization - [title]` comments use exact spec titles
- [ ] 9.5 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client widp` — all checks pass
