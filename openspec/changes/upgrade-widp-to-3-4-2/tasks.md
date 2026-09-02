# Tasks — WIDP upgrade to 3.4.2

Phases follow `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md`.

## 1. Prepare (runbook Phase 1-2)

- [x] 1.1 Preserve the earlier 3.4.1 attempt on a local branch
- [x] 1.2 Correct the fork-model rule in `.claude/CLAUDE.md` that caused the wrong merge
- [x] 1.3 Cut `feature-widp/bring_last_changes_3_4_2` from `develop-widp`
- [x] 1.4 Create this OpenSpec change (proposal, design, tasks)
- [x] 1.5 Create `eyeseetea-docs/upgrade/widp/upgrade-3.4.2-notes.md` from the template
- [x] 1.6 `openspec validate --all --strict` passes
- [ ] 1.7 Confirm `origin/develop-eyeseetea` is the intended baseline: `f87bec8c3`, `3.4.2-eyeseetea-fork-1`, SDK `1.14.2-eyeseetea-fork-1`
- [ ] 1.8 Merge `origin/develop-eyeseetea` — **stop at the conflict list, resolve nothing yet**

## 2. Classify (runbook Phase 3)

- [ ] 2.1 List every conflicted file and assign one of the four categories from `conflict-rules.md`
- [ ] 2.2 Cross-check the list against design.md D3; explain any file that appears in one and not the other
- [ ] 2.3 List baseline changes that applied **cleanly** to files in `customization-files.md` — these are the silent-deletion candidates
- [ ] 2.4 Record the classification table in `upgrade-3.4.2-notes.md`
- [ ] 2.5 **Developer review checkpoint** — do not resolve before this is approved

## 3. Resolve the easy batch (runbook Phase 4)

- [ ] 3.1 Apply `accept_theirs` to the 9 baseline/Oslo-owned files in D3
- [ ] 3.2 Apply `accept_ours` to flavor-isolated files, if any conflict at all
- [ ] 3.3 Restore `app/src/eyeseetea/.../GranularSyncModule.kt` to the baseline version at the correct package path `app/src/eyeseetea/java/org/dhis2/utils/granularsync/`
- [ ] 3.4 **Developer review checkpoint** before manual conflicts

## 4. Resolve manual conflicts (runbook Phase 5), by customization

Each sub-task ends with the exact `// EyeSeeTea customization - <title>` comment matching the spec's `#` heading.

- [ ] 4.1 **Two-factor authentication** — the 8 `login/` files
- [ ] 4.2 **Two-factor authentication** — `D2ErrorMessageProviderImpl.kt` and `commonskmm` strings. Guard: the file must keep 6 distinct real 2FA strings, not `defaultError()`
- [ ] 4.3 **Notifications system** — Koin DI graph (`NotificationsKoinModule.kt`, `ActivityGlobalAbstract`, `KoinInitialization`), replicated from the earlier attempt
- [ ] 4.4 **Notifications system** — register the download in `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt` (design D5)
- [ ] 4.5 **Notifications system** — replicate the `refresh()` pending-flag fix from the earlier attempt; confirm whether the rest of it is superseded by 4.4
- [ ] 4.6 **Change Server URL** — `PreferenceConstants.kt`, `App`, `UserComponent`, `strings.xml`
- [ ] 4.7 **URL data element field** — `EnrollmentRepository.kt`, `EventRepository.kt`
- [ ] 4.8 **Image upload without resizing** — confirm the customization's current state; it has no written requirement and only 1 of its 3 original commits ever reached WIDP
- [ ] 4.9 Fork identity: version `3.4.2-widp-fork-1`, SDK `1.14.2-eyeseetea-fork-1`, `app/build.gradle.kts` flavor block, CI workflow
- [ ] 4.10 `CLAUDE.md` ownership migration: Oslo's file verbatim + new `AGENTS-widp.md` from `eyeseetea-docs/templates/AGENTS-CLIENT.md.template`

## 5. Build and toolchain (runbook Phase 6, part 1)

- [ ] 5.1 `./gradlew assembleWidpDebug`
- [ ] 5.2 `./gradlew :app:compileEyeseeteaDebugKotlin` — **the gate the failed branch never ran**
- [ ] 5.3 Resolve Gradle 9.3.1 / Kotlin 2.3.20 fallout (`defer_after_build_verification`)
- [ ] 5.4 `./gradlew ktlintCheck`

## 6. Verify nothing was silently lost (runbook Phase 7)

- [ ] 6.1 Two-dot `git diff origin/develop-eyeseetea..HEAD` over **all 89 files** in `customization-files.md`
- [ ] 6.2 Confirm `gradle.properties` still carries `-Xmx8g -XX:MaxMetaspaceSize=1g`
- [ ] 6.3 List files where WIDP now matches upstream byte for byte; every one needs a recorded reason
- [ ] 6.4 Confirm no file under `eyeseetea-docs/customizations/eyeseetea/` was modified from this branch
- [ ] 6.5 Confirm `Shared drift still differing` is empty or every entry has a reason and a next action

## 7. Tests (runbook Phase 6, part 2)

Priority by risk: notifications > change-server-url > 2FA > image-upload > url-data-element.

- [ ] 7.1 `./gradlew testWidpDebugUnitTest`
- [ ] 7.2 `./gradlew :login:allTests` — the 2FA tests do not run in `testWidpDebugUnitTest`
- [ ] 7.3 Port `NotificationsModuleTest` and `NotificationsPresenterTest` from the earlier attempt
- [ ] 7.4 Add a test for the `PostMetadataSyncAction` registration (4.4)
- [ ] 7.5 Record the pre-existing red `FormValueStoreTest > Should try to resize image` and decide whether it belongs to 4.8

## 8. Manual validation

Every entry below has a matching flow in `eyeseetea-docs/upgrade/widp/upgrade-validation-checklist.md`.

- [ ] 8.1 Update the checklist for the 3.4.2 changes, replacing the 3.4.1 sections about the data-sync trigger
- [ ] 8.2 Notifications end-to-end, including the single-program profile and a background sync with the app closed (now expected to work again)
- [ ] 8.3 Change Server URL
- [ ] 8.4 2FA: TOTP, Email, SMS, and the seven error messages
- [ ] 8.5 Image upload
- [ ] 8.6 URL data element
- [ ] 8.7 Login and event creation against DHIS2 2.41 and 2.43
- [ ] 8.8 Record results in `upgrade-3.4.2-notes.md`

## 9. Close

- [ ] 9.1 Update `eyeseetea-docs/customizations/widp/customization-files.md`
- [ ] 9.2 Update `openspec/specs/*` only if behavior actually changed (none expected — see proposal)
- [ ] 9.3 `openspec validate --all --strict` and `eyeseetea-docs/scripts/check_upgrade_docs.py --client widp`
- [ ] 9.4 Open the PR against `develop-widp`
- [ ] 9.5 `/opsx:archive` once merged

## Carried over from the earlier 3.4.1 attempt

| Work | Decision |
|---|---|
| notifications DI Dagger → Koin | replicate — task 4.3 |
| notification display fixes | replicate partially — task 4.5 |
| Oslo patch ANDROAPP-7666 | **drop** — verified already in the baseline |
| `gradle.properties` heap setting | **drop** — no conflict via the baseline; guarded by 6.2 |
| documentation | rewrite for 3.4.2, not cherry-picked |
