Commit rule: a test task immediately followed by its implement task is one commit (red -> green). A standalone test over code that already passes is its own commit.

## 1. Receipt domain
Commit: one commit per behavior (test + minimal implementation).

- [x] 1.1 Test: printing a receipt sends it to the printer and returns success
- [x] 1.2 Implement `Receipt`, `ReceiptPrinter`, `ReceiptRepository`, `PrintReceiptUseCase` (draft)
- [x] 1.3 Test: printing fails with "no printer connected" when no printer is connected
- [x] 1.4 Implement `PrinterError` and its propagation through the use case
- [ ] 1.5 Test: printing returns a print-failed error when the connected printer fails to print
- [ ] 1.6 Implement `PrinterError.PrintFailed`

## 2. Fake printer and formatter
Commit: one commit per behavior (test + minimal implementation).

- [x] 2.1 Test: the formatted receipt contains a label and value line for each data value of the event
- [x] 2.2 Implement `ReceiptFormatter`
- [x] 2.3 Implement `FakeReceiptPrinter` (development printer that logs the receipt; no unit test, it has no logic of its own: the flow is covered in the use case and view model tests and validated manually in 3.5)

## 3. Menu trigger and feedback
Commit: one commit per behavior (test + minimal implementation).

- [ ] 3.1 Test: the view model exposes a success message after a successful print
- [ ] 3.2 Test: the view model exposes an error message when no printer is connected
- [ ] 3.3 Implement `ReceiptPrintViewModel` and strings
- [ ] 3.4 Add `PRINT_RECEIPT` to the dashboard menu and wire the click, mirroring `SEND_SMS`
  - `FakeReceiptPrinter` must only be bound in debug builds (`spIPDebug`), or the menu option stays hidden outside debug until the real printer exists: a release build must never report a receipt as printed when nothing was printed.
- [ ] 3.5 Manual validation on a physical device (menu shown, success and error snackbar)

## 4. Docs
Commit: single docs commit.

- [ ] 4.1 Register Print Receipt in `customization-files.md` and `upgrade-validation-checklist.md`

## Pending (not scheduled)
- [ ] Connected-printer detection (paired is not connected) and no-printer error against the real device (estimate item 1)
- [ ] Real Bluetooth/Zebra `ReceiptPrinter` (RFCOMM socket or Zebra SDK) once the printer model is confirmed (estimate item 4)
- [ ] Restrict the menu option to the distribution program/stage once its UIDs are known
- [ ] Automatic trigger on event completion if the client asks for it
