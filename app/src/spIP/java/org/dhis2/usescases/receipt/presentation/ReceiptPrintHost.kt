package org.dhis2.usescases.receipt.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

@Composable
fun ReceiptPrintHost(viewModel: ReceiptPrintViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    when (val state = uiState) {
        ReceiptPrintUiState.Idle -> Unit
        ReceiptPrintUiState.Printing -> PrintingDialog()
        is ReceiptPrintUiState.Finished ->
            if (state.message == ReceiptPrintMessage.NoPrinterConnected) {
                ErrorDialog(
                    text = stringResource(R.string.receipt_no_printer_connected),
                    onAccept = viewModel::messageShown,
                )
            }
    }

    val printedText = stringResource(R.string.receipt_printed)
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ReceiptPrintUiState.Finished && state.message == ReceiptPrintMessage.Printed) {
            snackbarHostState.showSnackbar(printedText)
            viewModel.messageShown()
        }
    }

    Box(modifier = Modifier.padding(16.dp)) {
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun PrintingDialog() {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        confirmButton = {},
        text = {
            Column {
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                Text(stringResource(R.string.receipt_printing))
            }
        },
    )
}

@Composable
private fun ErrorDialog(text: String, onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        confirmButton = {
            TextButton(onClick = onAccept) { Text(stringResource(R.string.receipt_error_accept)) }
        },
        text = { Text(text) },
    )
}
