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
- A `spIPDebug` build (it binds the development printer, which logs the receipt) and a user logged in on a server with a tracked entity enrolled in SP Beneficiary Hub, with a Distribution event (stage `tCC0YnvF9SJ`) that has data values, ideally both a completed one and an open one.
- An event of another stage of any program.
- Logcat open, filtered by `Receipt printed`.
- Read access to the server API (`/api/tracker/events`) to check the "Receipt Printed" flag after syncing.

Manual flow:
1. Open a Distribution event and open the top-right menu of the event screen.
2. Tap **Print receipt**.
3. Repeat and rotate the device while the printing dialog is visible.
4. Repeat and tap **Print receipt** twice quickly.
5. Tap other controls of the event screen (tabs, back) after the receipt flow, to check that the overlay does not swallow touches.
6. Repeat steps 1-2 on a completed and on a not completed Distribution event, and check the event status afterwards.
7. Open the menu of an event of another stage.
8. On a `spIPRelease` build, open a Distribution event and tap **Print receipt**.
9. After steps 1-2 and 6, sync the tracked entity (Sync button of the dashboard) and read the events of the stage from the server API with the data value of the data element `FHRqcSyJxLr`.

Expected result:
- The menu of a Distribution event shows **Print receipt** in both build types, completed or not.
- Step 2: a "Printing receipt…" dialog, then a "Receipt printed" snackbar; Logcat shows the plain-text receipt of that event: header (distribution, point, date, SPBH ID, beneficiary, geographical locations, print time), the items given and the signature line.
- Step 3: rotating neither cancels nor repeats the print; one receipt is logged.
- Step 4: only one receipt is logged.
- Step 5: the event screen keeps responding to touches.
- Step 6: both print; printing does not change the event status.
- Step 7: the menu does not show **Print receipt**.
- Step 8: an error dialog says no printer is connected and stays until **Accept** is pressed.
- Step 9: the printed events have `FHRqcSyJxLr = true` (also the completed one) and keep their status; the events not printed have no value for it.
- The "print failed" error cannot be produced by hand (the development printer never fails); it is covered by `ReceiptPrintHostTest`.
- The dashboard menu of the enrollment no longer shows **Print receipt**.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
