# 2FA support

## Purpose

The official DHIS2 Android SDK only supports TOTP-based two-factor authentication. WIDP deployments additionally need Email and SMS 2FA, plus a user-friendly resend mechanism and rate-limiting feedback. This capability is delivered together with a patch in the EyeSeeTea DHIS2 SDK fork.

## Requirements

### Requirement: Three supported 2FA types
The login flow SHALL support three 2FA types — **TOTP**, **Email**, and **SMS** — and SHALL detect which one applies based on the D2ErrorCode returned by the SDK after the first `blockingLogIn` call.

The recognized codes are:
- TOTP: the SDK indicates a 2FA challenge with the existing TOTP code.
- Email: `EMAIL_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`.
- SMS: `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_SMS`.
- Shared rate-limit: `TWO_FACTOR_MANY_SEND_ATTEMPTS`.

#### Scenario: Server requires TOTP
- **WHEN** the user submits valid credentials and the server requires TOTP 2FA
- **THEN** the app shows a text field labeled "TOTP Code"

#### Scenario: Server requires Email 2FA
- **WHEN** the user submits valid credentials and the server returns `EMAIL_TWO_FACTOR_CODE_SENT`
- **THEN** the app shows a text field labeled "Email Verification Code" plus a "Resend Email Code" button, and surfaces a blue info message "Email with two factor code sent"

#### Scenario: Server requires SMS 2FA
- **WHEN** the user submits valid credentials and the server returns `SMS_TWO_FACTOR_CODE_SENT`
- **THEN** the app shows a text field labeled "SMS Verification Code" plus a "Resend SMS Code" button, and surfaces a blue info message "SMS with two factor code sent"

### Requirement: Login retries with the entered 2FA code
After the user enters a 2FA code, the app SHALL call the SDK again via `blockingLogIn(username, password, serverUrl, twoFactorCode)` passing the code through the patched `LoginPayload.twoFactorCode` field.

#### Scenario: Correct code completes login
- **WHEN** the user enters the correct 2FA code
- **THEN** the SDK authenticates successfully, the session is established, and the app proceeds to the home screen

#### Scenario: Incorrect TOTP code
- **WHEN** the user enters an invalid TOTP code
- **THEN** the TOTP field shows "Incorrect two factor code" as a red error and remains focused

#### Scenario: Incorrect Email / SMS code
- **WHEN** the user enters an invalid Email or SMS code
- **THEN** the field shows "Incorrect authentication code" as a red error and remains focused

### Requirement: Resend mechanism for Email and SMS codes
The app SHALL provide a resend button for Email and SMS 2FA flows, implemented by calling `blockingLogIn` again with `twoFactorCode = null` to trigger the server to issue a new code.

#### Scenario: User requests a new email code
- **WHEN** the user clicks "Resend Email Code" during an Email 2FA challenge
- **THEN** the app re-calls the SDK with a null `twoFactorCode`, the server dispatches a new code, and the blue info message reappears

### Requirement: Resend button cooldown
After a resend attempt (Email or SMS), the resend button SHALL be disabled for 30 seconds to avoid accidental spamming.

#### Scenario: Cooldown is active
- **WHEN** the user clicks resend and then tries to click it again 10 seconds later
- **THEN** the button is disabled and does not trigger another request

#### Scenario: Cooldown expires
- **WHEN** 30 seconds have elapsed since the last resend attempt
- **THEN** the button becomes enabled again and a new attempt is allowed

### Requirement: Rate-limit feedback
When the server returns `TWO_FACTOR_MANY_SEND_ATTEMPTS`, the app SHALL display the message "Many send attempts. Contact your system administrator." and SHALL NOT allow further resends until the user restarts the login flow.

#### Scenario: Too many resend attempts
- **WHEN** the SDK surfaces `TWO_FACTOR_MANY_SEND_ATTEMPTS`
- **THEN** the app replaces the resend state with the administrator-contact message and disables further resend actions

### Requirement: SDK patch dependency
This capability SHALL only function when built against the EyeSeeTea SDK fork that provides:
- `LoginPayload.twoFactorCode: String?`
- `LogInCall.generate2FAErrorIfRequired()` that parses the DHIS2 v2.42+ 2FA status codes.
- The added D2ErrorCode values: `EMAIL_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`, `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_SMS`, `TWO_FACTOR_MANY_SEND_ATTEMPTS`.

#### Scenario: Building against stock SDK
- **WHEN** the project is built against the unpatched upstream SDK
- **THEN** the 2FA capability is reduced to TOTP-only, and the app SHALL fail fast with a clear compile-time or runtime error rather than silently falling back
