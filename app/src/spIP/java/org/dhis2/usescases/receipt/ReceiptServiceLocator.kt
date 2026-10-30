package org.dhis2.usescases.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.receipt.data.D2ReceiptRepository
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase
import org.dhis2.usescases.receipt.presentation.ReceiptPrintViewModel
import org.hisp.dhis.android.core.D2Manager

// Hand-written locator, only called from the dashboard wiring (see design.md, Dependency creation).
object ReceiptServiceLocator {

    fun provideReceiptPrintViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReceiptPrintViewModel(providePrintReceiptUseCase()) as T
        }

    private fun providePrintReceiptUseCase(): PrintReceiptUseCase =
        PrintReceiptUseCase(D2ReceiptRepository(D2Manager.getD2()), provideReceiptPrinter())
}
