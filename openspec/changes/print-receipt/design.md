## Context

Receipt = proof of goods given to one beneficiary in one Distribution event. It is printed from the event the user is viewing (Beneficiary Hub, stage `Distribution`), offline, over Bluetooth, on a Brother RJ-2055WB roll printer with 57 mm wide thermal paper (printable width and resolution to confirm from the printer specification). It contains the SP logo, distribution name, distribution point, date and time, ration card ID, beneficiary name, the beneficiary signature (an image) and the items given with their quantities. One receipt per press, unlimited reprints, no batch.

Each tablet is paired to one printer. Pairing is done in Android Settings; the app has no pairing screen.

## Goals / Non-Goals

**Goals:** an end-to-end testable flow (event menu -> receipt -> printer -> feedback -> flag) with the printer abstracted; a domain that survives a change of trigger, layout or printer.

**Non-Goals:** real Brother/Bluetooth integration in the first slice, pairing UI, printer selection, fallback when there is no printer, batch printing, QR label printing (done by a WebApp), non-Latin text.

## Decisions

- **Placement:** all new code in the flavor source set `app/src/spIP/java/org/dhis2/usescases/receipt/` (precedent: `usescases/sms/`). Shared code is touched only at the event-screen menu and activity (placement hierarchy level 4); the touch points are identified in task 5.1 and registered in the inventory.
- **Trigger:** menu item **Print receipt** in the top-right menu of the event screen of the Distribution stage. Always available, manual. The view model exposes a single `print(eventUid)` action. The earlier dashboard trigger (`PRINT_RECEIPT` in `EnrollmentMenuItem`, `buildMenuForEnrollment`, `mountReceiptPrintHost` in `TeiDashboardMobileActivity`) is removed.
- **Domain:**
  - `Receipt`: immutable value with a header (distribution name, distribution point, date and time, ration card ID, beneficiary name), the signature (optional image) and ordered item lines (label and quantity). It no longer is a generic list of label/value pairs. The repository absorbs how DHIS2 stores things (option code to option name, data element to `displayFormName`, file resource to image).
  - `ReceiptPrinter`: `suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit>`.
  - `PrintReceiptError`: sealed interface with `NoPrinterConnected` and `PrintFailed`. `NoReceiptAvailable` is removed: an event always yields a receipt because the TEI attributes are always there (decision to confirm in task 5.4).
  - `PrintReceiptUseCase`: input is the **event uid**; loads the receipt, prints it, and returns `Either<PrintReceiptError, Unit>`. It does not implement `UseCase<R, T>` (expected outcomes are typed values, see technique T6 in `eyeseetea-docs/customization-techniques.md`).
- **Receipt Printed flag:** written by a separate port (`ReceiptPrintedMarker`) after a successful print. It runs after printing and is best effort: errors are logged, never shown, and never turn a successful print into an error. It must not complete the event nor trigger program rules that change its status. Writing is skipped when the event is not editable, without error.
- **Rendering:** `ReceiptRenderer` turns a `Receipt` into a bitmap sized for the printable width of the 57 mm roll (to confirm from the printer specification). It replaces the plain-text `ReceiptFormatter`. Text is Latin only. The printer adapter decides how to send the bitmap (SDK or raster commands); the domain never knows.
- **Signature:** read via the SDK from the `IMAGE` data value (file resource). If it is missing or empty the receipt is still printed (assumption, pending confirmation).
- **Connected printer:** with Bluetooth SPP there is no "connected" state to query. The real `ReceiptPrinter` will look among the paired printers and try to connect (one printer per tablet), reporting `NoPrinterConnected` if it cannot. The fake can simulate it.
- **Dependency creation:** hand-written `ReceiptServiceLocator` in the flavor source set, next to `SPIPServiceLocator` (SMS), instead of Dagger/Koin, to avoid conflicts in upgrades. It is called only from the composition root (event activity and menu wiring). The locator provides a `ViewModelProvider.Factory` (view model at activity scope, so it survives rotation). `provideReceiptPrinter()` is defined per build type: `spIPDebug` (fake) and `spIPRelease` (always `NoPrinterConnected` until the real printer exists).
- **Feedback:** success in a snackbar; errors in a non-dismissable dialog until accepted. A second press is ignored while a print is in progress and allowed again when it finishes.
- **No SDK fork dependency.**

## Risks / Trade-offs

- Bitmap rendering changes the formatter, the tests and the way the printer is fed; layout fidelity can only be checked on paper with the real printer.
- The Oslo touch points of the event screen may differ from the dashboard ones and increase the conflict surface at upgrade time.
- Bluetooth behaviour (connection, roll width, paper type, firmware language) can only be validated with the Brother unit. Firmware and paper must be identical across production printers. Paper: 57 mm wide thermal rolls, 30 mm maximum outer diameter; Brother Economy RD001U5V2 for the first tests and generic "57 x 30 mm" rolls to test as a local option. The plain-text receipt must also fit the 57 mm roll (characters per line, decided with the real printer font).
- The Bluetooth runtime permission flow (Android 12+) cannot be tested on the Android 8 test tablet; it needs an emulator for the flow and a physical Android 12+ tablet for the real thing.
- Test server UIDs may differ in production.

## Open Questions

Printable width of the 57 mm paper and characters per line, Distribution point source, meaning of "Date & timestamp", empty signature behaviour, logo file, production Android version, production UIDs, Brother SDK licence and distribution.
