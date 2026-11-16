package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Plain-text receipt sent to the printer: the header, a blank line and the items given.
// The texts are provisional until the client confirms the layout.
class ReceiptFormatter {
    fun format(receipt: Receipt): List<String> {
        val header = buildList {
            receipt.distributionName?.let { add("Distribution: $it") }
            receipt.distributionPoint?.let { add("Distribution point: $it") }
            receipt.distributionDate?.let { add("Distribution date: ${formatted(it, DATE_PATTERN)}") }
            receipt.spbhId?.let { add("SPBH ID: $it") }
            receipt.beneficiaryName?.let { add("Beneficiary: $it") }
            receipt.geographicalLocations.forEach { add("Geographical Location ${it.level}: ${it.name}") }
            receipt.printedOn?.let { add("Receipt printed on: ${formatted(it, DATE_TIME_PATTERN)}") }
        }
        return header + "" + receipt.items.map { item -> "${item.label}: ${item.quantity}" }
    }

    private fun formatted(date: Date, pattern: String): String = SimpleDateFormat(pattern, Locale.US).format(date)

    private companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
    }
}
