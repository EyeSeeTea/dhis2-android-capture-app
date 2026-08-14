package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.flow.Flow

/**
 * Downloads the notification list from the DHIS2 datastore and stores it locally.
 *
 * Until upstream 3.4.0 this ran inside `SyncPresenterImpl.syncMetadata()`. That method moved to
 * the `:sync` module, which cannot depend on `:app` where this capability lives, so the trigger
 * was re-anchored to the end of a sync observed from the main screen. See
 * `eyeseetea-docs/upgrade/widp/upgrade-3.4.1-notes.md`.
 */
class SyncNotifications(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<Unit> = notificationRepository.sync()
}
