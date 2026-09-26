# Disabled account login

## Purpose

Ensure disabled accounts receive an actionable login error without session-navigation loops, while
preserving the established-session response and stopping authentication before user retrieval.

## Requirements

### Requirement: Disabled credential login remains on the login screen

When credential authentication reports that the submitted account is disabled, the app SHALL
finish the login attempt without closing, restarting or navigating away from the active login
screen.

#### Scenario: Disabled account is submitted from login

- **WHEN** a user submits credentials and authentication reports a disabled account
- **THEN** the app remains on the same login screen
- **AND** the login loading state ends
- **AND** the credential form becomes available again

### Requirement: Disabled credential login shows an actionable error

The app SHALL present the localized disabled-account error returned by authentication and MUST NOT
present it as an expired session or invalid credentials.

#### Scenario: Disabled-account result reaches the login UI

- **WHEN** the login operation returns the disabled-account error
- **THEN** the login screen displays that the account has been disabled
- **AND** the message directs the user to contact an administrator
- **AND** no session-expired message is displayed

### Requirement: Disabled login does not continue authentication

The app SHALL use the SDK's terminal disabled-account result and MUST NOT continue retrieving the
authenticated user after that result.

#### Scenario: Authentication endpoint reports a disabled account

- **WHEN** the authentication endpoint reports that the account is disabled
- **THEN** the login attempt performs no authenticated-user-details request
- **AND** the app handles the result as the single disabled-account domain condition

### Requirement: Disabled authenticated accounts still leave the protected area

The app SHALL preserve global disabled-account handling when an account becomes disabled while an
authenticated screen is active.

#### Scenario: Disabled-account deletion occurs in an authenticated session

- **WHEN** the app receives a disabled-account deletion event while an authenticated screen is
  visible
- **THEN** the affected local session is removed
- **AND** the app navigates to login
- **AND** the login destination explains that the account was disabled
