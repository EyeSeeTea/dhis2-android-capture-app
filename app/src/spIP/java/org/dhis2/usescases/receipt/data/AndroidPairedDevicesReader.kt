package org.dhis2.usescases.receipt.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context

// Thin Android glue, verified by hand. From Android 12 on, reading the paired devices needs the
// BLUETOOTH_CONNECT permission, which BluetoothReceiptPrinter checks before it calls this.
class AndroidPairedDevicesReader(
    private val context: Context,
) : PairedDevicesReader {
    @SuppressLint("MissingPermission")
    override fun read(): List<PairedDevice> {
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter ?: return emptyList()
        return adapter.bondedDevices.orEmpty().mapNotNull { device ->
            device.name?.let { name -> PairedDevice(name = name, address = device.address) }
        }
    }
}
