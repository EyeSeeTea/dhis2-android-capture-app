# Tasks — disabled-account login presentation

## 1. Confirm the SDK prerequisite

- [x] 1.1 Verify that the sibling SDK OpenSpec change `handle-disabled-account-login` is complete
      and that its focused and full `core` verification passed.
- [x] 1.2 Record the final published EyeSeeTea SDK version
      `febe27964b3a8a330d8e14c27b01a25cb44b6b91` that exposes only
      `D2ErrorCode.USER_ACCOUNT_DISABLED`, stops before `/api/me` and does not emit an account
      deletion event for a rejected login, while preserving local account data.

Verification evidence: the sibling SDK OpenSpec validates, focused and full `core` tests plus
static/API checks passed, and JitPack published
`com.github.EyeSeeTea:dhis2-android-sdk:febe27964b3a8a330d8e14c27b01a25cb44b6b91`
with `BUILD SUCCESSFUL` on 2026-09-23.

**Commit:** none. The implementation, tests and release work belong exclusively to the SDK
repository. This group only gates app integration on a published SDK artifact.

## 2. Make global disabled-session navigation context-aware

- [x] 2.1 Add a failing regression test at the closest testable session-navigation boundary proving
      that a login screen does not register or execute a redirect back to itself for a disabled
      account event.
- [x] 2.2 Scope `OpenIdSession` callback registration in `SessionManagerActivity` so
      `LoginActivity` is excluded, using the minimal shared-code edit and the marker
      `// EyeSeeTea customization - Disabled account login`.
- [x] 2.3 Add or extend coverage proving an authenticated activity still handles
      `AccountDeletionReason.ACCOUNT_DISABLED` by navigating to login with the disabled-account
      reason.
- [x] 2.4 Only if 2.3 demonstrates intent reuse, update `LoginActivity.onNewIntent()` to process the
      new disabled-account extra and add a regression test. Otherwise leave it unchanged and record
      that no edit was required.

Task 2.4 required no production edit: authenticated activities retain the existing callback that
starts `LoginActivity` with `DISABLED_ACCOUNT`, while `LoginActivity` itself no longer registers
that callback, so this flow does not reuse the active login instance through self-navigation.

**Commit (app repository):** 2.1–2.4 are one red-to-green commit (`fix(login):`). The production
guard and the tests that justify its exact scope must remain together.

## 3. Consume the finalized SDK and remove temporary mappings

- [x] 3.1 Replace `fix~account-disabled-login-SNAPSHOT` in `gradle/libs.versions.toml` with the final
      published EyeSeeTea SDK coordinate
      `febe27964b3a8a330d8e14c27b01a25cb44b6b91` containing the disabled-login fix.
- [x] 3.2 Remove `D2ErrorCode.ACCOUNT_DISABLED` from `D2ErrorMessageProviderImpl` and
      `DomainErrorMapper`; retain the existing `USER_ACCOUNT_DISABLED` mapping and localized
      `error_account_disabled` text.
- [x] 3.3 Add or update focused mapping coverage proving `USER_ACCOUNT_DISABLED` produces the
      disabled-account message. Do not add app behavior for the raw protocol string.
- [x] 3.4 Verify that no production app source references `D2ErrorCode.ACCOUNT_DISABLED` and that no
      branch-SNAPSHOT SDK coordinate remains.

**Commit (app repository):** 3.1–3.4 are one dependency-integration commit (`fix(login):`). Keep it
separate from Group 2 so the app control-flow fix and the SDK artifact transition can be reviewed
or reverted independently.

## 4. Verify login presentation and regressions

- [x] 4.1 Extend `CredentialsViewModelTest` to prove that the disabled-account `LoginResult.Error`
      changes `LoginState.Running` back to `LoginState.Enabled` and exposes the expected error
      message without an after-login navigation action.
- [x] 4.2 Review Compose UI coverage for `LoginStatus`. Retain the existing private implementation
      without a dedicated disabled-account test: it renders every `loginErrorMessage` identically,
      while 4.1 proves loading ends and exposes the message. A component test would inject both
      conditions directly and would require widening production visibility only for the test.
- [x] 4.3 Run the focused login tests:
      `./gradlew :login:testAndroidHostTest --tests
      "org.dhis2.mobile.login.main.ui.viewmodel.CredentialsViewModelTest"` plus the app test task
      containing the session-navigation coverage from Group 2.
- [x] 4.4 Run `./gradlew :app:assembleWidpDebug :login:testAndroidHostTest ktlintCheck` and resolve
      only findings introduced by this change.

**Commit (app repository):** 4.1–4.2 are one test commit (`test(login):`) if they add coverage over
already-green behavior. If either test exposes an additional production defect, commit that test
with its minimal fix as a separate red-to-green commit. Tasks 4.3–4.4 are verification only.

## 5. Validate both disabled-account contexts manually

- [x] 5.1 Against PROD-INDIV/UAT, attempt credential login with a disabled account and record that
      exactly one `/api/auth/login` request occurs, no `/api/me` request occurs, the app remains on
      the same login screen, loading ends and the localized disabled-account error is visible.
- [x] 5.2 Verify that accepting/dismissing the login error does not close the app and that another
      credential attempt can be made.
- [x] 5.3 With an existing authenticated/local account, trigger the disabled-account path and verify
      the local session is removed once, the app navigates to login and the account-disabled dialog
      is shown once.
- [x] 5.4 Record the package name, version name, SDK artifact version and results in the WIDP manual
      validation documentation so the evidence is tied to the tested build.

**Commit (app repository):** 5.1–5.4 are one documentation-only commit (`docs(login):`). Do not
commit screenshots containing server URLs, usernames, tokens or other credentials.

## 6. Record the customization and close the change

- [x] 6.1 Add the shared production file and its marker count to
      `eyeseetea-docs/customizations/widp/customization-files.md`; document the finalized SDK
      dependency and behavior.
- [x] 6.2 Add the disabled-account scenarios to
      `eyeseetea-docs/upgrade/widp/upgrade-validation-checklist.md` if Group 5 did not already place
      them there.
- [x] 6.3 Run `openspec validate show-disabled-account-login-error` and the WIDP documentation
      consistency checks.
- [x] 6.4 After implementation and verification are complete, sync/archive the
      `disabled-account-login` capability according to the repository's OpenSpec workflow.

**Commit (app repository):** 6.1–6.3 are one documentation commit (`docs(login):`). Archival in 6.4
is a separate final OpenSpec archive commit.
