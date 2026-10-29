package org.dhis2.usescases.receipt.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase

class ReceiptPrintViewModel(
    private val printReceiptUseCase: PrintReceiptUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptPrintUiState())
    val uiState: StateFlow<ReceiptPrintUiState> = _uiState.asStateFlow()

    fun print(eventUid: String) {
        _uiState.update { it.copy(isPrinting = true) }
        launchUseCase {
            val result = printReceiptUseCase(eventUid)
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    userMessage = if (result.isSuccess) ReceiptPrintMessage.Printed else it.userMessage,
                )
            }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
