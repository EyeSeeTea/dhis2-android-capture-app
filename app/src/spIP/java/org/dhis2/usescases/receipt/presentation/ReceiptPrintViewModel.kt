package org.dhis2.usescases.receipt.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.usescases.receipt.domain.model.PrinterError
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase

class ReceiptPrintViewModel(
    private val printReceiptUseCase: PrintReceiptUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptPrintUiState>(ReceiptPrintUiState.Idle)
    val uiState: StateFlow<ReceiptPrintUiState> = _uiState.asStateFlow()

    fun print(eventUid: String) {
        if (_uiState.value == ReceiptPrintUiState.Printing) return
        _uiState.value = ReceiptPrintUiState.Printing
        launchUseCase {
            _uiState.value = printReceiptUseCase(eventUid).toUiState()
        }
    }

    fun messageShown() {
        _uiState.update { if (it is ReceiptPrintUiState.Finished) ReceiptPrintUiState.Idle else it }
    }

    private fun Result<Unit>.toUiState(): ReceiptPrintUiState =
        when {
            isSuccess -> ReceiptPrintUiState.Finished(ReceiptPrintMessage.Printed)
            exceptionOrNull() is PrinterError.NoPrinterConnected ->
                ReceiptPrintUiState.Finished(ReceiptPrintMessage.NoPrinterConnected)
            exceptionOrNull() is PrinterError.PrintFailed ->
                ReceiptPrintUiState.Finished(ReceiptPrintMessage.PrintFailed)
            else -> ReceiptPrintUiState.Idle
        }
}
