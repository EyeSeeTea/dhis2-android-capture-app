package org.dhis2.usescases.receipt.presentation

sealed interface ReceiptPrintUiState {
    data object Idle : ReceiptPrintUiState

    data object Printing : ReceiptPrintUiState

    // The print waits for the user to answer the permission request; the event is kept to print it afterwards.
    data class NeedsPermission(val eventUid: String) : ReceiptPrintUiState

    data class Finished(val message: ReceiptPrintMessage) : ReceiptPrintUiState
}
