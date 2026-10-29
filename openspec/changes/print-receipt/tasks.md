Commit rule: a test task immediately followed by its implement task is one commit (red -> green). A standalone test over code that already passes is its own commit.

## 1. Receipt domain
Commit: one commit per behavior (test + minimal implementation).

- [x] 1.1 Test: printing a receipt sends it to the printer and returns success
- [x] 1.2 Implement `Receipt`, `ReceiptPrinter`, `ReceiptRepository`, `PrintReceiptUseCase` (draft)
- [x] 1.3 Test: printing fails with "no printer connected" when no printer is connected
- [x] 1.4 Implement `PrinterError` and its propagation through the use case
- [x] 1.5 Test: printing returns a print-failed error when the connected printer fails to print
- [x] 1.6 Implement `PrinterError.PrintFailed`

## 2. Fake printer and formatter
Commit: one commit per behavior (test + minimal implementation).

- [x] 2.1 Test: the formatted receipt contains a label and value line for each data value of the event
- [x] 2.2 Implement `ReceiptFormatter`
- [x] 2.3 Implement `FakeReceiptPrinter` (development printer that logs the receipt; no unit test, it has no logic of its own: the flow is covered in the use case and view model tests and validated manually in 3.5)

## 3. Presentation: view model, printing dialog and menu trigger
Commit: one commit per behavior (test + minimal implementation). Design per the Android architecture guidance: the view model is screen-scoped (the TEI dashboard activity), exposes a single `uiState`, and never holds a `Context`; ViewModel events become UI state (no one-shot events); the dialog is stateless (state + `onXxx` lambdas).

- [ ] 3.1 Test (JVM, real use case, fake repository and printer): printing sets `isPrinting` while the printer is working and then leaves a success message
- [ ] 3.2 Implement `ReceiptPrintViewModel` with `uiState` (`isPrinting`, `userMessage`) and `print(eventUid)`
- [ ] 3.3 Test: printing with no printer connected leaves a no-printer error message and `isPrinting` false
- [ ] 3.4 Implement the typed messages (`Printed`, `NoPrinterConnected`, `PrintFailed`) mapped from the use case `Result`
- [ ] 3.5 Test: after the message is shown, `messageShown()` clears it
- [ ] 3.6 Implement `messageShown()`
- [ ] 3.7 Stateless printing dialog (non-dismissable while printing; errors stay until accepted) and strings. Compose test (androidTest, one per state), following `BiometricsAttributeProviderTest`
- [ ] 3.8 Add `PRINT_RECEIPT` to the dashboard menu and wire it, mirroring `SEND_SMS`: the click calls `print(eventUid)`; the activity shows the dialog while `isPrinting` and the snackbar from `userMessage`, then calls `messageShown()`. The view model is created at activity scope, separate from `DashboardViewModel`.
  - `FakeReceiptPrinter` must only be bound in debug builds (`spIPDebug`), or the menu option stays hidden outside debug until the real printer exists: a release build must never report a receipt as printed when nothing was printed.
- [ ] 3.9 Manual validation on a physical device: menu shown, dialog while printing, success snackbar, no-printer error, rotating the device mid-print neither cancels nor repeats the print, double tap on the menu prints once

## 4. Docs
Commit: single docs commit.

- [ ] 4.1 Register Print Receipt in `customization-files.md` and `upgrade-validation-checklist.md`

## Pending (not scheduled)
- [ ] Connected-printer detection (paired is not connected) and no-printer error against the real device (estimate item 1)
- [ ] Real Bluetooth/Zebra `ReceiptPrinter` (RFCOMM socket or Zebra SDK) once the printer model is confirmed (estimate item 4)
  - Turn `PrinterError.PrintFailed` into a class that carries the original cause (`PrintFailed(cause)`): as a singleton object its stack trace is captured at class load and the real failure reason would be lost in the logs.
- [ ] Restrict the menu option to the distribution program/stage once its UIDs are known
- [ ] Automatic trigger on event completion if the client asks for it
