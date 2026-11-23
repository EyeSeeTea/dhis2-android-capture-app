package org.dhis2.usescases.receipt.data

// The adapter only checks; asking the user is a UI concern (see ReceiptPrintHost).
interface BluetoothPermission {
    fun isGranted(): Boolean
}
