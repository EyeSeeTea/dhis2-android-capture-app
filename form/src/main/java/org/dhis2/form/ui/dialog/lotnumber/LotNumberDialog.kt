package org.dhis2.form.ui.dialog.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.dhis2.form.di.lotnumber.LotNumberInjector
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@Composable
fun LotNumberDialog(
    eventUid: String,
    onCancelClick: () -> Unit,
    onLotNumberSelected: (lotNumber: String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: LotNumberDialogViewModel =
        viewModel(
            key = eventUid,
            factory = LotNumberInjector.provideLotNumberDialogViewModelFactory(context, eventUid),
        )
    val result by viewModel.result.collectAsStateWithLifecycle()

    // BottomSheetShell brings its own ModalBottomSheet, so no Dialog wrapper is needed.
    DHIS2Theme {
        LotNumberDialogScreen(
            result = result,
            onCancelClick = onCancelClick,
            onLotNumberClick = onLotNumberSelected,
        )
    }
}
