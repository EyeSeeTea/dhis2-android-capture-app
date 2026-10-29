package org.dhis2.usescases.receipt.presentation

import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.receipt.ReceiptServiceLocator
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem.PRINT_RECEIPT
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

fun MutableList<MenuItemData<EnrollmentMenuItem>>.addPrintReceiptMenuItem(resourceManager: ResourceManager) {
    add(
        MenuItemData(
            id = PRINT_RECEIPT,
            label = resourceManager.getString(R.string.receipt_menu_print),
            leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Print),
        ),
    )
}

// The view model lives in the activity scope, so a rotation neither cancels nor repeats a print.
fun mountReceiptPrintHost(activity: TeiDashboardMobileActivity) {
    activity.addContentView(
        ComposeView(activity).apply {
            setContent { DHIS2Theme { ReceiptPrintHost(receiptPrintViewModel(activity)) } }
        },
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
    )
}

fun printEnrollmentReceipt(activity: TeiDashboardMobileActivity, enrollmentUid: String) {
    receiptPrintViewModel(activity).print(enrollmentUid)
}

private fun receiptPrintViewModel(activity: TeiDashboardMobileActivity): ReceiptPrintViewModel =
    ViewModelProvider(activity, ReceiptServiceLocator.provideReceiptPrintViewModelFactory())[ReceiptPrintViewModel::class.java]
