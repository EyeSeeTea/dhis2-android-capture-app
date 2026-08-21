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
import org.dhis2.usescases.notifications.domain.SyncNotifications
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
 * Covers the two defects found during the 3.4.1 manual validation:
 * the pending flag was consumed even when there was nothing to show, and nothing refreshed
 * once the asynchronous download landed. See `upgrade-3.4.1-notes.md`.
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
            syncNotifications = SyncNotifications(notificationRepository),
            ioDispatcher = dispatcher,
            uiDispatcher = dispatcher
        )
    }

    @Before
    fun setUp() {
        ShowNotifications.isPending = false
    }

    @After
    fun tearDown() {
        ShowNotifications.isPending = false
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
            ShowNotifications.isPending
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
    fun `the download refreshes the view when it completes`() = runTest {
        whenever(notificationRepository.sync()) doReturn flowOf(Unit)
        whenever(notificationRepository.get()) doReturn flowOf(listOf(notification("downloaded")))

        presenter().syncNotifications(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("downloaded", view.renderCalls.single().single().id)
        assertFalse(ShowNotifications.isPending)
    }

    @Test
    fun `the download marks pending even without a view to refresh`() = runTest {
        whenever(notificationRepository.sync()) doReturn flowOf(Unit)

        presenter().syncNotifications()

        assertEquals(0, view.renderCalls.size)
        assertTrue(ShowNotifications.isPending)
    }

    private fun notification(id: String) =
        Notification(
            content = "content",
            createdAt = Date(0),
            id = id,
            readBy = emptyList(),
            recipients = Recipients(emptyList(), emptyList(), "ALL"),
            permissions = Permissions("", emptyList(), emptyList()),
            translations = emptyMap()
        )

    private class RecordingView : NotificationsView {
        val renderCalls = mutableListOf<List<Notification>>()

        override fun renderNotifications(notifications: List<Notification>) {
            renderCalls.add(notifications)
        }
    }
}
