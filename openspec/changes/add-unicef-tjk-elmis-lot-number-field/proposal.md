> **⚠️ Post-iteration note.** Parts of this proposal describe the *original* plan and no longer match the shipped code. Notably superseded: file paths moved from `org/dhis2/form/lotnumber/` to split `data/` + `model/` + `di/` packages; `LotNumberFieldConstants` → `LotNumberDataElements` with a `Set` of lot UIDs; `LotNumbersDTO` → `LotNumbersApiResponse`; the single lot UID → `LOT_NUMBER_DE_UIDS`; and the entire `app`-side `LotNumberSyncRepository` + `LotNumberSyncRepositoryNoOp` + `BuildConfig.FLAVOR` gate were **removed** in favor of a `RefreshLotNumbersCacheUseCase` (domain) with no flavor gate (this branch only builds `unicefTjkElmis`). The dialog is now driven by a `ViewModel`, and the repository is main-safe via an injected dispatcher. The authoritative inventory of the *current* code is `review-walkthrough.md` and `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md`; this file is retained for design intent and history.

## Why

Nurses using the UNICEF TJK eLMIS event form record medication administration per patient visit. For each product administered, the nurse must record the **lot number** so that stock can be reconciled when the event is uploaded to the server (the OpenBoxes ↔ DHIS2 sync pipeline matches consumption to stock by lot). Today the lot number is a plain free-text DataElement — nurses must remember or transcribe lot numbers from physical packaging, which is slow and error-prone.

The OpenBoxes ↔ DHIS2 sync already publishes the lot numbers available per organisation unit and product to the DHIS2 datastore (namespace `openboxes-dhis2-sync`, key `available-lot-numbers`). This change surfaces that data in the form: tapping a search affordance next to the lot number field opens a dialog listing the lot numbers available for the event's org unit and the product selected elsewhere in the same event — while always retaining the ability to type a lot number manually.

## What Changes

- A new "Lot Number" field input renders as a text field plus a search button, for a specific DataElement (hardcoded UID, UNICEF TJK eLMIS programme-specific).
- Tapping the search button opens a dialog listing lot numbers available for `(event org unit code, selected product code)`, sourced from a cached copy of the `openboxes-dhis2-sync/available-lot-numbers` datastore entry.
- The "selected product" is read from another DataElement in the same event (an OptionSet field whose stored value is the product option's `code`, matching `productCode` in the datastore JSON: `{orgUnitCode: {productCode: {lotNumbers: [...]}}}`).
- The datastore lookup is **network-first**: when the dialog opens, the app calls `GET dataStore/openboxes-dhis2-sync/available-lot-numbers` directly via `d2.httpServiceClient()` (same pattern as the existing `NotificationsApi`/`NotificationD2Repository` customization), and refreshes a local cache with the response. If the call fails (offline or error), the app falls back to the local cache.
- The local cache is also refreshed proactively as part of metadata sync (`SyncPresenterImpl.syncMetadata`), so data stays reasonably fresh even if a nurse never triggers an online lookup that day. The cache lives in `SharedPreferences` via the existing `PreferenceProvider.saveAsJson`/`getObjectFromJson` (same mechanism as the `NOTIFICATIONS` cache) — no new SQLite tables, no SDK changes.
- The dialog is non-blocking help, never a gate: the text field is always directly editable. The dialog itself communicates three states — no product selected yet ("select a product first"), no lot numbers found for the org unit/product ("not found, enter manually"), or a selectable list of lot numbers.
- Two DataElement UIDs (the "Lot Number" field this behavior attaches to, and the "Product" field it reads the selected product code from) are hardcoded constants specific to the UNICEF TJK eLMIS programme. Resolving the real UIDs against the live server's programme metadata is the first implementation task.

## Capabilities

### New Capabilities

- `lot-number-field`: a custom event-form field that augments a text DataElement with a lot-number lookup dialog, sourced from a network-first/cache-fallback copy of an OpenBoxes-published DHIS2 datastore entry, scoped to the current event's org unit and the product selected in another field of the same event.

### Modified Capabilities

(none — this is the first functional customization for `unicefTjkElmis`; `openspec/specs/` currently has no capabilities for this flavor)

## Impact

**Implementation-time correction:** Gradle module dependencies only go `app -> form` (not the reverse), and `:form` has no per-app-flavor source sets. So the data/use-case layer cannot live in `app/src/unicefTjkElmis/` and be referenced from `:form`. It moves into `form/src/main/java/org/dhis2/form/lotnumber/` as new, header-marked files (still UNICEF-TJK-eLMIS-specific, including the hardcoded DataElement UIDs — see design.md Decision 7).

**Second implementation-time correction:** a same-FQCN `app/src/unicefTjkElmis/java/org/dhis2/data/user/UserModule.kt` "override" of `app/src/main`'s `UserModule.kt` does not compile (`Redeclaration`, confirmed by build) — Kotlin source sets for `src/main` + `src/<flavor>` are additive, not override-by-replacement, unlike the `GranularSyncModule.kt` precedent (which has no `src/main` counterpart at all). There is no `app/src/unicefTjkElmis/java/org/dhis2/data/user/` source set. Instead, the sole `app/src/main/java/org/dhis2/data/user/UserModule.kt::lotNumberSyncRepository()` provider checks `BuildConfig.FLAVOR == "unicefTjkElmis"` and constructs the real `org.dhis2.form.lotnumber.LotNumberD2Repository` only for that flavor, else returns `LotNumberSyncRepositoryNoOp()` (see design.md Decision 4).

**Files added, in shared code (`:form`, new files, header `// EyeSeeTea customization - Lot Number Search Field`):**
- `form/src/main/java/org/dhis2/form/lotnumber/LotNumberFieldConstants.kt` — hardcoded UIDs for the "Lot Number" and "Product" DataElements, plus the datastore namespace/key
- `form/src/main/java/org/dhis2/form/lotnumber/LotNumbersApi.kt` — `d2.httpServiceClient()` call to `dataStore/openboxes-dhis2-sync/available-lot-numbers`, following the `NotificationsApi` pattern
- `form/src/main/java/org/dhis2/form/lotnumber/LotNumbersDTO.kt` — `@Serializable` DTOs matching `{orgUnitCode: {productCode: {lotNumbers: [...]}}}`
- `form/src/main/java/org/dhis2/form/lotnumber/LotNumberRepository.kt` + `LotNumberD2Repository.kt` — interface plus implementation: `LotNumberRepository` (read, network-first/cache-fallback) and `LotNumberSyncRepository` (proactive cache refresh during metadata sync)
- `form/src/main/java/org/dhis2/form/lotnumber/GetLotNumbersUseCase.kt` — domain use case resolving org unit code + product code for the current event, then querying the repository
- `form/src/main/java/org/dhis2/form/lotnumber/LotNumberInjector.kt` — small factory (mirrors `form/src/main/java/org/dhis2/form/di/Injector.kt`) constructing `LotNumberD2Repository`/`GetLotNumbersUseCase` via `D2Manager.getD2()` + `PreferenceProviderImpl(context)`, no Dagger
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/LotNumberFieldInput.kt` — Composable: text field + search button
- `form/src/main/java/org/dhis2/form/ui/dialog/LotNumberDialog.kt`, `LotNumberDialogScreen.kt` — dialog with the three states (no product / not found / list), modeled on the existing `OptionSetDialog` but implemented as a self-contained Compose `Dialog` (no `DialogFragment`)
- `app/src/main/java/org/dhis2/data/service/LotNumberSyncRepository.kt` — interface + `LotNumberSyncRepositoryNoOp` default implementation (bound for every flavor except `unicefTjkElmis`)
- `form/src/test/java/org/dhis2/form/lotnumber/LotNumberD2RepositoryTest.kt`, `GetLotNumbersUseCaseTest.kt` — unit tests (tasks 7.1/7.2)
- `form/src/androidTest/kotlin/org/dhis2/form/ui/dialog/LotNumberDialogScreenTest.kt` — Compose UI test for `LotNumberDialogScreen`'s three states + cancel (task 7.3)

**Files modified, Oslo shared code (minimal, marked edits):**
- `form/src/main/java/org/dhis2/form/data/FormRepository.kt` + `FormRepositoryImpl.kt` — new `recordUid(): String` method (interface + delegation to `formValueStore.recordUid()`), see design.md Decision 8
- `form/src/main/java/org/dhis2/form/ui/FormViewModel.kt` — new `recordUid(): String = repository.recordUid()` accessor
- `form/src/main/java/org/dhis2/form/ui/FormView.kt` and `Form.kt` — thread `recordUid()` as one new parameter down to `FieldProvider`
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt` — accept the new `eventUid` parameter, plus an early-return guard (`if (fieldUiModel.uid == LotNumberFieldConstants.LOT_NUMBER_DE_UID)`) before the existing dispatcher `when` block, constructing `LotNumberFieldInput`
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — one constructor parameter (`lotNumberSyncRepository: LotNumberSyncRepository`) plus one `.andThen(lotNumberSyncRepository.refreshCache())` appended to the existing `syncMetadata()` chain
- `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt`, `SyncDataWorkerModule.kt`, `SyncGranularRxModule.kt`, `SyncMetadataWorkerModule.kt` — each of the four `syncPresenter` `@Provides` methods gains the new `lotNumberSyncRepository: LotNumberSyncRepository` parameter, forwarded to `SyncPresenterImpl(...)` (Dagger resolves it from `UserModule`, see next item)
- `app/src/main/java/org/dhis2/data/user/UserModule.kt` — append one `@Provides @PerUser fun lotNumberSyncRepository(d2: D2, preferenceProvider: PreferenceProvider): LotNumberSyncRepository`; returns `LotNumberSyncRepositoryNoOp()` for every flavor except `unicefTjkElmis`, where it constructs the real `org.dhis2.form.lotnumber.LotNumberD2Repository`-backed adapter (append-only; single provider for all flavors, see design.md Decision 4)
- `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` — pass a mocked `LotNumberSyncRepository` to the updated `SyncPresenterImpl` constructor
- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — append one new key constant for the lot-numbers cache entry (append-only)
- `form/build.gradle.kts` — append `alias(libs.plugins.kotlin.serialization)` to the `plugins {}` block, needed for the `@Serializable` DTOs in `LotNumbersDTO.kt`
- `form/src/main/res/values/strings.xml` — append three new string resources for the dialog's three states (`lot_number_select_product_first`, `lot_number_none_found`, `lot_number_select_title`)

**Why this cannot live entirely in the flavor source set:** `FieldProvider.kt` is the single Compose dispatcher for every form field across all flavors and has no registry/extension point (plain `when` on `FieldUiModel` properties — confirmed by reading the dispatcher). `SyncPresenterImpl.syncMetadata()` is the single metadata-sync entry point and has no flavor hook today. Both files require a minimal, clearly marked edit; everything else (the actual lot-number logic, UI states, network/cache repository) lives in new files, isolated either in the flavor source set or as new shared files following the `NotificationsApi`/`NotificationD2Repository` precedent.

**Files NOT modified:**
- No SDK fork changes, no new SQLite tables/migrations, no changes to `D2` initialization.
- No other flavor's runtime behavior changes — the `FieldProvider.kt` guard never matches for UIDs other than the hardcoded UNICEF TJK eLMIS DataElement, and every flavor except `unicefTjkElmis` binds the no-op `LotNumberSyncRepository`.

**External inputs needed before/during implementation:**
- The real UIDs of the "Lot Number" and "Product" DataElements in the UNICEF TJK eLMIS programme metadata (query against `http://172.16.0.99:18081`).
- Confirmation that the "Product" DataElement is an OptionSet whose stored `dataValue` is the option **code** (DHIS2 default, but must be confirmed against this programme's actual metadata).
- A sample response from `GET /api/dataStore/openboxes-dhis2-sync/available-lot-numbers` to validate the DTO shape against real data (empty org units, products with no lot numbers, etc.).

**Build verification:**
- `./gradlew assembleUnicefTjkElmisDebug`, `./gradlew ktlintCheck`, `./gradlew testUnicefTjkElmisDebugUnitTest` must succeed, including new unit tests for `GetLotNumbersUseCase` and `LotNumberD2Repository` (network-first, cache-fallback, empty-cache paths).
