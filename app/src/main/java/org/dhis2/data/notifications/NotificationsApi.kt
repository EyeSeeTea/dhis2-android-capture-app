package org.dhis2.data.notifications
// EyeSeeTea customization - Notifications system
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

class NotificationsApi(
    private val client: HttpServiceClient,
) {
    suspend fun getData(): List<NotificationDTO> =
        client.get {
            url("dataStore/notifications/notifications")
        }

    suspend fun postData(notifications: List<NotificationDTO>): Unit =
        client.put {
            url("dataStore/notifications/notifications")
            body(notifications)
        }
}

class UserGroupsApi(
    private val client: HttpServiceClient,
) {
    suspend fun getData(userId: String): UserGroupsDTO =
        client.get {
            url("users/$userId?fields=userGroups")
        }
}
