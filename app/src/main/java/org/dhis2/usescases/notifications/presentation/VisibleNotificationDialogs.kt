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
    private val visible = mutableMapOf<String, () -> Unit>()

    /**
     * True while a dialog for [notificationId] is already on screen, in which case the caller
     * must not build another one.
     */
    fun isVisible(notificationId: String): Boolean = visible.containsKey(notificationId)

    /** [dismiss] closes that dialog; [dismissAll] uses it when the screen goes to the background. */
    fun onShown(notificationId: String, dismiss: () -> Unit) {
        visible[notificationId] = dismiss
    }

    /**
     * Closes every dialog this screen shows. Called when the screen goes to the background, so
     * only the screen in front ever holds a notification dialog: screens opened one after
     * another — the Home jumping into a single program, for instance — each showed their own,
     * and accepting the top one left the others live underneath, each recording another read.
     * The notification is still unread, so the screen shows it again when it returns.
     */
    fun dismissAll() {
        // Copied first: each dismiss calls back into onDismissed through the dialog's listener.
        visible.values.toList().forEach { it() }
        visible.clear()
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
