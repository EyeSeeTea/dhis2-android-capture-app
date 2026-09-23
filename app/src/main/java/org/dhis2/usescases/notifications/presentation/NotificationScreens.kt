package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.splash.SplashActivity

/**
 * Decides which screens may show a notification dialog: only those of the authenticated area.
 *
 * Every screen extends the base activity that renders notifications, including the ones in
 * front of that area. The splash only routes the user onwards, and the login screen also
 * serves the PIN unlock: a dialog there appeared before the user had reached the app, and
 * accepting it without a session could not reach the server, so the read was never recorded.
 * Screens reached from the login screen while logged out, such as the QR scanner, are excluded
 * by requiring a session.
 *
 * Kept out of the base activity so it can be unit tested; the base activity has no test harness
 * here.
 */
object NotificationScreens {
    private val entryScreens: Set<Class<*>> =
        setOf(SplashActivity::class.java, LoginActivity::class.java)

    fun canShowNotifications(hasUserSession: Boolean, screen: Class<*>): Boolean =
        hasUserSession && entryScreens.none { it.isAssignableFrom(screen) }
}
