# SPOCC Customization Spec

Functional specification of the known SPOCC customizations.

This file is the functional reference for SPOCC.

Use this file for:
- customization title
- current lifecycle status
- expected business behavior
- intent of the customization

Do not use this file for:
- merge progress
- temporary conflict notes
- raw diff inventory

## Known SPOCC customizations

### 1. Hide programs and datasets without write data access

Status:
- `active`

Functional intent:
- Users should only see programs and datasets where they have data write access.

Expected behavior:
- Programs without write access are hidden from selection lists.
- Datasets without write access are hidden from selection lists.
- The user should not be invited into a flow they cannot edit.

### 2. Select UPG

Status:
- `active`

Functional intent:
- In specific SPOCC event flows, the user must select a UPG value using a controlled selector instead of normal free form editing.

Expected behavior:
- The app prompts the user to select a UPG when the flow requires it.
- The selection is derived from the current org unit context.
- The underlying UPG field is not exposed as a normal editable form field when the selector flow is active.

### 3. Avoid resize images

Status:
- `active`

Functional intent:
- Images should preserve original size/quality instead of being resized by app-side processing.

Expected behavior:
- Image capture/upload flows avoid automatic downsizing when SPOCC requires original images.
- The chosen behavior must be compatible with the current base SDK/app implementation.

### 4. Session format ui like in server year-nextYear

Status:
- `active`

Functional intent:
- SPOCC wants yearly periods displayed in the same style used by the server, such as `2024-2025`.

Expected behavior:
- Yearly period labels are rendered as `year-nextYear` where applicable.
- The same formatting should be used consistently across relevant screens.

### 5. Hide Schedule menu in timeline view

Status:
- `active`

Functional intent:
- Users should not see schedule actions in timeline mode when that action is not valid or desired for SPOCC.

Expected behavior:
- The schedule option is hidden in the affected timeline menu/context.
- The UI should not suggest unsupported event creation behavior.

### 6. Avoid change org unit in tracker events

Status:
- `active`

Functional intent:
- Tracker event creation/edit flows should keep the event bound to the TEI/enrollment org unit instead of allowing the user to change it.

Expected behavior:
- Event org unit selection is skipped or disabled in the affected tracker flows.
- The event uses the TEI/enrollment org unit directly.

### 7. Validate or hide orgunit by Teamprofile

Status:
- `active`

Functional intent:
- Org units should be filtered or blocked according to Team Profile rules and active-period constraints.

Expected behavior:
- Non-valid org units are hidden or rejected in the relevant selectors.
- Validation may depend on the selected period and the program/dataset context.
- The user receives a clear message when a team/org unit is not valid for the selected period.

### 8. Hide re-open menu always

Status:
- `active`

Functional intent:
- The re-open action should not be available in the affected SPOCC menus.

Expected behavior:
- Re-open is hidden from the relevant menu even when the base app would normally show it.
- Users cannot trigger the hidden action through the normal UI.

### 9. Team change request

Status:
- `active`

Functional intent:
- SPOCC has a dedicated business flow to create and manage team change requests.

Expected behavior:
- The app supports the specific dataset/form/dialog behavior needed to create a team change request.
- Validation and dialog messaging should reflect that this is a dedicated workflow, not a generic dataset flow.

### 10. Multiple SDS org unit selection

Status:
- `active`

Functional intent:
- SPOCC supports selecting multiple SDS org units in the affected data entry flow.
- Only valid SDS org units should appear in the org unit selector.

Expected behavior:
- The relevant input and storage logic allow multiple SDS org units.
- Display, mapping, and validation behave consistently with multi-selection.
- When the user selects an org unit, the flow resolves from country level 4 to a level 5 org unit that exists in the SDS org unit group `yA9VnZi6g7f`.

## Maintenance rule

When a customization is confirmed to still exist after an upgrade:
- keep its functional meaning here
- keep its file-level implementation in `customization-files.md`
- keep its minimal manual validation in `eyeseetea-docs/upgrade/spocc/upgrade-validation-checklist.md`

When a customization is removed or absorbed by the base branch:
- update the status here explicitly
- do not leave obsolete functional specs as if they were still active
