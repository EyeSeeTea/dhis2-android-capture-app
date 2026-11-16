## Why

The SP IP programme distributes goods to households and hands the beneficiary a printed receipt of what was delivered. The receipt is printed from the **Distribution** event the user is viewing (Beneficiary Hub programme), offline, over Bluetooth, from the Android app. Staff who need another copy reprint it; there is no limit on reprints.

The client confirmed the scope on 2026-11-04/06 (Ramón Jiménez): trigger, event selection, content, copies, printer model and the "Receipt Printed" flag. Printer model is the Brother RJ-2055WB (a unit will be sent to us), printing on 57 mm wide thermal rolls (30 mm maximum outer diameter; Brother Economy RD001U5V2 for the first tests, generic "57 x 30 mm" rolls to be tested as a local option). The receipt is designed for 57 mm paper. Until it arrives, the flow is exercised end to end with a development printer behind the `ReceiptPrinter` port.

## What Changes

- **Trigger moves** from the TEI dashboard menu to the **top-right menu of the event screen** of the Distribution stage. Always available, manual only (never automatic on completion). The event does not need to be completed, and printing never completes it.
- **Receipt identity changes** from "latest completed event of the enrollment" to **the event being viewed** (`eventUid`).
- **Receipt content** is fixed: SP logo, distribution name, distribution point, date and time, ration card ID, beneficiary name, beneficiary signature, and the items given with their quantities (only items with a value). Attributes come from the TEI, the rest from the event.
- **The receipt is rendered as an image (bitmap)** because it carries the SP logo and the signature (a DHIS2 `IMAGE` data value). Latin characters are enough for text.
- **Receipt Printed flag**: after a successful print, write `SPBH - DIST - Receipt Printed` on the event. The flag is best effort: printing never depends on it and a failure to write it is not shown to the user.
- Keep the receipt domain (`Receipt`, `ReceiptPrinter`, `PrintReceiptError`, `PrintReceiptUseCase`), the development printer, the view model and the printing host, adapted to the new contract.
- The real Brother integration (detect paired printers and connect, Bluetooth permissions, SDK or commands, bitmap printing) stays **out of scope of this change's first slice**: it sits behind `ReceiptPrinter` and is tracked in the Pending tasks until we have the unit and have reviewed the Brother SDK.

## Capabilities

### New Capabilities
- `print-receipt`: print a goods-distribution receipt for the viewed Distribution event on the Bluetooth printer paired to the tablet.

### Modified Capabilities
None.

## Impact

- `app/src/spIP/**` (new and changed): `usescases/receipt/` (domain, development printer, bitmap renderer, repository, view model), string resources.
- Shared code (minimal, conflict surface): the TEI dashboard wiring done earlier (`PRINT_RECEIPT` in `EnrollmentMenuItem`, line in `buildMenuForEnrollment`, `mountReceiptPrintHost` in `TeiDashboardMobileActivity`) is **removed**, and replaced by the equivalent points in the event screen. The event screen is `EventCaptureActivity` (`app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCaptureActivity.kt`), shared by tracker events and events without registration. Touch points (all inline edits, placement level 4, each marked `// EyeSeeTea customization - Print Receipt`):
  1. `enum class EventCaptureMenuItem` (end of file): add `PRINT_RECEIPT`.
  2. `getMenuItems()`: one line that adds the item through a flavor function, only when the event belongs to the Distribution stage (`presenter.programStage()`).
  3. `when (itemId)` in `setupMoreOptionsMenu()`: one branch for `PRINT_RECEIPT` calling a flavor function with the **current** `eventUid` (the field can change in landscape via `updateLandscapeViewsOnEventChange`, so it is read at click time). The `when` is exhaustive, so the branch is mandatory once the enum has the new value.
  4. `onCreate()`: `mountReceiptPrintHost(this)` after `setupMoreOptionsMenu()`.
  Flavor side (`app/src/spIP/.../usescases/receipt/presentation/`): the dashboard wiring file (`ReceiptDashboardWiring.kt`) is replaced by an event wiring file with the menu item, the host mounting and `printEventReceipt(activity, eventUid)`; the host takes a `ComponentActivity` instead of `TeiDashboardMobileActivity`. `ReceiptServiceLocator` is reused. The flavor-side `customClick` branch in `spIP/.../teiDashboard/TeiDashboardMenu.kt` is removed.
  Same precedent as the dashboard wiring: shared code imports spIP-only functions, so other flavors would not compile this branch (they do not define these functions either today); accepted for this fork.
- No SDK fork changes. The signature is read through the SDK as a file resource; the flag is written through the SDK as a data value.
- Bluetooth permissions arrive with the real integration (`BLUETOOTH` and `BLUETOOTH_ADMIN` up to Android 11; `BLUETOOTH_CONNECT` and `BLUETOOTH_SCAN` at runtime from Android 12). The runtime permission flow can be tested on an emulator with API 31+; real pairing and printing only on a physical tablet with the printer.
- Upstream target version: not an upgrade change.

## Metadata (test server `http://172.16.3.100:8380`, DHIS2 2.42.7.1-spip-fork-2)

| Item | UID | Notes |
|---|---|---|
| Programme `SP Beneficiary Hub` | `csDh9fh1NvL` | tracker |
| Stage `Distribution` | `tCC0YnvF9SJ` | repeatable, event date label "Distribution date" |
| Flag `SPBH - DIST - Receipt Printed` | `FHRqcSyJxLr` | `TRUE_ONLY`, not shown in the form |
| `SPBH - DIST - Distribution` | `OQEWu6dpWYl` | stores the option code; name from option set `tlQCeLIQxMg` (`D0001` "TEST Distribution 1", `D0002` "TEST Distribution 2") |
| `SPBH - DIST - Recipient Signature` | `i5UpleAZkKm` | `IMAGE` |
| Attribute `SPBH - Ration Card ID` | `ynR25f3ZTig` | generated |
| Attribute `SPBH - Full Name` | `OehKwDX8asz` | beneficiary name |
| Items given | data elements with code `sp_DIST_<ITEM>_GIVEN` | label from `displayFormName` (e.g. "Rice (kg) given"); 3 "Other item" pairs: description `vZx5saCfQVV`, `IWgi1z9u1lI`, `Tiud233OGx2` and quantity `mksniRe5SBP`, `St3lvEgnok0`, `ulssuv5upaP` |

UIDs are for the test server; production UIDs must be confirmed before release. Select items by **code** (`sp_DIST_*_GIVEN`), not by a hard-coded list.

## Pending (client)

- **Distribution point**: the Distribution stage has no data element for it. Provisional assumption: the name of the event's organisation unit. To confirm.
- **Date & timestamp**: printing moment or the event's "Distribution date"? They differ on a reprint. To confirm.
- **Empty signature**: print the receipt anyway (assumed) or block?
- Logo file for the SP logo (black and white). Its maximum width depends on the printable width of the 57 mm roll, to confirm from the RJ-2055WB specification (the earlier 384 px figure assumed a 58 mm roll).
- Printable width of the 57 mm paper and characters per line for the plain-text receipt (from the RJ-2055WB specification and a test on the real printer).
- Production tablets' Android version and model (Ramón doubts it is Android 8; the test tablet is Android 8).
- Production metadata UIDs and the final production server.
