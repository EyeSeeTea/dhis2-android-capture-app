# Sports — Customization Specs

- Client: `sports`
- Flavor: `sports`
- Base branch: `develop-eyeseetea`
- Base commit: `83269ca92438e7574ea69749263cf8b8270be03f`
- Generated on: `2026-03-25`

## Purpose

This file describes:
- the functional title of each customization
- why it exists
- the expected behavior
- the current lifecycle status of the customization

This file should not become:
- a merge-progress notebook
- a raw file inventory
- a substitute for the validation checklist

## How to use

For each customization:
- use one exact title
- keep the title stable across upgrades
- describe behavior, not implementation detail
- keep the status aligned with the real baseline comparison

## Customizations

### 1. Sports branding

Status:
- `active`

Functional intent:
- Provide Sports-specific app identity: launcher icon, app name, and localized strings.

Expected behavior:
- The app displays the Sports launcher icon (not the default DHIS2 icon) on the device home screen.
- The app name shows the Sports-specific name in the user's locale (~30 supported locales).
- Debug builds display a distinct debug launcher icon.

### 2. Sports flavor build configuration

Status:
- `active`

Functional intent:
- Define the `sports` product flavor so the app can be built as a standalone Sports variant with its own application ID and build settings.

Expected behavior:
- `./gradlew :app:assembleSportsDebug` produces a working APK.
- The sports flavor source sets (`app/src/sports/`, `app/src/sportsDebug/`) are included in the build.
- Required flavor stubs (`CustomizableConstants`, `UserComponentFlavor`, `eventCaptureRepositoryFunctions`, `GranularSyncModule`) compile and wire correctly.

### 3. Notifications system

Status:
- `active`

Functional intent:
- Display server-side notifications to the user within the app. Notifications support Markdown content and multi-language translations.

Expected behavior:
- After sync, the user can see notifications delivered from the DHIS2 server.
- Notification content is rendered as Markdown (via markwon library).
- If the notification has a translation matching the device locale, the translated content is displayed.
- Notifications integrate into the main screen and sync flow.

### 4. Change server URL dialog

Status:
- `active`

Functional intent:
- Allow users to change the DHIS2 server URL from within the app without reinstalling or clearing app data.

Expected behavior:
- A dialog is accessible from the main screen to change the server URL.
- After changing the URL, the app reconnects to the new server on next sync.
- The new URL is persisted in shared preferences.

## Removed / Cleaned Up

### 5. PSI feedback tree view (atv dependency)

Status:
- `removed`

Functional intent:
- Was part of the PSI flavor feedback feature. Not used by Sports.

Note:
- The `atv` (AndroidTreeView) dependency should be removed during the upgrade. It is only referenced in `app/src/psi/`.

## Maintenance rule

When a customization still exists after an upgrade:
- keep its functional meaning here
- keep its technical file inventory in `customization-files.md`
- keep its manual validation flow in `upgrade-validation-checklist.md`

When a customization is removed or absorbed by the baseline:
- update the status here explicitly
- do not keep obsolete specs as if they were still active
