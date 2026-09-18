package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    fun refresh(notificationsView: NotificationsView) {
        if (!ShowNotifications.isPending) return

        CoroutineScope(uiDispatcher).launch {
            getNotifications().collect { notifications ->
                // The flag is not consumed here. Showing a notification is not the same as the
                // user acknowledging it: a dialog dismissed with back or by tapping outside leaves
                // it unread on the server, so it has to come back on the next resume. It is
                // cleared in markNotificationAsRead(), once nothing unread is left.
                // An empty list means the download has not landed yet, and the flag must survive
                // that too.
                if (notifications.isNotEmpty()) {
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
        ShowNotifications.onPending?.invoke()
    }

    fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(ioDispatcher).launch {
            // Offline, the repository cannot reach the datastore: it logs, returns an empty list,
            // and the use case reports success without having persisted anything. Guarded so a
            // failure here can never take the app down, and left pending on purpose — the local
            // store still holds the notification, so it is offered again instead of being lost.
            runCatching { markNotificationAsRead.invoke(notification.id).collect {} }
                .onFailure { Timber.e(it, "Could not mark the notification as read") }

            // Accepting one re-filters the local store, so the accepted notification drops out of
            // it. Only when nothing unread is left does the screen stop being asked to show
            // anything; with several notifications pending, the rest still have to appear.
            getNotifications().collect { remaining ->
                if (remaining.isEmpty()) {
                    ShowNotifications.isPending = false
                }
            }
        }
    }
}

object ShowNotifications {
    /**
     * Volatile because it is written from the metadata sync worker and from the IO dispatcher when
     * a notification is accepted, and read from the main thread on every activity resume. Without
     * it there is no happens-before edge and a cleared flag may not be visible to the screen.
     */
    @Volatile
    var isPending = false

    /**
     * Set by the screen that is currently resumed and cleared when it pauses, so that a
     * notification arriving from a background sync is shown immediately instead of waiting for
     * the next resume. Null when no screen is up, which is the case during a background sync.
     *
     * Volatile for the same reason as [isPending]: written from the main thread, read from the
     * metadata sync worker.
     */
    @Volatile
    var onPending: (() -> Unit)? = null
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
