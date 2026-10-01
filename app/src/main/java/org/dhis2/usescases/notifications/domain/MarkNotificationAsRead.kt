package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Date

class MarkNotificationAsRead(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(notificationId: String): Flow<Unit> {
        return notificationRepository.getById(notificationId).flatMapConcat { notification ->
            Timber.d("notification: $notification")

            notification?.let { notification ->
                val user = userRepository.getCurrentUser()

                // Accepting twice must not append the user twice: a second accept finds the user
                // already in the list the server returns, and only refreshes the cached copy.
                val readByUsers =
                    if (notification.readBy.any { it.id == user.uid }) {
                        notification.readBy
                    } else {
                        notification.readBy + ReadBy(Date(), user.uid, user.displayName)
                    }

                val notificationUpdated = notification.copy(readBy = readByUsers)

                notificationRepository.save(notificationUpdated)
            } ?: flow { emit(Unit) }
        }
    }
}
