package org.dhis2.usescases.receipt.presentation

sealed interface ReceiptPrintUiState {
    data object Idle : ReceiptPrintUiState

    data object Printing : ReceiptPrintUiState

    data class Finished(val message: ReceiptPrintMessage) : ReceiptPrintUiState
}
