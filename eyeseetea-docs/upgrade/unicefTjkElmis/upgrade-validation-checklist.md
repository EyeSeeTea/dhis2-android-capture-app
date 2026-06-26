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
3. Open the launcher. Confirm the entry shows the launcher name `UNICEF TJK eLMIS` and the UNICEF launcher icon (UNICEF logo on white background — placeholder until the final UNICEF / MoH TJK / combined branding decision is resolved).
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
- If the login fails with `CLEARTEXT communication ... not permitted`, the SDK fork's bundled `res/xml/network_security_configuration.xml` (which carries `<base-config cleartextTrafficPermitted="true">` today and is referenced from the merged manifest) has been tightened upstream — that file is the effective cleartext policy for every flavor in this repo. Inspect the merged manifest at `app/build/intermediates/merged_manifest/unicefTjkElmisDebug/processUnicefTjkElmisDebugMainManifest/AndroidManifest.xml` and the SDK's resolved XML at `~/.gradle/caches/<...>/jetified-dhis2-android-sdk-<version>/res/xml/network_security_configuration.xml`. If the SDK no longer permits cleartext, the fix is to add a flavor-scoped `network_security_config.xml` in a dedicated change proposal.
- VPN access is required for step 5 only. The build itself does not require VPN.

## 2. Lot Number Field — search dialog and proactive cache refresh

Preconditions:
- VPN connection to the UNICEF TJK eLMIS server (`http://172.16.0.99:18081`) is active for the online-lookup steps; some steps below explicitly require airplane mode / no connectivity instead.
- Logged in as a user with access to the UNICEF TJK eLMIS programme event that contains a "Lot Number" DataElement and the "Product" DataElement (`BzKc72LZLxw`). The lot field renders on any of the 4 product-family lot DataElements (`LOT_NUMBER_DE_UIDS`: `mh54C5HEI7C`, `mSiFpMaYeua`, `V86WBAXY0Sq`, `rXAG58ZVgd7`); which one appears depends on the selected product.
- At least one organisation unit + product combination with a non-empty `lotNumbers` list is known in the `openboxes-dhis2-sync/available-lot-numbers` datastore entry, and at least one combination with no entry / an empty list.

Manual flow:
1. Run a metadata sync (pull-to-refresh on the home screen, or open Sync settings and trigger a metadata sync) while online. This exercises `SyncPresenterImpl.syncMetadata()`'s new `lotNumberSyncRepository.refreshCache()` step. Sync must complete successfully (verify no new errors appear vs. a baseline sync without this change).
2. Open the UNICEF TJK eLMIS event form to the event containing the Lot Number / Product fields, for an org unit + product combination **known to have lot numbers**.
3. **No product selected yet**: before setting a value in the "Product" field, tap the search button next to "Lot Number". Confirm the dialog shows "Select a product first..." (`lot_number_select_product_first`), shows no list, and that the Lot Number text field is still directly editable (type a value, confirm it's accepted) while the dialog is open or after dismissing it.
4. Set the "Product" field to the product known to have lot numbers for this org unit. Tap the Lot Number search button again. Confirm the dialog now shows a selectable list of lot numbers matching the datastore entry for `(event org unit code, selected product code)`.
5. Tap one of the listed lot numbers. Confirm the dialog closes and the Lot Number text field is populated with the selected value.
6. Reopen the dialog and tap "Cancel" (`R.string.cancel`). Confirm the dialog closes and the Lot Number field's value is unchanged.
7. **Product with no lot numbers**: change the "Product" field to a product/org-unit combination with no entry (or an empty `lotNumbers` list) in the datastore. Tap the Lot Number search button. Confirm the dialog shows "No lot numbers found for this product. You can type it manually." (`lot_number_none_found`), and that the Lot Number text field remains editable — type a value manually and confirm it's accepted and persists after navigating away from and back to the field.
8. **Lot cleared on product change**: with a Lot Number value set (selected from the dialog or typed manually), change the "Product" field to a different product. Confirm the Lot Number field is cleared (empty). This must hold even when the new product renders a *different* lot DataElement than the previous one — the stale value must not survive on any lot DataElement of the event (save/reopen the event and confirm no lot value persists for the previous product).
9. **Offline with stale cache**: enable airplane mode (no connectivity), reopen the event (or a different event for the same org unit/product known to have lot numbers from step 1's sync). Tap the Lot Number search button. Confirm the dialog still shows the selectable list, sourced from the cache populated by step 1 (network-first lookup fails silently, falls back to cache).
10. While still offline, confirm manual entry in the Lot Number field continues to work and the value persists normally (save/reopen the event).

Expected result:
- The Lot Number field is always directly editable, in all three dialog states and regardless of connectivity.
- The dialog's three states ("select a product first" / selectable list / "no lot numbers found, enter manually") render correctly and match the datastore content for the resolved `(orgUnitCode, productCode)`.
- Selecting a lot number from the list populates the field and closes the dialog; "Cancel" closes without changing the field.
- Changing the product clears the Lot Number value (a lot is only valid for the product it was entered against), including when the new product renders a different lot DataElement.
- Offline lookups fall back to the cache populated by the most recent metadata sync (step 1) or a prior online lookup.
- Metadata sync completes successfully whether or not the lot-numbers datastore entry is reachable.

Notes:
- `form/src/androidTest/kotlin/org/dhis2/form/ui/dialog/lotnumber/LotNumberDialogScreenTest.kt` covers the three `LotNumberDialogScreen` render states + cancel/select callbacks at the Compose level (pure presentation, no `D2`/context dependency). `LotNumberDialog` and `LotNumberFieldInput` — which call `LotNumberInjector.provideGetLotNumbersUseCase(context)` via `D2Manager.getD2()` — are not covered by automated tests and rely on this manual flow as the last-line safety net (see `CLAUDE.md`'s "Post-merge check hierarchy").
- If step 1's sync fails specifically on the lot-numbers refresh, the rest of metadata sync must still succeed (per spec, "Metadata sync runs without affecting other sync steps") — check logcat for a swallowed/logged error from `LotNumberD2Repository.refreshCache()` rather than a sync failure.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md`
