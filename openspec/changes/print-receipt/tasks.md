Commit rule: a test task immediately followed by its implement task is one commit (red -> green). A standalone test over code that already passes is its own commit.

Sections 1-4 were done under the first scope (print from the TEI dashboard menu, latest completed event of the enrollment, generic label/value lines). They stay checked as history. Section 5 reworks them for the scope confirmed by the client on 2026-11-04/06; any task of sections 1-4 marked **superseded** is replaced by the task named next to it.

## 1. Receipt domain (first scope)
Commit: one commit per behavior (test + minimal implementation).

- [x] 1.1 Test: printing a receipt sends it to the printer and returns success
- [x] 1.2 Implement `Receipt`, `ReceiptPrinter`, `ReceiptRepository`, `PrintReceiptUseCase` (draft)
- [x] 1.3 Test: printing fails with "no printer connected" when no printer is connected
- [x] 1.4 Implement `PrinterError` and its propagation through the use case
- [x] 1.5 Test: printing returns a print-failed error when the connected printer fails to print
- [x] 1.6 Implement `PrinterError.PrintFailed`

## 2. Fake printer and formatter (first scope)
Commit: one commit per behavior (test + minimal implementation).

- [x] 2.1 Test: the formatted receipt contains a label and value line for each data value of the event (**superseded** by 5.5-5.7: generic lines and plain-text formatter are replaced by the fixed receipt content and the bitmap renderer)
- [x] 2.2 Implement `ReceiptFormatter` (**superseded** by 5.8)
- [x] 2.3 Implement `FakeReceiptPrinter` (development printer that logs the receipt; no unit test, it has no logic of its own)

## 3. Presentation: view model, printing host and menu trigger (first scope)
Commit: one commit per behavior (UI test first, then the minimal implementation). Design per the Android architecture guidance: the view model is screen-scoped, exposes a single `uiState`, and never holds a `Context`; ViewModel events become UI state (no one-shot events); the printing dialog is stateless (state + `onXxx` lambdas).

Testing approach: the presentation pillar is tested through what the user sees, in the style of `BiometricsAttributeProviderTest` (Compose UI test, `androidTest`), not through the view model API. The test mounts `ReceiptPrintHost` with the real view model and the real `PrintReceiptUseCase`, doubling only the repository and the printer (fakes). The use case is covered implicitly: each scenario also asserts the effect on the fake printer. Strings are read from resources, not literals.

- [x] 3.1 Test: pressing "Print receipt" shows the printing dialog while the printer works, then the printed confirmation, and the printer received the receipt of the event
- [x] 3.2 Implement `ReceiptPrintViewModel`, `ReceiptPrintHost`, the stateless printing dialog (not dismissable while printing) and strings
- [x] 3.3 Test: with no printer connected, the no-printer error stays on screen until it is accepted and nothing is printed
- [x] 3.4 Implement the typed messages (`Printed`, `NoPrinterConnected`, `PrintFailed`) and keeping errors until accepted
- [x] 3.5 Test: when the printer fails to print, a different error message is shown than the no-printer one
- [x] 3.6 Test: pressing "Print receipt" twice while printing prints only one receipt
- [x] 3.7 Implement the guard against a second print while one is in progress
- [x] 3.8 Add `PRINT_RECEIPT` to the TEI dashboard menu and wire it, mirroring `SEND_SMS` (**superseded** by 5.1-5.2: the trigger moves to the event screen; the dashboard wiring is removed). `ReceiptServiceLocator` and the per-build-type `provideReceiptPrinter()` stay.
- [x] 3.10 Test: with nothing to print for the enrollment, the no-receipt-available error stays on screen until it is accepted (**superseded** by 5.4)
- [x] 3.11 Implement `PrintReceiptError.NoReceiptAvailable` (**superseded** by 5.4)
- [x] 3.9 Manual validation on a physical device (dashboard trigger): menu shown, dialog while printing, confirmation, no-printer error, rotating mid-print neither cancels nor repeats the print

## 4. Docs (first scope)
Commit: single docs commit.

- [x] 4.1 Register Print Receipt in `customization-files.md` and `upgrade-validation-checklist.md` (the Oslo touch points listed there change with 5.2 and must be updated by 5.14)

## 5. Rework for the confirmed scope (event trigger, fixed content, image, flag)
Commit: one commit per behavior (test + minimal implementation). Do not start before the client answers the open questions that block each task (noted per task).

### Trigger and contract
- [x] 5.1 Identify the Oslo touch points of the event screen (menu enum/dispatch and activity) and record them in the proposal Impact section
- [x] 5.2 Move the trigger to the event screen and remove the dashboard wiring (`PRINT_RECEIPT` in `EnrollmentMenuItem`, `buildMenuForEnrollment`, `mountReceiptPrintHost` in `TeiDashboardMobileActivity`, `customClick` branch). No new automated test: it is wiring in Oslo code with no logic of its own (the stage rule is covered by `ReceiptMenuAvailabilityTest`; the printing flow by `ReceiptPrintHostTest`, a Compose test of the host without the activity). Manual validation on a device is still pending (checklist, 5.14): option shown only in a Distribution event, completed or not; printing does not complete the event
- [x] 5.3 Test: printing takes an event uid; the use case loads the receipt of that event and prints it (replaces the enrollment contract of 1.1-1.2)
- [ ] 5.4 Decide whether `NoReceiptAvailable` is removed: with TEI attributes the receipt is never empty. If removed, delete the error, its string and the 3.10/3.11 behaviour

### Content
- [ ] 5.5 Test: the receipt shows the distribution name (option name, not code), ration card ID and beneficiary name
- [ ] 5.6 Test: only items with a value appear, each with its label (`displayFormName`) and quantity; items are selected by data element code `sp_DIST_*_GIVEN`
- [ ] 5.7 Test: an "Other item" shows its description and quantity when both are present, and is omitted otherwise
- [ ] 5.7b Distribution point and date/time: **blocked** until the client confirms the source of the distribution point (provisional: event org unit name) and whether the time is the printing moment or the event date
- [ ] 5.8 Implement `Receipt` (header, optional signature, item lines) and `D2ReceiptRepository` reading TEI attributes and event data values for `eventUid`

### Image
- [ ] 5.9 Test: the receipt is rendered as a bitmap of the printable width of the roll (about 384 dots)
- [ ] 5.10 Implement `ReceiptRenderer` (header, items, logo, signature)
- [ ] 5.11 Test: the signature image is included when the event has one, and the receipt still prints when it has none (**blocked**: confirm empty-signature behaviour); implement signature reading through the SDK file resource
- [ ] 5.12 Add the SP logo asset (**blocked**: logo file from the client)

### Flag
- [ ] 5.13 Test: after a successful print the "Receipt Printed" flag is set; it is not set when printing fails; a failure to write it does not change the success message; the event is not completed; implement `ReceiptPrintedMarker`

### Docs
- [ ] 5.14 Update `customization-files.md` and `upgrade-validation-checklist.md` with the event-screen touch points, the new files, and the metadata UIDs. Done for the event-screen touch points, the wiring file and the checklist flow; the files and UIDs of 5.5-5.13 are added as those tasks land

## Pending (not scheduled)
- [ ] Review the Brother SDK (licence, distribution, support for the RJ-2055WB) and the command reference at support.brother.com, and decide SDK vs raster commands
- [ ] Real Brother `ReceiptPrinter`: look among paired printers and connect (one per tablet), send the bitmap, report `NoPrinterConnected`/`PrintFailed`
  - Turn `PrintReceiptError.PrintFailed` into a class that carries the original cause (`PrintFailed(cause)`): as a singleton its stack trace is captured at class load.
- [ ] Bluetooth permissions: `BLUETOOTH`/`BLUETOOTH_ADMIN` in the spIP manifest (check what `main` declares) and the runtime flow for `BLUETOOTH_CONNECT`/`BLUETOOTH_SCAN` on Android 12+ (flow testable on an API 31+ emulator; real behaviour needs a physical tablet)
- [ ] Re-estimate once the Brother unit arrives (the September estimate assumed a Zebra and plain text)
- [x] Restrict the menu option to the Distribution stage of Beneficiary Hub (done with `isPrintReceiptAvailable`; the production UIDs are still to confirm)
- [ ] Printer firmware/paper configuration identical across production printers (operational, with the client)
- [ ] Validate product texts with the client (`receipt_no_printer_connected`, `receipt_print_failed`)
