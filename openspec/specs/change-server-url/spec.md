# Change Server URL

## Purpose

WIDP field users need to switch between different DHIS2 server instances from within the installed app, without reinstalling, clearing data, or reconfiguring the device. The stock DHIS2 Android client does not expose this capability; it is contributed here for the `widp` flavor.

## Requirements

### Requirement: Settings entry to change the server URL
The app SHALL expose an explicit "Change server URL" action inside the settings / main menu of the authenticated user, visible only to users already logged in.

#### Scenario: Menu entry is visible when logged in
- **WHEN** an authenticated user opens the settings menu
- **THEN** the menu shows a "Change server URL" option

#### Scenario: Menu entry is hidden when not logged in
- **WHEN** the app is on the login screen and no user is authenticated
- **THEN** the "Change server URL" option is not reachable

### Requirement: Server URL input dialog with validation
The app SHALL prompt the user for a new DHIS2 server URL via a dialog that validates the URL is well-formed and reachable before applying the change.

#### Scenario: User enters a well-formed URL
- **WHEN** the user types a syntactically valid `https://…` URL and confirms
- **THEN** the app accepts the input and proceeds to the confirmation step

#### Scenario: User enters a malformed URL
- **WHEN** the user types a malformed URL (empty, missing scheme, illegal characters)
- **THEN** the app rejects the input, shows an inline error, and does not change the active server

### Requirement: Confirmation warning before applying the change
The app SHALL show an explicit warning dialog before applying the new server URL, because applying the change invalidates the current session and local database configuration.

#### Scenario: User confirms the change
- **WHEN** the user clicks "Accept" on the warning dialog
- **THEN** the app proceeds to switch the server URL

#### Scenario: User cancels the change
- **WHEN** the user dismisses or cancels the warning dialog
- **THEN** the app keeps the current server URL and no state is changed

### Requirement: Apply new server URL and re-authenticate
After confirmation, the app SHALL update its server configuration so that subsequent API calls go to the new server, and the user SHALL be re-authenticated against that new server before any further data access.

#### Scenario: Successful switch to a reachable server
- **WHEN** the user confirms and the new server is reachable with valid credentials
- **THEN** the app updates its stored server URL, re-downloads `SystemInfo` from the new server, and the user session continues against the new server

#### Scenario: New server is unreachable after confirmation
- **WHEN** the new server fails to respond or rejects credentials after the user confirms
- **THEN** the app surfaces the error to the user and does not silently fall back to the previous server

### Requirement: Previous server no longer used
After a successful change, the app SHALL NOT make any further API calls to the previous server URL within the same session.

#### Scenario: Subsequent sync hits only the new server
- **WHEN** the user triggers a sync after changing the server URL successfully
- **THEN** every outgoing request targets the new server and none targets the old one
