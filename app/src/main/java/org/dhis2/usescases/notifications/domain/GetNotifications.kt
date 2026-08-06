package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.flow.Flow

class GetNotifications(private val notificationRepository: NotificationRepository) {
    operator fun invoke(): Flow<List<Notification>> {
        return notificationRepository.get()
    }
}
