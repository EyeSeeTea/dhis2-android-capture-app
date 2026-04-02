package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun sync(): Flow<Unit>
    fun get(): Flow<List<Notification>>
    fun getById(id: String): Flow<Notification?>
    fun save(notification: Notification): Flow<Unit>
}