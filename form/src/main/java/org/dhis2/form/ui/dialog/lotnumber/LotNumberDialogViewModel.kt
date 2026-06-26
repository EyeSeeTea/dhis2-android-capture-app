package org.dhis2.form.ui.dialog.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.form.model.lotnumber.GetLotNumbersUseCase
import org.dhis2.form.model.lotnumber.LotNumbersResult

/**
 * Owns the coroutine that loads lot numbers so the lookup is started in [viewModelScope] and survives
 * configuration changes, instead of being triggered directly from the view. The use case (and the
 * repository behind it) is responsible for its own threading; this class only owns the scope.
 */
class LotNumberDialogViewModel(
    private val eventUid: String,
    private val getLotNumbers: GetLotNumbersUseCase,
) : ViewModel() {
    private val _result = MutableStateFlow<LotNumbersResult?>(null)
    val result: StateFlow<LotNumbersResult?> = _result.asStateFlow()

    init {
        viewModelScope.launch {
            _result.value = getLotNumbers(eventUid)
        }
    }

    class Factory(
        private val eventUid: String,
        private val getLotNumbers: GetLotNumbersUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LotNumberDialogViewModel(eventUid, getLotNumbers) as T
    }
}
