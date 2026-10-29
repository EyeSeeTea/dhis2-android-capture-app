package org.dhis2.usescases.receipt.presentation

data class ReceiptPrintUiState(
    val isPrinting: Boolean = false,
    val userMessage: ReceiptPrintMessage? = null,
)
