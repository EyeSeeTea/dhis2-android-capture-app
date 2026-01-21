package org.dhis2.usescases.sms.data.repository.audit

import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.sms.domain.model.audit.Audit
import org.dhis2.usescases.sms.domain.repository.audit.AuditRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection

private const val program = "SdHQreF7gdU"
private const val programStage = "IsrH6YFBfnM"
private const val dateDE = "lyw4MJ2v1p3"
private const val messageDE = "fws849y4UCG"
private const val translatedMessageDE = "gNFQebWtsb5"
private const val lastSMSSentAttribute = "QOlXGMKCtjQ"

class AuditD2Repository(
    private val d2: D2,
) : AuditRepository {

    override suspend fun save(audit: Audit) {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .uid(audit.patientUid).blockingGet()
            ?: throw IllegalArgumentException("No TEI found with uid: ${audit.patientUid}")

        updateLastSentAttribute(tei.uid(), audit)

        createAuditEvent(tei.uid(),audit)
    }

    private fun updateLastSentAttribute(teiUid:String,audit: Audit){
        val formattedDate = formatDateToUTC(audit.date)

        d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(lastSMSSentAttribute, teiUid)
            .blockingSet(formattedDate)
    }

    private fun createAuditEvent(teiUid:String, audit: Audit){
        val enrollments = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(program).blockingGet()

        val enrollment = enrollments.firstOrNull()
            ?: throw IllegalArgumentException("No enrollment found for TEI with uid: ${audit.patientUid}")

        val orgUnitUid = enrollment.organisationUnit()
            ?: throw IllegalArgumentException("No organisation unit found for enrollment: ${enrollment.uid()}")


        val eventUid = d2.eventModule().events().blockingAdd(
            EventCreateProjection.builder()
                .enrollment(enrollment.uid())
                .program(program)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .build()
        )

        d2.eventModule().events().uid(eventUid).setEventDate(audit.date)

        val formattedAuditDate = formatDateToUTC(audit.date)
        d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, dateDE)
            .blockingSet(formattedAuditDate)

        d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, messageDE)
            .blockingSet(audit.message)

        audit.translatedMessage?.let { translatedMessage ->
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(eventUid, translatedMessageDE)
                .blockingSet(translatedMessage)
        }
    }

    private fun formatDateToUTC(date: java.util.Date): String {
        return DateUtils.databaseDateFormatNoZulu().format(date)
    }
}