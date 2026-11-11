package org.dhis2.usescases.teiDashboard

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.usescases.sms.DI.SPIPServiceLocator
import org.dhis2.usescases.sms.domain.model.sms.SmsResult

fun customClick(
    menuId: EnrollmentMenuItem,
    teiDashboardMobileActivity: TeiDashboardMobileActivity,
    programUid: String,
    enrollmentUid: String,
    teiUid: String
) {
    when (menuId) {
        EnrollmentMenuItem.SEND_SMS -> {
            sendSms(
                activity = teiDashboardMobileActivity,
                parentView = teiDashboardMobileActivity.binding.root,
                teiUid = teiUid
            )
        }

        else -> {}
    }
}


fun sendSms(
    activity: TeiDashboardMobileActivity,
    parentView: View,
    teiUid: String
) {
    activity.lifecycleScope.launch(Dispatchers.IO) {
        when (val result = SPIPServiceLocator.provideSendSmsUseCase().invoke(teiUid)) {
            is SmsResult.Success -> {
                withContext(Dispatchers.Main) {
                    showCustomSnackbar(
                        activity,
                        parentView,
                        activity.getString(R.string.sent_sms_successfully),
                        true
                    )

                    activity.refreshEvents()
                }
            }

            is SmsResult.SuccessUsingEn -> {
                withContext(Dispatchers.Main) {
                    showCustomSnackbar(
                        activity,
                        parentView,
                        activity.getString(
                            R.string.sent_sms_using_en_successfully,
                            result.preferredLanguage
                        ),
                        true
                    )
                    activity.refreshEvents()
                }
            }

            is SmsResult.TemplateFailure -> {
                withContext(Dispatchers.Main) {
                    showCustomSnackbar(
                        activity,
                        parentView,
                        activity.getString(R.string.sent_sms_template_error),
                        false
                    )
                }
            }

            is SmsResult.SendFailure -> {
                withContext(Dispatchers.Main) {
                    showCustomSnackbar(
                        activity,
                        parentView,
                        activity.getString(R.string.sent_sms_error),
                        false
                    )
                }
            }
        }
    }
}

fun showCustomSnackbar(
    activity: AppCompatActivity,
    parentView: View,
    message: String,
    isSuccess: Boolean
) {
    val snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)

    val color =
        if (isSuccess) ContextCompat.getColor(activity, R.color.colorPrimaryDark_2e7)
        else ContextCompat.getColor(activity, R.color.colorPrimaryDarkRed)

    snackbar.setBackgroundTint(color)

    snackbar.view.findViewById<TextView>(R.id.snackbar_text)?.apply {
        maxLines = Int.MAX_VALUE
    }

    snackbar.show()
}