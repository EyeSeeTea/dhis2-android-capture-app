Commit rule: a test task immediately followed by its implement task is one commit (red -> green). A standalone test over code that already passes is its own commit.

## 1. Receipt domain
Commit: one commit per behavior (test + minimal implementation).

- [ ] 1.1 Test: printing a receipt sends it to the printer and returns success
- [ ] 1.2 Implement `Receipt`, `ReceiptPrinter`, `PrintReceiptUseCase` (draft)
- [ ] 1.3 Test: printing fails with "no printer connected" when no printer is connected
- [ ] 1.4 Implement `PrinterError` and its propagation through the use case

## 2. Fake printer and formatter
Commit: one commit per behavior (test + minimal implementation).

- [ ] 2.1 Test: the formatted receipt contains a label and value line for each data value of the event
- [ ] 2.2 Implement `ReceiptFormatter`
- [ ] 2.3 Test: the fake printer records the printed text and can simulate "no printer" and "print failed"
- [ ] 2.4 Implement `FakeReceiptPrinter`

## 3. Menu trigger and feedback
Commit: one commit per behavior (test + minimal implementation).

- [ ] 3.1 Test: the view model exposes a success message after a successful print
- [ ] 3.2 Test: the view model exposes an error message when no printer is connected
- [ ] 3.3 Implement `ReceiptPrintViewModel` and strings
- [ ] 3.4 Add `PRINT_RECEIPT` to the dashboard menu and wire the click, mirroring `SEND_SMS`
- [ ] 3.5 Manual validation on a physical device (menu shown, success and error snackbar)

## 4. Docs
Commit: single docs commit.

- [ ] 4.1 Register Print Receipt in `customization-files.md` and `upgrade-validation-checklist.md`

## Pending (not scheduled)
- [ ] Connected-printer detection (paired is not connected) and no-printer error against the real device (estimate item 1)
- [ ] Real Bluetooth/Zebra `ReceiptPrinter` (RFCOMM socket or Zebra SDK) once the printer model is confirmed (estimate item 4)
- [ ] Restrict the menu option to the distribution program/stage once its UIDs are known
- [ ] Automatic trigger on event completion if the client asks for it
