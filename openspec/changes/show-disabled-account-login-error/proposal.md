# Show the disabled-account error without closing login

## Why

When a user attempts to log in with a disabled account, the EyeSeeTea SDK now stops after
`/api/auth/login` and returns `USER_ACCOUNT_DISABLED` without requesting `/api/me`. The WIDP
app already has a localized disabled-account message and a normal login-error path capable of
rendering it, but that result never reaches the visible screen.

Before returning the error, the SDK emits `AccountDeletionReason.ACCOUNT_DISABLED`. Every
`SessionManagerActivity`, including `LoginActivity`, observes that event as a global session
termination and starts `LoginActivity` with `FLAG_ACTIVITY_CLEAR_TOP`, then finishes the current
activity. When login is already the foreground activity, Android reuses that same instance and
the app immediately finishes it. The user therefore sees the app close instead of the existing
disabled-account message.

The app must distinguish two contexts that currently share the same global event:

1. A disabled account detected while the user is in the authenticated area must continue to
   clear the affected session and route to login.
2. A disabled account returned by a login attempt must remain on the current login screen and
   show the actionable disabled-account error.

## What Changes

- Prevent the global account-deletion navigation from restarting or finishing the login screen
  while a credential login is already in progress there.
- Allow `USER_ACCOUNT_DISABLED` to complete the existing repository, use-case and ViewModel
  error path so the localized disabled-account message is rendered in the login screen's error
  `InfoBar` and the form becomes usable again.
- Preserve the existing behavior for an account that becomes disabled during an authenticated
  session: remove the local account, leave the authenticated area and explain the disabled
  account on the destination login screen.
- Consume one public SDK error contract, `USER_ACCOUNT_DISABLED`. The wire value
  `ACCOUNT_DISABLED` must be normalized inside the SDK and must not require a second app-level
  mapping for the same condition.
- Add regression coverage for both contexts: disabled login while already on `LoginActivity`,
  and disabled-account deletion while an authenticated activity is visible.
- Verify manually that a disabled login performs one `/api/auth/login` request, performs no
  `/api/me` request, remains on login, removes the loading state and displays the disabled-account
  message without closing the app.

## Capabilities

### New Capabilities

- `disabled-account-login`: defines how WIDP presents a disabled account during credential login
  and how that differs from handling a disabled account discovered during an authenticated
  session.

### Modified Capabilities

None. The current OpenSpec tree has no general login or session-termination capability to extend.
The existing `two-factor-auth` capability is unaffected.

## Impact

### Shared app code

- `app/src/main/java/org/dhis2/usescases/general/SessionManagerActivity.kt` — scope the global
  disabled-account observer/navigation so it cannot redirect from login back to itself.
- `app/src/main/java/org/dhis2/data/server/OpenIdSession.kt` — potentially clarify or separate the
  disabled-account event consumed by authenticated activities, depending on the final design.
- `app/src/main/java/org/dhis2/usescases/login/LoginActivity.kt` — only if defensive handling of a
  repeated login intent is still required after the observer is correctly scoped.
- `login/src/androidMain/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImpl.kt` and the
  existing common login state pipeline — expected to retain their current behavior; tests will
  prove that `USER_ACCOUNT_DISABLED` becomes the existing localized error rather than being
  interrupted by navigation.
- Corresponding unit/UI tests for session navigation and login error presentation.

These classes belong to the shared Oslo-based login and session infrastructure. The change cannot
live entirely under `app/src/widp/**` without duplicating an Activity, replacing shared manifest
wiring, or introducing flavor-specific indirection larger than the behavioral guard itself. The
implementation must therefore use the smallest possible shared-code edit and follow
`eyeseetea-docs/upgrade/conflict-rules.md`, with the matching EyeSeeTea customization marker.

### Flavor-isolated code

No production file under `app/src/widp/**` is expected. WIDP-specific documentation and manual
validation may be updated without duplicating the shared login implementation.

### SDK dependency

This change depends on the EyeSeeTea SDK patch in `LogInCall`: the `/api/auth/login` wire status
`ACCOUNT_DISABLED` must terminate authentication before credential persistence and `/api/me`, and
the caller must receive `D2ErrorCode.USER_ACCOUNT_DISABLED`.

The SDK should keep `ACCOUNT_DISABLED` as an internal protocol value rather than an additional
public application error. Consequently, the temporary app mappings for both `ACCOUNT_DISABLED`
and `USER_ACCOUNT_DISABLED` are not part of the intended final design. Upgrading the WIDP SDK
artifact to the finalized patch version is part of implementation.

### Behavior intentionally preserved

- Successful credential login and TOTP, email and SMS two-factor flows.
- Disabled-account cleanup and redirection when the event originates from an authenticated
  session.
- Existing translated `error_account_disabled`, `account_disable_title` and
  `account_disable_message` resources.
- Offline-first behavior and SDK-managed account storage.
