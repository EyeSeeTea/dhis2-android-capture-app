package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.userFriendlyValue
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.hisp.dhis.android.core.D2

class D2ReceiptRepository(private val d2: D2) : ReceiptRepository {

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
            if (lines.isEmpty()) null else Receipt(lines)
        }
}
