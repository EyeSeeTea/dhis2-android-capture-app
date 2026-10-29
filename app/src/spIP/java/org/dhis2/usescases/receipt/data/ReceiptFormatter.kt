package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt

class ReceiptFormatter {
    fun format(receipt: Receipt): List<String> =
        receipt.lines.map { line -> "${line.label}: ${line.value}" }
}
