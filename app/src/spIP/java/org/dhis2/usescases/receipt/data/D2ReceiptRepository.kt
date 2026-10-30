package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.userFriendlyValue
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventStatus

class D2ReceiptRepository(private val d2: D2) : ReceiptRepository {

    override suspend fun getReceipt(enrollmentUid: String): Receipt? =
        withContext(Dispatchers.IO) {
            val eventUid = lastCompletedEventUid(enrollmentUid) ?: return@withContext null
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
            if (lines.isEmpty()) null else Receipt(lines)
        }

    // Event selection is an assumption pending client confirmation: the latest completed event of the enrollment.
    private fun lastCompletedEventUid(enrollmentUid: String): String? =
        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byStatus().eq(EventStatus.COMPLETED)
            .byDeleted().isFalse
            .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
            .blockingGet()
            .firstOrNull()
            ?.uid()
}
