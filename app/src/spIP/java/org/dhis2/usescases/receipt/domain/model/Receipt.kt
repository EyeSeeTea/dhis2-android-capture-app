package org.dhis2.usescases.receipt.domain.model

import java.util.Date

// Draft: the content is expected to change once the client confirms the receipt layout.
// A header value is null when the event or the entity has no value for it.
data class Receipt(
    val eventUid: String = "",
    val items: List<ReceiptItem> = emptyList(),
    val distributionName: String? = null,
    val spbhId: String? = null,
    val beneficiaryName: String? = null,
    val distributionPoint: String? = null,
    val distributionDate: Date? = null,
    val geographicalLocations: List<GeographicalLocation> = emptyList(),
    val printedOn: Date? = null,
    val printed: Boolean = false,
) {
    fun markAsPrinted(): Receipt = copy(printed = true)
}
