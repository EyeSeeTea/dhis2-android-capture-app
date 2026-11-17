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
- [x] 5.4 Remove `NoReceiptAvailable` (decided 2026-11-17: with TEI attributes the receipt is never empty). Done: the error, its message, its string and the 3.10/3.11 behaviour are deleted; `ReceiptRepository.getReceipt` now returns a non-null `Receipt` and `D2ReceiptRepository` no longer discards an empty one. An event that does not exist would give a receipt with only the uid: it cannot happen from the event screen

### Content
- [x] 5.5 Test: the receipt shows the distribution name (option name, not code), SPBH ID (renamed from ration card ID in 5.5b) and beneficiary name. Written as a unit test of `D2ReceiptRepository` with `D2` doubled at the SDK boundary, link by link as in `OptionsRepositoryTest`. The metadata UIDs are injected through `ReceiptMetadata`. It does not cover the real metadata of the server: validate by hand against 8380
- [x] 5.6 Test: only items with a value appear, each with its label (`displayFormName`) and quantity; items are selected by data element code `sp_DIST_*_GIVEN`. Same repository unit test as 5.5. A blank value counts as no value (`userFriendlyValue` returns an empty string for it, not null)
- [x] 5.7 Test: an "Other food" slot (4 slots) and an "Other item" slot (3 slots) show the name assigned in the event (`_DESC`) as label and the quantity (`_GIVEN`) when a quantity is present, and are omitted when there is no quantity; with a quantity but an empty description the generic label is used; the `_DESC` elements never print as items of their own. Same treatment for Other food and Other item (decided 2026-11-15). UIDs and codes (`sp_DIST_OTHER_FOOD_N_*`, `sp_DIST_OTHER_ITEM_N_*`) confirmed on 8380
- [x] 5.7b Test: the receipt shows the distribution point (name of the event's org unit), the Distribution date (event date) and "Receipt printed on" (date and time of the real print, so a reprint shows its own time). Confirmed by the client 2026-11-11. Done: `Receipt.distributionPoint`/`distributionDate`/`printedOn`; the repository reads the event once (org unit name, event date); `PrintReceiptUseCase` stamps `printedOn` from an injected clock on every print; `ReceiptFormatter` prints the three lines (dates as `yyyy-MM-dd`, print time as `yyyy-MM-dd HH:mm`, provisional format)
- [x] 5.7c Test: the receipt shows Geographical Location 1, 2 and 3 (TEI attributes `lbkbogyOSwS`, `UqBDyVDudDV`, `uHbGxeMBkzK`, confirmed on 8380). Done: `Receipt.geographicalLocations` (list of `GeographicalLocation(level, name)`, in level order, a level without value is left out and the others keep their level); the UIDs are in `ReceiptMetadata.geographicalLocationAttributeUids`; the formatter prints `Geographical Location N: name`. Level 1 has an option set (the name is printed, not the code); levels 2 and 3 are free text. Not validated by hand: the test beneficiary has no value in these attributes
- [x] 5.5b Replace `rationCardId` by the SPBH ID (`SPBH - Unique Identifier`, the ID printed on the ration card) in `Receipt`, `ReceiptMetadata`, `D2ReceiptRepository` and `D2ReceiptRepositoryTest`: the client dropped the separate Ration Card ID. Attribute UID `DjPrtej5I1x`, confirmed on 8380 (2026-11-11)
- [x] 5.8b Adapt `ReceiptFormatter` (and its test) from the generic lines to the header and the items, as the plain-text format sent to the printer; then remove the generic `lines`
- [ ] 5.8c Demo for Ramón (no new code): a video or image of the event form and the plain-text receipt as the development printer logs it (header, geographical locations, items, print time). Add a note of what the plain-text version does not include (SP logo and beneficiary signature) so that the client decides, depending on the hours, whether the bitmap (5.9-5.12) is worth it. Decided 2026-11-17: the receipt keeps a blank line to sign by hand (see 5.8e); item labels stay as the form name comes (with "given"); item order stays unfixed until the client decides; the cash data element (`sp_DIST_CASH_AMOUNT_GIVEN`) stays as one more item, since it matches the `sp_DIST_*_GIVEN` rule (exclude it with a test only if the client says otherwise)
- [x] 5.8e Test: the plain-text receipt ends with a blank signature line for the beneficiary to sign by hand (one text line, no bitmap); implement it in `ReceiptFormatter`. The line must fit the 57 mm roll (see 5.8d). Done: `ReceiptFormatter` always ends with `Signature: ____________` (provisional text; also on a receipt without items); the width is to adjust with 5.8d
- [ ] 5.8d Test: the plain-text lines fit the 57 mm roll (no line longer than the characters per line, long labels wrap). The width is to confirm from the RJ-2055WB specification and a test on the real printer font. Client data 2026-11-16: 57 mm thermal paper, 30 mm maximum outer diameter
- [ ] 5.8 Implement `Receipt` (header, optional signature, item lines) and `D2ReceiptRepository` reading TEI attributes and event data values for `eventUid`. Header done with 5.5 (`Receipt.distributionName`/`rationCardId`/`beneficiaryName`, `ReceiptMetadata`, TEI attributes); item lines done with 5.6 (`Receipt.items`); the generic `lines` and `ReceiptLine` were removed with 5.8b; signature pending. The receipt rules (items by code `sp_DIST_*_GIVEN`, name from the `_DESC` sibling) stay in `D2ReceiptRepository`: they depend on the DHIS2 metadata, so they are not moved to the domain. The blank checks on values are defensive (DHIS2 does not store empty values); remove them if 8380 confirms that blank values never arrive

### Image (deferred: first version in plain text)
The client prefers a first version in plain text and will decide, depending on the hours available, whether to move to a bitmap with the signature and the logo. Until then 5.9-5.12 are not scheduled, and the plain-text `ReceiptFormatter` stays as the way to send the receipt (see 5.8b).
- [ ] 5.9 Test: the receipt is rendered as a bitmap of the printable width of the 57 mm roll (width to confirm from the RJ-2055WB specification; the earlier 384 dots assumed a 58 mm roll)
- [ ] 5.10 Implement `ReceiptRenderer` (header, items, logo, signature)
- [ ] 5.11 Test: the signature image is included when the event has one, and the receipt still prints when it has none (no signature: the receipt is printed anyway, confirmed 2026-11-11; deferred with the bitmap); implement signature reading through the SDK file resource
- [ ] 5.12 Add the SP logo asset (deferred with the bitmap; logo file from the client)

### Flag
- [x] 5.13 Test: after a successful print the "Receipt Printed" flag is set; it is not set when printing fails; a failure to write it does not change the success message; the event is not completed. Done with a different design than the first plan (decided 2026-11-17, no `ReceiptPrintedMarker` port): `Receipt` carries its identity (`eventUid`) and `printed` (`markAsPrinted()`); `PrintReceiptUseCase` prints, marks the receipt as printed and saves it through the same `ReceiptRepository.save`, which persists only the print status; `D2ReceiptRepository.save` writes `true` in `sp_DIST_RECEIPT_PRINTED` (`FHRqcSyJxLr`, `TRUE_ONLY`, in no section of the form) and writes nothing for a receipt that is not printed. Best effort: a failure to save is logged (`Timber.w`) and the success is still returned; cancellation is rethrown. The UID is in `ReceiptMetadata.printedFlagDataElementUid`. Validated by hand on 8380 (2026-11-17): the flag is `true` on an active and on a completed event, the status does not change and the events not printed have no flag. Not done: reading the flag back into `Receipt.printed` when the receipt is loaded (nothing uses it yet); `Receipt.eventUid` is required (a receipt without identity cannot be saved)

### Docs
- [ ] 5.14 Update `customization-files.md` and `upgrade-validation-checklist.md` with the event-screen touch points, the new files, and the metadata UIDs. Done for the event-screen touch points, the wiring file and the checklist flow; done up to 5.13 on 2026-11-17 (the flag adds no new file); the bitmap files of 5.9-5.12 are added if those tasks are done

## Pending (not scheduled)
- [ ] Real Brother `ReceiptPrinter` adapter prints the plain-text receipt (Ramón: first version in plain text; bitmap with logo and signature only if the client confirms and hours allow)
- [ ] Review the Brother SDK (licence, distribution, support for the RJ-2055WB) and the command reference at support.brother.com, and decide SDK vs raster commands
- [ ] Real Brother `ReceiptPrinter`: look among paired printers and connect (one per tablet), send the bitmap, report `NoPrinterConnected`/`PrintFailed`
  - Turn `PrintReceiptError.PrintFailed` into a class that carries the original cause (`PrintFailed(cause)`): as a singleton its stack trace is captured at class load.
- [ ] Bluetooth permissions: `BLUETOOTH`/`BLUETOOTH_ADMIN` in the spIP manifest (check what `main` declares) and the runtime flow for `BLUETOOTH_CONNECT`/`BLUETOOTH_SCAN` on Android 12+ (flow testable on an API 31+ emulator; real behaviour needs a physical tablet)
- [ ] Re-estimate once the Brother unit arrives (the September estimate assumed a Zebra and plain text); the paper is 57 mm wide
- [x] Restrict the menu option to the Distribution stage of Beneficiary Hub (done with `isPrintReceiptAvailable`; the client confirmed the production UIDs are the same as on 8380)
- [ ] Printer firmware/paper configuration identical across production printers (operational, with the client)
- [ ] Test the generic "57 x 30 mm" rolls (local option) against the Brother Economy RD001U5V2 used for the first tests
- [ ] Validate product texts with the client (`receipt_no_printer_connected`, `receipt_print_failed`)
