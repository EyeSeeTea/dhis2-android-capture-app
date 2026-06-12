package org.dhis2.usescases.notifications.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class MarkNotificationAsReadTest {

    private val notificationRepository: NotificationRepository = mock()
    private val userRepository: UserRepository = mock()
    private lateinit var markNotificationAsRead: MarkNotificationAsRead

    @Before
    fun setUp() {
        markNotificationAsRead = MarkNotificationAsRead(notificationRepository, userRepository)
    }

    @Test
    fun `should add current user to readBy list when marking notification as read`() = runTest {
        val notificationId = "notif-1"
        val currentUser = User(uid = "user-123", displayName = "Test User")
        val notification = createNotification(id = notificationId, readBy = emptyList())

        whenever(userRepository.getCurrentUser()) doReturn currentUser
        whenever(notificationRepository.getById(notificationId)) doReturn flowOf(notification)
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)

        markNotificationAsRead(notificationId).test {
            awaitItem()
            awaitComplete()
        }

        val captor = argumentCaptor<Notification>()
        verify(notificationRepository).save(captor.capture())

        val savedNotification = captor.firstValue
        assertEquals(1, savedNotification.readBy.size)
        assertEquals("user-123", savedNotification.readBy[0].id)
        assertEquals("Test User", savedNotification.readBy[0].name)
    }

    @Test
    fun `should preserve existing readBy entries when adding new one`() = runTest {
        val notificationId = "notif-1"
        val existingReadBy = ReadBy(date = Date(), id = "other-user", name = "Other User")
        val currentUser = User(uid = "user-123", displayName = "Test User")
        val notification = createNotification(id = notificationId, readBy = listOf(existingReadBy))

        whenever(userRepository.getCurrentUser()) doReturn currentUser
        whenever(notificationRepository.getById(notificationId)) doReturn flowOf(notification)
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)

        markNotificationAsRead(notificationId).test {
            awaitItem()
            awaitComplete()
        }

        val captor = argumentCaptor<Notification>()
        verify(notificationRepository).save(captor.capture())

        val savedNotification = captor.firstValue
        assertEquals(2, savedNotification.readBy.size)
        assertEquals("other-user", savedNotification.readBy[0].id)
        assertEquals("user-123", savedNotification.readBy[1].id)
    }

    @Test
    fun `should emit unit without saving when notification is not found`() = runTest {
        val notificationId = "non-existent"

        whenever(notificationRepository.getById(notificationId)) doReturn flowOf(null)

        markNotificationAsRead(notificationId).test {
            awaitItem()
            awaitComplete()
        }

        verify(notificationRepository, never()).save(any())
    }

    @Test
    fun `should set a date on the readBy entry`() = runTest {
        val notificationId = "notif-1"
        val currentUser = User(uid = "user-123", displayName = "Test User")
        val beforeTest = Date()
        val notification = createNotification(id = notificationId, readBy = emptyList())

        whenever(userRepository.getCurrentUser()) doReturn currentUser
        whenever(notificationRepository.getById(notificationId)) doReturn flowOf(notification)
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)

        markNotificationAsRead(notificationId).test {
            awaitItem()
            awaitComplete()
        }

        val captor = argumentCaptor<Notification>()
        verify(notificationRepository).save(captor.capture())

        val readByDate = captor.firstValue.readBy[0].date
        assertTrue(
            "ReadBy date should be on or after the test start time",
            !readByDate.before(beforeTest),
        )
    }

    private fun createNotification(
        id: String = "notif-id",
        readBy: List<ReadBy> = emptyList(),
    ) = Notification(
        id = id,
        content = "Test content",
        createdAt = Date(),
        readBy = readBy,
        recipients = Recipients(
            userGroups = emptyList(),
            users = emptyList(),
            wildcard = "",
        ),
        permissions = null,
        translations = null,
    )
}
