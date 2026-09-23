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
- if the dialog is dismissed **without** clicking OK (back button or tapping outside), the
  notification appears again on the next screen: it was never marked as read
- leaving the dialog **on screen**, pressing Home and returning shows the same single dialog,
  not a second one stacked on top: accepting once must add exactly one `readBy` entry
- dismissing the dialog without clicking OK, then **closing the app completely** (swipe it away
  from recents) and reopening it, shows the notification again **without** a new metadata sync:
  it is still unread and still cached
- after a restart the dialog does **not** appear on the loading screen nor on the login / PIN
  screen, only once the user is inside the app
- with a single program (the app opens the program screen by itself), the dialog appears once;
  after accepting it and going back to the Home, no second dialog is waiting there, and the
  datastore holds exactly one new `readBy` entry
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

## Before recording any result

Check **both** the version and the package name on the device first:

```
adb shell dumpsys package com.eyeseetea.widp.debug | grep versionName
```

A result recorded against the wrong build is worse than no result. During the 3.4.2 upgrade
one notifications check was run against the `dhis2` flavor (`com.dhis2.debug`), which carries
no WIDP customization at all — so nothing appeared, and for a while that looked like a
regression. The wrong flavor is indistinguishable from the right app on the device.

Use `./gradlew :app:installWidpDebug`, not `assembleWidpDebug`: a green assemble does not put
the code on the device, and testing against a stale APK looks exactly like a hook that never
fires.

## Pending for 3.4.2 (as of 2026-09-11)

Confirmed on `3.4.2-widp-fork-1`, Samsung SM-S928B:

- [x] notifications are downloaded after a metadata sync (evidence: `BASIC_SHARE_PREFS.xml`
      written at the sync timestamp, holding the datastore notifications)
- [x] metadata sync brings new server metadata down

Still to exercise on this build:

- [ ] **3. Notifications** — the dialog itself, with a notification the test user has **not**
      read. The 3.4.2 run could not exercise it: both datastore notifications were already in
      the user's `readBy`, so not showing them was correct
- [ ] **3. Notifications** — background sync with the app closed, then open it and check the
      notification is shown on resume
- [ ] **4a/4b/4c. 2FA** — TOTP, Email and SMS on this build. The 2FA login recorded earlier in
      this upgrade was run against `3.4.1-widp-fork-1` and was discarded
- [ ] **1. Change Server URL** — the full flow. Its DI was re-anchored in 3.4.2 because
      upstream deleted `App.java`, so this is not a formality
- [ ] **2. Image upload without resizing**
- [ ] **5. URL data element field**
- [ ] login against a DHIS2 2.41 server

Known and **out of scope**: 2FA with mandatory enrolment not activated shows an error
pointing at the administrator. Reproduced on `3.4.1-widp-fork-1`, so it is pre-existing, not
a regression of this upgrade.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
