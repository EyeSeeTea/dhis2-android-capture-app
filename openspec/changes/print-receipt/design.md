## Context

Receipt = goods distributed for one household, one basket, one day, built from a completed event: distribution, distribution point, receiver and relation to the beneficiary, verification performed, real household size and quantity of each commodity delivered. Printing is offline over Bluetooth to a roll-ticket printer (probably Zebra). Pairing and connection are handled by Android Settings; the app only uses the printer that is currently connected. One receipt per action, no batch.

## Goals / Non-Goals

**Goals:** a testable end-to-end flow (menu -> receipt -> printer -> feedback) with the printer abstracted; domain that survives a change of trigger.

**Non-Goals:** real Bluetooth/Zebra integration, pairing UI, printer selection, fallback when no printer, batch printing, QR label printing (done by a WebApp), field-priority/truncation logic.

## Decisions

- **Placement:** all new code in the flavor source set `app/src/spIP/java/org/dhis2/usescases/receipt/` (precedent: `usescases/sms/`). Shared code is touched only for the menu enum entry (placement hierarchy level 4, justified in the proposal).
- **Trigger:** menu item **Print receipt** in the TEI dashboard menu, like Send Welcome SMS. Chosen only to be able to test; the view model exposes a single "print" action so an automatic trigger can call it later.
- **Domain (draft):**
  - `Receipt`: immutable value made of ordered label/value lines taken from the data values of the completed event (generic, not fixed fields), so it does not depend on a distribution data model that is not defined yet on the test server. The business fields listed in Context are what the lines are expected to contain.
  - `ReceiptPrinter`: interface `suspend fun print(receipt: Receipt)` that throws/returns a `PrinterError`.
  - `PrinterError`: sealed, at least `NoPrinterConnected` and `PrintFailed`.
  - `PrintReceiptUseCase`: implements `UseCase<String, Unit>` (input: event uid), loads the receipt, delegates to the printer, returns `Result`.
- **Connected printer:** among the printers paired at OS level, the app uses the one currently connected (paired is not connected). With the abstraction this is the responsibility of the real `ReceiptPrinter`, which reports `NoPrinterConnected`; the fake can simulate it.
- **Dependency creation:** a hand-written `ReceiptServiceLocator` in the flavor source set, next to `SPIPServiceLocator` (SMS), instead of Oslo's Dagger components or Koin modules, to avoid conflicts in future upgrades. This deviates from the Koin guideline in `AGENTS.md` for the same reason SMS does. The locator is only called from the composition root (activity and menu wiring), never from `domain` or `presentation` logic, and tests build the view model by hand with fakes. Because the view model has state, the locator provides a `ViewModelProvider.Factory` (view model at activity scope), not the instance. The development printer is selected per build type without a DI framework: `provideReceiptPrinter()` is defined in `spIPDebug` (fake) and `spIPRelease` (no printer yet).
- **Printer implementations:** `FakeReceiptPrinter` (records output, can simulate errors) is wired for now; the real one replaces it behind the same interface via DI.
- **Formatter:** `ReceiptFormatter` turns a `Receipt` into plain text lines. The protocol-specific encoding (ZPL) belongs to the real printer implementation.
- **Event selection (assumption, pending):** from the dashboard menu, print the most recent completed event of the enrollment. To confirm with the client.
- **Feedback:** reuse the existing custom snackbar used by the SMS flow (success / error).
- **No SDK fork dependency.**

## Risks / Trade-offs

- Domain fields may change once the client confirms the layout; mitigated by keeping the domain small and marked as draft.
- The menu trigger lives in a dashboard-level menu while a receipt is per event; may be wrong UX, accepted for testing.
- Real Bluetooth behaviour (encoding, roll width, connection state) can only be validated with the physical printer.

## Open Questions

Copies per recipient (beneficiary and staff), printer model/protocol, receipt layout, final trigger and reprint, event selection rule.
