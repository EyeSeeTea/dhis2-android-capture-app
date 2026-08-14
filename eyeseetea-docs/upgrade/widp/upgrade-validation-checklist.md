# WIDP Validation Checklist

Manual validation checklist for the known WIDP customizations after an upgrade.

Use this file for:
- minimal manual test flows
- expected results
- identifying regressions after merge resolution

Do not use this file for:
- merge progress
- raw diff inventory
- implementation notes

## 1. Change Server URL

Preconditions:
- Login with a valid user on a known server.

Manual flow:
1. Open the settings screen.
2. Select the change server URL option from the menu.
3. Enter a valid alternative DHIS2 server URL.
4. Confirm the change (warning dialog appears, click Accept).

Expected result:
- the app applies the new server URL
- the user's credentials and database configuration are updated
- subsequent API calls use the new server
- SystemInfo is re-downloaded from the new server

## 2. Image upload without resizing

Preconditions:
- Use a flow with image capture or image attachment.

Manual flow:
1. Capture or attach an image in a data entry form.
2. Save the form and sync.
3. Check the uploaded image on the server if possible.

Expected result:
- the image is uploaded at original resolution
- no client-side compression or resizing is applied

## 3. Notifications system

Preconditions:
- The server has a `dataStore/notifications/notifications` namespace configured.
- At least one notification exists targeting the test user (via user group, direct user, or wildcard "ALL").
- The test user has not already read those notifications.

Manual flow:
1. Login and trigger a metadata sync.
2. Wait for the app to load after sync.
3. Check if notification dialogs are displayed.
4. Verify content is in the user's locale (if translations exist for that language).
5. Click OK/Accept on the notification dialog.
6. Close and reopen the app.

Expected result:
- notifications from the datastore are shown as Material AlertDialogs
- content supports Markdown rendering
- if translations exist for the device locale, translated content is shown; otherwise default content
- after clicking OK, the notification does not reappear (marked as read)
- the `readBy` list on the server includes the user's ID and timestamp

Additional checks:
- a notification with wildcard "Web" should NOT appear
- a notification targeting a different user group should NOT appear
- a notification already in readBy for this user should NOT appear

### 3.a Added for the 3.4.1 upgrade — the download trigger moved

The notification **download** used to run inside the metadata sync worker. Upstream 3.4 moved the
whole sync into the `:sync` module, which cannot see this capability, so the download is now
triggered by `HomeEffect.SyncNotifications`, emitted when a sync finishes with the main screen
alive. This is the highest-risk change of the whole upgrade and no automated test covers it.

Flow A — download still happens (the critical one):
1. Publish a **new** notification in the datastore for the test user.
2. With the app open on the main screen, trigger a sync (pull to refresh or the sync button).
3. **Expected:** once the sync finishes, the new notification appears without restarting the app.

Flow B — the branch asymmetry survives (ported from `MainPresenter.checkSingleProgramNavigation`):
1. Log in with a user who has **more than one** program → lands on Home.
   **Expected:** notifications are marked pending *and* refreshed → the dialog appears.
2. Log in with a user who has **exactly one** program → the app auto-navigates into it.
   **Expected:** no dialog on the program screen; it appears when returning to Home.

Flow C — known deviation, confirm the scope of the loss:
1. Close the app completely and let a periodic background sync run.
2. **Expected (accepted):** notifications published in the meantime do **not** arrive until the
   next sync with the app open. Confirm this is acceptable in the field, or escalate.

Flow D — refresh on resume still works:
1. With the app open, send the app to background and return.
2. **Expected:** `ActivityGlobalAbstract.onResume()` refreshes and shows any pending notification.

### 3.b Added for the 3.4.1 upgrade — menu entry points

1. Open the side menu → "Sync manager".
   **Expected:** notifications are marked pending (no dialog while in Settings).
2. Open the side menu → "Home".
   **Expected:** notifications are marked pending and refreshed → pending dialog appears.

## 4. 2FA support

> 3.4.1 note: this capability was re-applied by hand onto a rewritten login screen. Upstream added
> an OAuth branch to `onLoginClicked()` and wrapped `CredentialsContainer` in `if (!oAuthEnable)`.
> The whole flow below must be re-run — passing unit tests say nothing about it. Pay particular
> attention to the seven error messages: the shared baseline maps those same `D2ErrorCode` values
> to a generic error, and copying that resolution would look correct while silently destroying
> every scenario below.

Preconditions:
- Use a server (DHIS2 v2.42+) that has 2FA enabled for the test user.

### 4a. TOTP

Manual flow:
1. Attempt to login with a user that has TOTP 2FA enabled.
2. A "TOTP Code" text field appears.
3. Enter a correct TOTP code and login.
4. Try again with an incorrect code.

Expected result:
- correct code: login succeeds
- incorrect code: red error message "Incorrect two factor code"

### 4b. Email

Manual flow:
1. Attempt to login with a user that has Email 2FA enabled.
2. An "Email Verification Code" text field and "Resend Email Code" button appear.
3. A blue info message "Email with two factor code sent" is shown.
4. Enter the code received by email and login.
5. Try again with an incorrect code.
6. Click "Resend Email Code" and verify the button disables for 30 seconds.

Expected result:
- correct code: login succeeds
- incorrect code: red error message "Incorrect authentication code"
- resend button disables for 30s after click
- too many resends: "Many send attempts. Contact your system administrator."

### 4c. SMS

Manual flow:
1. Attempt to login with a user that has SMS 2FA enabled.
2. An "SMS Verification Code" text field and "Resend SMS Code" button appear.
3. A blue info message about SMS code sent is shown.
4. Enter the code received by SMS and login.

Expected result:
- same behavior as Email variant but via SMS

## 5. URL data element field

Status: `active` — reimplemented as inline supporting text in the Compose form

Preconditions:
- Use a program with a data element that has a `url` property configured on the server.

Manual flow:
1. Open an event data entry form containing that data element.
2. Locate the field in the form.

Expected result:
- the field supporting text shows the field description followed by the URL on a new line
- if the field has no description and has a URL, the supporting text shows only the URL
- the URL is visible inline under the field without opening a separate dialog

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
