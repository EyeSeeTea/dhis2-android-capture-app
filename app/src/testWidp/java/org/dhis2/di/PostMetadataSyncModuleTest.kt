package org.dhis2.di
// EyeSeeTea customization - Notifications system

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * The widp flavor is the only one that registers a post-metadata-sync action. This is the wiring
 * that replaced the pre-3.4.0 hook in `SyncPresenterImpl.syncMetadata().doOnComplete {}`, and a
 * missing binding here means notifications are never downloaded — the failure mode that reached
 * a device during the 3.4.1 attempt, where a broken graph was only found by crashing.
 *
 * The presenter is mocked on purpose. This test is about the wiring, not about what the presenter
 * does with the flag — `NotificationsPresenterTest` covers that. Building a real presenter here
 * would put its coroutine scopes and the global `ShowNotifications` object into the shared test
 * JVM, where a leaked coroutine is reported against whichever test happens to run next.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostMetadataSyncModuleTest {
    private val notificationRepository: NotificationRepository = mock()
    private val notificationsPresenter: NotificationsPresenter = mock()

    private fun koin(): Koin =
        koinApplication {
            modules(
                module {
                    single { notificationRepository }
                    single { notificationsPresenter }
                },
                postMetadataSyncModule,
            )
        }.koin

    @Test
    fun `the widp flavor registers exactly one post-metadata-sync action`() {
        val actions = koin().get<List<PostMetadataSyncAction>>()

        assertEquals(1, actions.size)
    }

    @Test
    fun `the action downloads the notifications and then marks them pending`() = runTest {
        whenever(notificationRepository.sync()) doReturn flowOf(Unit)
        val action = koin().get<List<PostMetadataSyncAction>>().single()

        val result = action()

        assertTrue(result.isSuccess)
        verify(notificationRepository).sync()
        verify(notificationsPresenter).markShowNotificationsAsPending()
    }

    @Test
    fun `a failing download is reported as a failure and marks nothing pending`() = runTest {
        whenever(notificationRepository.sync()) doReturn
            flow { throw IllegalStateException("server unreachable") }
        val action = koin().get<List<PostMetadataSyncAction>>().single()

        val result = action()

        // SyncMetadata logs and swallows a failed action on purpose, so a notifications outage
        // can never break the metadata sync itself.
        assertTrue(result.isFailure)
        verify(notificationsPresenter, never()).markShowNotificationsAsPending()
    }
}
