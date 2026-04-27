# UNICEF TJK eLMIS — Validation Checklist

Manual validation checklist for the UNICEF TJK eLMIS fork. One section per customization with Preconditions / Manual flow / Expected result.

This file is **not** for: merge progress, implementation details, raw diff tracking, or file-level inventories.

## 1. Flavor scaffold — first install and login

Preconditions:
- VPN connection to the UNICEF TJK eLMIS server is active
- Admin credentials for `http://172.16.0.99:18081` are available
- Android device or emulator (API ≥ minSdk per `gradle/libs.versions.toml`)

Manual flow:
1. From the main repo root, run `./gradlew assembleUnicefTjkElmisDebug`. Build must succeed.
2. Install the produced APK on the device or emulator (`adb install` or via the IDE).
3. Open the launcher. Confirm the entry shows the launcher name `UNICEF TJK eLMIS` (the launcher icon in PR 01 is the inherited Oslo default — final UNICEF / MoH TJK icon ships in PR 03 once Daler X1 is resolved).
4. Tap the launcher entry. The splash and login screen open without crashing.
5. In the login screen, enter:
   - Server URL: `http://172.16.0.99:18081`
   - Username: admin
   - Password: (admin credentials)
6. Submit. Login completes and the home screen renders without the cleartext-traffic error.

Expected result:
- Launcher entry name reads `UNICEF TJK eLMIS`.
- Login completes against `http://172.16.0.99:18081` over plain HTTP through the VPN.
- Home screen renders. No `CLEARTEXT communication ... not permitted` error appears in logcat.

Notes:
- If the login fails with `CLEARTEXT communication ... not permitted`, the flavor's `AndroidManifest.xml` did not pick up `android:networkSecurityConfig`. Verify the manifest merger output under `app/build/intermediates/merged_manifest/unicefTjkElmisDebug/AndroidManifest.xml` includes the attribute.
- VPN access is required for step 5 only. The build itself does not require VPN.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md`
