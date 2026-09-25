package org.dhis2.di
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.dhis2.usescases.notifications.presentation.ShowNotifications
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * The widp flavor is the only one that registers a post-metadata-sync action. This is the wiring
 * that replaced the pre-3.4.0 hook in `SyncPresenterImpl.syncMetadata().doOnComplete {}`, and a
 * missing binding here means notifications are never downloaded — the failure mode that reached
 * a device during the 3.4.1 attempt, where a broken graph was only found by crashing.
 *
 * The pending flag is checked as state on `ShowNotifications`, not as a call on a mocked
 * presenter: the flag is public and reachable without mocking, which is how
 * `NotificationsPresenterTest` verifies the same effect. The presenter is therefore real, but only
 * `markShowNotificationsAsPending()` is exercised here, and that neither launches a coroutine nor
 * touches a view. `ShowNotifications` is global, so it is reset around every test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostMetadataSyncModuleTest {
    private val notificationRepository: NotificationRepository = mock()
    private val userRepository: UserRepository = mock()
    private val notificationsPresenter =
        NotificationsPresenter(
            getNotifications = GetNotifications(notificationRepository),
            markNotificationAsRead = MarkNotificationAsRead(notificationRepository, userRepository),
            ioDispatcher = UnconfinedTestDispatcher(),
            uiDispatcher = UnconfinedTestDispatcher(),
        )

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
    fun `the widp flavor registers exactly one post-metadata-sync action`() {
        val actions = givenTheRegisteredActions()

        assertEquals(1, actions.size)
    }

    @Test
    fun `the action downloads the notifications and then marks them pending`() = runTest {
        givenTheDownloadSucceeds()
        val action = givenTheRegisteredAction()

        val result = action()

        assertTrue(result.isSuccess)
        verify(notificationRepository).sync()
        assertTrue(ShowNotifications.isPending)
    }

    @Test
    fun `a failing download is reported as a failure and marks nothing pending`() = runTest {
        givenTheDownloadFails("server unreachable")
        val action = givenTheRegisteredAction()

        val result = action()

        // SyncMetadata logs and swallows a failed action on purpose, so a notifications outage
        // can never break the metadata sync itself.
        assertTrue(result.isFailure)
        assertFalse(ShowNotifications.isPending)
    }

    private fun givenTheWidpFlavorGraph(): Koin =
        koinApplication {
            modules(
                module {
                    single { notificationRepository }
                    single { notificationsPresenter }
                },
                postMetadataSyncModule,
            )
        }.koin

    private fun givenTheRegisteredActions(): List<PostMetadataSyncAction> =
        givenTheWidpFlavorGraph().get<List<PostMetadataSyncAction>>()

    private fun givenTheRegisteredAction(): PostMetadataSyncAction = givenTheRegisteredActions().single()

    private fun givenTheDownloadSucceeds() {
        whenever(notificationRepository.sync()) doReturn flowOf(Unit)
    }

    // Mirrors what NotificationD2Repository.sync() does when the datastore cannot be read; that
    // side is pinned by NotificationD2RepositoryTest, so this is not a contract of its own.
    private fun givenTheDownloadFails(reason: String) {
        whenever(notificationRepository.sync()) doReturn flow { throw IllegalStateException(reason) }
    }
}
