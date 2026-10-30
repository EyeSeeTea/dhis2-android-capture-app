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

## 3. Presentation: view model, printing host and menu trigger
Commit: one commit per behavior (UI test first, then the minimal implementation). Design per the Android architecture guidance: the view model is screen-scoped (the TEI dashboard activity), exposes a single `uiState`, and never holds a `Context`; ViewModel events become UI state (no one-shot events); the printing dialog is stateless (state + `onXxx` lambdas).

Testing approach: the presentation pillar is tested through what the user sees, in the style of `BiometricsAttributeProviderTest` (Compose UI test, `androidTest`), not through the view model API. The test mounts `ReceiptPrintHost` with the real view model and the real `PrintReceiptUseCase`, doubling only the repository and the printer (fakes). The use case is covered implicitly: each scenario also asserts the effect on the fake printer. The explicit use case tests of section 1 stay. Strings are read from resources, not literals.

- [x] 3.1 Test: pressing "Print receipt" shows the printing dialog while the printer works, then the printed confirmation, and the printer received the receipt of the event
- [x] 3.2 Implement `ReceiptPrintViewModel` (`uiState` with `isPrinting` and `userMessage`, `print(enrollmentUid)`, `messageShown()`), `ReceiptPrintHost`, the stateless printing dialog (not dismissable while printing) and strings
- [x] 3.3 Test: with no printer connected, the no-printer error stays on screen until it is accepted and nothing is printed
- [x] 3.4 Implement the typed messages (`Printed`, `NoPrinterConnected`, `PrintFailed`) mapped from the use case `Result`, and keeping errors until accepted
- [x] 3.5 Test: when the printer fails to print, a different error message is shown than the no-printer one
- [x] 3.6 Test: pressing "Print receipt" twice while printing prints only one receipt
- [x] 3.7 Implement the guard against a second print while one is in progress
- [x] 3.8 Add `PRINT_RECEIPT` to the dashboard menu and wire it, mirroring `SEND_SMS`: the click calls `print(enrollmentUid)`; the activity mounts `ReceiptPrintHost`. The view model is created at activity scope, separate from `DashboardViewModel`.
  - Dependencies come from a hand-written `ReceiptServiceLocator` (flavor source set, like `SPIPServiceLocator`) that provides a `ViewModelProvider.Factory`; it is only called from this wiring. `provideReceiptPrinter()` is defined in `spIPDebug` (`FakeReceiptPrinter`) and `spIPRelease` (create the source set).
  - `FakeReceiptPrinter` is only bound in debug builds (`spIPDebug`). Release (`spIPRelease`) binds a printer that always reports `NoPrinterConnected`, so a release build never reports a receipt as printed when nothing was printed (the menu option stays visible).
  - Added for the wiring: `D2ReceiptRepository`, which receives the enrollment uid and resolves internally which event to print (latest completed event of the enrollment, the assumption of design.md), then reads its data values (label from the data element, user-friendly value). Validated manually in 3.9, with no automated test.
- [x] 3.10 Test: with nothing to print for the enrollment, the no-receipt-available error stays on screen until it is accepted and nothing is printed
- [x] 3.11 Implement `PrintReceiptError.NoReceiptAvailable`: `ReceiptRepository.getReceipt` returns `null` when the enrollment has no completed event or the event has no values; the use case returns the error; the host shows it (text pending validation with the client)
- [ ] 3.9 Manual validation on a physical device: menu shown, dialog while printing, confirmation, no-printer error, rotating the device mid-print neither cancels nor repeats the print

## 4. Docs
Commit: single docs commit.

- [x] 4.1 Register Print Receipt in `customization-files.md` and `upgrade-validation-checklist.md`

## Pending (not scheduled)
- [ ] Connected-printer detection (paired is not connected) and no-printer error against the real device (estimate item 1)
- [ ] Real Bluetooth/Zebra `ReceiptPrinter` (RFCOMM socket or Zebra SDK) once the printer model is confirmed (estimate item 4)
  - Turn `PrintReceiptError.PrintFailed` into a class that carries the original cause (`PrintFailed(cause)`): as a singleton object its stack trace is captured at class load and the real failure reason would be lost in the logs.
- [ ] Restrict the menu option to the distribution program/stage once its UIDs are known
- [ ] Automatic trigger on event completion if the client asks for it
