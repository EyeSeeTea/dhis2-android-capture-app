# spIP Validation Checklist

Manual validation checklist for the spIP fork (flavor `spIP`, application id `org.samaritan.platform`).

Only customizations documented in `eyeseetea-docs/customizations/spip/customization-files.md` have a flow here. Flows for the pre-existing customizations are pending and will be written during the next upgrade (see the pending item in that file).

Add one section per customization, using the exact title of its OpenSpec spec:

### N. [Customization title]

Preconditions:
- Starting state, user, data, or navigation point.

Manual flow:
1. Step one.
2. Step two.

Expected result:
- Expected visible or functional outcome.

### 1. Print Receipt

Preconditions:
- A `spIPDebug` build (it binds the development printer, which logs the receipt) and a user logged in on a server with a tracked entity enrolled in a program that has at least one completed event with data values.
- A second enrollment with no completed event.
- Logcat open, filtered by `Receipt printed`.

Manual flow:
1. Open the TEI dashboard of the first enrollment and open the overflow menu.
2. Tap **Print receipt**.
3. Repeat and rotate the device while the printing dialog is visible.
4. Repeat and tap **Print receipt** twice quickly.
5. Tap other dashboard controls (tabs, back) after the receipt flow, to check that the overlay does not swallow touches.
6. Open the dashboard of the enrollment with no completed event and tap **Print receipt**.
7. On a `spIPRelease` build, open any dashboard and tap **Print receipt**.

Expected result:
- The menu shows **Print receipt** in both build types.
- Step 2: a "Printing receipt…" dialog, then a "Receipt printed" snackbar; Logcat shows one line per data value of the latest completed event (data element name and value).
- Step 3: rotating neither cancels nor repeats the print; one receipt is logged.
- Step 4: only one receipt is logged.
- Step 5: the dashboard keeps responding to touches.
- Step 6: an error dialog says there is nothing to print and stays until **Accept** is pressed; nothing is logged.
- Step 7: an error dialog says no printer is connected and stays until **Accept** is pressed.
- The "print failed" error cannot be produced by hand (the development printer never fails); it is covered by `ReceiptPrintHostTest`.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
