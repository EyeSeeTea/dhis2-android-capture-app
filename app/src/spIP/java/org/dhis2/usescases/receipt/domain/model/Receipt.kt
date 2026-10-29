package org.dhis2.usescases.receipt.domain.model

// Draft: the lines are expected to change once the client confirms the receipt layout.
data class Receipt(val lines: List<ReceiptLine>)
