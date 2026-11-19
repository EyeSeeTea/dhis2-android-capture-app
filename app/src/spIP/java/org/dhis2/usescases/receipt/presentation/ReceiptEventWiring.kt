package org.dhis2.usescases.receipt.presentation

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureMenuItem
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureMenuItem.PRINT_RECEIPT
import org.dhis2.usescases.receipt.ReceiptServiceLocator
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

fun MutableList<MenuItemData<EventCaptureMenuItem>>.addPrintReceiptMenuItem(label: String, programStageUid: String) {
    if (!isPrintReceiptAvailable(programStageUid)) return
    add(
        MenuItemData(
            id = PRINT_RECEIPT,
            label = label,
            leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Print),
        ),
    )
}

// The view model lives in the activity scope, so a rotation neither cancels nor repeats a print.
fun mountReceiptPrintHost(activity: ComponentActivity) {
    activity.addContentView(
        ComposeView(activity).apply {
            setContent {
                DHIS2Theme { ReceiptPrintHost(receiptPrintViewModel(activity), rememberBluetoothPermissionRequester()) }
            }
        },
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
    )
}

fun printEventReceipt(activity: ComponentActivity, eventUid: String) {
    receiptPrintViewModel(activity).print(eventUid)
}

private fun receiptPrintViewModel(activity: ComponentActivity): ReceiptPrintViewModel =
    ViewModelProvider(activity, ReceiptServiceLocator.provideReceiptPrintViewModelFactory())[ReceiptPrintViewModel::class.java]
