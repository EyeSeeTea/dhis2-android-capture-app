package org.dhis2.usescases.receipt.presentation

// Asks the user for the permission the printer needs and reports whether it was granted.
fun interface PermissionRequester {
    fun request(onResult: (granted: Boolean) -> Unit)
}
