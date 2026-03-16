# SPOCC customization files vs develop-eyeseetea

Technical inventory of the SPOCC customization surface on top of `develop-eyeseetea`.

## Mandatory header

- Client: `spocc`
- Flavor: `spOCC`
- Base branch: `develop-eyeseetea`
- Base commit: `7901840c8`
- Generated on: `2026-03-16`
- Working tree status: `dirty`

This file is intentionally separate from `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`:
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` documents the shared EyeSeeTea reference branch
- this file documents the SPOCC-specific implementation points that still survive in code

## Scope

This inventory is based on:
- direct flavor files under `app/src/spOCC/` and `app/src/spOCCDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

This file is not a full raw diff dump. Its goal is to answer:
- which confirmed functional customizations still exist
- where they are implemented
- what their current technical status is

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

### 2.1 Hide programs and datasets without write data access

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/dhislogic/DhisProgramUtils.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramRepositoryImpl.kt`

Technical note:
- shared selectors still differ from `develop-eyeseetea` to filter out items without write access.

### 2.2 Select UPG

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormFragment.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `form/src/main/java/org/dhis2/form/ui/Form.kt`
- `app/src/main/res/values/strings.xml`

Supporting files in the same workflow:
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

Technical note:
- the UPG selector flow still lives in shared code and remains separate from the baseline form behavior.

### 2.3 Hide Schedule menu in timeline view

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/teiDashboard/domain/GetNewEventCreationTypeOptions.kt`

Technical note:
- the timeline menu still removes the schedule action for SPOCC-specific behavior.

### 2.4 Avoid change org unit in tracker events

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/eventCaptureFragment/EventCaptureFormPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/programStageSelection/ProgramStageSelectionActivity.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventInitial/EventInitialActivity.kt`

Technical note:
- tracker event flows still override the baseline org unit selection behavior.

### 2.5 Validate or hide orgunit by Teamprofile

Status: `active`

Main implementation points:
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

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/models/EventDetails.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/providers/InputFieldsProvider.kt`
- `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/ui/EventDetailsViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEActivity.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEContractsModule.java`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`
- `commons/src/main/java/org/dhis2/commons/orgunitselector/OUTreeViewModel.kt`
- `commons/src/main/java/org/dhis2/commons/team/dateToYearlyPeriod.kt`
- `commons/src/main/java/org/dhis2/commons/team/isActiveOrgUnit.kt`

Technical note:
- Teamprofile validation still drives org unit visibility and rejection rules across enrollment, event, and dataset flows.

### 2.6 Hide re-open menu always

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDashboardMenu.kt`

Technical note:
- the dashboard menu still suppresses the re-open action for SPOCC.

### 2.7 Session format ui like in server year-nextYear

Status: `active`

Main implementation points:
- `commons/src/main/java/org/dhis2/commons/resources/DhisPeriodUtils.kt`
- `app/src/test/java/org/dhis2/data/dhislogic/DhisPeriodUtilsTest.kt`

Technical note:
- yearly period labels still differ from the baseline format and require dedicated coverage.

### 2.8 Avoid resize images

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/server/ServerModule.kt`

Technical note:
- image upload configuration still prevents the baseline resize behavior.

### 2.9 Team change request

Status: `active`

Main implementation points:
- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepositoryImpl.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/data/DataSetInstanceRepository.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/model/DataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/DataSetModalDialogUiState.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModel.kt`

Supporting files in the same workflow:
- `aggregates/src/commonMain/composeResources/values/strings.xml`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/di/AggregateModule.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/DataSetModalDialogProvider.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/ResourceManager.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/component/ValidationBottomSheet.kt`
- `aggregates/build.gradle.kts`

Technical note:
- aggregate dataset flows still contain a dedicated team change request path not present in the shared baseline.

### 2.10 Multiple SDS org unit selection

Status: `active`

Main implementation points:
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

Supporting files in the same workflow:
- `aggregates/src/androidMain/kotlin/org/dhis2/mobile/aggregates/data/mappers/DataSetInstanceToDataSetDetails.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/domain/EyeSeeTeaConstants.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/provider/ResourceManager.kt`
- `aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/states/DataSetModalDialogUiState.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/input/UiActionHandler.kt`

Technical note:
- the dataset input flow still supports multi-selection and SDS-specific resolution beyond baseline behavior.

## 3. Shared drift still differing

Use this section only for temporary or still-unclassified differences.

- `app/build.gradle.kts` - pending classification because it still differs but no confirmed customization title has been assigned.
- `app/src/main/java/org/dhis2/data/forms/ScanCaptureActivity.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/data/forms/ScanCaptureManager.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/data/forms/ScanContract.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/data/user/UserComponent.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/about/AboutFragment.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialActivity.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialContract.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialModel.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialModule.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialPresenter.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/java/org/dhis2/usescases/datasets/datasetInitial/DataSetInitialRepositoryImpl.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/res/drawable/ic_alert.xml` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/res/drawable/ic_error_outline.xml` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/main/res/drawable/ic_saved_check.xml` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialPresenterTest.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/test/java/org/dhis2/usescases/datasets/dataSetInitial/DataSetInitialRepositoryImplTest.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/test/java/org/dhis2/usescases/eventsWithoutRegistration/eventDetails/domain/ConfigureEventDetailsTest.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `app/src/test/java/org/dhis2/usescases/main/program/ProgramRepositoryImplTest.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `commons/src/main/java/org/dhis2/commons/Constants.java` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `commonskmm/build.gradle.kts` - pending classification because the surviving diff has not yet been mapped to a functional customization.
- `form/src/main/java/org/dhis2/form/ui/provider/inputfield/FieldProvider.kt` - pending classification because the surviving diff has not yet been mapped to a functional customization.

## 4. Notes

- This inventory reflects the current branch state only.
- If files are merged, renamed, reverted, or reworked, regenerate this file from the current code and the diff against `develop-eyeseetea`.
- The source of truth for functional titles remains `customization-specs.md`.
- If code comments and functional titles diverge, prefer the title defined in `customization-specs.md` and update the code comment when possible.
