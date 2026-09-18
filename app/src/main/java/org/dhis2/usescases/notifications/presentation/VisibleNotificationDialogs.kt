package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

/**
 * Tracks which notification dialogs are on screen for one screen of the authenticated area.
 *
 * A notification stays pending until the user accepts it, so every resume asks to render it
 * again. Without this, resuming the same screen with a dialog already up builds a second
 * dialog on top of the first: accepting the top one leaves its copies live underneath, and
 * each copy marks the notification as read again, appending a duplicate `readBy` entry on the
 * server. Observed on a real server on 2026-09-17, two entries for the same user nine seconds
 * apart.
 *
 * Kept as its own class rather than a field in the base activity so it can be unit tested:
 * the base activity itself has no test harness in this project.
 *
 * Not thread safe on purpose — it is only ever touched from the main thread, where dialogs are
 * built and dismissed.
 */
class VisibleNotificationDialogs {
    private val visible = mutableSetOf<String>()

    /**
     * True while a dialog for [notificationId] is already on screen, in which case the caller
     * must not build another one.
     */
    fun isVisible(notificationId: String): Boolean = visible.contains(notificationId)

    fun onShown(notificationId: String) {
        visible.add(notificationId)
    }

    /**
     * Called for every way a dialog can go away — accepted, dismissed with back, or dismissed
     * by tapping outside. Releasing the id here is what keeps a notification that was dismissed
     * without accepting showing up again on the next resume.
     */
    fun onDismissed(notificationId: String) {
        visible.remove(notificationId)
    }
}
