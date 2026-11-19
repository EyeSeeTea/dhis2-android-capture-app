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
import timber.log.Timber

class ReceiptPrintViewModel(
    private val printReceiptUseCase: PrintReceiptUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptPrintUiState>(ReceiptPrintUiState.Idle)
    val uiState: StateFlow<ReceiptPrintUiState> = _uiState.asStateFlow()

    fun print(eventUid: String) {
        if (_uiState.value == ReceiptPrintUiState.Printing) return
        _uiState.value = ReceiptPrintUiState.Printing
        launchUseCase {
            _uiState.value = printReceiptUseCase(eventUid).toUiState(eventUid)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        val state = _uiState.value as? ReceiptPrintUiState.NeedsPermission ?: return
        if (granted) {
            print(state.eventUid)
        } else {
            _uiState.value = ReceiptPrintUiState.Finished(ReceiptPrintMessage.PermissionDenied)
        }
    }

    fun messageShown() {
        _uiState.update { if (it is ReceiptPrintUiState.Finished) ReceiptPrintUiState.Idle else it }
    }

    private fun Either<PrintReceiptError, Unit>.toUiState(eventUid: String): ReceiptPrintUiState =
        fold(
            fnL = { error ->
                when (error) {
                    PrintReceiptError.NoPrinterConnected ->
                        ReceiptPrintUiState.Finished(ReceiptPrintMessage.NoPrinterConnected)
                    PrintReceiptError.PermissionRequired -> ReceiptPrintUiState.NeedsPermission(eventUid)
                    is PrintReceiptError.PrintFailed -> {
                        Timber.w(error.cause, "The receipt could not be printed")
                        ReceiptPrintUiState.Finished(ReceiptPrintMessage.PrintFailed)
                    }
                }
            },
            fnR = { ReceiptPrintUiState.Finished(ReceiptPrintMessage.Printed) },
        )
}
