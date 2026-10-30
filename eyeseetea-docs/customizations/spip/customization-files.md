# spIP customization files vs develop-eyeseetea

Technical inventory of the spIP customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `spip`
- Flavor: `spIP`
- Base branch: `develop-eyeseetea`
- Base commit: `d87193d003` (merge-base with `develop-eyeseetea`, upstream 3.3.1; spIP is currently `3.3.1-spIP-fork-3`)
- Generated on: `2026-10-30`
- Working tree status: `clean`

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch
- this file documents the spIP-specific implementation points

## Scope

This inventory is incremental. Only customizations created after the onboarding of `eyeseetea-docs/` are documented here, one at a time, as they are built. Pre-existing spIP customizations are **not** inventoried yet, see the pending item below.

## Pending: document pre-existing customizations

TODO (do it during the next upgrade of spIP, no date set): spIP existed before `eyeseetea-docs/` was introduced and its older customizations are undocumented here (for example Send Welcome SMS, image sync, file download limit).

When the upgrade starts, follow Phase 3 and Phase 4 of `eyeseetea-docs/onboarding-fork-guide.md`:
- list the surface with `git diff develop-eyeseetea..develop-spip --stat` (on 2026-10-28: about 105 files under `app/src/spIP/` and about 526 outside it; spIP is on 3.3.1 while `develop-eyeseetea` is on 3.4.2)
- confirm the real business customizations with the developer who owns them, do not infer functional titles from the diff
- write one `openspec/specs/<capability>/spec.md` per active customization and add the `// EyeSeeTea customization - [Title]` comments
- fill section 2 and the validation checklist with the confirmed customizations
- only then run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client spip`; it is expected to fail until the inventory is complete, so it is used only during the upgrade

## 1. Direct spIP flavor surface

### 1.1 Flavor code

- `app/src/spIP/`

### 1.2 Flavor resources and branding

- `app/src/spIP/res/`
- `app/src/spIPDebug/`
- `app/src/spIPRelease/` (created for Print Receipt: binds the release-safe printer)

### 1.3 Flavor test source sets

- `app/src/testSpIP/` (JVM unit tests)
- `app/src/androidTestSpIP/` (instrumented Compose tests)

## 2. Shared-code customization implementation points

Entries are added here as they are built, using the exact `# Title` of the matching OpenSpec spec.

### 2.1 Print Receipt

Status: `needs_validation` (manual validation on a device pending, task 3.9 of the change)

Main implementation points (Oslo files touched, each marked `// EyeSeeTea customization - Print Receipt`):
- `app/src/main/java/org/dhis2/usescases/teiDashboard/TeiDashboardMobileActivity.kt` (`PRINT_RECEIPT` value in `EnrollmentMenuItem`, and `mountReceiptPrintHost(this)` at the end of `onCreate`)
- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDashboardMenu.kt` (one line in `buildMenuForEnrollment`: `addPrintReceiptMenuItem`)
- `app/src/spIP/java/org/dhis2/usescases/teiDashboard/TeiDashboardMenu.kt` (flavor `customClick`: routes `PRINT_RECEIPT` to the receipt wiring; the click does not touch the activity `when`)

Supporting files in the same workflow (all new, flavor-only):
- `app/src/spIP/java/org/dhis2/usescases/receipt/ReceiptServiceLocator.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/model/PrintReceiptError.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/model/Receipt.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/model/ReceiptLine.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/repository/ReceiptPrinter.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/repository/ReceiptRepository.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/domain/usecase/PrintReceiptUseCase.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/data/D2ReceiptRepository.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/data/FakeReceiptPrinter.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/data/ReceiptFormatter.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/presentation/ReceiptDashboardWiring.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/presentation/ReceiptPrintHost.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/presentation/ReceiptPrintMessage.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/presentation/ReceiptPrintUiState.kt`
- `app/src/spIP/java/org/dhis2/usescases/receipt/presentation/ReceiptPrintViewModel.kt`
- `app/src/spIP/res/values/strings.xml` (`receipt_*` strings)
- `app/src/spIPDebug/java/org/dhis2/usescases/receipt/ReceiptPrinterProvider.kt` (binds `FakeReceiptPrinter`)
- `app/src/spIPRelease/java/org/dhis2/usescases/receipt/ReceiptPrinterProvider.kt` (always reports no printer connected)
- `app/src/testSpIP/java/org/dhis2/usescases/receipt/data/ReceiptFormatterTest.kt`
- `app/src/testSpIP/java/org/dhis2/usescases/receipt/domain/usecase/PrintReceiptUseCaseTest.kt`
- `app/src/androidTestSpIP/java/org/dhis2/usescases/receipt/presentation/ReceiptPrintHostTest.kt`
- `commonskmm/src/commonMain/kotlin/org/eyeseetea/functional/Either.kt`
- `commonskmm/src/commonTest/kotlin/org/eyeseetea/functional/EitherTest.kt`

Technical note:
- The Oslo touches are three marked lines; everything else lives in the flavor. Dependencies come from a hand-written `ReceiptServiceLocator` (like `SPIPServiceLocator` for SMS) instead of Dagger/Koin, to avoid conflicts in upgrades.
- `Either.kt` and its test are not spIP code: they were added to `develop-eyeseetea` (`173334dbb`, technique T6 in `eyeseetea-docs/customization-techniques.md` there) and cherry-picked here because this branch cannot merge the baseline outside an upgrade. After the upgrade merge they are the same file; drop the cherry-picked copy if git reports a conflict.
- The printer is a development fake: debug logs the receipt, release always reports no printer connected. The real Bluetooth printer is pending the printer model.

## 3. Shared drift still differing

Pending, see "Pending: document pre-existing customizations". Nothing has been classified yet, so every difference against `develop-eyeseetea` other than the customizations in section 2 is unclassified drift.

## 4. Notes

Feat commits of Print Receipt (`git show <sha> --stat` to check that no wiring file is missing from section 2.1):
- `df5b67996` — feat(spIP): print receipt use case
- `c02974777` — feat(spIP): return no-printer-connected error when printing
- `879d65653` — feat(spIP): format receipt lines for printing
- `a479e5cd5` — feat(spIP): add development receipt printer
- `d64c16133` — feat(spIP): return print-failed error when the printer fails
- `eec1a1b1a` — feat(spIP): show printing dialog and confirmation when printing a receipt
- `7739a3d2d` — feat(spIP): show a no-printer error until it is accepted
- `394e9c7dc` — feat(spIP): show a print-failed error when the printer fails
- `ae3783de1` — feat(spIP): ignore a second print request while one is in progress
- `f25fcd980` — feat(spIP): add Print receipt to the dashboard menu
- `02e1ae4e5` — feat: add Either for typed domain outcomes (cherry-pick from `develop-eyeseetea`)
- `327c6df0a` — refactor(spIP): return Either instead of Result from print receipt
- `9a0dc9253` — feat(spIP): show a no-receipt error when there is nothing to print

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
