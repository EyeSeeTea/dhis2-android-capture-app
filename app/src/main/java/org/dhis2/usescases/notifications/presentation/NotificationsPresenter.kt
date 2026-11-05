package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification
import timber.log.Timber

class NotificationsPresenter(
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    // What is left to show of the cached unread notifications, one dialog at a time.
    private val queue = ArrayDeque<Notification>()

    /**
     * Shows the cached unread notifications if something marked them pending: entering the app
     * and a metadata sync that has just finished. The mark is consumed before rendering, as in
     * WIDP 3.3.1, so resuming a screen again does not show the same notification twice. A
     * notification that was dismissed stays cached and unread; it is not offered again until the
     * next trigger, which is the next app start or the next sync.
     */
    fun refresh(notificationsView: NotificationsView) {
        if (!ShowNotifications.isPending) return
        ShowNotifications.isPending = false

        CoroutineScope(uiDispatcher).launch {
            getNotifications().collect { notifications ->
                queue.clear()
                queue.addAll(notifications)
                showNext(notificationsView)
            }
        }
    }

    fun markShowNotificationsAsPending() {
        ShowNotifications.isPending = true
    }

    /** The user closed the dialog without accepting: it stays cached and unread. */
    fun onNotificationDismissed(notificationsView: NotificationsView) {
        showNext(notificationsView)
    }

    /**
     * Accepting updates the server and the cached list as one operation, and only then is the next
     * notification shown. If it fails the cache is left as it was, so the notification stays
     * unread and can be retried; the user is not asked again during this entry.
     */
    fun markNotificationAsRead(
        notification: Notification,
        notificationsView: NotificationsView,
    ) {
        CoroutineScope(ioDispatcher).launch {
            // Offline, the repository cannot reach the datastore: it logs, returns an empty list,
            // and the use case reports success without having persisted anything. Guarded so a
            // failure here can never take the app down.
            runCatching { markNotificationAsRead.invoke(notification.id).collect {} }
                .onFailure { Timber.e(it, "Could not mark the notification as read") }

            withContext(uiDispatcher) { showNext(notificationsView) }
        }
    }

    private fun showNext(notificationsView: NotificationsView) {
        queue.removeFirstOrNull()?.let { notificationsView.renderNotification(it) }
    }
}

object ShowNotifications {
    /**
     * Set when the cached notifications have to be offered: the app was entered, or a metadata
     * sync has just downloaded them. Consumed by the first screen that shows them.
     *
     * Volatile because it is set from the metadata sync worker and read from the main thread.
     */
    @Volatile
    var isPending = false
}

interface NotificationsView {
    fun renderNotification(notification: Notification)
}
