package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.userFriendlyValue
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.hisp.dhis.android.core.D2

class D2ReceiptRepository(
    private val d2: D2,
    private val metadata: ReceiptMetadata,
) : ReceiptRepository {

    override suspend fun getReceipt(eventUid: String): Receipt? =
        withContext(Dispatchers.IO) {
            val lines = d2.trackedEntityModule().trackedEntityDataValues()
                .byEvent().eq(eventUid)
                .blockingGet()
                .mapNotNull { dataValue ->
                    val dataElementUid = dataValue.dataElement() ?: return@mapNotNull null
                    val dataElement = d2.dataElementModule().dataElements()
                        .uid(dataElementUid).blockingGet()
                    ReceiptLine(
                        label = dataElement?.displayFormName() ?: dataElement?.displayName() ?: dataElementUid,
                        value = dataValue.userFriendlyValue(d2) ?: return@mapNotNull null,
                    )
                }
            val teiUid = trackedEntityInstanceUid(eventUid)
            val receipt = Receipt(
                lines = lines,
                distributionName = d2.trackedEntityModule().trackedEntityDataValues()
                    .value(eventUid, metadata.distributionDataElementUid).blockingGet()
                    .userFriendlyValue(d2),
                rationCardId = attributeValue(teiUid, metadata.rationCardIdAttributeUid),
                beneficiaryName = attributeValue(teiUid, metadata.beneficiaryNameAttributeUid),
            )
            receipt.takeUnless { it.isEmpty() }
        }

    private fun trackedEntityInstanceUid(eventUid: String): String? {
        val enrollmentUid = d2.eventModule().events().uid(eventUid).blockingGet()?.enrollment() ?: return null
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()?.trackedEntityInstance()
    }

    private fun attributeValue(teiUid: String?, attributeUid: String): String? =
        teiUid?.let {
            d2.trackedEntityModule().trackedEntityAttributeValues().value(attributeUid, it).blockingGet()
                ?.userFriendlyValue(d2)
        }

    private fun Receipt.isEmpty() =
        lines.isEmpty() && distributionName == null && rationCardId == null && beneficiaryName == null
}
