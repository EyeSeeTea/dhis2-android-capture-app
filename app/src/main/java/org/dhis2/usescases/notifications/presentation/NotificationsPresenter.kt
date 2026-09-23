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
        // The cached list is the source of truth, not ShowNotifications.isPending. The flag lives
        // in memory and starts false in every new process, so gating on it hid a notification
        // dismissed before an app restart until the next metadata sync, although it was still
        // unread on the server and in the cache. WIDP 3.3.1 re-read the cache on every cold start
        // through MainPresenter.checkSingleProgramNavigation(); the 3.4.2 Home rewrite dropped
        // that trigger. The cache only ever holds this user's unread notifications, and accepting
        // one removes it, so reading it on every resume shows exactly what is still pending.
        CoroutineScope(uiDispatcher).launch {
            getNotifications().collect { notifications ->
                // Showing a notification is not the user acknowledging it: a dialog dismissed with
                // back or by tapping outside leaves it cached and unread, so the next resume shows
                // it again.
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
            // it and the next resume no longer finds it. The flag is cleared only once nothing
            // unread is left; with several notifications pending, the rest still have to appear.
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
     * Records that a download has landed with notifications not yet accepted: set after a metadata
     * sync, cleared once accepting leaves nothing unread. It no longer decides whether the screen
     * shows anything — refresh() reads the cached list on every resume — because it lives in
     * memory and is false again in every new process.
     *
     * Volatile because it is written from the metadata sync worker and from the IO dispatcher when
     * a notification is accepted. Without it there is no happens-before edge between them.
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
