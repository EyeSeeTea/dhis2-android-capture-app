package org.dhis2.usescases.notifications.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class GetNotificationsTest {

    private val notificationRepository: NotificationRepository = mock()
    private lateinit var getNotifications: GetNotifications

    @Before
    fun setUp() {
        getNotifications = GetNotifications(notificationRepository)
    }

    @Test
    fun `should return notifications from repository`() = runTest {
        val notifications = listOf(
            createNotification(id = "notif-1", content = "First notification"),
            createNotification(id = "notif-2", content = "Second notification"),
        )
        whenever(notificationRepository.get()) doReturn flowOf(notifications)

        getNotifications().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("notif-1", result[0].id)
            assertEquals("First notification", result[0].content)
            assertEquals("notif-2", result[1].id)
            assertEquals("Second notification", result[1].content)
            awaitComplete()
        }
    }

    @Test
    fun `should return empty list when no notifications exist`() = runTest {
        whenever(notificationRepository.get()) doReturn flowOf(emptyList())

        getNotifications().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `should delegate to repository get method`() = runTest {
        val notifications = listOf(
            createNotification(id = "notif-1", content = "Markdown **bold** content"),
        )
        whenever(notificationRepository.get()) doReturn flowOf(notifications)

        getNotifications().test {
            val result = awaitItem()
            assertEquals("Markdown **bold** content", result[0].content)
            awaitComplete()
        }
    }

    @Test
    fun `should return notifications with translations`() = runTest {
        val translations = mapOf("es" to "Notificacion traducida")
        val notifications = listOf(
            createNotification(
                id = "notif-1",
                content = "Translated notification",
                translations = translations,
            ),
        )
        whenever(notificationRepository.get()) doReturn flowOf(notifications)

        getNotifications().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Notificacion traducida", result[0].translations?.get("es"))
            awaitComplete()
        }
    }

    private fun createNotification(
        id: String = "notif-id",
        content: String = "content",
        translations: Map<String, String>? = null,
    ) = Notification(
        id = id,
        content = content,
        createdAt = Date(),
        readBy = emptyList(),
        recipients = Recipients(
            userGroups = emptyList(),
            users = emptyList(),
            wildcard = "",
        ),
        permissions = null,
        translations = translations,
    )
}
