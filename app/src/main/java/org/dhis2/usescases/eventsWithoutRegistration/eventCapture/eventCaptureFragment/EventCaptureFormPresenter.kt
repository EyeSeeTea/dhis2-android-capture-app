package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.team.ValidationData
import org.dhis2.commons.team.dateToYearlyPeriod
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.form.data.EventRepository.Companion.EVENT_ORG_UNIT_UID
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.usescases.eventsWithoutRegistration.EventIdlingResourceSingleton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ReOpenEventUseCase
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.domain.UPGItem
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventStatus

data class UPGProgram(val programUid: String, val upgName: String, val upgUid: String)

private const val seasonPlanProgramUid = "WCJhvPcJomX"
private const val seasonPlanUPGName = "cFKggVHL4pu"
private const val seasonPlanUPGUId = "LDh4Dt7xGD0"

private const val endOfSeasonReportProgramUid = "JsM6wTUTsL6"
private const val endOfSeasonReportUPGName = "T4TY8UuBiza"
private const val endOfSeasonReportUPGUId = "e3m1f3pdLAl"

private val programsWithUPG = listOf(
    UPGProgram(seasonPlanProgramUid, seasonPlanUPGName, seasonPlanUPGUId),
    UPGProgram(endOfSeasonReportProgramUid, endOfSeasonReportUPGName, endOfSeasonReportUPGUId)
)


class EventCaptureFormPresenter(
    private val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    private val d2: D2,
    private val eventUid: String,
    private val resourceManager: ResourceManager,
    private val reOpenEventUseCase: ReOpenEventUseCase,
    private val dispatcherProvider: DispatcherProvider,
) {
    fun showOrHideSaveButton() {
        val isEditable =
            d2.eventModule().eventService().getEditableStatus(eventUid = eventUid).blockingGet()

        when (isEditable) {
            is EventEditableStatus.Editable -> {
                view.showSaveButton()
                view.hideNonEditableMessage()
            }

            is EventEditableStatus.NonEditable -> {
                view.hideSaveButton()
                configureNonEditableMessage(isEditable.reason)
            }
        }
    }

    fun saveAndExit(eventStatus: EventStatus?) {
        activityPresenter.saveAndExit(eventStatus)
    }

    private fun configureNonEditableMessage(eventNonEditableReason: EventNonEditableReason) {
        val (reason, canBeReOpened) = when (eventNonEditableReason) {
            EventNonEditableReason.BLOCKED_BY_COMPLETION -> resourceManager.getString(R.string.blocked_by_completion) to canReopen()
            EventNonEditableReason.EXPIRED -> resourceManager.getString(R.string.edition_expired) to false
            EventNonEditableReason.NO_DATA_WRITE_ACCESS -> resourceManager.getString(R.string.edition_no_write_access) to false
            EventNonEditableReason.EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE -> resourceManager.getString(R.string.event_date_not_in_orgunit_range) to false
            EventNonEditableReason.NO_CATEGORY_COMBO_ACCESS -> resourceManager.getString(R.string.edition_no_catcombo_access) to false
            EventNonEditableReason.ENROLLMENT_IS_NOT_OPEN -> resourceManager.formatWithEnrollmentLabel(
                d2.eventModule().events().uid(eventUid).blockingGet()?.program(),
                R.string.edition_enrollment_is_no_open_V2,
                1,
            ) to false

            EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE -> resourceManager.getString(R.string.edition_orgunit_capture_scope) to false
        }
        view.showNonEditableMessage(reason, canBeReOpened)
    }

    fun reOpenEvent() {
        EventIdlingResourceSingleton.increment()
        CoroutineScope(dispatcherProvider.ui()).launch {
            reOpenEventUseCase(eventUid).fold(
                onSuccess = {
                    view.onReopen()
                    view.showSaveButton()
                    view.hideNonEditableMessage()
                    EventIdlingResourceSingleton.decrement()
                },
                onFailure = { error ->
                    resourceManager.parseD2Error(error)
                    EventIdlingResourceSingleton.decrement()
                },
            )
        }
    }

    private fun canReopen(): Boolean = getEvent()?.let {
        it.status() == EventStatus.COMPLETED && hasReopenAuthority()
    } ?: false

    fun getEvent(): Event? {
        return d2.eventModule().events().uid(eventUid).blockingGet()
    }

    fun getEventStatus(eventUid: String): EventStatus? {
        return d2.eventModule().events().uid(eventUid).blockingGet()?.status()
    }

    private fun hasReopenAuthority(): Boolean = d2.userModule().authorities()
        .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
        .one()
        .blockingExists()

    //EyeSeeTea customization
    private var upgUidUIModel: FieldUiModel? = null
    private var upgNameUIModel: FieldUiModel? = null
    private var savingSelectedUPG = false

    fun onFieldsLoading(fields: List<FieldUiModel>): List<FieldUiModel> {
        val fieldsWithOrgUnitValidation = mapWithOrgUnitValidation(fields)

        return mapUPGFields(fieldsWithOrgUnitValidation)
    }

    private fun mapWithOrgUnitValidation(fields: List<FieldUiModel>): List<FieldUiModel> {
        val programUid: String =
            d2.eventModule().events().byUid().eq(eventUid).one().blockingGet()?.program() ?: ""

        val event = d2.eventModule().events().byUid().eq(eventUid).one().blockingGet()

        val isEventFromEnrolment = event?.enrollment() != null

        return if (isEventFromEnrolment) {
            fields.map {
                if (it.uid == EVENT_ORG_UNIT_UID) {
                    it.setEditable(false)
                } else {
                    it
                }
            }
        } else {
            fields.map {
                if (it.uid == EVENT_ORG_UNIT_UID) {
                    val period = event?.eventDate()?.let { date ->
                        dateToYearlyPeriod(date) ?: ""
                    } ?: ""

                    it.setOrgUnitDataValidation(ValidationData(programUid, period))
                } else {
                    it
                }
            }
        }
    }

    private fun mapUPGFields(fields: List<FieldUiModel>): List<FieldUiModel> {
        val programUid: String =
            d2.eventModule().events().byUid().eq(eventUid).one().blockingGet()?.program() ?: ""

        val programWithUPG = programsWithUPG.find { it.programUid == programUid } ?: return fields

        //EyeSeeTea customization - Remove UPG field from the form
        val finalFields = fields
            .map { field ->
                if (field.uid == programWithUPG.upgUid) {
                    field.setVisible(false)
                } else {
                    field
                }
            }

        upgUidUIModel = finalFields.find { it.uid == programWithUPG.upgUid }
        upgNameUIModel = finalFields.find { it.uid == programWithUPG.upgName }

        if (upgUidUIModel != null && upgNameUIModel != null) {
            val event = d2.eventModule().events().byUid().eq(eventUid).one().blockingGet()

            (upgNameUIModel as FieldUiModelImpl).setOverrideFocusCallback {
                if (event?.organisationUnit() != null && !savingSelectedUPG) {
                    view.selectUPG(event.organisationUnit()!!)
                }
            }
        }

        return finalFields
    }

    fun onUPGSelected(upg: UPGItem) {
        savingSelectedUPG = true

        if (upg.guid.isNotBlank()) {
            upgUidUIModel?.onSave(upg.guid)
        }

        upgNameUIModel?.onSave(upg.name)

        savingSelectedUPG = false
    }
}
