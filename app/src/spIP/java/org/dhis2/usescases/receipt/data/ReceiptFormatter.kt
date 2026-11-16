package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt

// Plain-text receipt sent to the printer: the header, a blank line and the items given.
// The texts are provisional until the client confirms the layout.
class ReceiptFormatter {
    fun format(receipt: Receipt): List<String> {
        val header = listOfNotNull(
            receipt.distributionName?.let { "Distribution: $it" },
            receipt.spbhId?.let { "SPBH ID: $it" },
            receipt.beneficiaryName?.let { "Beneficiary: $it" },
        )
        return header + "" + receipt.items.map { item -> "${item.label}: ${item.quantity}" }
    }
}
