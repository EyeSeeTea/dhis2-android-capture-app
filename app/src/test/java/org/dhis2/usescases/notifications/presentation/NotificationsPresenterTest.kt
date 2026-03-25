package org.dhis2.usescases.notifications.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.Recipients
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsPresenterTest {

    private val getNotifications: GetNotifications = mock()
    private val markNotificationAsRead: MarkNotificationAsRead = mock()
    private val notificationsView: NotificationsView = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var presenter: NotificationsPresenter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        presenter = NotificationsPresenter(getNotifications, markNotificationAsRead)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        ShowNotifications.isPending = false
    }

    @Test
    fun `should render notifications when refresh is called and show is pending`() {
        val notifications = listOf(
            createNotification(id = "notif-1", content = "First"),
            createNotification(id = "notif-2", content = "Second"),
        )
        whenever(getNotifications.invoke()) doReturn flowOf(notifications)

        ShowNotifications.isPending = true
        presenter.refresh(notificationsView)

        verify(notificationsView).renderNotifications(notifications)
    }

    @Test
    fun `should not fetch notifications when show is not pending`() {
        ShowNotifications.isPending = false
        presenter.refresh(notificationsView)

        verifyNoInteractions(getNotifications)
        verifyNoInteractions(notificationsView)
    }

    @Test
    fun `should reset pending flag after refresh`() {
        whenever(getNotifications.invoke()) doReturn flowOf(emptyList())

        ShowNotifications.isPending = true
        presenter.refresh(notificationsView)

        assertFalse(ShowNotifications.isPending)
    }

    @Test
    fun `should set pending flag when markShowNotificationsAsPending is called`() {
        ShowNotifications.isPending = false
        presenter.markShowNotificationsAsPending()

        assertTrue(ShowNotifications.isPending)
    }

    // Note: markNotificationAsRead uses Dispatchers.IO without injection,
    // making it untestable without a dispatcher override. This would require
    // refactoring the presenter to accept a CoroutineDispatcher parameter.

    @Test
    fun `should render empty list when no notifications exist`() {
        whenever(getNotifications.invoke()) doReturn flowOf(emptyList())

        ShowNotifications.isPending = true
        presenter.refresh(notificationsView)

        verify(notificationsView).renderNotifications(emptyList())
    }

    private fun createNotification(
        id: String = "notif-id",
        content: String = "content",
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
        translations = null,
    )
}
