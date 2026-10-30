package org.dhis2.usescases.receipt.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase
import org.eyeseetea.functional.Either

class ReceiptPrintViewModel(
    private val printReceiptUseCase: PrintReceiptUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptPrintUiState>(ReceiptPrintUiState.Idle)
    val uiState: StateFlow<ReceiptPrintUiState> = _uiState.asStateFlow()

    fun print(enrollmentUid: String) {
        if (_uiState.value == ReceiptPrintUiState.Printing) return
        _uiState.value = ReceiptPrintUiState.Printing
        launchUseCase {
            _uiState.value = printReceiptUseCase(enrollmentUid).toUiState()
        }
    }

    fun messageShown() {
        _uiState.update { if (it is ReceiptPrintUiState.Finished) ReceiptPrintUiState.Idle else it }
    }

    private fun Either<PrintReceiptError, Unit>.toUiState(): ReceiptPrintUiState =
        fold(
            fnL = { error ->
                when (error) {
                    PrintReceiptError.NoPrinterConnected ->
                        ReceiptPrintUiState.Finished(ReceiptPrintMessage.NoPrinterConnected)
                    PrintReceiptError.PrintFailed ->
                        ReceiptPrintUiState.Finished(ReceiptPrintMessage.PrintFailed)
                }
            },
            fnR = { ReceiptPrintUiState.Finished(ReceiptPrintMessage.Printed) },
        )
}
