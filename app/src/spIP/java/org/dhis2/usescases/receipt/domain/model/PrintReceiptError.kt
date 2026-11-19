package org.dhis2.usescases.receipt.domain.model

sealed interface PrintReceiptError {
    data object NoPrinterConnected : PrintReceiptError
    data object PermissionRequired : PrintReceiptError
    // Carries the original cause (a class, not a singleton: a singleton's stack trace would be captured at class load).
    data class PrintFailed(val cause: Throwable) : PrintReceiptError
}
