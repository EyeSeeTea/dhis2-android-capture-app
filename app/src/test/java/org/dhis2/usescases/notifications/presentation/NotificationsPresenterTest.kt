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
import org.dhis2.usescases.notifications.domain.User
import org.dhis2.usescases.notifications.domain.UserRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
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

    @Before
    fun setUp() {
        ShowNotifications.isPending = false
        ShowNotifications.onPending = null
        ShowNotifications.acceptedInThisSession.clear()
    }

    @After
    fun tearDown() {
        ShowNotifications.isPending = false
        ShowNotifications.onPending = null
        ShowNotifications.acceptedInThisSession.clear()
    }

    @Test
    fun `shows the cached unread notifications on a fresh app process`() = runTest {
        // A fresh process: nothing has marked anything pending since the restart, but the
        // notification dismissed before it is still cached and still unread.
        assertFalse(ShowNotifications.isPending)
        givenStoredNotifications("a")

        givenAPresenter().refresh(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("a", view.renderCalls.single().single().id)
    }

    @Test
    fun `renders and keeps the pending flag until the notification is accepted`() = runTest {
        givenStoredNotifications("a")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("a", view.renderCalls.single().single().id)
        assertTrue(
            "showing is not accepting: a dialog dismissed without OK must come back",
            ShowNotifications.isPending,
        )
    }

    @Test
    fun `shows the notification again on the next resume when it was not accepted`() = runTest {
        givenStoredNotifications("a")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)
        presenter.refresh(view)

        assertEquals(2, view.renderCalls.size)
    }

    @Test
    fun `accepting the last notification stops it from being shown again`() = runTest {
        val notification = givenANotification("a")
        givenStoredNotifications("a")
        givenTheNotificationCanBeAccepted(notification)
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        // Accepting re-filters the store, so the accepted notification drops out of it.
        givenNoStoredNotifications()
        presenter.markNotificationAsRead(notification)

        assertFalse(ShowNotifications.isPending)

        presenter.refresh(view)
        assertEquals(1, view.renderCalls.size)
    }

    @Test
    fun `accepting while offline keeps the notification pending`() = runTest {
        val notification = givenANotification("a")
        givenStoredNotifications("a")
        // Offline the datastore read comes back empty, so the use case finds nothing to update
        // and the local store keeps the notification.
        whenever(notificationRepository.getById(notification.id)) doReturn flowOf(null)
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        presenter.markNotificationAsRead(notification)

        assertTrue(
            "nothing was persisted, so the notification must be offered again",
            ShowNotifications.isPending,
        )
    }

    @Test
    fun `an accepted notification is not shown again while its read is still being saved`() =
        runTest {
            val notification = givenANotification("a")
            givenStoredNotifications("a")
            givenTheNotificationCanBeAccepted(notification)
            val presenter = givenAPresenter()
            presenter.refresh(view)

            // The cache still holds it: the save has not re-filtered the store yet, which is
            // what the user hits by going back as soon as they accept.
            presenter.markNotificationAsRead(notification)
            presenter.refresh(view)

            assertEquals(1, view.renderCalls.size)
        }

    @Test
    fun `an accepted notification whose read could not be saved is not shown again in the session`() =
        runTest {
            val notification = givenANotification("a")
            givenStoredNotifications("a")
            whenever(notificationRepository.getById(notification.id)) doReturn flowOf(null)
            val presenter = givenAPresenter()

            presenter.markNotificationAsRead(notification)
            presenter.refresh(view)
            presenter.refresh(view)

            // Not offered on every screen while the server keeps failing: it comes back after
            // the next download or in a new process, where the read is retried.
            assertEquals(0, view.renderCalls.size)
        }

    @Test
    fun `accepting one notification does not hide the others`() = runTest {
        val accepted = givenANotification("a")
        givenStoredNotifications("a", "b")
        givenTheNotificationCanBeAccepted(accepted)
        val presenter = givenAPresenter()

        presenter.markNotificationAsRead(accepted)
        presenter.refresh(view)

        assertEquals(listOf("b"), view.renderCalls.single().map { it.id })
    }

    @Test
    fun `an accept that was not saved is offered again once a download lands`() = runTest {
        val notification = givenANotification("a")
        givenStoredNotifications("a")
        whenever(notificationRepository.getById(notification.id)) doReturn flowOf(null)
        val presenter = givenAPresenter()
        presenter.markNotificationAsRead(notification)

        // The download brings back what the server holds, where it is still unread.
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        assertEquals(1, view.renderCalls.size)
    }

    @Test
    fun `an accept that was not saved is offered again in a new app process`() = runTest {
        val notification = givenANotification("a")
        givenStoredNotifications("a")
        whenever(notificationRepository.getById(notification.id)) doReturn flowOf(null)
        givenAPresenter().markNotificationAsRead(notification)

        givenANewAppProcess()
        givenAPresenter().refresh(view)

        assertEquals(1, view.renderCalls.size)
    }

    @Test
    fun `accepting one of several keeps the rest pending`() = runTest {
        val accepted = givenANotification("a")
        givenStoredNotifications("a", "b")
        givenTheNotificationCanBeAccepted(accepted)
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        givenStoredNotifications("b")
        presenter.markNotificationAsRead(accepted)

        assertTrue(ShowNotifications.isPending)
    }

    @Test
    fun `keeps the pending flag when the download has not landed yet`() = runTest {
        givenNoStoredNotifications()
        val presenter = givenAPresenter()
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
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        // The program screen resumes before the download finished: nothing to show yet.
        givenNoStoredNotifications()
        presenter.refresh(view)
        assertEquals(0, view.renderCalls.size)

        // The download lands and the user resumes again.
        givenStoredNotifications("late")
        presenter.refresh(view)

        assertEquals(1, view.renderCalls.size)
        assertEquals("late", view.renderCalls.single().single().id)
    }

    @Test
    fun `marking pending notifies the visible screen so the dialog appears without navigating`() =
        runTest {
            givenStoredNotifications("live")
            val presenter = givenAPresenter()
            ShowNotifications.onPending = { presenter.refresh(view) }

            presenter.markShowNotificationsAsPending()

            assertEquals(1, view.renderCalls.size)
            assertEquals("live", view.renderCalls.single().single().id)
        }

    @Test
    fun `marking pending with no screen registered still records the flag`() = runTest {
        val presenter = givenAPresenter()
        ShowNotifications.onPending = null

        presenter.markShowNotificationsAsPending()

        assertTrue(ShowNotifications.isPending)
        assertEquals(0, view.renderCalls.size)
    }

    // The dispatcher must share the scheduler of the enclosing runTest, otherwise the coroutines
    // the presenter launches on its own scopes are reported as uncaught in the *next* test.
    private fun TestScope.givenAPresenter(): NotificationsPresenter {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return NotificationsPresenter(
            getNotifications = GetNotifications(notificationRepository),
            markNotificationAsRead = MarkNotificationAsRead(notificationRepository, userRepository),
            ioDispatcher = dispatcher,
            uiDispatcher = dispatcher,
        )
    }

    private fun givenStoredNotifications(vararg ids: String) {
        whenever(notificationRepository.get()) doReturn flowOf(ids.map { givenANotification(it) })
    }

    private fun givenTheNotificationCanBeAccepted(notification: Notification) {
        whenever(notificationRepository.getById(notification.id)) doReturn flowOf(notification)
        whenever(userRepository.getCurrentUser()) doReturn User("user1", "User One")
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)
    }

    // Only process-lifetime state is lost; the cache on disk survives a restart.
    private fun givenANewAppProcess() {
        ShowNotifications.isPending = false
        ShowNotifications.onPending = null
        ShowNotifications.acceptedInThisSession.clear()
    }

    private fun givenNoStoredNotifications() {
        whenever(notificationRepository.get()) doReturn flowOf(emptyList())
    }

    private fun givenANotification(id: String) =
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
