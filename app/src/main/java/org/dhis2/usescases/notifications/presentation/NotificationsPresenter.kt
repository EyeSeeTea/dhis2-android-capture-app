package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification

class NotificationsPresenter(
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    fun refresh(notificationsView: NotificationsView) {
        if (!ShowNotifications.isPending) return

        CoroutineScope(uiDispatcher).launch {
            getNotifications().collect { notifications ->
                if (notifications.isNotEmpty()) {
                    // Consume the flag only once something actually reaches the screen. Consuming
                    // it up front loses the notification whenever the list comes back empty, which
                    // is the normal case while the download is still in flight.
                    ShowNotifications.isPending = false
                    notificationsView.renderNotifications(notifications)
                }
            }
        }
    }

    fun markShowNotificationsAsPending() {
        ShowNotifications.isPending = true
        // Push to whatever screen is up. Without this the dialog would only appear once the user
        // navigated away and back: since 3.4.x the download runs from a PostMetadataSyncAction,
        // with no screen in front of it waiting to resume.
        ShowNotifications.onPending?.run()
    }

    fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(ioDispatcher).launch {
            markNotificationAsRead.invoke(notification.id).collect {}
        }
    }
}

object ShowNotifications {
    var isPending = false

    /**
     * Set by the screen that is currently resumed and cleared when it pauses, so that a
     * notification arriving from a background sync is shown immediately instead of waiting for
     * the next resume. Null when no screen is up, which is the case during a background sync.
     */
    var onPending: Runnable? = null
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
