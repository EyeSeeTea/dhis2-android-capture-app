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
import org.dhis2.usescases.notifications.domain.SyncNotifications

class NotificationsPresenter(
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead,
    private val syncNotifications: SyncNotifications,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    /**
     * Downloads notifications from the server. Called when a sync finishes; before 3.4.0 this
     * ran inside the metadata sync worker.
     *
     * The download is asynchronous, so the caller cannot know when the result is available.
     * Until 3.4.1 nothing re-checked once it landed, and the notification only surfaced if the
     * user happened to navigate through Home afterwards. Marking pending and refreshing here
     * closes that race: passing the view is what makes the dialog appear without navigating.
     */
    fun syncNotifications(notificationsView: NotificationsView? = null) {
        CoroutineScope(ioDispatcher).launch {
            syncNotifications.invoke().collect {}

            markShowNotificationsAsPending()

            notificationsView?.let { view ->
                withContext(uiDispatcher) { refresh(view) }
            }
        }
    }

    /**
     * The pending flag is only consumed once something is actually rendered. Clearing it
     * unconditionally lost notifications whenever an activity resumed while the download was
     * still in flight — the common case for single-program users, whose program screen resumes
     * milliseconds after the flag is set.
     */
    fun refresh(notificationsView: NotificationsView) {
        if (!ShowNotifications.isPending) return

        CoroutineScope(uiDispatcher).launch {
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
        CoroutineScope(ioDispatcher).launch {
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
