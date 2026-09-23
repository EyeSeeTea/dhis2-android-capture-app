# Design — disabled-account login presentation

## Context

The username/password flow has two independent ways to report a disabled account:

1. The login call returns `D2ErrorCode.USER_ACCOUNT_DISABLED`. The existing app pipeline maps it
   to the localized `error_account_disabled` text, converts it to `LoginResult.Error`, places it in
   `CredentialsUiState.errorMessage` and renders it in the login `InfoBar`.
2. Before returning that error, the current EyeSeeTea SDK calls
   `AccountManager.deleteAccountAndEmit(..., ACCOUNT_DISABLED)`. `OpenIdSession` observes the event
   and every `SessionManagerActivity` registers a callback that opens `LoginActivity` with
   `FLAG_ACTIVITY_CLEAR_TOP` and finishes the current activity.

The second path interrupts the first. When `LoginActivity` is already on top, Android can deliver
the new intent to the same instance and the shared navigation helper then finishes that instance.
The ViewModel that was about to render the error is destroyed, so the app appears to close.

The SDK patch is maintained in the sibling `dhis2-android-sdk` repository. At the login response
boundary it must translate the wire status `ACCOUNT_DISABLED` into the existing public error
`USER_ACCOUNT_DISABLED` before credentials are stored or `/api/me` is requested.

This change touches shared Oslo-derived code, so implementation and future upgrades must follow
`eyeseetea-docs/upgrade/conflict-rules.md` and use the smallest possible customization markers.

## Goals / Non-Goals

**Goals:**

- Keep an unsuccessful disabled-account attempt on the current login screen.
- Let the existing login error pipeline display the localized disabled-account message.
- Stop the loading state as soon as the login result is handled.
- Preserve global account removal and redirection when a disabled account is detected from an
  authenticated screen.
- Expose one SDK error contract to the app: `USER_ACCOUNT_DISABLED`.
- Protect the behavior with focused automated tests and a real-server manual check.

**Non-Goals:**

- Changing the visual design of the login `InfoBar`.
- Replacing the existing account-disabled dialog used after an authenticated session is removed.
- Changing successful login, offline login, OpenID Connect or any 2FA state.
- Moving the shared login Activity or session infrastructure into the WIDP source set.
- Retaining `ACCOUNT_DISABLED` as a second app-facing error code.

## Decisions

### D1. Do not register login as a consumer of global session-termination navigation

`LoginActivity` is already the destination of the disabled-account session callback. It must not
subscribe to an event whose only action is navigating to `LoginActivity` and finishing the current
screen. Registration of `OpenIdSession` will therefore be scoped to activities from which that
navigation is meaningful, excluding `LoginActivity`.

This is preferred over checking the Activity type inside the callback because no subscription is
needed on login at all. It prevents both the self-navigation and the callback's ownership from
being replaced by a screen that cannot act on it correctly.

Alternative considered: special-case `LoginActivity` inside the callback and show the
account-disabled dialog there. Rejected because the credential call already returns a precise
error through the login state pipeline. Handling the same failure through a second asynchronous
channel would retain the race and could display both a dialog and an inline error.

### D2. Keep authenticated-session behavior unchanged

Authenticated activities continue subscribing to `AccountDeletionReason.ACCOUNT_DISABLED`. When
that event is emitted for a stored session, the current activity is finished, the task routes to
login and `EXTRA_ACCOUNT_DISABLED` causes the existing account-disabled dialog to be shown.

The scope guard must not filter the event inside `OpenIdSession` globally: doing so would also
disable the valid authenticated-session behavior. The decision belongs at the Activity
registration boundary, where the current UI context is known.

### D3. The normal login error pipeline owns presentation during login

No new ViewModel state or dialog is introduced. `LoginRepositoryImpl` continues translating the
SDK failure into the localized message, `BaseLogin` continues returning `LoginResult.Error`, and
`CredentialsViewModel` continues setting `errorMessage`. Once global navigation no longer destroys
the screen, `LoginStatus` renders the message and `invokeOnCompletion` restores
`LoginState.Enabled`.

The existing `error_account_disabled` resource is reused. Copy changes, if requested by product,
are independent from the control-flow fix.

`DomainErrorMapper` is not on this username/password login path and is not part of the behavioral
fix. Any temporary `ACCOUNT_DISABLED` branches added there or in `D2ErrorMessageProviderImpl` only
to compile against the snapshot SDK must be removed once the SDK exposes the unified contract.

### D4. Normalize the protocol status inside the SDK

The DHIS2 server sends the string `ACCOUNT_DISABLED` in `LoginResponse.loginStatus`; applications
already understand the SDK-domain error `USER_ACCOUNT_DISABLED`. The finalized SDK patch will:

- compare the response against an internal protocol constant for `ACCOUNT_DISABLED`;
- throw `D2Error` with `D2ErrorCode.USER_ACCOUNT_DISABLED`;
- stop before credential persistence and `/api/me`;
- avoid adding `ACCOUNT_DISABLED` as a second public `D2ErrorCode` that every downstream exhaustive
  mapping must handle.

Patch surface in the SDK fork:

- `core/.../user/internal/LogInCall.kt` — response-status classification and early termination;
- `core/.../maintenance/D2ErrorCode.java` and `core/api/core.api` — remove the temporary public
  protocol enum value if it has not been released;
- `core/.../user/internal/LogInCallUnitShould.kt` — regression contract.

The SDK should also stop emitting a global account-deletion event when no stored account/session
exists for a failed first login. That semantic correction removes the event at its source. The app
guard from D1 remains worthwhile defensive scoping: a destination screen must never navigate to
and finish itself because of a global session event.

### D5. Handle repeated intents defensively only if a reproducible authenticated path needs it

`LoginActivity.onNewIntent()` currently stores the new intent without rerunning `checkMessage()`.
The normal authenticated-session path is expected to create a login destination because login was
finished when the authenticated area was opened. No edit to `onNewIntent()` is planned unless a
test demonstrates that a legitimate authenticated-session event reuses an existing login
instance and loses `EXTRA_ACCOUNT_DISABLED`.

This avoids broadening the change based only on the broken self-navigation path, which D1 removes.

## Test Strategy

- SDK unit tests prove that the wire status produces `USER_ACCOUNT_DISABLED`, stores no
  credentials, performs no user-details request and does not emit account deletion when there is
  no stored account.
- App tests at the session-navigation boundary prove that `LoginActivity` does not register the
  global redirect while authenticated activities still do.
- Login ViewModel/UI coverage proves that `LoginResult.Error(error_account_disabled)` stops the
  running state and renders the error path. Existing generic error tests can be extended with the
  disabled-account wording rather than duplicating the whole login suite.
- Manual verification against PROD-INDIV/UAT confirms the complete cross-repository behavior and
  the actual network request sequence.

## Risks / Trade-offs

- **SDK event is still emitted by a snapshot build.** The app guard prevents self-navigation even
  before the SDK semantic cleanup lands. The final artifact must nevertheless remove the
  inappropriate no-account event.
- **A broad Activity exclusion hides a real session event.** Only `LoginActivity` is excluded; it
  has no protected content and is already the redirect destination.
- **Shared-file merge conflicts.** The minimal guard lives in Oslo-derived session code because
  the Activity hierarchy is shared. Mark it as `// EyeSeeTea customization - Disabled account
  login` and verify it after every baseline merge according to `conflict-rules.md`.
- **Two messages compete.** The authenticated-session dialog and credential-login `InfoBar` remain
  context-specific. Tests must ensure only one presentation route is active in each context.
- **Snapshot/public API drift.** The app must not be merged with the temporary SDK coordinate or
  temporary `ACCOUNT_DISABLED` mappings; use the final published EyeSeeTea artifact.

## Migration Plan

1. Finalize and publish the SDK patch with the unified `USER_ACCOUNT_DISABLED` contract.
2. Update the app to the final SDK artifact and remove temporary protocol-code mappings.
3. Scope global disabled-account session navigation away from `LoginActivity`.
4. Run focused app tests, WIDP build and lint.
5. Validate both login-time and authenticated-session scenarios on a device.

Rollback consists of reverting the app guard and restoring the previous SDK coordinate. There are
no database schema or stored-data migrations.

