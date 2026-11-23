package org.dhis2.usescases.receipt.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Thin Android glue, verified by hand: it launches the system dialog for BLUETOOTH_CONNECT (Android 12+).
@Composable
fun rememberBluetoothPermissionRequester(): PermissionRequester {
    var pendingResult by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingResult?.invoke(granted)
        pendingResult = null
    }
    return remember(launcher) {
        PermissionRequester { onResult ->
            pendingResult = onResult
            launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }
}
