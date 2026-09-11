package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.Permissions
import org.dhis2.usescases.notifications.domain.Recipients
import org.dhis2.usescases.notifications.domain.UserRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

/**
 * Covers the two display defects found during the 3.4.1 manual validation — the pending flag was
 * consumed even when there was nothing to show, and nothing refreshed once the asynchronous
 * download landed — plus the listener that replaced the view-passing fix now that the download
 * runs off any Activity, inside a `PostMetadataSyncAction`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsPresenterTest {
    private val notificationRepository: NotificationRepository = mock()
    private val userRepository: UserRepository = mock()
    private val view = RecordingView()

    // The dispatcher must share the scheduler of the enclosing runTest, otherwise the coroutines
    // the presenter launches on its own scopes are reported as uncaught in the *next* test.
    private fun TestScope.presenter(): NotificationsPresenter {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return NotificationsPresenter(
            getNotifications = GetNotifications(notificationRepository),
            markNotificationAsRead = MarkNotificationAsRead(notificationRepository, userRepository),
            ioDispatcher = dispatcher,
            uiDispatcher = dispatcher,
        )
    }

    @Before
    fun setUp() {
        ShowNotifications.isPending = false
        ShowNotifications.onPending = null
    }

    @After
    fun tearDown() {
        ShowNotifications.isPending = false
        ShowNotifications.onPending = null
    }

    @Test
    fun `does nothing when no refresh is pending`() = runTest {
        whenever(notificationRepository.get()) doReturn flowOf(listOf(notification("a")))

        presenter().refresh(view)

        assertEquals(0, view.renderCalls.size)
    }

    @Test
    fun `renders and consumes the pending flag when there are notifications`() = runTest {
        whenever(notificationRepository.get()) doReturn flowOf(listOf(notification("a")))
        val presenter = presenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("a", view.renderCalls.single().single().id)
        assertFalse(ShowNotifications.isPending)
    }

    @Test
    fun `keeps the pending flag when the download has not landed yet`() = runTest {
        whenever(notificationRepository.get()) doReturn flowOf(emptyList())
        val presenter = presenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)

        assertEquals(0, view.renderCalls.size)
        assertTrue(
            "the flag must survive so the notification is not lost once the download lands",
            ShowNotifications.isPending,
        )
    }

    @Test
    fun `a resume while the download is in flight does not lose the notification`() = runTest {
        val presenter = presenter()
        presenter.markShowNotificationsAsPending()

        // The program screen resumes before the download finished: nothing to show yet.
        whenever(notificationRepository.get()) doReturn flowOf(emptyList())
        presenter.refresh(view)
        assertEquals(0, view.renderCalls.size)

        // The download lands and the user resumes again.
        whenever(notificationRepository.get()) doReturn flowOf(listOf(notification("late")))
        presenter.refresh(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("late", view.renderCalls.single().single().id)
    }

    @Test
    fun `marking pending notifies the visible screen so the dialog appears without navigating`() =
        runTest {
            whenever(notificationRepository.get()) doReturn flowOf(listOf(notification("live")))
            val presenter = presenter()
            ShowNotifications.onPending = Runnable { presenter.refresh(view) }

            presenter.markShowNotificationsAsPending()

            assertEquals(1, view.renderCalls.size)
            assertEquals("live", view.renderCalls.single().single().id)
        }

    @Test
    fun `marking pending with no screen registered still records the flag`() = runTest {
        val presenter = presenter()
        ShowNotifications.onPending = null

        presenter.markShowNotificationsAsPending()

        assertTrue(ShowNotifications.isPending)
        assertEquals(0, view.renderCalls.size)
    }

    private fun notification(id: String) =
        Notification(
            content = "content",
            createdAt = Date(0),
            id = id,
            readBy = emptyList(),
            recipients = Recipients(emptyList(), emptyList(), "ALL"),
            permissions = Permissions("", emptyList(), emptyList()),
            translations = emptyMap(),
        )

    private class RecordingView : NotificationsView {
        val renderCalls = mutableListOf<List<Notification>>()

        override fun renderNotifications(notifications: List<Notification>) {
            renderCalls.add(notifications)
        }
    }
}
