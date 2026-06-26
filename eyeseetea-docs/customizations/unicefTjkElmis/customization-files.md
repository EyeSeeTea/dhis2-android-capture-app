# UNICEF TJK eLMIS — Customization Files

Technical inventory of the UNICEF TJK eLMIS fork. Lists where each customization is implemented, separating direct flavor surface from shared-code implementation points, and tracks technical status against `develop-eyeseetea`.

This file is **not** for: raw full diff dumps, temporary upgrade progress, stable merge rules, or functional intent / business justification.

## Mandatory header

- Client: `unicefTjkElmis`
- Flavor: `unicefTjkElmis`
- Base branch: `develop-eyeseetea`
- Base commit: `8a4866305`
- Generated on: `2026-06-15`
- Working tree status: untracked `dhis2-android-sdk/` and `dhis2-rule-engine/` are local SDK fork checkouts no longer needed by `develop-eyeseetea` since 2FA was removed; they do not impact this inventory. PR 02 (`add-unicef-tjk-elmis-lot-number-field`, §2/§4) changes are uncommitted at time of writing.

## Scope

This inventory is based on:
- direct flavor files under `app/src/unicefTjkElmis/`, `app/src/unicefTjkElmisDebug/`, `app/src/unicefTjkElmisRelease/`
- shared-code implementation points marked with `// EyeSeeTea customization` (introduced by PR 02, see §2)
- current diffs against `develop-eyeseetea` used only as supporting evidence

## 1. Direct unicefTjkElmis flavor surface

### 1.1 Flavor code

- `app/src/unicefTjkElmis/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` — Dagger module required by the Oslo build's annotation processor; every flavor ships its own copy. This is build-wiring boilerplate, not a customization. Contents are copied verbatim from the `dhis2` flavor's version.

(no other Kotlin/Java sources — UNICEF-specific Kotlin/Java arrives with the first functional customization PR.)

### 1.2 Flavor resources and branding

- `app/src/unicefTjkElmis/res/values/strings.xml` — default-locale `app_name`, `logo_text`, `logo_number`
- `app/src/unicefTjkElmis/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` — same three keys per locale, overriding the translated brand strings declared by `app/src/main/res/values-<locale>/strings.xml`. The UNICEF brand does not translate, so every locale uses identical values.
- `app/src/unicefTjkElmis/res/values/ic_launcher_background.xml` — `ic_launcher_background = #FFFFFF`
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` — UNICEF logo on white, square (48-192 px)
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.webp` — same logo, masked round at runtime
- `app/src/unicefTjkElmis/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.webp` — adaptive icon foreground, transparent canvas with logo in central safe zone (108-432 px)
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher.xml` — adaptive icon wrapper (background `@color/ic_launcher_background`, foreground `@mipmap/ic_launcher_foreground`)
- `app/src/unicefTjkElmis/res/mipmap-anydpi-v26/ic_launcher_round.xml` — same wrapper for the round variant
- `app/src/unicefTjkElmis/ic_launcher-playstore.png` — 512×512 Play Store icon
- `app/src/unicefTjkElmisDebug/res/values/strings.xml` — debug-build-type override of `app_name`, `logo_text`, `logo_number`. Required because `app/src/debug/res/values/strings.xml` declares `app_name="Dhis2 Dev"` and Android resource merging applies build-type overrides after flavor overrides; without this file, the launcher renders `Dhis2 Dev` instead of `UNICEF TJK eLMIS` on debug installs.
- `app/src/unicefTjkElmisDebug/res/values/ic_launcher_background.xml` — `ic_launcher_background = #FFFFFF` (identical to the release flavor; debug installs are differentiated by `applicationIdSuffix=".debug"`, not by icon)
- `app/src/unicefTjkElmisDebug/res/values-{ar,ckb,cs,es,es-rES,fr,id,km,lo,nb,nl,pt,ru,sv,uk,uz,uz-rUZ,vi,zh,zh-rCN}/strings.xml` — debug-variant locale overrides for `app_name`, `logo_text`, `logo_number` (same content as the release flavor; explicit per-locale override so debug installs render the UNICEF brand consistently across system locales)
- `app/src/unicefTjkElmisDebug/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher{,_round,_foreground}.webp` — debug-variant icon set. Note: the `ic_launcher.webp` and `ic_launcher_round.webp` legacy files were generated with a warm-tinted pixel background that pre-dates the decision to make debug visually identical to release. On Android API 26+ the adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` referencing `@color/ic_launcher_background = #FFFFFF`) takes precedence and the launcher renders pure white; on pre-API 26 devices the legacy webp would render with a warm tint. Not regenerated because the target population is Android 8+ where the adaptive icon wins.
- `app/src/unicefTjkElmisDebug/ic_launcher-playstore.png` — debug variant of the Play Store icon

**Branding placeholder note:** the launcher icon set is a UNICEF-only placeholder generated from a single source PNG. The final UNICEF / MoH TJK / combined branding decision is tracked in a separate change proposal; when that decision lands, the assets here are replaced wholesale (no merge or migration needed because every file in this list is flavor-scoped).

### 1.3 Build wiring

- `app/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { ... } }` block declaring `applicationId = "org.unicef.tjk.elmis"`, `dimension = "default"`, `versionCode = libs.versions.vCode.get().toInt()`, `versionName = libs.versions.vName.get()`
- `login/build.gradle.kts` — `productFlavors { create("unicefTjkElmis") { buildConfigField("String", "LOGIN_TEST", "\"test\"") } }` block
- `gradle/libs.versions.toml` — `vName = "3.3.1-unicefTjkElmis-fork-1"` (branch-wide; this branch only distributes the unicefTjkElmis flavor)

## 2. Shared-code customization implementation points

### 2.1 Lot Number Field (PR 02, `add-unicef-tjk-elmis-lot-number-field`)

Spec: `openspec/specs/lot-number-field/spec.md`. Title used in `// EyeSeeTea customization - Lot Number Search Field` comments matches the spec heading "Lot Number Field" (comment text predates a later spec title tweak — both refer to the same capability; treat as equivalent, no action needed unless the spec title is itself revised).

**New files, isolated (no Oslo file touched):**

Domain/model layer (`form/src/main/java/org/dhis2/form/model/lotnumber/`):
- `LotNumberRepository.kt` — repository interface (domain layer): `suspend fun getLotNumbers(eventUid: String): LotNumbersResult` + `suspend fun refreshCache()`
- `GetLotNumbersUseCase.kt` — delegates to `LotNumberRepository`; also defines `LotNumbersResult` sealed class (three states: `NoProductSelected`, `NotFound`, `Available`)
- `RefreshLotNumbersCacheUseCase.kt` — domain use case that delegates `suspend fun invoke()` to `LotNumberRepository.refreshCache()`. Invoked by the sync layer so the repository is only ever reached through a use case (Clean Architecture); replaces the former `LotNumberSyncRepository` wrapper.

Data layer (`form/src/main/java/org/dhis2/form/data/lotnumber/`):
- `LotNumberDataElements.kt` — single source of truth for the DataElement UIDs: `PRODUCT_DE_UID = "BzKc72LZLxw"` (top-level const) and `LOT_NUMBER_DE_UIDS` (top-level `Set<String>` of the 4 product-family lot DataElements). Lives in the data layer so `FormViewModel` can reference it without depending on the UI layer.
- `LotNumbersApiResponse.kt` — `@Serializable` DTOs, shape `{orgUnitCode: {productCode: {lotNumbers: [...]}}}` (`LotNumbersApiResponse` typealias + `LotNumbersEntry` data class)
- `LotNumbersApi.kt` — `d2.httpServiceClient()` GET `dataStore/openboxes-dhis2-sync/available-lot-numbers`; `LOT_NUMBERS_DATASTORE_NAMESPACE` / `LOT_NUMBERS_DATASTORE_KEY` declared here as top-level consts
- `LotNumberD2Repository.kt` — implements `LotNumberRepository`; resolves product code and org unit code from D2, then does network-first/cache-fallback lookup. Both `getLotNumbers` and `refreshCache` are `suspend` and wrap their blocking work (D2 `blockingGet`, synchronous HTTP, Gson cache) in `withContext(ioDispatcher)` — the repository owns main-safety (per Android guidance). `ioDispatcher: CoroutineDispatcher = Dispatchers.IO` is injected via the constructor so the unit test can drive it with a `StandardTestDispatcher`. The shared "fetch from remote, cache locally" logic lives in a single private `fetchFromRemoteAndCache()` reused by both methods.

DI factory (`form/src/main/java/org/dhis2/form/di/lotnumber/`):
- `LotNumberInjector.kt` — no-Dagger factory (`D2Manager.getD2()` + `PreferenceProviderImpl(context)`), mirrors `form/src/main/java/org/dhis2/form/di/Injector.kt`

UI — dialog (`form/src/main/java/org/dhis2/form/ui/dialog/lotnumber/`):
- `LotNumberDialog.kt` — screen-level Composable; obtains `LotNumberDialogViewModel` via the Compose `viewModel(key = eventUid, factory = ...)` and collects its `result` with `collectAsStateWithLifecycle()`, then hoists state down to `LotNumberDialogScreen` (the lower composables never see the ViewModel). `BottomSheetShell` provides its own `ModalBottomSheet`, so no `Dialog` wrapper is used.
- `LotNumberDialogViewModel.kt` — `ViewModel` that owns the lot-number load coroutine: launches `GetLotNumbersUseCase` in `viewModelScope` (survives configuration changes, structured cancellation) and exposes the outcome as `StateFlow<LotNumbersResult?>`. Includes a `Factory` taking `eventUid` + the use case. Replaces the earlier `LaunchedEffect`-in-the-view approach so the view no longer triggers business logic directly (per Android's coroutine/architecture guidance). Keyed by `eventUid` so a different event/product yields a fresh instance.
- `LotNumberDialogScreen.kt` — pure-presentation Composable, three states (loading / no product / not found / selectable list)

UI — field input (`form/src/main/java/org/dhis2/form/ui/provider/inputfield/lotnumber/`):
- `LotNumberFieldInput.kt` — text field + clear/search button Composable (UIDs consumed from `LotNumberDataElements.LOT_NUMBER_DE_UIDS`). `InputText` is rendered with `showDeleteButton = false`; the clear (`Icons.Outlined.Cancel`) and search (`Icons.Filled.Search`) buttons and the single vertical separator between them are overlaid on top via a `Box` + `Row` anchored to `TopEnd` (with `top` padding 8dp = `InputShell` paddingValues top), replicating the internal `InputShellButtonSeparator` layout (`VerticalDivider` height=40dp, color=`Ash600`/`0xFFC5CED6`, button slot 48dp, separator drawn only between the two buttons). `showDeleteButton = false` is required because otherwise the design system's built-in clear button and our overlaid search button would render as two overlapping icons. This overlay workaround is necessary because `BasicTextInput` — the only component that exposes a `secondaryButton` slot matching the design system's button-area layout — is marked `internal` in `designsystem 0.7.0` and is not accessible from outside the module. If a future version of the design system exposes that slot publicly (e.g., via an `actionButton` parameter on `InputText`), this overlay should be replaced.

Tests:
- `form/src/test/java/org/dhis2/form/data/lotnumber/LotNumberD2RepositoryTest.kt` — unit tests for `LotNumberD2Repository` (network-first, cache fallback, empty cache, no product)
- `form/src/androidTest/kotlin/org/dhis2/form/ui/dialog/lotnumber/LotNumberDialogScreenTest.kt` — Compose UI test for `LotNumberDialogScreen`'s three states + cancel

**Modified Oslo files (marked `// EyeSeeTea customization - Lot Number Search Field`, minimal/append-only edits):**
- `form/src/main/java/org/dhis2/form/data/FormRepository.kt` + `FormRepositoryImpl.kt` — new `recordUid(): String`
- `form/src/main/java/org/dhis2/form/ui/FormViewModel.kt` — new `recordUid(): String = repository.recordUid()` accessor; plus `clearLotNumbersOnProductChange(savedFieldUid)` (new private fn) called inside `handleOnSaveAction` after a successful save: when the saved field is `PRODUCT_DE_UID`, every `LOT_NUMBER_DE_UIDS` value of the event is cleared (`repository.save(lotUid, null, null)` + `updateValueOnList(lotUid, null, null)`). Centralized here — not in the composable — because each product renders a different lot DataElement, so the stale value may live on a field that is no longer composed. The inline call site is a single line; the bulk lives in the new private fn.
- `form/src/main/java/org/dhis2/form/ui/FormView.kt`, `Form.kt` — thread `recordUid()` down to `FieldProvider`
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt` — accept `eventUid` param + early-return guard on `fieldUiModel.uid in LOT_NUMBER_DE_UIDS` constructing `LotNumberFieldInput`
- `form/src/main/res/values/strings.xml` — append `lot_number_select_product_first`, `lot_number_none_found`, `lot_number_select_title`
- `form/build.gradle.kts` — append `alias(libs.plugins.kotlin.serialization)` to `plugins {}` (needed for `@Serializable` DTOs); append `implementation(libs.androidx.lifecycle.viewmodel.compose)` (Compose `viewModel()`) and `implementation(libs.lifecycle.runtime.compose)` (`collectAsStateWithLifecycle`) — the module already had `lifecycle-viewmodel-ktx` transitively (used by `FormViewModel`), but neither Compose↔lifecycle bridge artifact was present, as `LotNumberDialog` is the first ViewModel consumed directly from a Composable in `:form`
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` — new constructor param `refreshLotNumbersCache: RefreshLotNumbersCacheUseCase` + `.andThen(rxCompletable { refreshLotNumbersCache() })` appended to `syncMetadata()`. The `suspend` use case is adapted to the sync's RxJava chain with `kotlinx.coroutines.rx2.rxCompletable { }` — the coroutine↔Rx conversion lives only at this boundary; the rest of the lot-number stack stays `suspend`.
- `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt`, `SyncDataWorkerModule.kt`, `SyncGranularRxModule.kt`, `SyncMetadataWorkerModule.kt` — each `syncPresenter` `@Provides` gains a `refreshLotNumbersCache: RefreshLotNumbersCacheUseCase` param (referenced by fully-qualified name to avoid touching the import block), forwarded to `SyncPresenterImpl(...)`. All four live in `app/src/main` only — **no flavor-override module**, so no desync risk on future Oslo upgrades to these files.
- `app/src/main/java/org/dhis2/data/user/UserModule.kt` — new `@Provides @PerUser fun refreshLotNumbersCacheUseCase(d2: D2, preferenceProvider: PreferenceProvider): RefreshLotNumbersCacheUseCase` that builds the real `LotNumberD2Repository` and wraps it in the use case. **No flavor gate / no no-op:** this branch only ever builds the `unicefTjkElmis` flavor, so the former `BuildConfig.FLAVOR` check and `LotNumberSyncRepositoryNoOp` were removed as defensive complexity for a case that cannot occur here. Single construction point for the repository.
- `app/src/test/java/org/dhis2/data/services/SyncPresenterTest.kt` — mocked `RefreshLotNumbersCacheUseCase` passed to `SyncPresenterImpl`; its `suspend operator fun invoke()` is stubbed inside `runBlocking { }`
- `commons/src/main/java/org/dhis2/commons/prefs/Preference.kt` — append `LOT_NUMBERS_CACHE` key constant

## 3. Shared drift still differing

PR 02 (§2.1) is the only shared-code drift against `develop-eyeseetea` baseline at HEAD `8a4866305`; all entries are additive/append-only as listed above. No other shared-code drift.

## 4. Feat commits

Tracks the commits that implement each customization, for cross-checking against §2 during automerge verification.

PR 01 (`feat/new_unicefTjkElmis_flavor`):
- (commits will be listed by SHA after merge)

PR 02 (`add-unicef-tjk-elmis-lot-number-field`):
- (commits will be listed by SHA after merge)

## 5. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
- The inventory must be completed before any baseline merge. Per `eyeseetea-docs/upgrade/conflict-rules.md`, the §"Automerge verification" rule runs `git diff develop-eyeseetea -- <file>` for every file listed here, so an incomplete inventory invites silent automerge loss after upgrades.
