package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.splash.SplashActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the defect found on the device on 2026-09-23: after an app restart the dialog appeared
 * on the splash and on the login screen, before the user had reached the app, and accepting it
 * there without a session recorded nothing on the server.
 */
class NotificationScreensTest {
    @Test
    fun `a screen of the authenticated area shows notifications when there is a session`() {
        assertTrue(NotificationScreens.canShowNotifications(SESSION, AuthenticatedScreen::class.java))
    }

    @Test
    fun `no screen shows notifications without a session`() {
        assertFalse(NotificationScreens.canShowNotifications(NO_SESSION, AuthenticatedScreen::class.java))
    }

    @Test
    fun `the splash never shows notifications, even with a session`() {
        assertFalse(NotificationScreens.canShowNotifications(SESSION, SplashActivity::class.java))
    }

    @Test
    fun `the login screen never shows notifications, even with a session to unlock`() {
        assertFalse(NotificationScreens.canShowNotifications(SESSION, LoginActivity::class.java))
    }

    private class AuthenticatedScreen

    private companion object {
        const val SESSION = true
        const val NO_SESSION = false
    }
}
