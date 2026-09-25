package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.notifications.domain.NotificationRepository

/**
 * Forgets the notifications of the user whose session is ending: the cached list, and what this
 * process remembers about them.
 *
 * The cache is a single entry for the whole app and nothing cleared it, so the next user on the
 * device was shown the previous user's notifications, or their own from another server, and
 * accepting one recorded nothing because it does not exist on that server. Found on the device
 * on 2026-09-25. Called when a logout or an account deletion completes, and when the server
 * session ends; the next user's own notifications arrive with their sync.
 */
object NotificationsOnSessionEnd {
    fun forget(repository: NotificationRepository) {
        repository.clear()
        ShowNotifications.isPending = false
        ShowNotifications.acceptedInThisSession.clear()
    }
}
