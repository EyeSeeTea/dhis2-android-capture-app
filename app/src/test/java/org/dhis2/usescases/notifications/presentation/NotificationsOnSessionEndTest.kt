package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Covers the defect found on the device on 2026-09-25: the next user on the device was shown the
 * previous user's cached notifications.
 */
class NotificationsOnSessionEndTest {
    private val repository: NotificationRepository = mock()

    @After
    fun tearDown() {
        ShowNotifications.isPending = false
        ShowNotifications.acceptedInThisSession.clear()
    }

    @Test
    fun `the leaving user's cached notifications are forgotten`() {
        NotificationsOnSessionEnd.forget(repository)

        verify(repository).clear()
    }

    @Test
    fun `what this process remembers about the leaving user is forgotten`() {
        // An id accepted by the leaving user would otherwise stay hidden from the next one.
        givenTheLeavingUserAcceptedOneAndHasOnePending()

        NotificationsOnSessionEnd.forget(repository)

        assertFalse(ShowNotifications.isPending)
        assertTrue(ShowNotifications.acceptedInThisSession.isEmpty())
    }

    private fun givenTheLeavingUserAcceptedOneAndHasOnePending() {
        ShowNotifications.isPending = true
        ShowNotifications.acceptedInThisSession.add("oW3ZfHz3snI")
    }
}
