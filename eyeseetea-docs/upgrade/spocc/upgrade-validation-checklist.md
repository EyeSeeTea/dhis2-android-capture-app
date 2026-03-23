# SPOCC Validation Checklist

Manual validation checklist for the known SPOCC customizations after an upgrade.

Use this file for:
- minimal manual test flows
- expected results
- identifying regressions after merge resolution

Do not use this file for:
- merge progress
- raw diff inventory
- implementation notes

## 1. Hide programs and datasets without write data access

Preconditions:
- Login with a user that has mixed read/write access.

Manual flow:
1. Login with a user that has mixed read/write access.
2. Open program selection.
3. Open dataset selection.

Expected result:
- programs without write access are not shown
- datasets without write access are not shown

## 2. Select UPG

Preconditions:
- Use a flow such as End of Season Report or Season Plan where UPG is required.

Manual flow:
1. Enter an event flow that requires UPG.
2. Load the form.
3. Select the UPG through the SPOCC flow.

Expected result:
- the UPG selector appears when required
- the raw field is not exposed as a normal editable field
- the selected value is stored correctly

## 3. Avoid resize images

Preconditions:
- Use a flow with image capture or image attachment.

Manual flow:
1. Capture or attach an image in the affected flow.
2. Save and inspect the uploaded result if possible.

Expected result:
- the app does not apply unintended client-side resize behavior

## 4. Session format ui like in server year-nextYear

Preconditions:
- Open a screen that shows yearly periods.

Manual flow:
1. Open a screen showing yearly periods.
2. Inspect a yearly session label.

Expected result:
- yearly periods are displayed like `2024-2025`

## 5. Hide Schedule menu in timeline view

Preconditions:
- Open the affected timeline view.

Manual flow:
1. Open the affected timeline view.
2. Open the menu/actions available there.

Expected result:
- the schedule action is not visible

## 6. Avoid change org unit in tracker events

Preconditions:
- Use a TEI/enrollment flow that allows tracker event creation.

Manual flow:
1. Create a tracker event from the affected TEI/enrollment flow.
2. Observe the org unit behavior.

Expected result:
- the user cannot change the event org unit
- the event uses the TEI/enrollment org unit directly

## 7. Validate or hide orgunit by Teamprofile

Preconditions:
- Use a Teamprofile-controlled flow with org unit selection.

Manual flow:
1. Open an org unit selector in a Teamprofile-controlled flow.
2. Try valid and invalid org units for the selected period.

Expected result:
- invalid org units are hidden or rejected
- valid org units remain selectable
- feedback is clear when selection is not allowed

## 8. Hide re-open menu always

Preconditions:
- Open the dashboard/menu state where re-open would normally be available in base behavior.

Manual flow:
1. Open the affected dashboard/menu state where re-open would normally appear.

Expected result:
- re-open is never shown

## 9. Team change request

Preconditions:
- Open the dataset flow that should trigger a team change request.

Manual flow:
1. Open the team change request dataset flow.
2. Save in the path where the dedicated dialog should appear.
3. Accept the dialog.

Expected result:
- the team change request dialog appears
- accepting triggers the dedicated team change request logic
- the generic save flow is not used instead

## 10. Multiple SDS org unit selection

Preconditions:
- Open the Team Profile flow with SDS org unit selection enabled.

Manual flow:
1. Open the Team Profile.
2. Open the org unit selector for SDS.
3. Select one org unit and verify that the selector only offers valid SDS org units.
4. When selecting an org unit, verify that the flow resolves from country level 4 to a level 5 org unit that exists in the SDS org unit group `yA9VnZi6g7f`.
5. Select multiple org units.
6. Reopen the field or return to the screen.

Expected result:
- only valid SDS org units appear in the selector
- the selected org unit resolves to a valid level 5 SDS org unit from group `yA9VnZi6g7f`
- multiple org units can be selected
- the stored value is preserved
- the display label shows the selected org unit names consistently
