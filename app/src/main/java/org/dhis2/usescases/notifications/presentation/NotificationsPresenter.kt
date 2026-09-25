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
import java.util.concurrent.ConcurrentHashMap

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
                // Minus what the user accepted in this session: the save re-filters the cache
                // only once the server has answered, and a screen resumed in between — going
                // back straight after accepting — read the stale cache and offered it again.
                val pending = notifications.filterNot {
                    it.id in ShowNotifications.acceptedInThisSession
                }
                if (pending.isNotEmpty()) {
                    notificationsView.renderNotifications(pending)
                }
            }
        }
    }

    fun markShowNotificationsAsPending() {
        // A download has just replaced the cache with what the server holds, so an accept whose
        // save failed is unread there again: it may be offered again, and the read retried.
        ShowNotifications.acceptedInThisSession.clear()
        ShowNotifications.isPending = true
        // Push to whatever screen is up. Without this the dialog would only appear once the user
        // navigated away and back: since 3.4.x the download runs from a PostMetadataSyncAction,
        // with no screen in front of it waiting to resume.
        ShowNotifications.onPending?.invoke()
    }

    fun markNotificationAsRead(notification: Notification) {
        // Recorded before the save starts, and kept for the whole session whether the save
        // succeeds or not. If it fails, the notification is still unread on the server and still
        // cached, and it is offered again after the next download or in a new process — once per
        // start, as WIDP 3.3.1 did — instead of on every screen while the server keeps failing.
        ShowNotifications.acceptedInThisSession.add(notification.id)
        CoroutineScope(ioDispatcher).launch {
            // Offline, the repository cannot reach the datastore: it logs, returns an empty list,
            // and the use case reports success without having persisted anything. Guarded so a
            // failure here can never take the app down; the local store still holds the
            // notification, so it is not lost.
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

    /**
     * Notifications the user accepted since the process started or the last download landed.
     * They are not offered again in that time, whether their read was saved or not.
     *
     * Concurrent because it is written from the main thread when the user accepts and cleared
     * from the metadata sync worker when a download lands.
     */
    val acceptedInThisSession: MutableSet<String> = ConcurrentHashMap.newKeySet()
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
