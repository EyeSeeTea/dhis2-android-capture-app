package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification

class NotificationsPresenter(
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead,
) {
    /**
     * The pending flag is only consumed once something is actually rendered. Clearing it
     * unconditionally lost notifications whenever an activity resumed while the download was
     * still in flight — the common case for single-program users, whose program screen resumes
     * milliseconds after the flag is set.
     */
    fun refresh(notificationsView: NotificationsView) {
        if (!ShowNotifications.isPending) return

        CoroutineScope(Dispatchers.Main).launch {
            getNotifications().collect { notifications ->
                if (notifications.isNotEmpty()) {
                    ShowNotifications.isPending = false
                    notificationsView.renderNotifications(notifications)
                }
            }
        }
    }

    fun markShowNotificationsAsPending() {
        ShowNotifications.isPending = true
    }

    fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            markNotificationAsRead.invoke(notification.id).collect {}
        }
    }
}

object ShowNotifications {
    var isPending = false
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
