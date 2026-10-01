package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

/**
 * Accepting twice must not record the user twice. A second accept — a dialog left behind a screen
 * that was opened on top, for instance — finds the user already in the list the server returns.
 */
class MarkNotificationAsReadTest {
    private val notificationRepository: NotificationRepository = mock()
    private val userRepository: UserRepository = mock()
    private val markAsRead = MarkNotificationAsRead(notificationRepository, userRepository)

    @Test
    fun `accepting records the user once`() = runBlocking {
        givenTheServerHolds(givenANotification(readBy = emptyList()))

        markAsRead(NOTIFICATION_ID).first()

        assertEquals(listOf(USER_ID), savedNotification().readBy.map { it.id })
    }

    @Test
    fun `accepting again does not record the user twice`() = runBlocking {
        givenTheServerHolds(
            givenANotification(readBy = listOf(ReadBy(Date(0), USER_ID, "User One"))),
        )

        markAsRead(NOTIFICATION_ID).first()

        assertEquals(listOf(USER_ID), savedNotification().readBy.map { it.id })
    }

    @Test
    fun `accepting keeps what other users already recorded`() = runBlocking {
        givenTheServerHolds(
            givenANotification(readBy = listOf(ReadBy(Date(0), "other", "Other User"))),
        )

        markAsRead(NOTIFICATION_ID).first()

        assertEquals(listOf("other", USER_ID), savedNotification().readBy.map { it.id })
    }

    private fun savedNotification(): Notification {
        val captor = argumentCaptor<Notification>()
        verify(notificationRepository).save(captor.capture())
        return captor.firstValue
    }

    private fun givenTheServerHolds(notification: Notification) {
        whenever(notificationRepository.getById(NOTIFICATION_ID)) doReturn flowOf(notification)
        whenever(userRepository.getCurrentUser()) doReturn User(USER_ID, "User One")
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)
    }

    private fun givenANotification(readBy: List<ReadBy>) =
        Notification(
            content = "content",
            createdAt = Date(0),
            id = NOTIFICATION_ID,
            readBy = readBy,
            recipients = Recipients(emptyList(), emptyList(), "ALL"),
            permissions = Permissions("", emptyList(), emptyList()),
            translations = emptyMap(),
        )

    private companion object {
        const val NOTIFICATION_ID = "oW3ZfHzUsnI"
        const val USER_ID = "user1"
    }
}
