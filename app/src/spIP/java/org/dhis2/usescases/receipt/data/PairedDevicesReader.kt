package org.dhis2.usescases.receipt.data

// Reads the Bluetooth devices already paired with this tablet. Reading them needs the Bluetooth permission.
fun interface PairedDevicesReader {
    fun read(): List<PairedDevice>
}

data class PairedDevice(val name: String, val address: String)
