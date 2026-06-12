# Sports — Upgrade Validation Checklist

- Client: `sports`
- Flavor: `sports`
- Base branch: `develop-eyeseetea`
- Base commit: `83269ca92438e7574ea69749263cf8b8270be03f`
- Generated on: `2026-03-25`

## Purpose

This file is for:
- minimal manual validation per customization
- expected result per flow
- regression checking after merge resolution

This file is not for:
- merge progress
- implementation details
- raw diff tracking
- file-level inventories

## Validation Flows

### 1. Sports branding

Preconditions:
- Sports debug APK installed on device or emulator.

Manual flow:
1. Install the Sports debug APK.
2. Check the home screen launcher icon.
3. Open the app and check the app name in the title bar / settings.
4. Change device language to a supported locale (e.g., French, Spanish).
5. Verify the app name updates to the localized version.

Expected result:
- Launcher icon is the Sports-specific icon (not default DHIS2).
- Debug builds show a distinct debug icon variant.
- App name matches the Sports-specific name in each locale.

### 2. Sports flavor build configuration

Preconditions:
- Clean checkout of the upgraded branch.

Manual flow:
1. Run `./gradlew :app:assembleSportsDebug`.
2. Run `./gradlew :app:assembleDhis2Debug` (verify base flavor still works).
3. Verify the APK is produced in `app/build/outputs/apk/sports/debug/`.

Expected result:
- Both builds complete without errors.
- Sports APK has the correct application ID.
- No references to removed flavors (PSI, WIDP, Simprints) cause build failures.

### 3. Notifications system

Preconditions:
- Sports debug APK installed and logged into a DHIS2 server that has notifications configured.
- At least one notification exists on the server (with Markdown content and/or translations).

Manual flow:
1. Open the app and trigger a sync (pull).
2. Navigate to the notifications section from the main screen.
3. Verify notifications are displayed.
4. Check that Markdown formatting (bold, links, lists) renders correctly.
5. Change device language to a locale with notification translations.
6. Verify the translated content is shown.

Expected result:
- Notifications appear after sync.
- Markdown content renders as formatted text (not raw Markdown).
- Translated notifications display in the device locale when available.
- No crashes or blank screens in the notification flow.

### 4. Change server URL dialog

Preconditions:
- Sports debug APK installed and logged into a DHIS2 server.

Manual flow:
1. Open the app main screen.
2. Access the change server URL option (menu or settings).
3. Enter a new valid DHIS2 server URL.
4. Confirm the change.
5. Trigger a sync.

Expected result:
- The dialog opens without errors.
- After changing the URL, the app persists the new URL.
- Subsequent syncs connect to the new server.
- If the new URL is invalid, an appropriate error is shown.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `customization-specs.md`
- keep its technical inventory in `customization-files.md`
