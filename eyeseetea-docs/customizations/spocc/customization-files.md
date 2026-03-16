# SPOCC customization files vs develop-eyeseetea

Technical inventory of the SPOCC customization surface on top of `develop-eyeseetea`.

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch.
- this file documents the SPOCC-specific implementation points that still survive in code

## Scope

This inventory is based on:
- direct flavor files under `app/src/spOCC/` and `app/src/spOCCDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

This file is not a full raw diff dump. Its goal is to answer:
- which functional customizations still exist
- where they are implemented

## 1. Direct SPOCC flavor surface

These files belong to the SPOCC flavor itself.

### 1.1 SPOCC flavor code

- `app/src/spOCC/java/org/dhis2/data/user/UserComponentFlavor.kt`
- `app/src/spOCC/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureRepositoryFunctions.kt`
- `app/src/spOCC/java/org/dhis2/usescases/main/program/ProgramAnimation.kt`
- `app/src/spOCC/java/org/dhis2/usescases/teiDashboard/TeiDashboardMenu.kt`
- `app/src/spOCC/java/org/dhis2/utils/CustomizableConstants.kt`
- `app/src/spOCC/java/org/dhis2/utils/JsonChecker.kt`
- `app/src/spOCC/java/org/dhis2/utils/granularsync/GranularSyncModule.kt`

### 1.2 SPOCC flavor resources and branding

- `app/src/spOCC/`
- `app/src/spOCCDebug/`

## 2. Shared-code customization implementation points

These are the current shared files where SPOCC customization logic is explicitly implemented.

### 2.1 Only programs and datasets with write access

- `app/src/main/java/org/dhis2/data/dhislogic/DhisProgramUtils.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramRepositoryImpl.kt`

### 2.2 Select UPG

- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormFragment.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `form/src/main/java/org/dhis2/form/ui/Form.kt`
- `app/src/main/res/values/strings.xml`

Supporting SPOCC-specific UPG flow files differing from `develop-eyeseetea`:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormView.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/SelectUPGDialogComponent.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/SelectUPGDialogModule.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/data/UPGD2Repository.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/domain/GetUPGItems.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/domain/UPGItem.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/domain/UPGRepository.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/ui/SelectUPGDialog.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/ui/SelectUPGDialogAdapter.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/ui/SelectUPGDialogHolder.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/ui/SelectUPGDialogPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/upg/ui/SelectUPGDialogView.kt`
- `app/src/main/res/layout/dialog_select_upg.xml`
- `app/src/main/res/layout/item_upg.xml`

### 2.3 Hide Schedule menu in timeline view

- `app/src/main/java/org/dhis2/usescases/teiDashboard/domain/GetNewEventCreationTypeOptions.kt`

Note:
- the current inline comment in code says `Not show schedule events when programStage is null`; this implementation should still be treated as the SPOCC customization titled `Hide Schedule menu in timeline view`

### 2.4 Avoid change org unit in tracker events

- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/programStageSelection/ProgramStageSelectionActivity.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`

Supporting file differing in the same flow:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventInitial/EventInitialActivity.kt`

### 2.5 Validate or hide orgunit by Teamprofile

- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentActivity.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/FormInjector.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/data/EventDetailsRepository.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/domain/ConfigureEventDetails.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsFragment.kt`
- `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailActivity.kt`
- `app/src/main/java/org/dhis2/usescases/programEventDetail/ProgramEventDetailModule.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OURepositoryConfiguration.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeFragment.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeModule.kt`
- `form/src/main/java/org/dhis2/form/model/FieldUiModel.kt`
- `form/src/main/java/org/dhis2/form/model/FieldUiModelImpl.kt`
- `form/src/main/java/org/dhis2/form/model/SectionUiModelImpl.kt`
- `form/src/main/java/org/dhis2/form/ui/FormView.kt`
- `form/src/main/java/org/dhis2/form/ui/FormViewFragmentFactory.kt`
- `form/src/main/java/org/dhis2/form/ui/event/RecyclerViewUiEvents.kt`
- `app/src/main/res/values/strings.xml`

Supporting files differing in the same customization area:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/models/EventDetails.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/providers/InputFieldsProvider.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEActivity.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEContractsModule.java`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeViewModel.kt`
- `commons/src/main/java/org/dhis2/commons/team/dateToYearlyPeriod.kt`
- `commons/src/main/java/org/dhis2/commons/team/isActiveOrgUnit.kt`

### 2.6 Hide re-open menu always

- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDashboardMenu.kt`

### 2.7 Session format ui like in server year-nextYear

- `commons/src/main/java/org/dhis2/commons/resources/DhisPeriodUtils.kt`
- `app/src/test/java/org/dhis2/data/dhislogic/DhisPeriodUtilsTest.kt`

### 2.8 Avoid resize images

- `app/src/main/java/org/dhis2/data/server/ServerModule.kt`

### 2.9 Team change request

- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepositoryImpl.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepository.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/model/DataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/DataSetModalDialogUiState.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModel.kt`

Supporting files differing in the same workflow:
- `aggregates/src/commonMain/composeResources/values/strings.xml`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/di/AggregateModule.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/DataSetModalDialogProvider.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/ResourceManager.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/component/ValidationBottomSheet.kt`
- `aggregates/build.gradle.kts`

### 2.10 Multiple SDS org unit selection

- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepositoryImpl.kt`
- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/ui/UiActionHandlerImpl.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepository.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/CreateDisplayValue.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/GetDataValueData.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/GetDataValueInput.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/inputs/InputProvider.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/mapper/InputDataUiStateMapper.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModel.kt`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OURepositoryConfiguration.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/input/UiAction.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/orgunit/OrgUnitSelectorScope.kt`

Supporting files differing in the same workflow:
- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/mappers/DataSetInstanceToDataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/EyeSeeTeaConstants.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/ResourceManager.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/DataSetModalDialogUiState.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/input/UiActionHandler.kt`

## 3. Shared drift still differing but not mapped to a documented customization title

These files still differ from `develop-eyeseetea`, but the current code comments do not map them cleanly to a documented SPOCC customization title. They should be reviewed case by case during upgrades.

- `app/build.gradle.kts`
- `app/src/main/java/org/dhis2/data/forms/ScanCaptureActivity.kt`
- `app/src/main/java/org/dhis2/data/forms/ScanCaptureManager.kt`
- `app/src/main/java/org/dhis2/data/forms/ScanContract.kt`
- `app/src/main/java/org/dhis2/data/user/UserComponent.java`
- `app/src/main/java/org/dhis2/usescases/about/AboutFragment.kt`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialActivity.java`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialContract.java`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialModel.kt`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialModule.java`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialRepositoryImpl.java`
- `app/src/main/res/drawable/ic_alert.xml`
- `app/src/main/res/drawable/ic_error_outline.xml`
- `app/src/main/res/drawable/ic_saved_check.xml`
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialPresenterTest.kt`
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialRepositoryImplTest.kt`
- `app/src/test/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/domain/ConfigureEventDetailsTest.kt`
- `app/src/test/java/org/dhis2/usescases/main/program/ProgramRepositoryImplTest.kt`
- `commons/src/main/java/org/dhis2/commons/Constants.java`
- `commonskmm/build.gradle.kts`
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt`

## 4. Notes

- This inventory reflects the current branch state only.
- If files are merged, renamed, reverted, or reworked, regenerate this file from the current code and the diff against `develop-eyeseetea`.
- The source of truth for functional titles remains `customization-specs.md`.
- If code comments and functional titles diverge, prefer the title defined in `customization-specs.md` and update the code comment when possible.
