package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.splash.SplashActivity
import org.dhis2.usescases.sync.SyncActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers where the notification dialog may appear. Found on the device: on 2026-09-23 it
 * appeared on the splash and the login screen, where accepting without a session recorded
 * nothing; on 2026-09-25 on the sync progress screen after login, and in settings when a sync
 * finished there.
 */
class NotificationScreensTest {
    @Test
    fun `the Home shows notifications while it shows the program list`() {
        assertTrue(canShow(MainActivity::class.java, onProgramList = true))
    }

    @Test
    fun `the Home does not show notifications in its other sections`() {
        // Settings, about and troubleshooting are sections of the same activity.
        assertFalse(canShow(MainActivity::class.java, onProgramList = false))
    }

    @Test
    fun `the event list shows notifications`() {
        assertTrue(canShow(ProgramEventDetailActivity::class.java))
    }

    @Test
    fun `the tracked entity list shows notifications`() {
        assertTrue(canShow(SearchTEActivity::class.java))
    }

    @Test
    fun `the data set list shows notifications`() {
        assertTrue(canShow(DataSetDetailActivity::class.java))
    }

    @Test
    fun `the sync progress screen does not show notifications`() {
        assertFalse(canShow(SyncActivity::class.java))
    }

    @Test
    fun `a data entry form does not show notifications`() {
        assertFalse(canShow(EventCaptureActivity::class.java))
    }

    @Test
    fun `the splash does not show notifications`() {
        assertFalse(canShow(SplashActivity::class.java))
    }

    @Test
    fun `the login screen does not show notifications, even with a session to unlock`() {
        assertFalse(canShow(LoginActivity::class.java))
    }

    @Test
    fun `no screen shows notifications without a session`() {
        assertFalse(
            NotificationScreens.canShowNotifications(
                hasUserSession = false,
                screen = ProgramEventDetailActivity::class.java,
                onProgramList = true,
            ),
        )
    }

    private fun canShow(screen: Class<*>, onProgramList: Boolean = true) =
        NotificationScreens.canShowNotifications(
            hasUserSession = true,
            screen = screen,
            onProgramList = onProgramList,
        )
}
