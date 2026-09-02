## Context

WIDP moves from `3.3.1-widp-fork-1` to the `develop-eyeseetea` head (`f87bec8c3`, `3.4.2-eyeseetea-fork-1`). Merge-base with the baseline is `8a4866305`.

Conflict classification follows the four categories in `eyeseetea-docs/upgrade/conflict-rules.md`: `accept_ours`, `accept_theirs`, `manual_reapply_on_theirs`, `defer_after_build_verification`. Execution follows `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md`.

### Why this is done from the baseline

An earlier attempt merged Oslo directly. Three defects traced to that single decision, and all three were **silent** — no merge conflict, no compile error, no failing test:

1. `gradle.properties` reverted to `-Xmx4096M`, dropping the fork's `-Xmx8g -XX:MaxMetaspaceSize=1g`. Via the baseline there is no conflict at all: merge-base, `develop-widp` and `develop-eyeseetea` all carry the same value.
2. The `eyeseetea` flavor stopped compiling (`:app:kspEyeseeteaDebugKotlin FAILED`): `GranularSyncModule.kt` stayed pre-3.4.1 and kept a malformed package path. Nothing in CI compiles that flavor.
3. The shared fork docs went stale, including `conflict-rules.md` itself — whose missing step 4 is what would have prevented the wrong merge.

## Decisions

### D1. Merge the baseline, never Oslo

`git merge origin/develop-eyeseetea` into a branch cut from `develop-widp`. The baseline is not another client fork: its own customizations are flavor-isolated under `app/src/eyeseetea/`, and the issue requires this branch to carry that flavor regardless.

### D2. Target 3.4.2, not 3.4.1

`PostMetadataSyncAction` was promoted to baseline in `4e5635da5`, after the `3.4.1-eyeseetea-fork-1` tag. Merging the tag would leave the notifications trigger without its intended mechanism and require a second upgrade.

### D3. Conflict classification of the 37-file overlap

`accept_theirs` — baseline or Oslo owns the file; verified to carry no WIDP customization:

| File | Reason |
|---|---|
| `eyeseetea-docs/upgrade/conflict-rules.md` | baseline-owned; editing it from a client fork is forbidden |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | baseline-owned, another flavor's inventory |
| `app/src/main/.../searchTrackEntity/SearchTEIViewModel.kt` | verified byte-identical to the baseline |
| `app/src/main/.../searchTrackEntity/listView/SearchTEList.kt` | idem |
| `app/src/test/.../SearchTEIViewModelTest.kt` | idem |
| `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` | idem |
| `app/src/main/java/org/dhis2/data/service/SyncGranularRxModule.kt` | customization absorbed upstream; recorded as deliberate |
| `login/build.gradle.kts` | idem |
| `.gitignore` | tooling only |

`manual_reapply_on_theirs` — take the baseline version, then reapply the minimum client behavior:

| File(s) | Customization |
|---|---|
| `login/` — `CredentialsScreen.kt`, `CredentialsUiState.kt`, `CredentialsViewModel.kt`, `CredentialsViewModelTest.kt`, `LoginUser.kt`, `LoginRepository.kt`, `LoginRepositoryImpl.kt`, `composeResources/values/strings.xml` | Two-factor authentication |
| `commonskmm/.../D2ErrorMessageProviderImpl.kt`, `commonskmm/src/commonMain/composeResources/values/strings.xml` | Two-factor authentication |
| `commonskmm/.../providers/PreferenceConstants.kt` | Change Server URL |
| `app/src/main/java/org/dhis2/App.java`, `data/user/UserComponent.java`, `res/values/strings.xml` | Notifications system, Change Server URL |
| `usescases/general/ActivityGlobalAbstract.java`, `usescases/main/{MainActivity.kt,MainPresenter.kt,MainView.kt}` | Notifications system |
| `data/service/{SyncDataWorkerModule,SyncInitWorkerModule,SyncMetadataWorkerModule,SyncPresenterImpl}.kt` | Notifications system — expected to be **absorbed** by D5 |
| `form/.../{EnrollmentRepository,EventRepository}.kt` | URL data element field |
| `app/build.gradle.kts`, `gradle/libs.versions.toml`, `.github/workflows/eyeseetea-main.yml` | fork identity, version, SDK, CI |

`defer_after_build_verification`: the Gradle `8.13` → `9.3.1` and Kotlin `2.2.21` → `2.3.20` fallout. Do not pre-emptively edit build scripts; fix what the build actually reports.

### D4. Pre-registered silent traps

These destroy behavior without any signal. Each is checked explicitly, not opportunistically.

| Trap | Why it is silent | Guard |
|---|---|---|
| `D2ErrorMessageProviderImpl.kt` maps all seven `D2ErrorCode.*TWO_FACTOR*` values to `defaultError()` in the baseline | compiles, passes CI, and every 2FA message silently becomes "unexpected error" | count the 2FA branches after resolving; the WIDP file must keep 6 distinct real strings |
| Automerge can delete customization wiring with no conflict markers | git applies a baseline deletion cleanly | two-dot `git diff origin/develop-eyeseetea..HEAD` over **all 89 files** in `customization-files.md` |
| `eyeseetea` flavor breakage | nothing in CI compiles it | `./gradlew :app:compileEyeseeteaDebugKotlin` is a required gate |
| `CLAUDE.md` | an ownership change looks like a content conflict | take Oslo's file verbatim, move WIDP content to `AGENTS-widp.md` |

### D5. Notifications download moves to `PostMetadataSyncAction`

Until 3.4.0 the download ran in `SyncPresenterImpl.syncMetadata().doOnComplete {}`. Upstream moved sync into the `:sync` module, which cannot depend on `:app`. The baseline provides the replacement: a `PostMetadataSyncAction` fun-interface in `commonskmm`, invoked by `SyncMetadata.runPostMetadataSyncActions()`, registered per flavor through a `PostMetadataSyncModule.kt` that `KoinInitialization` loads unconditionally.

WIDP registers its action in a **new file in the flavor source set** — `app/src/widp/java/org/dhis2/di/PostMetadataSyncModule.kt`. This is level 1 of the placement hierarchy: zero Oslo files touched, zero future conflict surface. It also restores the behavior the spec requires ("during metadata sync"), independent of any Activity or ViewModel lifecycle, and covers login, manual sync, and foreground and background periodic sync.

The file is mandatory in every flavor source set, so the baseline's own flavors already provide theirs.

### D6. Notifications DI graph moves to Koin

Upstream migrated `MainActivity` to Koin and dropped the Dagger `inject()` that populated the inherited `notificationsPresenter`, which crashes with an NPE on entering the main screen. The graph is published as a Koin module (`NotificationsKoinModule.kt`, a new shared file) and `ActivityGlobalAbstract` resolves it through a getter. This work is replicated from the earlier 3.4.1 attempt, where it was verified on device.

### SDK fork dependency

`EyeSeeTea/dhis2-android-sdk` moves `1.13.1-eyeseetea-fork-3` → `1.14.2-eyeseetea-fork-1`. The 2FA capability depends on this fork for `D2ErrorCode` values (`INCORRECT_TWO_FACTOR_CODE{,_TOTP,_EMAIL,_SMS}`, `EMAIL_TWO_FACTOR_CODE_SENT`, `SMS_TWO_FACTOR_CODE_SENT`, `TWO_FACTOR_MANY_SEND_ATTEMPTS`), `LogInCall`, and `LoginPayload`. The version comes from the baseline; it is not chosen here.

Known and out of scope: the SDK's `LogInCall.generate2FAErrorIfRequired()` returns `null` for `REQUIRES_TWO_FACTOR_ENROLMENT`, so a user who must enrol in 2FA but has not gets a crash instead of a message. Unchanged in `1.14.2`. Tracked separately; no SDK patch in this change.

## Risks

| Risk | Mitigation |
|---|---|
| A 2FA regression passes every automated gate | D4 guard plus the full manual flow in the validation checklist, section 4 |
| Gradle 9 / Kotlin 2.3 fallout blocks the merge | `defer_after_build_verification`; resolve behavior first, toolchain second |
| Customization wiring lost to a clean automerge | D4 two-dot diff over the full 89-file inventory |
| Work from the earlier attempt is lost | that branch is preserved locally; the fixes worth keeping are listed in tasks.md |
