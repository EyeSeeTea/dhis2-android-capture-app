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

## 4. 2FA support

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

Status: `broken` — validate after reimplementation

Preconditions:
- Use a program with a data element that has a `url` property configured on the server.

Manual flow:
1. Open a data entry form containing that data element.
2. Tap the info/description icon on the field.

Expected result (after fix):
- the description dialog shows the field description followed by the URL on a new line
- the URL is visible and accessible to the user

Current state:
- the URL value is loaded from the SDK but NOT displayed anywhere — the rendering was lost during the upstream Compose migration. This test will fail until the rendering is reimplemented.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `customization-specs.md`
- keep its technical inventory in `customization-files.md`
