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
        ShowNotifications.markPending()
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
     * Set by the activity that is currently visible, cleared when it pauses, so at most one
     * listener is ever registered and it is always tied to a live screen.
     *
     * The download runs after a metadata sync, off any Activity. Without this, a notification
     * that landed while the user was standing still on a screen did not appear until the next
     * screen transition produced an `onResume()`.
     */
    @JvmField
    var onPending: Runnable? = null

    fun markPending() {
        isPending = true
        onPending?.run()
    }
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
