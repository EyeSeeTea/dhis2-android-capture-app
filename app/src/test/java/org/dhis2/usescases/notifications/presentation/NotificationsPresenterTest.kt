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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

/**
 * Covers the notification flow: cached unread notifications are offered when the app is entered
 * and after a sync, once, one dialog at a time. The mark is consumed before rendering, so resuming
 * a screen again never shows the same notification twice.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsPresenterTest {
    private val notificationRepository: NotificationRepository = mock()
    private val userRepository: UserRepository = mock()
    private val view = RecordingView()

    @Before
    fun setUp() {
        ShowNotifications.isPending = false
    }

    @After
    fun tearDown() {
        ShowNotifications.isPending = false
    }

    @Test
    fun `does nothing when nothing marked the notifications pending`() = runTest {
        givenStoredNotifications("a")

        givenAPresenter().refresh(view)

        assertEquals(emptyList<String>(), view.shownIds())
    }

    @Test
    fun `entering the app shows the cached unread notification of a fresh process`() = runTest {
        // A fresh process: the notification dismissed before the restart is still cached and unread.
        assertFalse(ShowNotifications.isPending)
        givenStoredNotifications("a")
        val presenter = givenAPresenter()

        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        assertEquals(listOf("a"), view.shownIds())
    }

    @Test
    fun `the pending mark is consumed before rendering, so resuming again shows nothing new`() =
        runTest {
            givenStoredNotifications("a")
            val presenter = givenAPresenter()
            presenter.markShowNotificationsAsPending()

            presenter.refresh(view)
            presenter.refresh(view)

            assertEquals(listOf("a"), view.shownIds())
            assertFalse(ShowNotifications.isPending)
        }

    @Test
    fun `shows one notification at a time`() = runTest {
        givenStoredNotifications("a", "b", "c")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)

        assertEquals(listOf("a"), view.shownIds())
    }

    @Test
    fun `dismissing one shows the next, and it stays unread`() = runTest {
        givenStoredNotifications("a", "b")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        presenter.onNotificationDismissed(view)

        assertEquals(listOf("a", "b"), view.shownIds())
        verify(notificationRepository, never()).save(any())
    }

    @Test
    fun `a dismissed notification is not offered again until the next trigger`() = runTest {
        givenStoredNotifications("a")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)
        presenter.onNotificationDismissed(view)

        presenter.refresh(view)
        assertEquals(listOf("a"), view.shownIds())

        // The next sync, or the next start of the app, marks them pending again.
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)
        assertEquals(listOf("a", "a"), view.shownIds())
    }

    @Test
    fun `accepting saves the read and then shows the next one`() = runTest {
        val first = givenANotification("a")
        givenStoredNotifications("a", "b")
        givenTheNotificationCanBeAccepted(first)
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        presenter.markNotificationAsRead(first, view)

        verify(notificationRepository).save(any())
        assertEquals(listOf("a", "b"), view.shownIds())
    }

    @Test
    fun `an accept that cannot reach the server leaves the cache alone and shows the next one`() =
        runTest {
            val first = givenANotification("a")
            givenStoredNotifications("a", "b")
            // Offline the datastore read comes back empty, so there is nothing to update.
            whenever(notificationRepository.getById(first.id)) doReturn flowOf(null)
            val presenter = givenAPresenter()
            presenter.markShowNotificationsAsPending()
            presenter.refresh(view)

            presenter.markNotificationAsRead(first, view)

            verify(notificationRepository, never()).save(any())
            assertEquals(listOf("a", "b"), view.shownIds())
        }

    @Test
    fun `an accept that fails never takes the app down`() = runTest {
        val first = givenANotification("a")
        givenStoredNotifications("a", "b")
        whenever(notificationRepository.getById(first.id)) doReturn flowOf(first)
        // UserD2Repository dereferences the SDK user with !!, so a session being torn down throws.
        whenever(userRepository.getCurrentUser()) doThrow NullPointerException("no current user")
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()
        presenter.refresh(view)

        presenter.markNotificationAsRead(first, view)

        verify(notificationRepository, never()).save(any())
        assertEquals(listOf("a", "b"), view.shownIds())
    }

    @Test
    fun `an empty cache shows nothing and still consumes the mark`() = runTest {
        givenNoStoredNotifications()
        val presenter = givenAPresenter()
        presenter.markShowNotificationsAsPending()

        presenter.refresh(view)

        assertEquals(emptyList<String>(), view.shownIds())
        assertTrue(!ShowNotifications.isPending)
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

    private fun givenNoStoredNotifications() {
        whenever(notificationRepository.get()) doReturn flowOf(emptyList())
    }

    private fun givenTheNotificationCanBeAccepted(notification: Notification) {
        whenever(notificationRepository.getById(notification.id)) doReturn flowOf(notification)
        whenever(userRepository.getCurrentUser()) doReturn User("user1", "User One")
        whenever(notificationRepository.save(any())) doReturn flowOf(Unit)
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
        private val shown = mutableListOf<Notification>()

        fun shownIds() = shown.map { it.id }

        override fun renderNotification(notification: Notification) {
            shown.add(notification)
        }
    }
}
