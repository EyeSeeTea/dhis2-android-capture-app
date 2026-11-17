package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.userFriendlyValue
import org.dhis2.usescases.receipt.domain.model.GeographicalLocation
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

class D2ReceiptRepository(
    private val d2: D2,
    private val metadata: ReceiptMetadata,
) : ReceiptRepository {

    override suspend fun getReceipt(eventUid: String): Receipt =
        withContext(Dispatchers.IO) {
            val dataValues = d2.trackedEntityModule().trackedEntityDataValues()
                .byEvent().eq(eventUid)
                .blockingGet()
            val items = dataValues.mapNotNull { dataValue -> toItem(dataValue, dataValues) }
            val event = d2.eventModule().events().uid(eventUid).blockingGet()
            val teiUid = trackedEntityInstanceUid(event)
            Receipt(
                eventUid = eventUid,
                items = items,
                distributionName = d2.trackedEntityModule().trackedEntityDataValues()
                    .value(eventUid, metadata.distributionDataElementUid).blockingGet()
                    .userFriendlyValue(d2),
                spbhId = attributeValue(teiUid, metadata.spbhIdAttributeUid),
                beneficiaryName = attributeValue(teiUid, metadata.beneficiaryNameAttributeUid),
                distributionPoint = distributionPoint(event),
                distributionDate = event?.eventDate(),
                geographicalLocations = geographicalLocations(teiUid),
            )
        }

    // The flag is TRUE_ONLY: it is only ever set, never cleared, so a receipt that was not printed writes nothing.
    override suspend fun save(receipt: Receipt) {
        if (!receipt.printed) return
        withContext(Dispatchers.IO) {
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(receipt.eventUid, metadata.printedFlagDataElementUid)
                .blockingSet("true")
        }
    }

    // An item given is a data element whose code is sp_DIST_<ITEM>_GIVEN; only the ones with a value are printed.
    private fun toItem(dataValue: TrackedEntityDataValue, dataValues: List<TrackedEntityDataValue>): ReceiptItem? {
        if (dataValue.value().isNullOrBlank()) return null
        val dataElement = dataValue.dataElement()
            ?.let { d2.dataElementModule().dataElements().uid(it).blockingGet() }
            ?: return null
        if (dataElement.code()?.matches(ITEM_GIVEN_CODE) != true) return null
        return ReceiptItem(
            label = assignedName(dataElement.code(), dataValues)
                ?: dataElement.displayFormName() ?: dataElement.displayName() ?: dataElement.uid(),
            quantity = dataValue.userFriendlyValue(d2) ?: return null,
        )
    }

    // A free-text slot (sp_DIST_OTHER_FOOD_N_GIVEN) has its name in the sibling sp_DIST_OTHER_FOOD_N_DESC data element.
    private fun assignedName(givenCode: String?, dataValues: List<TrackedEntityDataValue>): String? {
        val descriptionCode = givenCode?.removeSuffix("_GIVEN")?.plus("_DESC") ?: return null
        return dataValues.firstOrNull { dataValue ->
            dataValue.dataElement()
                ?.let { d2.dataElementModule().dataElements().uid(it).blockingGet() }
                ?.code() == descriptionCode
        }?.value()?.takeUnless { it.isBlank() }
    }

    // The distribution point is the organisation unit of the event (to confirm with the client).
    private fun distributionPoint(event: Event?): String? {
        val orgUnitUid = event?.organisationUnit() ?: return null
        return d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()?.displayName()
    }

    private fun trackedEntityInstanceUid(event: Event?): String? {
        val enrollmentUid = event?.enrollment() ?: return null
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()?.trackedEntityInstance()
    }

    private fun geographicalLocations(teiUid: String?): List<GeographicalLocation> =
        metadata.geographicalLocationAttributeUids.mapIndexedNotNull { index, attributeUid ->
            attributeValue(teiUid, attributeUid)?.takeUnless { it.isBlank() }
                ?.let { name -> GeographicalLocation(level = index + 1, name = name) }
        }

    private fun attributeValue(teiUid: String?, attributeUid: String): String? =
        teiUid?.let {
            d2.trackedEntityModule().trackedEntityAttributeValues().value(attributeUid, it).blockingGet()
                ?.userFriendlyValue(d2)
        }

    private companion object {
        val ITEM_GIVEN_CODE = Regex("sp_DIST_.+_GIVEN")
    }
}
