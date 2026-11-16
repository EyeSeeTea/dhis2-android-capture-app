package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt
import java.text.SimpleDateFormat
import java.util.Locale

// Plain-text receipt sent to the printer: the header, a blank line and the items given.
// The texts are provisional until the client confirms the layout.
class ReceiptFormatter {
    fun format(receipt: Receipt): List<String> {
        val header = listOfNotNull(
            receipt.distributionName?.let { "Distribution: $it" },
            receipt.distributionPoint?.let { "Distribution point: $it" },
            receipt.distributionDate?.let { "Distribution date: ${SimpleDateFormat(DATE_PATTERN, Locale.US).format(it)}" },
            receipt.spbhId?.let { "SPBH ID: $it" },
            receipt.beneficiaryName?.let { "Beneficiary: $it" },
            receipt.printedOn?.let { "Receipt printed on: ${SimpleDateFormat(DATE_TIME_PATTERN, Locale.US).format(it)}" },
        )
        return header + "" + receipt.items.map { item -> "${item.label}: ${item.quantity}" }
    }

    private companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
    }
}
