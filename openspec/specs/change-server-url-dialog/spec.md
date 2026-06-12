# Change Server URL Dialog

## Context

The sports fork includes a dialog that allows users to change the DHIS2 server URL from within the app without logging out and re-authenticating. This is useful in deployment scenarios where the server endpoint changes (e.g., migration between staging and production, or DNS changes) and users need to redirect the app to the new URL while preserving their local data.

The dialog is accessible from the main screen and performs the URL change by updating preferences, updating SDK credentials/database configuration, clearing cached system info, and re-downloading system info from the new server.

This is a custom EyeSeeTea feature not present in the upstream DHIS2 Android Capture App.

## Requirements

### REQ-CS-01: Allow URL change from main screen

The app MUST provide a UI entry point (menu item or button) on the main screen that opens the Change Server URL dialog.

The entry point MUST be identified by the resource ID `change_url`.

### REQ-CS-02: Display current server URL

When the dialog opens, it MUST display the current server URL (with the `/api` suffix stripped) in an editable text field.

### REQ-CS-03: Validate URL input

The OK/Save button MUST be disabled when:
- The text field is empty
- The entered URL is identical to the current server URL

The OK/Save button MUST be enabled when the entered URL is non-empty and different from the current URL.

### REQ-CS-04: Require user confirmation

When the user taps OK/Save with a changed URL, the dialog MUST first show a warning/confirmation step before proceeding with the change. The user MUST explicitly confirm the action.

### REQ-CS-05: Persist new URL

Upon confirmation, the app MUST:
1. Update the server URL in the preference store
2. Update the URL set in `PREFS_URLS` (removing old URL, adding new URL)
3. Call `d2.userModule().accountManager().changeServerUrl(newServerURL)` to update SDK credentials and database configuration
4. Clear cached system info (`DELETE FROM SystemInfo`)
5. Re-download system info from the new server URL

### REQ-CS-06: Show progress during URL change

While the URL change is being processed (credential update, system info download), the dialog MUST display a progress indicator and hide the URL input and warning views.

### REQ-CS-07: Handle errors gracefully

If the URL change fails (e.g., new server unreachable, authentication failure), the dialog MUST:
- Display a user-readable error message
- Hide the progress indicator
- Return to the edit mode so the user can correct the URL or cancel

### REQ-CS-08: Close dialog on success

After a successful URL change, the dialog MUST display a success message and close automatically.

### REQ-CS-09: Reconnect on next sync

After the URL change, the next synchronization cycle MUST target the new server URL. No app restart SHALL be required for the URL change to take effect on sync operations.

## Scenarios

### Scenario: User opens change server URL dialog

- **GIVEN** the user is on the main screen
- **WHEN** the user taps the change server URL entry point
- **THEN** a dialog opens showing the current server URL in an editable field
- **AND** the OK button is disabled

### Scenario: User enters a new URL

- **GIVEN** the change server URL dialog is open with the current URL displayed
- **WHEN** the user modifies the URL to a different value
- **THEN** the OK button becomes enabled

### Scenario: User clears the URL field

- **GIVEN** the change server URL dialog is open
- **WHEN** the user clears the text field completely
- **THEN** the OK button is disabled

### Scenario: User confirms URL change

- **GIVEN** the user has entered a new URL and the OK button is enabled
- **WHEN** the user taps OK
- **THEN** a warning/confirmation message is displayed
- **AND** the URL input field is hidden

### Scenario: User accepts confirmation

- **GIVEN** the confirmation warning is displayed
- **WHEN** the user taps Accept/OK again
- **THEN** a progress indicator is shown
- **AND** the server URL is updated in preferences and SDK configuration
- **AND** system info is re-downloaded from the new URL
- **AND** a success message is shown
- **AND** the dialog closes

### Scenario: URL change fails

- **GIVEN** the user confirmed a URL change
- **AND** the new server is unreachable or returns an error
- **WHEN** the URL change process completes with failure
- **THEN** an error message is displayed
- **AND** the dialog returns to edit mode with the progress indicator hidden

### Scenario: Sync uses new URL after change

- **GIVEN** the user successfully changed the server URL
- **WHEN** the next data synchronization runs
- **THEN** the sync targets the new server URL
- **AND** no app restart was required
