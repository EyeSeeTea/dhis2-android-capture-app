package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity

/**
 * Decides which screens may show a notification dialog: the Home while it shows the program
 * list, and the list a program opens into — events, tracked entities or data sets. Nothing else:
 * not the splash, the login screen (which also serves the PIN unlock) or the sync progress
 * screen, not the Home's other sections (settings, about, troubleshooting), and not the forms or
 * dashboards reached from a list, where a dialog would interrupt data entry.
 *
 * The first version excluded screens instead of listing the allowed ones. That let the dialog
 * appear on the sync progress screen after login, and in settings when a sync finished there,
 * found on the device on 2026-09-25. An explicit list also keeps screens Oslo adds later out
 * until someone decides they belong.
 *
 * A session is required too: accepting without one cannot reach the server, so the read would
 * never be recorded.
 *
 * Kept out of the base activity so it can be unit tested; the base activity has no test harness
 * here.
 */
object NotificationScreens {
    private val listScreens: Set<Class<*>> =
        setOf(
            ProgramEventDetailActivity::class.java,
            SearchTEActivity::class.java,
            DataSetDetailActivity::class.java,
        )

    /**
     * [onProgramList] only matters for the Home, which hosts all its sections in one activity:
     * it is true while the Home shows the program list.
     */
    fun canShowNotifications(
        hasUserSession: Boolean,
        screen: Class<*>,
        onProgramList: Boolean,
    ): Boolean =
        hasUserSession &&
            when {
                MainActivity::class.java.isAssignableFrom(screen) -> onProgramList
                else -> listScreens.any { it.isAssignableFrom(screen) }
            }
}
