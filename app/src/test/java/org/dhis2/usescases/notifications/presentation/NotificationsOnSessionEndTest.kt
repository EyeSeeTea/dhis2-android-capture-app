package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.junit.After
import org.junit.Assert.assertFalse
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
    }

    @Test
    fun `the leaving user's cached notifications are forgotten`() {
        NotificationsOnSessionEnd.forget(repository)

        verify(repository).clear()
    }

    @Test
    fun `the mark that the leaving user had notifications pending is forgotten`() {
        ShowNotifications.isPending = true

        NotificationsOnSessionEnd.forget(repository)

        assertFalse(ShowNotifications.isPending)
    }
}
