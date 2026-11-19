package org.dhis2.usescases.receipt.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// Thin Android glue, verified by hand. Before Android 12 the install-time BLUETOOTH permission is enough,
// and BLUETOOTH_CONNECT does not exist there, so there is nothing to ask for.
class AndroidBluetoothPermission(
    private val context: Context,
) : BluetoothPermission {
    override fun isGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
}
