# SPOCC customizations vs develop-eyeseetea

Inventory of the current branch (`feature-spocc/bring_last_changes_3_3_1`) compared against `develop-eyeseetea`.

This file is intentionally separate from `customizations-eyeseetea.md`:
- `customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch.
- This file documents what is specific to the current SPOCC branch on top of `develop-eyeseetea`.

## Scope

This inventory is based on the current repository state after bringing `develop-eyeseetea`.

Included:
- Flavor-specific files under `app/src/spOCC/` and `app/src/spOCCDebug/`
- Shared-code files that still differ from `develop-eyeseetea`
- Files currently in merge conflict, which are the clearest signal of SPOCC-specific logic that still needs resolution

Excluded:
- `customizations-eyeseetea.md`
- PSI/WIDP and other flavor-specific files not related to SPOCC
- Generated/build output
- The vendored SDK tree

## Summary

- **Flavor-specific SPOCC files:** 40 under `app/src/spOCC/`
- **Flavor-specific SPOCC debug files:** 33 under `app/src/spOCCDebug/`
- **Files currently in merge conflict:** 45

## 1. Direct SPOCC flavor customizations

These files exist specifically for the `spOCC` flavor and are part of the SPOCC customization surface.

### 1.1 SPOCC flavor code

- `app/src/spOCC/java/org/dhis2/data/user/UserComponentFlavor.kt`
- `app/src/spOCC/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureRepositoryFunctions.kt`
- `app/src/spOCC/java/org/dhis2/usescases/main/program/ProgramAnimation.kt`
- `app/src/spOCC/java/org/dhis2/usescases/teiDashboard/TeiDashboardMenu.kt`
- `app/src/spOCC/java/org/dhis2/utils/CustomizableConstants.kt`
- `app/src/spOCC/java/org/dhis2/utils/JsonChecker.kt`
- `app/src/spOCC/java/org/dhis2/utils/granularsync/GranularSyncModule.kt`

### 1.2 SPOCC flavor branding/resources

- `app/src/spOCC/ic_launcher-playstore.png`
- `app/src/spOCC/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/spOCC/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/spOCC/res/mipmap-hdpi/ic_launcher.png`
- `app/src/spOCC/res/mipmap-hdpi/ic_launcher_foreground.png`
- `app/src/spOCC/res/mipmap-hdpi/ic_launcher_round.png`
- `app/src/spOCC/res/mipmap-mdpi/ic_launcher.png`
- `app/src/spOCC/res/mipmap-mdpi/ic_launcher_foreground.png`
- `app/src/spOCC/res/mipmap-mdpi/ic_launcher_round.png`
- `app/src/spOCC/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/spOCC/res/mipmap-xhdpi/ic_launcher_foreground.png`
- `app/src/spOCC/res/mipmap-xhdpi/ic_launcher_round.png`
- `app/src/spOCC/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/spOCC/res/mipmap-xxhdpi/ic_launcher_foreground.png`
- `app/src/spOCC/res/mipmap-xxhdpi/ic_launcher_round.png`
- `app/src/spOCC/res/mipmap-xxxhdpi/ic_launcher.png`
- `app/src/spOCC/res/mipmap-xxxhdpi/ic_launcher_foreground.png`
- `app/src/spOCC/res/mipmap-xxxhdpi/ic_launcher_round.png`
- `app/src/spOCC/res/values/strings.xml`
- `app/src/spOCC/res/values/ic_launcher_background.xml`
- `app/src/spOCC/res/values-ar/strings.xml`
- `app/src/spOCC/res/values-cs/strings.xml`
- `app/src/spOCC/res/values-es/strings.xml`
- `app/src/spOCC/res/values-fr/strings.xml`
- `app/src/spOCC/res/values-id/strings.xml`
- `app/src/spOCC/res/values-km/strings.xml`
- `app/src/spOCC/res/values-lo/strings.xml`
- `app/src/spOCC/res/values-nb/strings.xml`
- `app/src/spOCC/res/values-pt/strings.xml`
- `app/src/spOCC/res/values-ru/strings.xml`
- `app/src/spOCC/res/values-sv/strings.xml`
- `app/src/spOCC/res/values-vi/strings.xml`
- `app/src/spOCC/res/values-zh-rCN/strings.xml`

### 1.3 SPOCC debug branding/resources

- `app/src/spOCCDebug/ic_launcher-playstore.png`
- `app/src/spOCCDebug/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/spOCCDebug/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/spOCCDebug/res/mipmap-hdpi/ic_launcher.png`
- `app/src/spOCCDebug/res/mipmap-hdpi/ic_launcher_foreground.png`
- `app/src/spOCCDebug/res/mipmap-hdpi/ic_launcher_round.png`
- `app/src/spOCCDebug/res/mipmap-mdpi/ic_launcher.png`
- `app/src/spOCCDebug/res/mipmap-mdpi/ic_launcher_foreground.png`
- `app/src/spOCCDebug/res/mipmap-mdpi/ic_launcher_round.png`
- `app/src/spOCCDebug/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/spOCCDebug/res/mipmap-xhdpi/ic_launcher_foreground.png`
- `app/src/spOCCDebug/res/mipmap-xhdpi/ic_launcher_round.png`
- `app/src/spOCCDebug/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/spOCCDebug/res/mipmap-xxhdpi/ic_launcher_foreground.png`
- `app/src/spOCCDebug/res/mipmap-xxhdpi/ic_launcher_round.png`
- `app/src/spOCCDebug/res/mipmap-xxxhdpi/ic_launcher.png`
- `app/src/spOCCDebug/res/mipmap-xxxhdpi/ic_launcher_foreground.png`
- `app/src/spOCCDebug/res/mipmap-xxxhdpi/ic_launcher_round.png`
- `app/src/spOCCDebug/res/values/strings.xml`
- `app/src/spOCCDebug/res/values/ic_launcher_background.xml`
- `app/src/spOCCDebug/res/values-ar/strings.xml`
- `app/src/spOCCDebug/res/values-cs/strings.xml`
- `app/src/spOCCDebug/res/values-es/strings.xml`
- `app/src/spOCCDebug/res/values-fr/strings.xml`
- `app/src/spOCCDebug/res/values-id/strings.xml`
- `app/src/spOCCDebug/res/values-km/strings.xml`
- `app/src/spOCCDebug/res/values-lo/strings.xml`
- `app/src/spOCCDebug/res/values-nb/strings.xml`
- `app/src/spOCCDebug/res/values-pt/strings.xml`
- `app/src/spOCCDebug/res/values-ru/strings.xml`
- `app/src/spOCCDebug/res/values-sv/strings.xml`
- `app/src/spOCCDebug/res/values-vi/strings.xml`
- `app/src/spOCCDebug/res/values-zh-rCN/strings.xml`

## 2. Shared-code files currently in conflict

These are the most important files to resolve because both `develop-eyeseetea` and the SPOCC branch modified them.

### 2.1 Aggregates

- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/mappers/DataSetInstanceToDataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/di/AggregateModule.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/GetDataValueData.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/GetDataValueInput.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/inputs/InputProvider.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/input/UiAction.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/DataSetModalDialogProvider.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/mapper/InputDataUiStateMapper.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModel.kt`
- `aggregates/src/commonTest/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModelTest.kt`

### 2.2 App

- `app/src/main/java/org/dhis2/data/dhislogic/DhisProgramUtils.kt`
- `app/src/main/java/org/dhis2/data/server/ServerModule.kt`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialModel.java`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialRepositoryImpl.java`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentActivity.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/FormInjector.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormFragment.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/data/EventDetailsRepository.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/domain/ConfigureEventDetails.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsFragment.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailActivity.kt`
- `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailModule.kt`
- `app/src/main/java/org/dhis2/usescases/programStageSelection/ProgramStageSelectionActivity.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDashboardMenu.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialPresenterTest.kt`
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialRepositoryImplTest.kt`
- `app/src/test/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/domain/ConfigureEventDetailsTest.kt`
- `app/src/test/java/org/dhis2/usescases/main/program/ProgramRepositoryImplTest.kt`

### 2.3 Commons

- `commons/src/main/java/org/dhis2/commons/orgunitselector/OURepositoryConfiguration.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeFragment.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeModule.kt`
- `commons/src/main/java/org/dhis2/commons/resources/DhisPeriodUtils.kt`

### 2.4 Form

- `form/src/main/java/org/dhis2/form/model/FieldUiModel.kt`
- `form/src/main/java/org/dhis2/form/model/FieldUiModelImpl.kt`
- `form/src/main/java/org/dhis2/form/model/SectionUiModelImpl.kt`
- `form/src/main/java/org/dhis2/form/ui/Form.kt`
- `form/src/main/java/org/dhis2/form/ui/FormView.kt`
- `form/src/main/java/org/dhis2/form/ui/FormViewFragmentFactory.kt`
- `form/src/main/java/org/dhis2/form/ui/event/RecyclerViewUiEvents.kt`

Confirmed shared SPOCC customization already re-applied on top of `develop-eyeseetea`:

- `Validate or hide orgunit by Teamprofile`
  Confirmed files:
  `commons/src/main/java/org/dhis2/commons/orgunitselector/OURepositoryConfiguration.kt`,
  `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeFragment.kt`,
  `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeModule.kt`,
  `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentActivity.kt`,
  `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`,
  `app/src/main/java/org/dhis2/usescases/enrollment/FormInjector.kt`,
  `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/data/EventDetailsRepository.kt`,
  `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsFragment.kt`,
  `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailActivity.kt`,
  `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailModule.kt`,
  `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`,
  `form/src/main/java/org/dhis2/form/model/FieldUiModel.kt`,
  `form/src/main/java/org/dhis2/form/model/FieldUiModelImpl.kt`,
  `form/src/main/java/org/dhis2/form/model/SectionUiModelImpl.kt`,
  `form/src/main/java/org/dhis2/form/ui/FormView.kt`,
  `form/src/main/java/org/dhis2/form/ui/FormViewFragmentFactory.kt`,
  `form/src/main/java/org/dhis2/form/ui/event/RecyclerViewUiEvents.kt`.

- `Select UPG`
  Confirmed files:
  `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormFragment.kt`,
  `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`,
  `form/src/main/java/org/dhis2/form/ui/Form.kt`.

- `Hide re-open menu always`
  Confirmed files:
  `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDashboardMenu.kt`.

- `Session format ui like in server year-nextYear`
  Confirmed files:
  `commons/src/main/java/org/dhis2/commons/resources/DhisPeriodUtils.kt`.

- `Avoid change org unit in tracker events`
  Confirmed files:
  `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`,
  `app/src/main/java/org/dhis2/usescases/programStageSelection/ProgramStageSelectionActivity.kt`,
  `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`,
  `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`.

- `Avoid resize images`
  Confirmed files:
  `app/src/main/java/org/dhis2/data/server/ServerModule.kt`.


## 3. Shared-code differences already present outside `spOCC/`

Besides the direct flavor files and the current conflicts, the branch still differs from `develop-eyeseetea` in shared modules. The most likely SPOCC-specific areas are:

### 3.1 Team / org unit / dataset behavior

- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepositoryImpl.kt`
- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/ui/UiActionHandlerImpl.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepository.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/CreateChangeTeamRequest.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/CreateDisplayValue.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/EyeSeeTeaConstants.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/model/DataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/component/ValidationBottomSheet.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/ResourceManager.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/DataSetModalDialogUiState.kt`

### 3.2 Event / enrollment / TEI flows

- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCaptureContract.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCaptureActivity.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCapturePresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCaptureRepositoryImpl.java`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormView.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/models/EventDetails.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/providers/InputFieldsProvider.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventInitial/EventInitialActivity.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventInitial/EventInitialRepositoryImpl.java`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/TeiDashboardMobileActivity.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TeiDataRepository.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TeiDataRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/domain/GetNewEventCreationTypeOptions.kt`

### 3.3 Search / program / form integration

- `app/src/main/java/org/dhis2/usescases/main/program/ProgramFragment.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramModule.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImpl.java`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImplKt.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEActivity.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEContractsModule.java`
- `form/src/main/java/org/dhis2/form/data/FormValueStore.kt`
- `form/src/main/java/org/dhis2/form/data/EnrollmentRepository.kt`
- `form/src/main/java/org/dhis2/form/data/EventRepository.kt`
- `form/src/main/java/org/dhis2/form/data/metadata/EnrollmentConfiguration.kt`
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactory.kt`
- `form/src/main/java/org/dhis2/form/ui/FieldViewModelFactoryImpl.kt`
- `form/src/main/java/org/dhis2/form/ui/FormViewModel.kt`
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt`

### 3.4 Build/config files that still differ

- `app/build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`

## 4. Practical interpretation

For resolving the merge and maintaining the inventory:

- Files in `app/src/spOCC/` and `app/src/spOCCDebug/` are direct SPOCC customizations.
- Files in section 2 are shared-code customizations that must be manually reconciled.
- Files in section 3 are the next review set after conflicts are resolved.

If needed, a later revision of this file can be reduced further from "inventory of differences" to "final SPOCC customizations kept after merge resolution".
