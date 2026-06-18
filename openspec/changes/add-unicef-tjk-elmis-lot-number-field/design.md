## Context

The form rendering pipeline lives in the `:form` module (Oslo, shared across all flavors). `FieldProvider.kt` (`form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt`) is the single Compose dispatcher: a `when` block branches on structural properties of `FieldUiModel` (`optionSet != null`, `customIntent != null`, `eventCategories != null`, else `ProvideByValueType` on `valueType`). There is no registry, multibinding, or per-UID extension point — every branch is hardcoded. `UiRenderType` is a plain Kotlin `enum class` and cannot be extended from a flavor.

Field-level autosave (`form/src/main/java/org/dhis2/form/data/FormValueStore.kt`, `saveDataElement()`) writes synchronously to SQLite via `d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid)` on `ON_SAVE` — which fires when focus moves away from a field (`FormViewModel.handleFocusOrNextAction()` → `saveLastFocusedItem()`). There is no debounce. This means: by the time a user's focus reaches the "Lot Number" field (and they can tap its search button), the previously-focused field — including "Product" — has already been persisted. This makes it safe to read the current product selection directly from `trackedEntityDataValues()` rather than from the in-memory `FormRepositoryImpl.itemList` (which has no public getter for cross-field reads anyway).

Metadata sync is orchestrated by `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt`, `syncMetadata()` (a `Completable` chain: `d2.metadataModule().download()` → map layers → file resources → `blockingAwait()`). `SyncPresenterImpl` is constructed via Dagger from `SyncMetadataWorkerModule.kt` (`app/src/main/java/org/dhis2/data/service/`), which has **no per-flavor override today**. A precedent for per-flavor Dagger module overrides via the Android source-set mechanism (same fully-qualified class name in `app/src/<flavor>/java/...`, only one compiled per variant) already exists: `GranularSyncModule.kt`.

A directly-applicable precedent for "call a DHIS2 datastore endpoint via `d2.httpServiceClient()`, cache the result in `SharedPreferences`, fall back to cache on error" exists on `origin/feature-widp/bring_last_changes_3_3_1`: `app/src/main/java/org/dhis2/data/notifications/NotificationsApi.kt` + `NotificationD2Repository.kt`, wired via `app/src/main/java/org/dhis2/usescases/notifications/di/NotificationsModule.kt`. That code is marked `// EyeSeeTea customization - Notifications system` and uses `D2Manager.getD2()`, `kotlinx.serialization` DTOs, and `PreferenceProvider.saveAsJson`/`getObjectFromJson` (`commons/src/main/java/org/dhis2/commons/prefs/`). This change reuses that exact shape for lot numbers.

The two DataElement UIDs this feature attaches to ("Lot Number" and "Product") belong to the UNICEF TJK eLMIS programme and are not yet known — they must be read from the programme metadata on `http://172.16.0.99:18081`. Per `eyeseetea-docs/upgrade/conflict-rules.md`, `develop-unicef-tjk-elmis` is a single-client branch (no cross-flavor multi-tenancy in this binary); the only future conflict source is merging `develop-eyeseetea` forward, so hardcoded UNICEF-specific UIDs in shared files are an accepted, documented trade-off rather than a cross-client contamination risk.

## Goals / Non-Goals

**Goals:**
- Render a search-assisted lot-number field for one specific DataElement (UID TBD, UNICEF TJK eLMIS programme) without making `FieldProvider.kt`'s dispatcher generically extensible.
- Resolve `(orgUnitCode, productCode)` for the current event using only local SDK reads (no network call needed just to open the dialog's "loading" state).
- Fetch `dataStore/openboxes-dhis2-sync/available-lot-numbers` network-first, with a `SharedPreferences`-backed cache fallback, reusing the `NotificationsApi`/`NotificationD2Repository` shape verbatim.
- Refresh that cache proactively at the end of `SyncPresenterImpl.syncMetadata()`, via a flavor-swappable dependency that is a no-op for every flavor except `unicefTjkElmis`.
- Keep the lot-number text field always directly editable — the dialog is assistive, never a gate.
- Minimize the diff inside Oslo files (`FieldProvider.kt`, `SyncPresenterImpl.kt`, `SyncMetadataWorkerModule.kt`, `Preference.kt`) to small, clearly marked, additive edits.

**Non-Goals:**
- A generic "custom field renderer registry" for `:form`. Out of scope — would be a much larger refactor of Oslo code for a single field.
- Reading or writing the OpenBoxes-side data. This change only consumes the datastore entry that the existing OpenBoxes ↔ DHIS2 sync already publishes.
- Validating that a typed-in lot number actually exists in OpenBoxes/the datastore. The field accepts free text regardless of dialog content (see "UI is help, not a gate" in Decisions).
- Any change to `d2.dataStoreModule()` (SDK local datastore) — the remote read is a direct API call via `d2.httpServiceClient()`, not the SDK's datastore module.
- Resolving the real DataElement UIDs as part of this design — that is the first implementation task (`tasks.md`), informed by querying the live server.

## Decisions

### Decision 1: Direct API call via `d2.httpServiceClient()`, mirroring `NotificationsApi`/`NotificationD2Repository`

The datastore entry `openboxes-dhis2-sync/available-lot-numbers` is fetched with a small `LotNumbersApi` class (`suspend fun getData(): LotNumbersDTO`) calling `d2.httpServiceClient().get { url("dataStore/openboxes-dhis2-sync/available-lot-numbers") }`, exactly as `NotificationsApi.getData()` calls `dataStore/notifications/notifications`. `d2.httpServiceClient()` already carries the authenticated session against `http://172.16.0.99:18081`, so no new HTTP client, auth, or base-URL configuration is needed.

`LotNumberD2Repository` mirrors `NotificationD2Repository`: a `try { remote } catch (e) { cache }` shape, using `D2Manager.getD2()` and `PreferenceProvider` injected the same way `NotificationsModule` does. On a successful remote call, the repository both returns the fresh data and writes it to the cache (write-through), so a later offline lookup benefits from any earlier successful online lookup — not only from the periodic metadata-sync refresh.

**Alternative considered:** `d2.dataStoreModule()` (SDK's local datastore module, used elsewhere in the codebase via `localDataStore()`). Rejected — no code in this project demonstrates a *remote* datastore fetch through the SDK's datastore module (only local key-value access was found), and the user explicitly directed reuse of the proven `NotificationsApi` direct-API pattern, which is already validated in this codebase on a sibling branch.

### Decision 2: `(orgUnitCode, productCode)` resolved from local SDK state, not from `FormRepositoryImpl.itemList`

`GetLotNumbersUseCase` resolves:
- `orgUnitCode` from `event.organisationUnit()` (UID) → `d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet().code()` — a local SDK read, no network.
- `productCode` from `d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, PRODUCT_DE_UID).blockingGet()?.value()` — also a local SDK read. Because OptionSet DataElements store the option's `code` (not UID) as the data value, this is directly comparable to `productCode` in the datastore JSON, with no UID↔code mapping step.

This avoids adding a cross-field read API to `FormRepository`/`FormRepositoryImpl` (neither currently exposes `getValue(uid)` for arbitrary fields). As established in Context, the field-level autosave guarantee means the "Product" value is in SQLite by the time the user can interact with "Lot Number"'s search button.

**Alternative considered:** Add `FormRepository.getValueFromUid(uid): String?` and read from the in-memory `itemList`. Rejected — adds a new method to an Oslo interface (`form/src/main/java/org/dhis2/form/data/FormRepository.kt`) and its implementation (`FormRepositoryImpl.kt`), a larger and less localized edit than reading two values directly from the SDK, for no behavioral benefit given the autosave guarantee.

### Decision 3: `FieldProvider.kt` gets a single early-return guard; everything else is a new file

```kotlin
// EyeSeeTea customization - Lot Number Search Field
if (fieldUiModel.uid == LotNumberFieldConstants.LOT_NUMBER_DE_UID) {
    LotNumberFieldInput(fieldUiModel, ...)
    return
}
```

placed before the existing dispatcher `when` block (~5 lines total). `LotNumberFieldInput` (the composable: text field + search button) is a new file under `form/src/main/java/org/dhis2/form/ui/provider/inputfield/`, header-commented `// EyeSeeTea customization - Lot Number Search Field`. The dialog (`LotNumberDialog.kt`, `LotNumberDialogScreen.kt`, `LotNumberDialogViewModel.kt`) is modeled on the existing `OptionSetDialog`/`OptionSetDialogScreen` (same package, same DialogFragment + Compose screen shape), also new files with the same header.

`LotNumberFieldConstants.LOT_NUMBER_DE_UID` and `PRODUCT_DE_UID` are hardcoded `const val` strings. They live in a new file `form/src/main/java/org/dhis2/form/lotnumber/LotNumberFieldConstants.kt` (see Decision 7 for why the entire data/use-case layer lives in `:form`) rather than as a registry/config object, because — per Context — this branch is single-client and the only conflict risk is the `develop-eyeseetea` merge direction, where a 5-line additive guard is low-risk regardless of where the UID constant physically lives.

**Alternative considered:** Add a `LOT_NUMBER_SEARCH` value to `UiRenderType` and dispatch on `renderingType`. Rejected — `UiRenderType` is a plain enum populated from SDK `ValueTypeRenderingType` metadata via `FieldUiModelProvider`; making this field's UID map to a new render type would require edits in more places (the enum itself, plus the mapper) than the single `if` guard, for the same outcome.

### Decision 4: `SyncPresenterImpl` gets a flavor-swappable `LotNumberSyncRepository`, provided via a single `UserModule` provider with a `BuildConfig.FLAVOR` check

> **⚠️ Superseded (post-iteration).** This decision is no longer the shipped design. The `app`-side `LotNumberSyncRepository` interface + `LotNumberSyncRepositoryNoOp` and the `BuildConfig.FLAVOR` gate were **removed**:
> - The sync path now reaches the repository through a domain use case, `RefreshLotNumbersCacheUseCase` (in `:form`, `org.dhis2.form.model.lotnumber`), so the repository is only ever invoked via a use case (Clean Architecture) — no `app`-side wrapper interface, no anonymous adapter.
> - `refreshCache()` lives on the `LotNumberRepository` domain interface as a `suspend` function; `SyncPresenterImpl` adapts it to its RxJava chain at the boundary with `rxCompletable { refreshLotNumbersCache() }`.
> - **No flavor gate / no no-op:** this branch only ever builds the `unicefTjkElmis` flavor, so the `BuildConfig.FLAVOR` check and the no-op were dropped as defensive complexity for a case that cannot occur here. `UserModule` provides `RefreshLotNumbersCacheUseCase` unconditionally.
>
> The rest of this section is retained as the historical record of why the wrapper/gate approach was attempted and corrected during implementation.

`LotNumberSyncRepository` (new interface, `app/src/main/java/org/dhis2/data/service/LotNumberSyncRepository.kt`) declares `fun refreshCache(): Completable`. A `LotNumberSyncRepositoryNoOp` (same file, `app/src/main/`) returns `Completable.complete()`.

`SyncPresenterImpl`'s constructor (currently 6 dependencies) gains a 7th: `lotNumberSyncRepository: LotNumberSyncRepository`. `syncMetadata()`'s existing `Completable` chain gains one more `.andThen(lotNumberSyncRepository.refreshCache())` after the existing `fileResourceDownloader...download()` step.

**Implementation-time correction to this decision:** `SyncPresenterImpl` is constructed from **four** separate Dagger `@Module`s — `SyncInitWorkerModule`, `SyncDataWorkerModule`, `SyncGranularRxModule`, and `SyncMetadataWorkerModule` (all `app/src/main/java/org/dhis2/data/service/`) — each a `+` subcomponent of `UserComponent`/`UserModule`. Overriding only `SyncMetadataWorkerModule` would leave the other three modules' `syncPresenter` providers unable to resolve the new constructor parameter. Instead, `UserModule.kt` (`app/src/main/java/org/dhis2/data/user/`, the single `@PerUser`-scoped module that `UserComponent` is built from) gets one new `@Provides @PerUser fun lotNumberSyncRepository(d2: D2, preferenceProvider: PreferenceProvider): LotNumberSyncRepository` — an append, not a modification of the existing `userRepository` provider. Dagger resolves this dependency for all four `syncPresenter` providers automatically, since they are all descendants of `UserComponent`.

**Second implementation-time correction — source-set override of `UserModule.kt` does not compile:** the original plan was an `app/src/unicefTjkElmis/java/org/dhis2/data/user/UserModule.kt` with the same FQCN as `app/src/main/`'s, expecting Android's flavor source-set mechanism to make the flavor copy "win" (the pattern attributed to `GranularSyncModule.kt`). This does not hold for Kotlin/Java sources: `src/main` and `src/<flavor>` are compiled together (additively) for a flavor's variant, so two classes with the same FQCN produce a hard `Redeclaration` compile error (`:app:compileUnicefTjkElmisDebugKotlin FAILED`, confirmed by build). On closer inspection, `GranularSyncModule.kt` is not actually an override — it exists *only* in `app/src/unicefTjkElmis/`, with no `app/src/main/` counterpart, so there was never a collision to resolve. True override-by-replacement of a same-FQCN Kotlin class across `src/main`/`src/<flavor>` is not possible in this build setup.

**Resolution:** there is a single `app/src/main/java/org/dhis2/data/user/UserModule.kt::lotNumberSyncRepository()` provider. It takes `d2: D2` and `preferenceProvider: PreferenceProvider` as Dagger-injected parameters (precedent: `GranularSyncModule.kt`'s `@Provides` methods already do this). If `BuildConfig.FLAVOR != "unicefTjkElmis"`, it returns `LotNumberSyncRepositoryNoOp()`. Otherwise it constructs `org.dhis2.form.lotnumber.LotNumberD2Repository` (per Decision 7, lives in `:form`, referenceable from `app` because `app -> form` is valid) and wraps it in an anonymous `LotNumberSyncRepository` adapter delegating `refreshCache()`. Precedent for `BuildConfig.FLAVOR == "..."` checks directly in `app/src/main` shared code: `MainPresenter.kt:369`, `MainActivity.kt:266`. No `app/src/unicefTjkElmis/java/org/dhis2/data/user/` source set exists.

**Alternative considered:** A no-op-default + flavor-binding pattern via a smaller, dedicated Dagger module with a same-FQCN override (rather than the single-provider flavor check). Rejected — same redeclaration problem applies to any same-FQCN Kotlin class duplicated across `src/main` and `src/unicefTjkElmis`, regardless of how small the module is; the flavor check inside one shared provider is the only approach that avoids file duplication entirely.

### Decision 7: The entire lot-number data/use-case layer lives in `:form/src/main`, not `app/src/unicefTjkElmis`

**Implementation-time correction:** the original plan placed `LotNumbersApi`, `LotNumbersDTO`, `LotNumberD2Repository`, `LotNumberRepository`, `GetLotNumbersUseCase`, and `LotNumberFieldConstants` in `app/src/unicefTjkElmis/java/org/dhis2/`, to be referenced from `LotNumberFieldInput.kt`/`FieldProvider.kt` in `:form`. This is not possible: Gradle module dependencies only go `app -> form` (confirmed in `app/build.gradle.kts` / `form/build.gradle.kts`), so `:form` cannot reference `app/src/unicefTjkElmis` types. Additionally, `:form` has only a single "default" flavor dimension (`form/build.gradle.kts`) — there is no `form/src/unicefTjkElmis/` source set for a flavor-scoped override either.

Resolution: the entire data/use-case layer — `LotNumberFieldConstants.kt`, `LotNumbersDTO.kt`, `LotNumbersApi.kt`, `LotNumberRepository` (interface) + `LotNumberD2Repository` (impl), and `GetLotNumbersUseCase` — moves into `form/src/main/java/org/dhis2/form/lotnumber/`, as new files header-marked `// EyeSeeTea customization - Lot Number Search Field`. This includes the hardcoded UNICEF UIDs (`LOT_NUMBER_DE_UID`, `PRODUCT_DE_UID`), which per Context/Decision 3 is an accepted trade-off for a single-client branch.

`LotNumberFieldInput` (in `:form`) constructs `LotNumberD2Repository` and `GetLotNumbersUseCase` directly via a small factory (e.g. `LotNumberInjector`, mirroring the existing `form/src/main/java/org/dhis2/form/di/Injector.kt` pattern), using `D2Manager.getD2()` (static singleton, no Dagger graph traversal) and `PreferenceProviderImpl(context)` (via `LocalContext.current` in the composable) — exactly as `Injector.provideOptionSetDialog()` already does for `SearchOptionSetOption`. **No new Dagger module is needed for the read path.**

For the sync-side write path (task group 2's `LotNumberSyncRepository`), `app/src/main/java/org/dhis2/data/user/UserModule.kt`'s `lotNumberSyncRepository()` provider (Decision 4) constructs the real `org.dhis2.form.lotnumber.LotNumberD2Repository` directly when `BuildConfig.FLAVOR == "unicefTjkElmis"` (same `D2`/`PreferenceProvider` construction as the read-path factory) — `app` can reference `:form` types because `app -> form` is a valid dependency direction.

**Why this is still a small Oslo footprint despite moving files into `:form`:** every file in `form/src/main/java/org/dhis2/form/lotnumber/` is a brand-new file (tier 2 of the placement hierarchy — "new file in shared code with header comment"), not an edit to an existing Oslo file. The only edits to *existing* shared files remain `FieldProvider.kt` (Decision 3's guard) and the sync-chain files from Decision 4/task group 2. The hardcoded UNICEF UIDs living in a new `:form` file (rather than a new `app/src/unicefTjkElmis` file) does not change the diff-against-`develop-eyeseetea` risk profile — it is still purely additive new files.

**Alternative considered:** thread a generic extension point through `Form.kt`/`FormView.kt` (e.g. an optional `customFieldRenderer` lambda passed from `app` down to `FieldProvider`). Rejected — `FormView` is a `Fragment` reused by every screen that renders a form (event entry, enrollment, etc.); adding a new constructor/parameter path through it is a larger, more invasive Oslo edit than accepting UNICEF-specific new files inside `:form`.

### Decision 5: Cache is a single JSON blob in `SharedPreferences`, not a new SQLite table

The entire `available-lot-numbers` response (`{orgUnitCode: {productCode: {lotNumbers: [...]}}}`) is stored as one JSON string via `PreferenceProvider.saveAsJson(Preference.LOT_NUMBERS_CACHE, dto)` / `getObjectFromJson(Preference.LOT_NUMBERS_CACHE, ..., emptyMap())`, exactly as `NotificationD2Repository` does for `Preference.NOTIFICATIONS`. `Preference.kt` (`commons/src/main/java/org/dhis2/commons/prefs/`) gets one new `const val LOT_NUMBERS_CACHE = "..."` appended to its companion object.

This is intentionally a single key/blob, not per-org-unit or per-product keys: the dataset is small (LMIS facility/product catalogs, not a transactional table), and a single write-through on every successful fetch keeps the cache trivially consistent — there is no partial-update or merge logic to get wrong.

**Alternative considered:** Persist via the SDK's local datastore (`d2.dataStoreModule().localDataStore()`, used by `:stock-usecase`'s `StockTableDimensionStore`). Rejected per explicit instruction — `SharedPreferences` avoids touching the SDK/local-database surface entirely, and the `Preference`/`PreferenceProvider` mechanism is already the established pattern for exactly this kind of cached-blob-from-datastore in this codebase (`NOTIFICATIONS`).

### Decision 6: Dialog states are help, not a gate — three explicit states plus always-editable text field

`LotNumberDialogViewModel` exposes a sealed state:
- `NoProductSelected` — `productCode` resolved to null/blank. Dialog shows "select a product first"; no list, no search performed. The underlying text field remains editable regardless.
- `NotFound` — a `productCode` was resolved, the lookup (online or cached) completed, but `lotNumbers` for `(orgUnitCode, productCode)` is missing or empty. Dialog shows "no lot numbers found for this product — you can enter one manually".
- `Available(lotNumbers: List<String>)` — non-empty list; tapping an item writes it into the field's value and closes the dialog (mirroring `OptionSetDialog`'s `onOptionClick`); "Cancel" closes without changing the field.

No state blocks typing into the underlying field — `LotNumberFieldInput` always renders an editable `TextField`; the dialog is reached only via the adjacent search button and never intercepts keyboard input.

**Open question (not blocking):** whether `Available`/`NotFound` should additionally indicate "this data came from cache as of <timestamp>" when the network call failed. Deferred — see Open Questions.

### Decision 8: `eventUid` reaches `LotNumberFieldInput` via a new `FormRepository.recordUid()` method, threaded through `FormViewModel` → `FormView` → `Form` → `FieldProvider`

**Implementation-time correction:** Decision 2 established that `GetLotNumbersUseCase` reads `productCode`/`orgUnitCode` directly from the SDK given an `eventUid`, avoiding a cross-field read API. But it did not address how `LotNumberFieldInput` (a Composable receiving only `fieldUiModel` and the standard `FieldProvider` parameters — `intentHandler`, `uiEventHandler`, `resources`, `focusManager`, etc.) obtains that `eventUid` in the first place. No record identifier is currently threaded down to field-level composables; `FormValueStore.recordUid()` exists but `FormValueStore` is private to `FormRepositoryImpl` and constructed per-form-instance via `Injector`, not reachable from a standalone composable.

Resolution: add `fun recordUid(): String` to the `FormRepository` interface (`form/src/main/java/org/dhis2/form/data/FormRepository.kt`), implemented in `FormRepositoryImpl` by delegating to the existing `formValueStore.recordUid()`. Expose it from `FormViewModel` (`form/src/main/java/org/dhis2/form/ui/FormViewModel.kt`) as `fun recordUid(): String = repository.recordUid()`. Thread it as one new parameter down the existing call chain: `FormView.kt` passes `viewModel.recordUid()` into `Form(...)`, `Form.kt` passes it into `FieldProvider(...)`, `FieldProvider.kt` passes it into `LotNumberFieldInput(...)`. All additions are additive (new interface method, new parameter with no default removed from existing call sites since there are only two: `FormView.kt` → `Form` and `Form.kt` → `FieldProvider`).

For enrollment/TEI-attribute forms (`isEvent() == false`), `recordUid()` still returns a valid (enrollment) UID — `GetLotNumbersUseCase` is only ever invoked from `LotNumberFieldInput`, which only renders for `LOT_NUMBER_DE_UID` (an event-program DataElement), so the enrollment case never reaches the use case in practice. No special-casing is added for this.

Each touched file gets `// EyeSeeTea customization - Lot Number Search Field` directly above the added line(s): the new interface method, its implementation, the `FormViewModel` accessor, and each of the three call-site edits.

**Alternative considered:** re-query the SDK from `GetLotNumbersUseCase` to find "the current event" without an explicit `eventUid` (e.g., most-recently-modified `trackedEntityDataValue` for `PRODUCT_DE_UID`). Rejected as unreliable — ambiguous if multiple events for the same program have been edited recently, and silently wrong rather than failing safely.

## Risks / Trade-offs

- **[Risk]** `app/src/unicefTjkElmis/java/org/dhis2/data/user/UserModule.kt` is a *full copy* of the `app/src/main/` module with one provider changed. If a future `develop-eyeseetea` merge adds a new `@Provides` to the `main` version, the flavor's copy silently lacks it. → **Mitigation:** this is the same accepted, documented risk as `GranularSyncModule.kt` (already in `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md`'s pattern); it fails at **Dagger compile-time** for the `unicefTjkElmis` variant (missing `@Provides` for a new dependency), not silently at runtime. `UserModule.kt` is a single-provider file today, so the desync surface is small. Add `UserModule.kt` to `customization-files.md` so the "automerge verification" step (`git diff develop-eyeseetea -- <file>` per `eyeseetea-docs/upgrade/conflict-rules.md`) checks it on every baseline merge.
- **[Risk]** Reading `productCode` via `trackedEntityDataValues()` relies on the autosave-on-focus-loss guarantee (Context). If a future Oslo change alters when `ON_SAVE` fires (e.g., introduces a debounce), the "Product" value might not yet be persisted when "Lot Number"'s search button is tapped. → **Mitigation:** `GetLotNumbersUseCase` treats a missing/null `productCode` exactly like "no product selected" (Decision 6's `NoProductSelected` state) — the failure mode is "show the help message", never a crash or incorrect lot list. Re-validate this assumption manually after any upgrade that touches `FormValueStore.kt`.
- **[Risk]** The "Product" DataElement might, in this specific programme's metadata, store the option **UID** rather than **code** (non-default OptionSet configuration). → **Mitigation:** this is called out explicitly as an external input to confirm before/during implementation (proposal "External inputs needed"); if false, `GetLotNumbersUseCase` needs one extra local lookup (`d2.optionModule().options().uid(...).blockingGet().code()`) to map UID→code, which is a small, isolated addition to the use case and does not change the rest of the design.
- **[Trade-off]** Hardcoded DataElement UIDs in `LotNumberFieldConstants` (and the `FieldProvider.kt` guard that compares against them) are UNICEF-TJK-eLMIS-specific values inside files that, by module location, are nominally "shared". Per Context, this branch is single-client, so the only practical consequence is that these constants/guard exist in the diff against `develop-eyeseetea` — already accounted for as the change's Oslo footprint.

## Open Questions

- **Cache staleness indicator**: should the dialog show "data as of <last successful fetch>" when serving from cache after a failed online attempt? Decision 6 leaves this open; it does not change the repository/use-case contract (the timestamp would be stored alongside the cached blob in the same `SharedPreferences` entry or a sibling key) and can be added in `tasks.md` as an optional enhancement.
- **DTO shape for empty/missing entries**: the real `GET /api/dataStore/openboxes-dhis2-sync/available-lot-numbers` response may have org units with no products, or products with an empty `lotNumbers` array, or the key may not exist at all (404) if OpenBoxes sync hasn't run yet. `LotNumbersDTO` and `LotNumberD2Repository` must handle all three as "no lot numbers" (→ `NotFound` state), not as errors — confirm against a real sample response during implementation.
