## Why

The SP IP programme distributes goods to households and hands the beneficiary (and programme staff) a printed receipt of what was delivered. The receipt is generated from a completed event (1 household, 1 basket, 1 day) and must be printed offline over Bluetooth from the Android app.

Printer model, receipt layout and final trigger are not confirmed by the client yet. This change delivers the initial, hardware-independent slice so the flow can be exercised end to end before the real Bluetooth integration exists.

## What Changes

- Add a **Print receipt** item to the TEI dashboard menu (top-right), following the precedent of **Send Welcome SMS**. The trigger is a stopgap for testing; if the client asks for automatic printing on event completion it will be replaced without touching the domain.
- Add a receipt domain (`Receipt`, `ReceiptPrinter`, `PrinterError`, `PrintReceiptUseCase`) marked as draft.
- Add a fake printer and a text formatter so the flow works without hardware.
- Show a success or error message after printing (including "no printer connected").
- The task as a whole includes building the printer capability (the app cannot drive a printer today). This change delivers only the abstraction; the real Bluetooth integration is **out of scope**: it sits behind `ReceiptPrinter` and will be a follow-up change once the printer model is known.

## Capabilities

### New Capabilities
- `print-receipt`: print a goods-distribution receipt for a completed event on the connected Bluetooth printer.

### Modified Capabilities
None.

## Impact

- `app/src/spIP/**` (new): `usescases/receipt/` (domain, fake printer, formatter, view model), string resources.
- Shared code (minimal, conflict surface): one `PRINT_RECEIPT` entry in the `EnrollmentMenuItem` enum, and wiring of the menu item and click in the spIP dashboard menu code, mirroring `SEND_SMS`. Cannot live entirely in the flavor because the menu enum and its dispatch are in shared code.
- No SDK changes. No new permissions in this change (Bluetooth permissions arrive with the real integration).
- Upstream target version: not an upgrade change.

## Pending (client)

- Printer model and protocol (likely Zebra: ZPL or Link-OS SDK).
- Receipt layout and fields.
- Final trigger (menu vs automatic on completion; reprint).
- Beneficiary and program staff both receive a receipt: one print with two copies, or one print per recipient? (The confirmed "single label per action" only rules out batch printing; it does not say how many copies.)
- The distribution program/stage does not exist yet on the test server (`SP Beneficiary Hub` only has health stages): UIDs and data model to be provided.
- Which event is printed when launched from the dashboard menu.
